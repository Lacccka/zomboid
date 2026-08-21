/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.annotation.OverrideAnnotationOf;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.MethodLocatable;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.bind.v2.model.impl.AttributePropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.DummyPropertyInfo;
import com.sun.xml.bind.v2.model.impl.ElementPropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.FieldPropertySeed;
import com.sun.xml.bind.v2.model.impl.GetterSetterPropertySeed;
import com.sun.xml.bind.v2.model.impl.MapPropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.impl.PropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.PropertySeed;
import com.sun.xml.bind.v2.model.impl.ReferencePropertyInfoImpl;
import com.sun.xml.bind.v2.model.impl.TypeInfoImpl;
import com.sun.xml.bind.v2.model.impl.ValuePropertyInfoImpl;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.bind.v2.util.EditDistance;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

public class ClassInfoImpl<T, C, F, M>
extends TypeInfoImpl<T, C, F, M>
implements ClassInfo<T, C>,
Element<T, C> {
    protected final C clazz;
    private final QName elementName;
    private final QName typeName;
    private FinalArrayList<PropertyInfoImpl<T, C, F, M>> properties;
    private String[] propOrder;
    private ClassInfoImpl<T, C, F, M> baseClass;
    private boolean baseClassComputed = false;
    private boolean hasSubClasses = false;
    protected PropertySeed<T, C, F, M> attributeWildcard;
    private M factoryMethod = null;
    private static final SecondaryAnnotation[] SECONDARY_ANNOTATIONS = SecondaryAnnotation.values();
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    private static final HashMap<Class, Integer> ANNOTATION_NUMBER_MAP = new HashMap();
    private static final String[] DEFAULT_ORDER;

    ClassInfoImpl(ModelBuilder<T, C, F, M> builder, Locatable upstream, C clazz) {
        super(builder, upstream);
        String[] propOrder;
        this.clazz = clazz;
        assert (clazz != null);
        this.elementName = this.parseElementName(clazz);
        XmlType t = this.reader().getClassAnnotation(XmlType.class, clazz, this);
        this.typeName = this.parseTypeName(clazz, t);
        this.propOrder = t != null ? ((propOrder = t.propOrder()).length == 0 ? null : (propOrder[0].length() == 0 ? DEFAULT_ORDER : propOrder)) : DEFAULT_ORDER;
        XmlAccessorOrder xao = this.reader().getPackageAnnotation(XmlAccessorOrder.class, clazz, this);
        if (xao != null && xao.value() == XmlAccessOrder.UNDEFINED) {
            this.propOrder = null;
        }
        if ((xao = this.reader().getClassAnnotation(XmlAccessorOrder.class, clazz, this)) != null && xao.value() == XmlAccessOrder.UNDEFINED) {
            this.propOrder = null;
        }
        if (this.nav().isInterface(clazz)) {
            builder.reportError(new IllegalAnnotationException(Messages.CANT_HANDLE_INTERFACE.format(this.nav().getClassName(clazz)), this));
        }
        if (!this.hasFactoryConstructor(t) && !this.nav().hasDefaultConstructor(clazz)) {
            if (this.nav().isInnerClass(clazz)) {
                builder.reportError(new IllegalAnnotationException(Messages.CANT_HANDLE_INNER_CLASS.format(this.nav().getClassName(clazz)), this));
            } else if (this.elementName != null) {
                builder.reportError(new IllegalAnnotationException(Messages.NO_DEFAULT_CONSTRUCTOR.format(this.nav().getClassName(clazz)), this));
            }
        }
    }

    public ClassInfoImpl<T, C, F, M> getBaseClass() {
        if (!this.baseClassComputed) {
            Object s = this.nav().getSuperClass(this.clazz);
            if (s == null || s == this.nav().asDecl(Object.class)) {
                this.baseClass = null;
            } else {
                NonElement b = this.builder.getClassInfo(s, true, this);
                if (b instanceof ClassInfoImpl) {
                    this.baseClass = (ClassInfoImpl)b;
                    this.baseClass.hasSubClasses = true;
                } else {
                    this.baseClass = null;
                }
            }
            this.baseClassComputed = true;
        }
        return this.baseClass;
    }

    @Override
    public final Element<T, C> getSubstitutionHead() {
        ClassInfo c;
        for (c = this.getBaseClass(); c != null && !((ClassInfoImpl)c).isElement(); c = ((ClassInfoImpl)c).getBaseClass()) {
        }
        return c;
    }

    @Override
    public final C getClazz() {
        return this.clazz;
    }

    public ClassInfoImpl<T, C, F, M> getScope() {
        return null;
    }

    @Override
    public final T getType() {
        return (T)this.nav().use(this.clazz);
    }

    @Override
    public boolean canBeReferencedByIDREF() {
        for (PropertyInfo<T, C> p : this.getProperties()) {
            if (p.id() != ID.ID) continue;
            return true;
        }
        ClassInfo base = this.getBaseClass();
        if (base != null) {
            return ((ClassInfoImpl)base).canBeReferencedByIDREF();
        }
        return false;
    }

    @Override
    public final String getName() {
        return this.nav().getClassName(this.clazz);
    }

    public <A extends Annotation> A readAnnotation(Class<A> a) {
        return this.reader().getClassAnnotation(a, this.clazz, this);
    }

    @Override
    public Element<T, C> asElement() {
        if (this.isElement()) {
            return this;
        }
        return null;
    }

    @Override
    public List<? extends PropertyInfo<T, C>> getProperties() {
        if (this.properties != null) {
            return this.properties;
        }
        XmlAccessType at = this.getAccessType();
        this.properties = new FinalArrayList();
        this.findFieldProperties(this.clazz, at);
        this.findGetterSetterProperties(at);
        if (this.propOrder == DEFAULT_ORDER || this.propOrder == null) {
            XmlAccessOrder ao = this.getAccessorOrder();
            if (ao == XmlAccessOrder.ALPHABETICAL) {
                Collections.sort(this.properties);
            }
        } else {
            PropertySorter sorter = new PropertySorter();
            for (PropertyInfoImpl propertyInfoImpl : this.properties) {
                sorter.checkedGet(propertyInfoImpl);
            }
            Collections.sort(this.properties, sorter);
            sorter.checkUnusedProperties();
        }
        PropertyInfoImpl vp = null;
        PropertyInfoImpl ep = null;
        block6: for (PropertyInfoImpl propertyInfoImpl : this.properties) {
            switch (propertyInfoImpl.kind()) {
                case ELEMENT: 
                case REFERENCE: 
                case MAP: {
                    ep = propertyInfoImpl;
                    continue block6;
                }
                case VALUE: {
                    if (vp != null) {
                        this.builder.reportError(new IllegalAnnotationException(Messages.MULTIPLE_VALUE_PROPERTY.format(new Object[0]), vp, (Locatable)propertyInfoImpl));
                    }
                    if (this.getBaseClass() != null) {
                        this.builder.reportError(new IllegalAnnotationException(Messages.XMLVALUE_IN_DERIVED_TYPE.format(new Object[0]), propertyInfoImpl));
                    }
                    vp = propertyInfoImpl;
                    continue block6;
                }
                case ATTRIBUTE: {
                    continue block6;
                }
            }
            assert (false);
        }
        if (ep != null && vp != null) {
            this.builder.reportError(new IllegalAnnotationException(Messages.ELEMENT_AND_VALUE_PROPERTY.format(new Object[0]), vp, (Locatable)ep));
        }
        return this.properties;
    }

    private void findFieldProperties(C c, XmlAccessType at) {
        Object sc = this.nav().getSuperClass(c);
        if (this.shouldRecurseSuperClass(sc)) {
            this.findFieldProperties(sc, at);
        }
        for (Object f : this.nav().getDeclaredFields(c)) {
            Annotation[] annotations = this.reader().getAllFieldAnnotations(f, this);
            boolean isDummy = this.reader().hasFieldAnnotation(OverrideAnnotationOf.class, f);
            if (this.nav().isTransient(f)) {
                if (!ClassInfoImpl.hasJAXBAnnotation(annotations)) continue;
                this.builder.reportError(new IllegalAnnotationException(Messages.TRANSIENT_FIELD_NOT_BINDABLE.format(this.nav().getFieldName(f)), ClassInfoImpl.getSomeJAXBAnnotation(annotations)));
                continue;
            }
            if (this.nav().isStaticField(f)) {
                if (!ClassInfoImpl.hasJAXBAnnotation(annotations)) continue;
                this.addProperty(this.createFieldSeed(f), annotations, false);
                continue;
            }
            if (at == XmlAccessType.FIELD || at == XmlAccessType.PUBLIC_MEMBER && this.nav().isPublicField(f) || ClassInfoImpl.hasJAXBAnnotation(annotations)) {
                if (isDummy) {
                    ClassInfo top;
                    for (top = this.getBaseClass(); top != null && top.getProperty("content") == null; top = top.getBaseClass()) {
                    }
                    DummyPropertyInfo prop = (DummyPropertyInfo)((Object)top.getProperty("content"));
                    PropertySeed seed = this.createFieldSeed(f);
                    prop.addType(this.createReferenceProperty(seed));
                } else {
                    this.addProperty(this.createFieldSeed(f), annotations, false);
                }
            }
            this.checkFieldXmlLocation(f);
        }
    }

    @Override
    public final boolean hasValueProperty() {
        ClassInfo bc = this.getBaseClass();
        if (bc != null && ((ClassInfoImpl)bc).hasValueProperty()) {
            return true;
        }
        for (PropertyInfo<T, C> p : this.getProperties()) {
            if (!(p instanceof ValuePropertyInfo)) continue;
            return true;
        }
        return false;
    }

    @Override
    public PropertyInfo<T, C> getProperty(String name) {
        for (PropertyInfo<T, C> p : this.getProperties()) {
            if (!p.getName().equals(name)) continue;
            return p;
        }
        return null;
    }

    protected void checkFieldXmlLocation(F f) {
    }

    private <T extends Annotation> T getClassOrPackageAnnotation(Class<T> type) {
        T t = this.reader().getClassAnnotation(type, this.clazz, this);
        if (t != null) {
            return t;
        }
        return this.reader().getPackageAnnotation(type, this.clazz, this);
    }

    private XmlAccessType getAccessType() {
        XmlAccessorType xat = this.getClassOrPackageAnnotation(XmlAccessorType.class);
        if (xat != null) {
            return xat.value();
        }
        return XmlAccessType.PUBLIC_MEMBER;
    }

    private XmlAccessOrder getAccessorOrder() {
        XmlAccessorOrder xao = this.getClassOrPackageAnnotation(XmlAccessorOrder.class);
        if (xao != null) {
            return xao.value();
        }
        return XmlAccessOrder.UNDEFINED;
    }

    @Override
    public boolean hasProperties() {
        return !this.properties.isEmpty();
    }

    private static <T> T pickOne(T ... args2) {
        for (T arg : args2) {
            if (arg == null) continue;
            return arg;
        }
        return null;
    }

    private static <T> List<T> makeSet(T ... args2) {
        FinalArrayList l = new FinalArrayList();
        for (T arg : args2) {
            if (arg == null) continue;
            l.add(arg);
        }
        return l;
    }

    private void checkConflict(Annotation a, Annotation b) throws DuplicateException {
        assert (b != null);
        if (a != null) {
            throw new DuplicateException(a, b);
        }
    }

    private void addProperty(PropertySeed<T, C, F, M> seed, Annotation[] annotations, boolean dummy) {
        XmlTransient t = null;
        XmlAnyAttribute aa = null;
        XmlAttribute a = null;
        XmlValue v = null;
        XmlElement e1 = null;
        XmlElements e2 = null;
        XmlElementRef r1 = null;
        XmlElementRefs r2 = null;
        XmlAnyElement xae = null;
        XmlMixed mx = null;
        OverrideAnnotationOf ov = null;
        int secondaryAnnotations = 0;
        try {
            block25: for (Annotation ann : annotations) {
                Integer index = ANNOTATION_NUMBER_MAP.get(ann.annotationType());
                if (index == null) continue;
                switch (index) {
                    case 0: {
                        this.checkConflict(t, ann);
                        t = (XmlTransient)ann;
                        continue block25;
                    }
                    case 1: {
                        this.checkConflict(aa, ann);
                        aa = (XmlAnyAttribute)ann;
                        continue block25;
                    }
                    case 2: {
                        this.checkConflict(a, ann);
                        a = (XmlAttribute)ann;
                        continue block25;
                    }
                    case 3: {
                        this.checkConflict(v, ann);
                        v = (XmlValue)ann;
                        continue block25;
                    }
                    case 4: {
                        this.checkConflict(e1, ann);
                        e1 = (XmlElement)ann;
                        continue block25;
                    }
                    case 5: {
                        this.checkConflict(e2, ann);
                        e2 = (XmlElements)ann;
                        continue block25;
                    }
                    case 6: {
                        this.checkConflict(r1, ann);
                        r1 = (XmlElementRef)ann;
                        continue block25;
                    }
                    case 7: {
                        this.checkConflict(r2, ann);
                        r2 = (XmlElementRefs)ann;
                        continue block25;
                    }
                    case 8: {
                        this.checkConflict(xae, ann);
                        xae = (XmlAnyElement)ann;
                        continue block25;
                    }
                    case 9: {
                        this.checkConflict(mx, ann);
                        mx = (XmlMixed)ann;
                        continue block25;
                    }
                    case 10: {
                        this.checkConflict(ov, ann);
                        ov = (OverrideAnnotationOf)ann;
                        continue block25;
                    }
                    default: {
                        secondaryAnnotations |= 1 << index - 20;
                    }
                }
            }
            PropertyGroup group = null;
            int groupCount = 0;
            if (t != null) {
                group = PropertyGroup.TRANSIENT;
                ++groupCount;
            }
            if (aa != null) {
                group = PropertyGroup.ANY_ATTRIBUTE;
                ++groupCount;
            }
            if (a != null) {
                group = PropertyGroup.ATTRIBUTE;
                ++groupCount;
            }
            if (v != null) {
                group = PropertyGroup.VALUE;
                ++groupCount;
            }
            if (e1 != null || e2 != null) {
                group = PropertyGroup.ELEMENT;
                ++groupCount;
            }
            if (r1 != null || r2 != null || xae != null || mx != null || ov != null) {
                group = PropertyGroup.ELEMENT_REF;
                ++groupCount;
            }
            if (groupCount > 1) {
                List<Annotation> err = ClassInfoImpl.makeSet(t, aa, a, v, ClassInfoImpl.pickOne(e1, e2), ClassInfoImpl.pickOne(r1, r2, xae));
                throw new ConflictException(err);
            }
            if (group == null) {
                assert (groupCount == 0);
                group = this.nav().isSubClassOf(seed.getRawType(), this.nav().ref(Map.class)) && !seed.hasAnnotation(XmlJavaTypeAdapter.class) ? PropertyGroup.MAP : PropertyGroup.ELEMENT;
            } else if (group.equals((Object)PropertyGroup.ELEMENT) && this.nav().isSubClassOf(seed.getRawType(), this.nav().ref(Map.class)) && !seed.hasAnnotation(XmlJavaTypeAdapter.class)) {
                group = PropertyGroup.MAP;
            }
            if ((secondaryAnnotations & group.allowedsecondaryAnnotations) != 0) {
                for (SecondaryAnnotation sa : SECONDARY_ANNOTATIONS) {
                    if (group.allows(sa)) continue;
                    for (Class<? extends Annotation> m : sa.members) {
                        Annotation offender = seed.readAnnotation(m);
                        if (offender == null) continue;
                        this.builder.reportError(new IllegalAnnotationException(Messages.ANNOTATION_NOT_ALLOWED.format(m.getSimpleName()), offender));
                        return;
                    }
                }
                assert (false);
            }
            switch (group) {
                case TRANSIENT: {
                    return;
                }
                case ANY_ATTRIBUTE: {
                    if (this.attributeWildcard != null) {
                        this.builder.reportError(new IllegalAnnotationException(Messages.TWO_ATTRIBUTE_WILDCARDS.format(this.nav().getClassName(this.getClazz())), (Annotation)aa, this.attributeWildcard));
                        return;
                    }
                    this.attributeWildcard = seed;
                    if (this.inheritsAttributeWildcard()) {
                        this.builder.reportError(new IllegalAnnotationException(Messages.SUPER_CLASS_HAS_WILDCARD.format(new Object[0]), (Annotation)aa, this.getInheritedAttributeWildcard()));
                        return;
                    }
                    if (!this.nav().isSubClassOf(seed.getRawType(), this.nav().ref(Map.class))) {
                        this.builder.reportError(new IllegalAnnotationException(Messages.INVALID_ATTRIBUTE_WILDCARD_TYPE.format(this.nav().getTypeName(seed.getRawType())), (Annotation)aa, this.getInheritedAttributeWildcard()));
                        return;
                    }
                    return;
                }
                case ATTRIBUTE: {
                    this.properties.add(this.createAttributeProperty(seed));
                    return;
                }
                case VALUE: {
                    this.properties.add(this.createValueProperty(seed));
                    return;
                }
                case ELEMENT: {
                    this.properties.add(this.createElementProperty(seed));
                    return;
                }
                case ELEMENT_REF: {
                    this.properties.add(this.createReferenceProperty(seed));
                    return;
                }
                case MAP: {
                    this.properties.add(this.createMapProperty(seed));
                    return;
                }
            }
            assert (false);
        }
        catch (ConflictException x) {
            List<Annotation> err = x.annotations;
            this.builder.reportError(new IllegalAnnotationException(Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(this.nav().getClassName(this.getClazz()) + '#' + seed.getName(), err.get(0).annotationType().getName(), err.get(1).annotationType().getName()), err.get(0), err.get(1)));
        }
        catch (DuplicateException e) {
            this.builder.reportError(new IllegalAnnotationException(Messages.DUPLICATE_ANNOTATIONS.format(e.a1.annotationType().getName()), e.a1, e.a2));
        }
    }

    protected ReferencePropertyInfoImpl<T, C, F, M> createReferenceProperty(PropertySeed<T, C, F, M> seed) {
        return new ReferencePropertyInfoImpl<T, C, F, M>(this, seed);
    }

    protected AttributePropertyInfoImpl<T, C, F, M> createAttributeProperty(PropertySeed<T, C, F, M> seed) {
        return new AttributePropertyInfoImpl<T, C, F, M>(this, seed);
    }

    protected ValuePropertyInfoImpl<T, C, F, M> createValueProperty(PropertySeed<T, C, F, M> seed) {
        return new ValuePropertyInfoImpl<T, C, F, M>(this, seed);
    }

    protected ElementPropertyInfoImpl<T, C, F, M> createElementProperty(PropertySeed<T, C, F, M> seed) {
        return new ElementPropertyInfoImpl<T, C, F, M>(this, seed);
    }

    protected MapPropertyInfoImpl<T, C, F, M> createMapProperty(PropertySeed<T, C, F, M> seed) {
        return new MapPropertyInfoImpl<T, C, F, M>(this, seed);
    }

    private void findGetterSetterProperties(XmlAccessType at) {
        LinkedHashMap getters = new LinkedHashMap();
        LinkedHashMap setters = new LinkedHashMap();
        Object c = this.clazz;
        do {
            this.collectGetterSetters(this.clazz, getters, setters);
        } while (this.shouldRecurseSuperClass(c = this.nav().getSuperClass(c)));
        TreeSet<String> complete = new TreeSet<String>(getters.keySet());
        complete.retainAll(setters.keySet());
        this.resurrect(getters, complete);
        this.resurrect(setters, complete);
        for (String name : complete) {
            Annotation[] r;
            Object getter = getters.get(name);
            Object setter = setters.get(name);
            Annotation[] ga = getter != null ? this.reader().getAllMethodAnnotations(getter, new MethodLocatable(this, getter, this.nav())) : EMPTY_ANNOTATIONS;
            Annotation[] sa = setter != null ? this.reader().getAllMethodAnnotations(setter, new MethodLocatable(this, setter, this.nav())) : EMPTY_ANNOTATIONS;
            boolean hasAnnotation = ClassInfoImpl.hasJAXBAnnotation(ga) || ClassInfoImpl.hasJAXBAnnotation(sa);
            boolean isOverriding = false;
            if (!hasAnnotation) {
                boolean bl = isOverriding = getter != null && this.nav().isOverriding(getter, c) && setter != null && this.nav().isOverriding(setter, c);
            }
            if (!(at == XmlAccessType.PROPERTY && !isOverriding || at == XmlAccessType.PUBLIC_MEMBER && this.isConsideredPublic(getter) && this.isConsideredPublic(setter) && !isOverriding) && !hasAnnotation) continue;
            if (getter != null && setter != null && !this.nav().isSameType(this.nav().getReturnType(getter), this.nav().getMethodParameters(setter)[0])) {
                this.builder.reportError(new IllegalAnnotationException(Messages.GETTER_SETTER_INCOMPATIBLE_TYPE.format(this.nav().getTypeName(this.nav().getReturnType(getter)), this.nav().getTypeName(this.nav().getMethodParameters(setter)[0])), new MethodLocatable(this, getter, this.nav()), new MethodLocatable(this, setter, this.nav())));
                continue;
            }
            if (ga.length == 0) {
                r = sa;
            } else if (sa.length == 0) {
                r = ga;
            } else {
                r = new Annotation[ga.length + sa.length];
                System.arraycopy(ga, 0, r, 0, ga.length);
                System.arraycopy(sa, 0, r, ga.length, sa.length);
            }
            this.addProperty(this.createAccessorSeed(getter, setter), r, false);
        }
        getters.keySet().removeAll(complete);
        setters.keySet().removeAll(complete);
    }

    private void collectGetterSetters(C c, Map<String, M> getters, Map<String, M> setters) {
        Object sc = this.nav().getSuperClass(c);
        if (this.shouldRecurseSuperClass(sc)) {
            this.collectGetterSetters(sc, getters, setters);
        }
        Collection methods = this.nav().getDeclaredMethods(c);
        LinkedHashMap allSetters = new LinkedHashMap();
        for (Object MethodT : methods) {
            boolean used = false;
            if (this.nav().isBridgeMethod(MethodT)) continue;
            String name = this.nav().getMethodName(MethodT);
            int arity = this.nav().getMethodParameters(MethodT).length;
            if (this.nav().isStaticMethod(MethodT)) {
                this.ensureNoAnnotation(MethodT);
                continue;
            }
            String propName = ClassInfoImpl.getPropertyNameFromGetMethod(name);
            if (propName != null && arity == 0) {
                getters.put(propName, MethodT);
                used = true;
            }
            if ((propName = ClassInfoImpl.getPropertyNameFromSetMethod(name)) != null && arity == 1) {
                ArrayList propSetters = (ArrayList)allSetters.get(propName);
                if (null == propSetters) {
                    propSetters = new ArrayList();
                    allSetters.put(propName, propSetters);
                }
                propSetters.add(MethodT);
                used = true;
            }
            if (used) continue;
            this.ensureNoAnnotation(MethodT);
        }
        block1: for (Map.Entry entry : getters.entrySet()) {
            String propName = (String)entry.getKey();
            Object getter = entry.getValue();
            List propSetters = (List)allSetters.remove(propName);
            if (null == propSetters) continue;
            Object getterType = this.nav().getReturnType(getter);
            for (Object setter : propSetters) {
                Object setterType = this.nav().getMethodParameters(setter)[0];
                if (!this.nav().isSameType(setterType, getterType)) continue;
                setters.put(propName, setter);
                continue block1;
            }
        }
        for (Map.Entry entry : allSetters.entrySet()) {
            setters.put((String)entry.getKey(), (M)((List)entry.getValue()).get(0));
        }
    }

    private boolean shouldRecurseSuperClass(C sc) {
        return sc != null && (this.builder.isReplaced(sc) || this.reader().hasClassAnnotation(sc, XmlTransient.class));
    }

    private boolean isConsideredPublic(M m) {
        return m == null || this.nav().isPublicMethod(m);
    }

    private void resurrect(Map<String, M> methods, Set<String> complete) {
        for (Map.Entry<String, M> e : methods.entrySet()) {
            if (complete.contains(e.getKey()) || !ClassInfoImpl.hasJAXBAnnotation(this.reader().getAllMethodAnnotations(e.getValue(), this))) continue;
            complete.add(e.getKey());
        }
    }

    private void ensureNoAnnotation(M method) {
        Annotation[] annotations;
        for (Annotation a : annotations = this.reader().getAllMethodAnnotations(method, this)) {
            if (!ClassInfoImpl.isJAXBAnnotation(a)) continue;
            this.builder.reportError(new IllegalAnnotationException(Messages.ANNOTATION_ON_WRONG_METHOD.format(new Object[0]), a));
            return;
        }
    }

    private static boolean isJAXBAnnotation(Annotation a) {
        return ANNOTATION_NUMBER_MAP.containsKey(a.annotationType());
    }

    private static boolean hasJAXBAnnotation(Annotation[] annotations) {
        return ClassInfoImpl.getSomeJAXBAnnotation(annotations) != null;
    }

    private static Annotation getSomeJAXBAnnotation(Annotation[] annotations) {
        for (Annotation a : annotations) {
            if (!ClassInfoImpl.isJAXBAnnotation(a)) continue;
            return a;
        }
        return null;
    }

    private static String getPropertyNameFromGetMethod(String name) {
        if (name.startsWith("get") && name.length() > 3) {
            return name.substring(3);
        }
        if (name.startsWith("is") && name.length() > 2) {
            return name.substring(2);
        }
        return null;
    }

    private static String getPropertyNameFromSetMethod(String name) {
        if (name.startsWith("set") && name.length() > 3) {
            return name.substring(3);
        }
        return null;
    }

    protected PropertySeed<T, C, F, M> createFieldSeed(F f) {
        return new FieldPropertySeed(this, f);
    }

    protected PropertySeed<T, C, F, M> createAccessorSeed(M getter, M setter) {
        return new GetterSetterPropertySeed(this, getter, setter);
    }

    @Override
    public final boolean isElement() {
        return this.elementName != null;
    }

    @Override
    public boolean isAbstract() {
        return this.nav().isAbstract(this.clazz);
    }

    @Override
    public boolean isOrdered() {
        return this.propOrder != null;
    }

    @Override
    public final boolean isFinal() {
        return this.nav().isFinal(this.clazz);
    }

    @Override
    public final boolean hasSubClasses() {
        return this.hasSubClasses;
    }

    @Override
    public final boolean hasAttributeWildcard() {
        return this.declaresAttributeWildcard() || this.inheritsAttributeWildcard();
    }

    @Override
    public final boolean inheritsAttributeWildcard() {
        return this.getInheritedAttributeWildcard() != null;
    }

    @Override
    public final boolean declaresAttributeWildcard() {
        return this.attributeWildcard != null;
    }

    private PropertySeed<T, C, F, M> getInheritedAttributeWildcard() {
        for (ClassInfo c = this.getBaseClass(); c != null; c = ((ClassInfoImpl)c).getBaseClass()) {
            if (((ClassInfoImpl)c).attributeWildcard == null) continue;
            return ((ClassInfoImpl)c).attributeWildcard;
        }
        return null;
    }

    @Override
    public final QName getElementName() {
        return this.elementName;
    }

    @Override
    public final QName getTypeName() {
        return this.typeName;
    }

    @Override
    public final boolean isSimpleType() {
        List<PropertyInfo<T, C>> props = this.getProperties();
        if (props.size() != 1) {
            return false;
        }
        return props.get(0).kind() == PropertyKind.VALUE;
    }

    @Override
    void link() {
        this.getProperties();
        HashMap<String, PropertyInfoImpl> names = new HashMap<String, PropertyInfoImpl>();
        for (PropertyInfoImpl propertyInfoImpl : this.properties) {
            propertyInfoImpl.link();
            PropertyInfoImpl old = names.put(propertyInfoImpl.getName(), propertyInfoImpl);
            if (old == null) continue;
            this.builder.reportError(new IllegalAnnotationException(Messages.PROPERTY_COLLISION.format(propertyInfoImpl.getName()), propertyInfoImpl, (Locatable)old));
        }
        super.link();
    }

    @Override
    public Location getLocation() {
        return this.nav().getClassLocation(this.clazz);
    }

    private boolean hasFactoryConstructor(XmlType t) {
        if (t == null) {
            return false;
        }
        String method = t.factoryMethod();
        Object fClass = this.reader().getClassValue(t, "factoryClass");
        if (method.length() > 0) {
            if (this.nav().isSameType(fClass, this.nav().ref(XmlType.DEFAULT.class))) {
                fClass = this.nav().use(this.clazz);
            }
            for (Object m : this.nav().getDeclaredMethods(this.nav().asDecl(fClass))) {
                if (!this.nav().getMethodName(m).equals(method) || !this.nav().isSameType(this.nav().getReturnType(m), this.nav().use(this.clazz)) || this.nav().getMethodParameters(m).length != 0 || !this.nav().isStaticMethod(m)) continue;
                this.factoryMethod = m;
                break;
            }
            if (this.factoryMethod == null) {
                this.builder.reportError(new IllegalAnnotationException(Messages.NO_FACTORY_METHOD.format(this.nav().getClassName(this.nav().asDecl(fClass)), method), this));
            }
        } else if (!this.nav().isSameType(fClass, this.nav().ref(XmlType.DEFAULT.class))) {
            this.builder.reportError(new IllegalAnnotationException(Messages.FACTORY_CLASS_NEEDS_FACTORY_METHOD.format(this.nav().getClassName(this.nav().asDecl(fClass))), this));
        }
        return this.factoryMethod != null;
    }

    public Method getFactoryMethod() {
        return (Method)this.factoryMethod;
    }

    public String toString() {
        return "ClassInfo(" + this.clazz + ')';
    }

    static {
        Class[] annotations = new Class[]{XmlTransient.class, XmlAnyAttribute.class, XmlAttribute.class, XmlValue.class, XmlElement.class, XmlElements.class, XmlElementRef.class, XmlElementRefs.class, XmlAnyElement.class, XmlMixed.class, OverrideAnnotationOf.class};
        HashMap<Class, Integer> m = ANNOTATION_NUMBER_MAP;
        for (Class c : annotations) {
            m.put(c, m.size());
        }
        int index = 20;
        for (SecondaryAnnotation sa : SECONDARY_ANNOTATIONS) {
            for (Class<? extends Annotation> member : sa.members) {
                m.put(member, index);
            }
            ++index;
        }
        DEFAULT_ORDER = new String[0];
    }

    private static final class PropertyGroup
    extends Enum<PropertyGroup> {
        public static final /* enum */ PropertyGroup TRANSIENT = new PropertyGroup(false, false, false, false, false, false);
        public static final /* enum */ PropertyGroup ANY_ATTRIBUTE = new PropertyGroup(true, false, false, false, false, false);
        public static final /* enum */ PropertyGroup ATTRIBUTE = new PropertyGroup(true, true, true, false, true, true);
        public static final /* enum */ PropertyGroup VALUE = new PropertyGroup(true, true, true, false, true, true);
        public static final /* enum */ PropertyGroup ELEMENT = new PropertyGroup(true, true, true, true, true, true);
        public static final /* enum */ PropertyGroup ELEMENT_REF = new PropertyGroup(true, false, false, true, false, false);
        public static final /* enum */ PropertyGroup MAP = new PropertyGroup(false, false, false, true, false, false);
        final int allowedsecondaryAnnotations;
        private static final /* synthetic */ PropertyGroup[] $VALUES;

        public static PropertyGroup[] values() {
            return (PropertyGroup[])$VALUES.clone();
        }

        public static PropertyGroup valueOf(String name) {
            return Enum.valueOf(PropertyGroup.class, name);
        }

        private PropertyGroup(boolean ... bits) {
            int mask = 0;
            assert (bits.length == SECONDARY_ANNOTATIONS.length);
            for (int i = 0; i < bits.length; ++i) {
                if (!bits[i]) continue;
                mask |= SECONDARY_ANNOTATIONS[i].bitMask;
            }
            this.allowedsecondaryAnnotations = ~mask;
        }

        boolean allows(SecondaryAnnotation a) {
            return (this.allowedsecondaryAnnotations & a.bitMask) == 0;
        }

        static {
            $VALUES = new PropertyGroup[]{TRANSIENT, ANY_ATTRIBUTE, ATTRIBUTE, VALUE, ELEMENT, ELEMENT_REF, MAP};
        }
    }

    private static enum SecondaryAnnotation {
        JAVA_TYPE(1, XmlJavaTypeAdapter.class),
        ID_IDREF(2, XmlID.class, XmlIDREF.class),
        BINARY(4, XmlInlineBinaryData.class, XmlMimeType.class, XmlAttachmentRef.class),
        ELEMENT_WRAPPER(8, XmlElementWrapper.class),
        LIST(16, XmlList.class),
        SCHEMA_TYPE(32, XmlSchemaType.class);

        final int bitMask;
        final Class<? extends Annotation>[] members;

        private SecondaryAnnotation(int bitMask, Class<? extends Annotation> ... members) {
            this.bitMask = bitMask;
            this.members = members;
        }
    }

    private static final class DuplicateException
    extends Exception {
        final Annotation a1;
        final Annotation a2;

        public DuplicateException(Annotation a1, Annotation a2) {
            this.a1 = a1;
            this.a2 = a2;
        }
    }

    private static final class ConflictException
    extends Exception {
        final List<Annotation> annotations;

        public ConflictException(List<Annotation> one) {
            this.annotations = one;
        }
    }

    private final class PropertySorter
    extends HashMap<String, Integer>
    implements Comparator<PropertyInfoImpl> {
        PropertyInfoImpl[] used;
        private Set<String> collidedNames;

        PropertySorter() {
            super(ClassInfoImpl.this.propOrder.length);
            this.used = new PropertyInfoImpl[ClassInfoImpl.this.propOrder.length];
            for (String name : ClassInfoImpl.this.propOrder) {
                if (this.put(name, this.size()) == null) continue;
                ClassInfoImpl.this.builder.reportError(new IllegalAnnotationException(Messages.DUPLICATE_ENTRY_IN_PROP_ORDER.format(name), ClassInfoImpl.this));
            }
        }

        @Override
        public int compare(PropertyInfoImpl o1, PropertyInfoImpl o2) {
            int lhs = this.checkedGet(o1);
            int rhs = this.checkedGet(o2);
            return lhs - rhs;
        }

        private int checkedGet(PropertyInfoImpl p) {
            int ii;
            Integer i = (Integer)this.get(p.getName());
            if (i == null) {
                if (p.kind().isOrdered) {
                    ClassInfoImpl.this.builder.reportError(new IllegalAnnotationException(Messages.PROPERTY_MISSING_FROM_ORDER.format(p.getName()), p));
                }
                i = this.size();
                this.put(p.getName(), i);
            }
            if ((ii = i.intValue()) < this.used.length) {
                if (this.used[ii] != null && this.used[ii] != p) {
                    if (this.collidedNames == null) {
                        this.collidedNames = new HashSet<String>();
                    }
                    if (this.collidedNames.add(p.getName())) {
                        ClassInfoImpl.this.builder.reportError(new IllegalAnnotationException(Messages.DUPLICATE_PROPERTIES.format(p.getName()), p, (Locatable)this.used[ii]));
                    }
                }
                this.used[ii] = p;
            }
            return i;
        }

        public void checkUnusedProperties() {
            for (int i = 0; i < this.used.length; ++i) {
                boolean isOverriding;
                if (this.used[i] != null) continue;
                String unusedName = ClassInfoImpl.this.propOrder[i];
                String nearest = EditDistance.findNearest(unusedName, (Collection<String>)new AbstractList<String>(){

                    @Override
                    public String get(int index) {
                        return ((PropertyInfoImpl)ClassInfoImpl.this.properties.get(index)).getName();
                    }

                    @Override
                    public int size() {
                        return ClassInfoImpl.this.properties.size();
                    }
                });
                boolean bl = isOverriding = i > ClassInfoImpl.this.properties.size() - 1 ? false : ((PropertyInfoImpl)ClassInfoImpl.this.properties.get(i)).hasAnnotation(OverrideAnnotationOf.class);
                if (isOverriding) continue;
                ClassInfoImpl.this.builder.reportError(new IllegalAnnotationException(Messages.PROPERTY_ORDER_CONTAINS_UNUSED_ENTRY.format(unusedName, nearest), ClassInfoImpl.this));
            }
        }
    }
}

