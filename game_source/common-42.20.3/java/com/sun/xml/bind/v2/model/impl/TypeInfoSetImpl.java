/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.BuiltinLeafInfo;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.impl.AnyTypeImpl;
import com.sun.xml.bind.v2.model.impl.ArrayInfoImpl;
import com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.ElementInfoImpl;
import com.sun.xml.bind.v2.model.impl.EnumLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.ModelBuilder;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.RuntimeUtil;
import com.sun.xml.bind.v2.util.FlattenIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

class TypeInfoSetImpl<T, C, F, M>
implements TypeInfoSet<T, C, F, M> {
    @XmlTransient
    public final Navigator<T, C, F, M> nav;
    @XmlTransient
    public final AnnotationReader<T, C, F, M> reader;
    private final Map<T, BuiltinLeafInfo<T, C>> builtins = new LinkedHashMap<T, BuiltinLeafInfo<T, C>>();
    private final Map<C, EnumLeafInfoImpl<T, C, F, M>> enums = new LinkedHashMap<C, EnumLeafInfoImpl<T, C, F, M>>();
    private final Map<T, ArrayInfoImpl<T, C, F, M>> arrays = new LinkedHashMap<T, ArrayInfoImpl<T, C, F, M>>();
    @XmlJavaTypeAdapter(value=RuntimeUtil.ToStringAdapter.class)
    private final Map<C, ClassInfoImpl<T, C, F, M>> beans = new LinkedHashMap<C, ClassInfoImpl<T, C, F, M>>();
    @XmlTransient
    private final Map<C, ClassInfoImpl<T, C, F, M>> beansView = Collections.unmodifiableMap(this.beans);
    private final Map<C, Map<QName, ElementInfoImpl<T, C, F, M>>> elementMappings = new LinkedHashMap<C, Map<QName, ElementInfoImpl<T, C, F, M>>>();
    private final Iterable<? extends ElementInfoImpl<T, C, F, M>> allElements = new Iterable<ElementInfoImpl<T, C, F, M>>(){

        @Override
        public Iterator<ElementInfoImpl<T, C, F, M>> iterator() {
            return new FlattenIterator(TypeInfoSetImpl.this.elementMappings.values());
        }
    };
    private final NonElement<T, C> anyType;
    private Map<String, Map<String, String>> xmlNsCache;

    public TypeInfoSetImpl(Navigator<T, C, F, M> nav, AnnotationReader<T, C, F, M> reader, Map<T, ? extends BuiltinLeafInfoImpl<T, C>> leaves) {
        this.nav = nav;
        this.reader = reader;
        this.builtins.putAll(leaves);
        this.anyType = this.createAnyType();
        for (Map.Entry<Class, Class> e : RuntimeUtil.primitiveToBox.entrySet()) {
            this.builtins.put(nav.getPrimitive(e.getKey()), leaves.get(nav.ref(e.getValue())));
        }
        this.elementMappings.put(null, new LinkedHashMap());
    }

    protected NonElement<T, C> createAnyType() {
        return new AnyTypeImpl<T, C>(this.nav);
    }

    @Override
    public Navigator<T, C, F, M> getNavigator() {
        return this.nav;
    }

    public void add(ClassInfoImpl<T, C, F, M> ci) {
        this.beans.put(ci.getClazz(), ci);
    }

    public void add(EnumLeafInfoImpl<T, C, F, M> li) {
        this.enums.put(li.clazz, li);
    }

    public void add(ArrayInfoImpl<T, C, F, M> ai) {
        this.arrays.put(ai.getType(), ai);
    }

    @Override
    public NonElement<T, C> getTypeInfo(T type) {
        LeafInfo l = this.builtins.get(type = this.nav.erasure(type));
        if (l != null) {
            return l;
        }
        if (this.nav.isArray(type)) {
            return this.arrays.get(type);
        }
        C d = this.nav.asDecl(type);
        if (d == null) {
            return null;
        }
        return this.getClassInfo(d);
    }

    @Override
    public NonElement<T, C> getAnyTypeInfo() {
        return this.anyType;
    }

    @Override
    public NonElement<T, C> getTypeInfo(Ref<T, C> ref) {
        assert (!ref.valueList);
        C c = this.nav.asDecl(ref.type);
        if (c != null && this.reader.getClassAnnotation(XmlRegistry.class, c, null) != null) {
            return null;
        }
        return this.getTypeInfo(ref.type);
    }

    @Override
    public Map<C, ? extends ClassInfoImpl<T, C, F, M>> beans() {
        return this.beansView;
    }

    @Override
    public Map<T, ? extends BuiltinLeafInfo<T, C>> builtins() {
        return this.builtins;
    }

    @Override
    public Map<C, ? extends EnumLeafInfoImpl<T, C, F, M>> enums() {
        return this.enums;
    }

    @Override
    public Map<? extends T, ? extends ArrayInfoImpl<T, C, F, M>> arrays() {
        return this.arrays;
    }

    @Override
    public NonElement<T, C> getClassInfo(C type) {
        LeafInfo l = this.builtins.get(this.nav.use(type));
        if (l != null) {
            return l;
        }
        l = this.enums.get(type);
        if (l != null) {
            return l;
        }
        if (this.nav.asDecl(Object.class).equals(type)) {
            return this.anyType;
        }
        return this.beans.get(type);
    }

    public ElementInfoImpl<T, C, F, M> getElementInfo(C scope, QName name) {
        while (scope != null) {
            ElementInfoImpl<T, C, F, M> r;
            Map<QName, ElementInfoImpl<T, C, F, M>> m = this.elementMappings.get(scope);
            if (m != null && (r = m.get(name)) != null) {
                return r;
            }
            scope = this.nav.getSuperClass(scope);
        }
        return this.elementMappings.get(null).get(name);
    }

    public final void add(ElementInfoImpl<T, C, F, M> ei, ModelBuilder<T, C, F, M> builder) {
        ElementInfoImpl<T, C, F, M> existing;
        Map<QName, ElementInfoImpl<T, C, F, M>> m;
        Object scope = null;
        if (ei.getScope() != null) {
            scope = ei.getScope().getClazz();
        }
        if ((m = this.elementMappings.get(scope)) == null) {
            m = new LinkedHashMap<QName, ElementInfoImpl<T, C, F, M>>();
            this.elementMappings.put(scope, m);
        }
        if ((existing = m.put(ei.getElementName(), ei)) != null) {
            QName en = ei.getElementName();
            builder.reportError(new IllegalAnnotationException(Messages.CONFLICTING_XML_ELEMENT_MAPPING.format(en.getNamespaceURI(), en.getLocalPart()), ei, existing));
        }
    }

    @Override
    public Map<QName, ? extends ElementInfoImpl<T, C, F, M>> getElementMappings(C scope) {
        return this.elementMappings.get(scope);
    }

    @Override
    public Iterable<? extends ElementInfoImpl<T, C, F, M>> getAllElements() {
        return this.allElements;
    }

    @Override
    public Map<String, String> getXmlNs(String namespaceUri) {
        Map<String, String> r;
        if (this.xmlNsCache == null) {
            this.xmlNsCache = new HashMap<String, Map<String, String>>();
            for (ClassInfoImpl<T, C, F, M> ci : this.beans().values()) {
                XmlSchema xs = this.reader.getPackageAnnotation(XmlSchema.class, ci.getClazz(), null);
                if (xs == null) continue;
                String uri = xs.namespace();
                Map<String, String> m = this.xmlNsCache.get(uri);
                if (m == null) {
                    m = new HashMap<String, String>();
                    this.xmlNsCache.put(uri, m);
                }
                for (XmlNs xns : xs.xmlns()) {
                    m.put(xns.prefix(), xns.namespaceURI());
                }
            }
        }
        if ((r = this.xmlNsCache.get(namespaceUri)) != null) {
            return r;
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getSchemaLocations() {
        HashMap<String, String> r = new HashMap<String, String>();
        for (ClassInfoImpl<T, C, F, M> ci : this.beans().values()) {
            String loc;
            XmlSchema xs = this.reader.getPackageAnnotation(XmlSchema.class, ci.getClazz(), null);
            if (xs == null || (loc = xs.location()).equals("##generate")) continue;
            r.put(xs.namespace(), loc);
        }
        return r;
    }

    @Override
    public final XmlNsForm getElementFormDefault(String nsUri) {
        for (ClassInfoImpl<T, C, F, M> ci : this.beans().values()) {
            XmlNsForm xnf;
            XmlSchema xs = this.reader.getPackageAnnotation(XmlSchema.class, ci.getClazz(), null);
            if (xs == null || !xs.namespace().equals(nsUri) || (xnf = xs.elementFormDefault()) == XmlNsForm.UNSET) continue;
            return xnf;
        }
        return XmlNsForm.UNSET;
    }

    @Override
    public final XmlNsForm getAttributeFormDefault(String nsUri) {
        for (ClassInfoImpl<T, C, F, M> ci : this.beans().values()) {
            XmlNsForm xnf;
            XmlSchema xs = this.reader.getPackageAnnotation(XmlSchema.class, ci.getClazz(), null);
            if (xs == null || !xs.namespace().equals(nsUri) || (xnf = xs.attributeFormDefault()) == XmlNsForm.UNSET) continue;
            return xnf;
        }
        return XmlNsForm.UNSET;
    }

    @Override
    public void dump(Result out) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(this.getClass());
        Marshaller m = context.createMarshaller();
        m.marshal((Object)this, out);
    }
}

