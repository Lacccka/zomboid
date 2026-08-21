/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.DummyPropertyInfo;
import com.sun.xml.bind.v2.model.impl.ERPropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.PropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

class ReferencePropertyInfoImpl<T, C, F, M>
extends ERPropertyInfoImpl<T, C, F, M>
implements ReferencePropertyInfo<T, C>,
DummyPropertyInfo<T, C, F, M> {
    private Set<Element<T, C>> types;
    private Set<ReferencePropertyInfoImpl<T, C, F, M>> subTypes = new LinkedHashSet<ReferencePropertyInfoImpl<T, C, F, M>>();
    private final boolean isMixed;
    private final WildcardMode wildcard;
    private final C domHandler;
    private Boolean isRequired;
    private static boolean is2_2 = true;

    public ReferencePropertyInfoImpl(ClassInfoImpl<T, C, F, M> classInfo, PropertySeed<T, C, F, M> seed) {
        super(classInfo, seed);
        this.isMixed = seed.readAnnotation(XmlMixed.class) != null;
        XmlAnyElement xae = seed.readAnnotation(XmlAnyElement.class);
        if (xae == null) {
            this.wildcard = null;
            this.domHandler = null;
        } else {
            this.wildcard = xae.lax() ? WildcardMode.LAX : WildcardMode.SKIP;
            this.domHandler = this.nav().asDecl(this.reader().getClassValue(xae, "value"));
        }
    }

    public Set<? extends Element<T, C>> ref() {
        return this.getElements();
    }

    @Override
    public PropertyKind kind() {
        return PropertyKind.REFERENCE;
    }

    @Override
    public Set<? extends Element<T, C>> getElements() {
        if (this.types == null) {
            this.calcTypes(false);
        }
        assert (this.types != null);
        return this.types;
    }

    private void calcTypes(boolean last) {
        this.types = new LinkedHashSet<Element<T, C>>();
        XmlElementRefs refs = this.seed.readAnnotation(XmlElementRefs.class);
        XmlElementRef ref = this.seed.readAnnotation(XmlElementRef.class);
        if (refs != null && ref != null) {
            this.parent.builder.reportError(new IllegalAnnotationException(Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(this.nav().getClassName(this.parent.getClazz()) + '#' + this.seed.getName(), ref.annotationType().getName(), refs.annotationType().getName()), (Annotation)ref, refs));
        }
        Object ann = refs != null ? refs.value() : (ref != null ? new XmlElementRef[]{ref} : null);
        this.isRequired = !this.isCollection();
        if (ann != null) {
            Navigator nav = this.nav();
            AnnotationReader reader = this.reader();
            Object defaultType = nav.ref(XmlElementRef.DEFAULT.class);
            Object je = nav.asDecl(JAXBElement.class);
            for (XmlElementRef r : ann) {
                Object type = reader.getClassValue(r, "type");
                if (this.nav().isSameType(type, defaultType)) {
                    type = nav.erasure(this.getIndividualType());
                }
                boolean yield = nav.getBaseClass(type, je) != null ? this.addGenericElement(r) : this.addAllSubtypes(type);
                if (this.isRequired.booleanValue() && !this.isRequired(r)) {
                    this.isRequired = false;
                }
                if (!last || yield) continue;
                if (this.nav().isSameType(type, nav.ref(JAXBElement.class))) {
                    this.parent.builder.reportError(new IllegalAnnotationException(Messages.NO_XML_ELEMENT_DECL.format(this.getEffectiveNamespaceFor(r), r.name()), this));
                } else {
                    this.parent.builder.reportError(new IllegalAnnotationException(Messages.INVALID_XML_ELEMENT_REF.format(type), this));
                }
                return;
            }
        }
        for (ReferencePropertyInfoImpl<T, C, F, M> info : this.subTypes) {
            PropertySeed sd = info.seed;
            refs = sd.readAnnotation(XmlElementRefs.class);
            ref = sd.readAnnotation(XmlElementRef.class);
            if (refs != null && ref != null) {
                this.parent.builder.reportError(new IllegalAnnotationException(Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(this.nav().getClassName(this.parent.getClazz()) + '#' + this.seed.getName(), ref.annotationType().getName(), refs.annotationType().getName()), (Annotation)ref, refs));
            }
            if ((ann = refs != null ? refs.value() : (ref != null ? new XmlElementRef[]{ref} : null)) == null) continue;
            Navigator nav = this.nav();
            AnnotationReader reader = this.reader();
            Object defaultType = nav.ref(XmlElementRef.DEFAULT.class);
            Object je = nav.asDecl(JAXBElement.class);
            for (XmlElementRef r : ann) {
                Object type = reader.getClassValue(r, "type");
                if (this.nav().isSameType(type, defaultType)) {
                    type = nav.erasure(this.getIndividualType());
                }
                boolean yield = nav.getBaseClass(type, je) != null ? this.addGenericElement(r, info) : this.addAllSubtypes(type);
                if (!last || yield) continue;
                if (this.nav().isSameType(type, nav.ref(JAXBElement.class))) {
                    this.parent.builder.reportError(new IllegalAnnotationException(Messages.NO_XML_ELEMENT_DECL.format(this.getEffectiveNamespaceFor(r), r.name()), this));
                } else {
                    this.parent.builder.reportError(new IllegalAnnotationException(Messages.INVALID_XML_ELEMENT_REF.format(new Object[0]), this));
                }
                return;
            }
        }
        this.types = Collections.unmodifiableSet(this.types);
    }

    @Override
    public boolean isRequired() {
        if (this.isRequired == null) {
            this.calcTypes(false);
        }
        return this.isRequired;
    }

    private boolean isRequired(XmlElementRef ref) {
        if (!is2_2) {
            return true;
        }
        try {
            return ref.required();
        }
        catch (LinkageError e) {
            is2_2 = false;
            return true;
        }
    }

    private boolean addGenericElement(XmlElementRef r) {
        String nsUri = this.getEffectiveNamespaceFor(r);
        return this.addGenericElement(this.parent.owner.getElementInfo(this.parent.getClazz(), new QName(nsUri, r.name())));
    }

    private boolean addGenericElement(XmlElementRef r, ReferencePropertyInfoImpl<T, C, F, M> info) {
        String nsUri = super.getEffectiveNamespaceFor(r);
        ElementInfo ei = this.parent.owner.getElementInfo(info.parent.getClazz(), new QName(nsUri, r.name()));
        this.types.add(ei);
        return true;
    }

    private String getEffectiveNamespaceFor(XmlElementRef r) {
        String nsUri = r.namespace();
        XmlSchema xs = this.reader().getPackageAnnotation(XmlSchema.class, this.parent.getClazz(), this);
        if (xs != null && xs.attributeFormDefault() == XmlNsForm.QUALIFIED && nsUri.length() == 0) {
            nsUri = this.parent.builder.defaultNsUri;
        }
        return nsUri;
    }

    private boolean addGenericElement(ElementInfo<T, C> ei) {
        if (ei == null) {
            return false;
        }
        this.types.add(ei);
        for (ElementInfo<T, C> subst : ei.getSubstitutionMembers()) {
            this.addGenericElement(subst);
        }
        return true;
    }

    private boolean addAllSubtypes(T type) {
        Navigator nav = this.nav();
        NonElement t = this.parent.builder.getClassInfo(nav.asDecl(type), this);
        if (!(t instanceof ClassInfo)) {
            return false;
        }
        boolean result = false;
        ClassInfo c = (ClassInfo)t;
        if (c.isElement()) {
            this.types.add(c.asElement());
            result = true;
        }
        for (ClassInfo classInfo : this.parent.owner.beans().values()) {
            if (!classInfo.isElement() || !nav.isSubClassOf(classInfo.getType(), type)) continue;
            this.types.add(classInfo.asElement());
            result = true;
        }
        for (ElementInfo elementInfo : this.parent.owner.getElementMappings(null).values()) {
            if (!nav.isSubClassOf(elementInfo.getType(), type)) continue;
            this.types.add(elementInfo);
            result = true;
        }
        return result;
    }

    @Override
    protected void link() {
        super.link();
        this.calcTypes(true);
    }

    @Override
    public final void addType(PropertyInfoImpl<T, C, F, M> info) {
        this.subTypes.add((ReferencePropertyInfoImpl)info);
    }

    @Override
    public final boolean isMixed() {
        return this.isMixed;
    }

    @Override
    public final WildcardMode getWildcard() {
        return this.wildcard;
    }

    @Override
    public final C getDOMHandler() {
        return this.domHandler;
    }
}

