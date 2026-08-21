/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.Util;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import javax.activation.MimeType;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import javax.xml.namespace.QName;

abstract class PropertyInfoImpl<T, C, F, M>
implements PropertyInfo<T, C>,
Locatable,
Comparable<PropertyInfoImpl> {
    protected final PropertySeed<T, C, F, M> seed;
    private final boolean isCollection;
    private final ID id;
    private final MimeType expectedMimeType;
    private final boolean inlineBinary;
    private final QName schemaType;
    protected final ClassInfoImpl<T, C, F, M> parent;
    private final Adapter<T, C> adapter;

    protected PropertyInfoImpl(ClassInfoImpl<T, C, F, M> parent, PropertySeed<T, C, F, M> spi) {
        this.seed = spi;
        this.parent = parent;
        if (parent == null) {
            throw new AssertionError();
        }
        MimeType mt = Util.calcExpectedMediaType(this.seed, parent.builder);
        if (mt != null && !this.kind().canHaveXmlMimeType) {
            parent.builder.reportError(new IllegalAnnotationException(Messages.ILLEGAL_ANNOTATION.format(XmlMimeType.class.getName()), this.seed.readAnnotation(XmlMimeType.class)));
            mt = null;
        }
        this.expectedMimeType = mt;
        this.inlineBinary = this.seed.hasAnnotation(XmlInlineBinaryData.class);
        T t = this.seed.getRawType();
        XmlJavaTypeAdapter xjta = this.getApplicableAdapter(t);
        if (xjta != null) {
            this.isCollection = false;
            this.adapter = new Adapter<T, C>(xjta, this.reader(), this.nav());
        } else {
            this.isCollection = this.nav().isSubClassOf(t, this.nav().ref(Collection.class)) || this.nav().isArrayButNotByteArray(t);
            xjta = this.getApplicableAdapter(this.getIndividualType());
            if (xjta == null) {
                XmlAttachmentRef xsa = this.seed.readAnnotation(XmlAttachmentRef.class);
                if (xsa != null) {
                    parent.builder.hasSwaRef = true;
                    this.adapter = new Adapter<T, C>(this.nav().asDecl(SwaRefAdapter.class), this.nav());
                } else {
                    this.adapter = null;
                    xjta = this.seed.readAnnotation(XmlJavaTypeAdapter.class);
                    if (xjta != null) {
                        T ad = this.reader().getClassValue(xjta, "value");
                        parent.builder.reportError(new IllegalAnnotationException(Messages.UNMATCHABLE_ADAPTER.format(this.nav().getTypeName(ad), this.nav().getTypeName(t)), xjta));
                    }
                }
            } else {
                this.adapter = new Adapter<T, C>(xjta, this.reader(), this.nav());
            }
        }
        this.id = this.calcId();
        this.schemaType = Util.calcSchemaType(this.reader(), this.seed, parent.clazz, this.getIndividualType(), this);
    }

    public ClassInfoImpl<T, C, F, M> parent() {
        return this.parent;
    }

    protected final Navigator<T, C, F, M> nav() {
        return this.parent.nav();
    }

    protected final AnnotationReader<T, C, F, M> reader() {
        return this.parent.reader();
    }

    public T getRawType() {
        return this.seed.getRawType();
    }

    public T getIndividualType() {
        if (this.adapter != null) {
            return (T)this.adapter.defaultType;
        }
        T raw = this.getRawType();
        if (!this.isCollection()) {
            return raw;
        }
        if (this.nav().isArrayButNotByteArray(raw)) {
            return this.nav().getComponentType(raw);
        }
        T bt = this.nav().getBaseClass(raw, this.nav().asDecl(Collection.class));
        if (this.nav().isParameterizedType(bt)) {
            return this.nav().getTypeArgument(bt, 0);
        }
        return this.nav().ref(Object.class);
    }

    @Override
    public final String getName() {
        return this.seed.getName();
    }

    private boolean isApplicable(XmlJavaTypeAdapter jta, T declaredType) {
        if (jta == null) {
            return false;
        }
        T type = this.reader().getClassValue(jta, "type");
        if (this.nav().isSameType(declaredType, type)) {
            return true;
        }
        T ad = this.reader().getClassValue(jta, "value");
        T ba = this.nav().getBaseClass(ad, this.nav().asDecl(XmlAdapter.class));
        if (!this.nav().isParameterizedType(ba)) {
            return true;
        }
        T inMemType = this.nav().getTypeArgument(ba, 1);
        return this.nav().isSubClassOf(declaredType, inMemType);
    }

    private XmlJavaTypeAdapter getApplicableAdapter(T type) {
        XmlJavaTypeAdapter jta = this.seed.readAnnotation(XmlJavaTypeAdapter.class);
        if (jta != null && this.isApplicable(jta, type)) {
            return jta;
        }
        XmlJavaTypeAdapters jtas = this.reader().getPackageAnnotation(XmlJavaTypeAdapters.class, this.parent.clazz, this.seed);
        if (jtas != null) {
            for (XmlJavaTypeAdapter xjta : jtas.value()) {
                if (!this.isApplicable(xjta, type)) continue;
                return xjta;
            }
        }
        if (this.isApplicable(jta = this.reader().getPackageAnnotation(XmlJavaTypeAdapter.class, this.parent.clazz, this.seed), type)) {
            return jta;
        }
        C refType = this.nav().asDecl(type);
        if (refType != null && (jta = this.reader().getClassAnnotation(XmlJavaTypeAdapter.class, refType, this.seed)) != null && this.isApplicable(jta, type)) {
            return jta;
        }
        return null;
    }

    @Override
    public Adapter<T, C> getAdapter() {
        return this.adapter;
    }

    @Override
    public final String displayName() {
        return this.nav().getClassName(this.parent.getClazz()) + '#' + this.getName();
    }

    @Override
    public final ID id() {
        return this.id;
    }

    private ID calcId() {
        if (this.seed.hasAnnotation(XmlID.class)) {
            if (!this.nav().isSameType(this.getIndividualType(), this.nav().ref(String.class))) {
                this.parent.builder.reportError(new IllegalAnnotationException(Messages.ID_MUST_BE_STRING.format(this.getName()), this.seed));
            }
            return ID.ID;
        }
        if (this.seed.hasAnnotation(XmlIDREF.class)) {
            return ID.IDREF;
        }
        return ID.NONE;
    }

    @Override
    public final MimeType getExpectedMimeType() {
        return this.expectedMimeType;
    }

    @Override
    public final boolean inlineBinaryData() {
        return this.inlineBinary;
    }

    @Override
    public final QName getSchemaType() {
        return this.schemaType;
    }

    @Override
    public final boolean isCollection() {
        return this.isCollection;
    }

    protected void link() {
        if (this.id == ID.IDREF) {
            for (TypeInfo ti : this.ref()) {
                if (ti.canBeReferencedByIDREF()) continue;
                this.parent.builder.reportError(new IllegalAnnotationException(Messages.INVALID_IDREF.format(this.parent.builder.nav.getTypeName(ti.getType())), this));
            }
        }
    }

    @Override
    public Locatable getUpstream() {
        return this.parent;
    }

    @Override
    public Location getLocation() {
        return this.seed.getLocation();
    }

    protected final QName calcXmlName(XmlElement e) {
        if (e != null) {
            return this.calcXmlName(e.namespace(), e.name());
        }
        return this.calcXmlName("##default", "##default");
    }

    protected final QName calcXmlName(XmlElementWrapper e) {
        if (e != null) {
            return this.calcXmlName(e.namespace(), e.name());
        }
        return this.calcXmlName("##default", "##default");
    }

    private QName calcXmlName(String uri, String local) {
        TODO.checkSpec();
        if (local.length() == 0 || local.equals("##default")) {
            local = this.seed.getName();
        }
        if (uri.equals("##default")) {
            XmlSchema xs = this.reader().getPackageAnnotation(XmlSchema.class, this.parent.getClazz(), this);
            if (xs != null) {
                switch (xs.elementFormDefault()) {
                    case QUALIFIED: {
                        QName typeName = this.parent.getTypeName();
                        uri = typeName != null ? typeName.getNamespaceURI() : xs.namespace();
                        if (uri.length() != 0) break;
                        uri = this.parent.builder.defaultNsUri;
                        break;
                    }
                    case UNQUALIFIED: 
                    case UNSET: {
                        uri = "";
                    }
                }
            } else {
                uri = "";
            }
        }
        return new QName(uri.intern(), local.intern());
    }

    @Override
    public int compareTo(PropertyInfoImpl that) {
        return this.getName().compareTo(that.getName());
    }

    @Override
    public final <A extends Annotation> A readAnnotation(Class<A> annotationType) {
        return this.seed.readAnnotation(annotationType);
    }

    @Override
    public final boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return this.seed.hasAnnotation(annotationType);
    }
}

