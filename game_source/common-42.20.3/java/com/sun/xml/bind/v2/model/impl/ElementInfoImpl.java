/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.AnnotationSource;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.impl.RegistryInfoImpl;
import com.sun.xml.bind.v2.model.impl.TypeInfoImpl;
import com.sun.xml.bind.v2.model.impl.Util;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.activation.MimeType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

class ElementInfoImpl<T, C, F, M>
extends TypeInfoImpl<T, C, F, M>
implements ElementInfo<T, C> {
    private final QName tagName;
    private final NonElement<T, C> contentType;
    private final T tOfJAXBElementT;
    private final T elementType;
    private final ClassInfo<T, C> scope;
    private final XmlElementDecl anno;
    private ElementInfoImpl<T, C, F, M> substitutionHead;
    private FinalArrayList<ElementInfoImpl<T, C, F, M>> substitutionMembers;
    private final M method;
    private final Adapter<T, C> adapter;
    private final boolean isCollection;
    private final ID id;
    private final PropertyImpl property;
    private final MimeType expectedMimeType;
    private final boolean inlineBinary;
    private final QName schemaType;

    public ElementInfoImpl(ModelBuilder<T, C, F, M> builder, RegistryInfoImpl<T, C, F, M> registry, M m) throws IllegalAnnotationException {
        super(builder, registry);
        this.method = m;
        this.anno = this.reader().getMethodAnnotation(XmlElementDecl.class, m, this);
        assert (this.anno != null);
        assert (this.anno instanceof Locatable);
        this.elementType = this.nav().getReturnType(m);
        Object baseClass = this.nav().getBaseClass(this.elementType, this.nav().asDecl(JAXBElement.class));
        if (baseClass == null) {
            throw new IllegalAnnotationException(Messages.XML_ELEMENT_MAPPING_ON_NON_IXMLELEMENT_METHOD.format(this.nav().getMethodName(m)), this.anno);
        }
        this.tagName = this.parseElementName(this.anno);
        TypeT[] methodParams = this.nav().getMethodParameters(m);
        Adapter<Object, Object> a = null;
        if (methodParams.length > 0) {
            XmlJavaTypeAdapter adapter = this.reader().getMethodAnnotation(XmlJavaTypeAdapter.class, m, this);
            if (adapter != null) {
                a = new Adapter(adapter, this.reader(), this.nav());
            } else {
                XmlAttachmentRef xsa = this.reader().getMethodAnnotation(XmlAttachmentRef.class, m, this);
                if (xsa != null) {
                    TODO.prototype("in Annotation Processing swaRefAdapter isn't avaialble, so this returns null");
                    a = new Adapter(this.owner.nav.asDecl(SwaRefAdapter.class), this.owner.nav);
                }
            }
        }
        this.adapter = a;
        Object object = this.tOfJAXBElementT = methodParams.length > 0 ? methodParams[0] : this.nav().getTypeArgument(baseClass, 0);
        if (this.adapter == null) {
            Object list = this.nav().getBaseClass(this.tOfJAXBElementT, this.nav().asDecl(List.class));
            if (list == null) {
                this.isCollection = false;
                this.contentType = builder.getTypeInfo(this.tOfJAXBElementT, this);
            } else {
                this.isCollection = true;
                this.contentType = builder.getTypeInfo(this.nav().getTypeArgument(list, 0), this);
            }
        } else {
            this.contentType = builder.getTypeInfo(this.adapter.defaultType, this);
            this.isCollection = false;
        }
        Object s = this.reader().getClassValue(this.anno, "scope");
        if (this.nav().isSameType(s, this.nav().ref(XmlElementDecl.GLOBAL.class))) {
            this.scope = null;
        } else {
            NonElement<T, C> scp = builder.getClassInfo(this.nav().asDecl(s), this);
            if (!(scp instanceof ClassInfo)) {
                throw new IllegalAnnotationException(Messages.SCOPE_IS_NOT_COMPLEXTYPE.format(this.nav().getTypeName(s)), this.anno);
            }
            this.scope = (ClassInfo)scp;
        }
        this.id = this.calcId();
        this.property = this.createPropertyImpl();
        this.expectedMimeType = Util.calcExpectedMediaType(this.property, builder);
        this.inlineBinary = this.reader().hasMethodAnnotation(XmlInlineBinaryData.class, this.method);
        this.schemaType = Util.calcSchemaType(this.reader(), this.property, registry.registryClass, this.getContentInMemoryType(), this);
    }

    @Override
    final QName parseElementName(XmlElementDecl e) {
        String local = e.name();
        String nsUri = e.namespace();
        if (nsUri.equals("##default")) {
            XmlSchema xs = this.reader().getPackageAnnotation(XmlSchema.class, this.nav().getDeclaringClassForMethod(this.method), this);
            nsUri = xs != null ? xs.namespace() : this.builder.defaultNsUri;
        }
        return new QName(nsUri.intern(), local.intern());
    }

    protected PropertyImpl createPropertyImpl() {
        return new PropertyImpl();
    }

    @Override
    public ElementPropertyInfo<T, C> getProperty() {
        return this.property;
    }

    @Override
    public NonElement<T, C> getContentType() {
        return this.contentType;
    }

    @Override
    public T getContentInMemoryType() {
        if (this.adapter == null) {
            return this.tOfJAXBElementT;
        }
        return (T)this.adapter.customType;
    }

    @Override
    public QName getElementName() {
        return this.tagName;
    }

    @Override
    public T getType() {
        return this.elementType;
    }

    @Override
    public final boolean canBeReferencedByIDREF() {
        return false;
    }

    private ID calcId() {
        if (this.reader().hasMethodAnnotation(XmlID.class, this.method)) {
            return ID.ID;
        }
        if (this.reader().hasMethodAnnotation(XmlIDREF.class, this.method)) {
            return ID.IDREF;
        }
        return ID.NONE;
    }

    @Override
    public ClassInfo<T, C> getScope() {
        return this.scope;
    }

    @Override
    public ElementInfo<T, C> getSubstitutionHead() {
        return this.substitutionHead;
    }

    @Override
    public Collection<? extends ElementInfoImpl<T, C, F, M>> getSubstitutionMembers() {
        if (this.substitutionMembers == null) {
            return Collections.emptyList();
        }
        return this.substitutionMembers;
    }

    @Override
    void link() {
        if (this.anno.substitutionHeadName().length() != 0) {
            QName name = new QName(this.anno.substitutionHeadNamespace(), this.anno.substitutionHeadName());
            this.substitutionHead = this.owner.getElementInfo((Object)null, name);
            if (this.substitutionHead == null) {
                this.builder.reportError(new IllegalAnnotationException(Messages.NON_EXISTENT_ELEMENT_MAPPING.format(name.getNamespaceURI(), name.getLocalPart()), this.anno));
            } else {
                super.addSubstitutionMember(this);
            }
        } else {
            this.substitutionHead = null;
        }
        super.link();
    }

    private void addSubstitutionMember(ElementInfoImpl<T, C, F, M> child) {
        if (this.substitutionMembers == null) {
            this.substitutionMembers = new FinalArrayList();
        }
        this.substitutionMembers.add(child);
    }

    @Override
    public Location getLocation() {
        return this.nav().getMethodLocation(this.method);
    }

    protected class PropertyImpl
    implements ElementPropertyInfo<T, C>,
    TypeRef<T, C>,
    AnnotationSource {
        protected PropertyImpl() {
        }

        @Override
        public NonElement<T, C> getTarget() {
            return ElementInfoImpl.this.contentType;
        }

        @Override
        public QName getTagName() {
            return ElementInfoImpl.this.tagName;
        }

        @Override
        public List<? extends TypeRef<T, C>> getTypes() {
            return Collections.singletonList(this);
        }

        public List<? extends NonElement<T, C>> ref() {
            return Collections.singletonList(ElementInfoImpl.this.contentType);
        }

        @Override
        public QName getXmlName() {
            return ElementInfoImpl.this.tagName;
        }

        @Override
        public boolean isCollectionRequired() {
            return false;
        }

        @Override
        public boolean isCollectionNillable() {
            return true;
        }

        @Override
        public boolean isNillable() {
            return true;
        }

        @Override
        public String getDefaultValue() {
            String v = ElementInfoImpl.this.anno.defaultValue();
            if (v.equals("\u0000")) {
                return null;
            }
            return v;
        }

        public ElementInfoImpl<T, C, F, M> parent() {
            return ElementInfoImpl.this;
        }

        @Override
        public String getName() {
            return "value";
        }

        @Override
        public String displayName() {
            return "JAXBElement#value";
        }

        @Override
        public boolean isCollection() {
            return ElementInfoImpl.this.isCollection;
        }

        @Override
        public boolean isValueList() {
            return ElementInfoImpl.this.isCollection;
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public PropertyKind kind() {
            return PropertyKind.ELEMENT;
        }

        @Override
        public Adapter<T, C> getAdapter() {
            return ElementInfoImpl.this.adapter;
        }

        @Override
        public ID id() {
            return ElementInfoImpl.this.id;
        }

        @Override
        public MimeType getExpectedMimeType() {
            return ElementInfoImpl.this.expectedMimeType;
        }

        @Override
        public QName getSchemaType() {
            return ElementInfoImpl.this.schemaType;
        }

        @Override
        public boolean inlineBinaryData() {
            return ElementInfoImpl.this.inlineBinary;
        }

        @Override
        public PropertyInfo<T, C> getSource() {
            return this;
        }

        @Override
        public <A extends Annotation> A readAnnotation(Class<A> annotationType) {
            return ElementInfoImpl.this.reader().getMethodAnnotation(annotationType, ElementInfoImpl.this.method, ElementInfoImpl.this);
        }

        @Override
        public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
            return ElementInfoImpl.this.reader().hasMethodAnnotation(annotationType, ElementInfoImpl.this.method);
        }
    }
}

