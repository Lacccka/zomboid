/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.txw2.output.ResultFactory
 */
package com.sun.xml.bind.v2.runtime;

import com.sun.istack.NotNull;
import com.sun.istack.Pool;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.api.ErrorListener;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.RawAccessor;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.bind.util.Which;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.annotation.RuntimeInlineAnnotationReader;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.impl.RuntimeBuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.RuntimeModelBuilder;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimeArrayInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeBuiltinLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeEnumLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeLeafInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeTypeInfoSet;
import com.sun.xml.bind.v2.runtime.AnyTypeBeanInfo;
import com.sun.xml.bind.v2.runtime.ArrayBeanInfoImpl;
import com.sun.xml.bind.v2.runtime.BinderImpl;
import com.sun.xml.bind.v2.runtime.BridgeAdapter;
import com.sun.xml.bind.v2.runtime.BridgeContextImpl;
import com.sun.xml.bind.v2.runtime.BridgeImpl;
import com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl;
import com.sun.xml.bind.v2.runtime.CompositeStructureBeanInfo;
import com.sun.xml.bind.v2.runtime.ElementBeanInfoImpl;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationsException;
import com.sun.xml.bind.v2.runtime.InternalBridge;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.LeafBeanInfoImpl;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.bind.v2.runtime.Messages;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.NameBuilder;
import com.sun.xml.bind.v2.runtime.NameList;
import com.sun.xml.bind.v2.runtime.RuntimeUtil;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import com.sun.xml.bind.v2.runtime.ValueListBeanInfoImpl;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.property.AttributeProperty;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator;
import com.sun.xml.bind.v2.util.EditDistance;
import com.sun.xml.bind.v2.util.QNameMap;
import com.sun.xml.bind.v2.util.XmlFactory;
import com.sun.xml.txw2.output.ResultFactory;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
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
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class JAXBContextImpl
extends JAXBRIContext {
    private final Map<TypeReference, Bridge> bridges = new LinkedHashMap<TypeReference, Bridge>();
    private static DocumentBuilder db;
    private final QNameMap<JaxBeanInfo> rootMap = new QNameMap();
    private final HashMap<QName, JaxBeanInfo> typeMap = new HashMap();
    private final Map<Class, JaxBeanInfo> beanInfoMap = new LinkedHashMap<Class, JaxBeanInfo>();
    protected Map<RuntimeTypeInfo, JaxBeanInfo> beanInfos = new LinkedHashMap<RuntimeTypeInfo, JaxBeanInfo>();
    private final Map<Class, Map<QName, ElementBeanInfoImpl>> elements = new LinkedHashMap<Class, Map<QName, ElementBeanInfoImpl>>();
    public final Pool<Marshaller> marshallerPool = new Pool.Impl<Marshaller>(){

        @Override
        @NotNull
        protected Marshaller create() {
            return JAXBContextImpl.this.createMarshaller();
        }
    };
    public final Pool<Unmarshaller> unmarshallerPool = new Pool.Impl<Unmarshaller>(){

        @Override
        @NotNull
        protected Unmarshaller create() {
            return JAXBContextImpl.this.createUnmarshaller();
        }
    };
    public NameBuilder nameBuilder = new NameBuilder();
    public final NameList nameList;
    private final String defaultNsUri;
    private final Class[] classes;
    protected final boolean c14nSupport;
    public final boolean xmlAccessorFactorySupport;
    public final boolean allNillable;
    public final boolean retainPropertyInfo;
    public final boolean supressAccessorWarnings;
    public final boolean improvedXsiTypeHandling;
    public final boolean disableSecurityProcessing;
    private WeakReference<RuntimeTypeInfoSet> typeInfoSetCache;
    @NotNull
    private RuntimeAnnotationReader annotationReader;
    private boolean hasSwaRef;
    @NotNull
    private final Map<Class, Class> subclassReplacements;
    public final boolean fastBoot;
    private Set<XmlNs> xmlNsSet = null;
    public Boolean backupWithParentNamespace = null;
    private Encoded[] utf8nameTable;
    private static final Comparator<QName> QNAME_COMPARATOR;

    public Set<XmlNs> getXmlNsSet() {
        return this.xmlNsSet;
    }

    /*
     * WARNING - void declaration
     */
    private JAXBContextImpl(JAXBContextBuilder builder) throws JAXBException {
        JaxBeanInfo bi2;
        boolean fastB;
        this.defaultNsUri = builder.defaultNsUri;
        this.retainPropertyInfo = builder.retainPropertyInfo;
        this.annotationReader = builder.annotationReader;
        this.subclassReplacements = builder.subclassReplacements;
        this.c14nSupport = builder.c14nSupport;
        this.classes = builder.classes;
        this.xmlAccessorFactorySupport = builder.xmlAccessorFactorySupport;
        this.allNillable = builder.allNillable;
        this.supressAccessorWarnings = builder.supressAccessorWarnings;
        this.improvedXsiTypeHandling = builder.improvedXsiTypeHandling;
        this.disableSecurityProcessing = builder.disableSecurityProcessing;
        this.backupWithParentNamespace = builder.backupWithParentNamespace;
        Collection typeRefs = builder.typeRefs;
        try {
            fastB = Boolean.getBoolean(JAXBContextImpl.class.getName() + ".fastBoot");
        }
        catch (SecurityException e) {
            fastB = false;
        }
        this.fastBoot = fastB;
        RuntimeTypeInfoSet typeSet = this.getTypeInfoSet();
        this.elements.put(null, new LinkedHashMap());
        for (RuntimeBuiltinLeafInfo runtimeBuiltinLeafInfo : RuntimeBuiltinLeafInfoImpl.builtinBeanInfos) {
            LeafBeanInfoImpl bi3 = new LeafBeanInfoImpl(this, runtimeBuiltinLeafInfo);
            this.beanInfoMap.put(runtimeBuiltinLeafInfo.getClazz(), bi3);
            for (QName qName : bi3.getTypeNames()) {
                this.typeMap.put(qName, bi3);
            }
        }
        for (RuntimeEnumLeafInfo runtimeEnumLeafInfo : typeSet.enums().values()) {
            bi2 = this.getOrCreate(runtimeEnumLeafInfo);
            for (QName qName : bi2.getTypeNames()) {
                this.typeMap.put(qName, bi2);
            }
            if (!runtimeEnumLeafInfo.isElement()) continue;
            this.rootMap.put(runtimeEnumLeafInfo.getElementName(), bi2);
        }
        for (RuntimeArrayInfo runtimeArrayInfo : typeSet.arrays().values()) {
            JaxBeanInfo ai = this.getOrCreate(runtimeArrayInfo);
            for (QName qName : ai.getTypeNames()) {
                this.typeMap.put(qName, ai);
            }
        }
        for (Map.Entry entry : typeSet.beans().entrySet()) {
            bi2 = this.getOrCreate((RuntimeClassInfo)entry.getValue());
            XmlSchema xs = this.annotationReader.getPackageAnnotation(XmlSchema.class, entry.getKey(), null);
            if (xs != null && xs.xmlns() != null && xs.xmlns().length > 0) {
                if (this.xmlNsSet == null) {
                    this.xmlNsSet = new HashSet<XmlNs>();
                }
                this.xmlNsSet.addAll(Arrays.asList(xs.xmlns()));
            }
            if (bi2.isElement()) {
                this.rootMap.put(((RuntimeClassInfo)entry.getValue()).getElementName(), bi2);
            }
            for (QName qn : bi2.getTypeNames()) {
                this.typeMap.put(qn, bi2);
            }
        }
        for (RuntimeElementInfo runtimeElementInfo : typeSet.getAllElements()) {
            RuntimeClassInfo scope;
            Class clazz;
            Map<QName, ElementBeanInfoImpl> m;
            bi2 = this.getOrCreate(runtimeElementInfo);
            if (runtimeElementInfo.getScope() == null) {
                this.rootMap.put(runtimeElementInfo.getElementName(), bi2);
            }
            if ((m = this.elements.get(clazz = (scope = runtimeElementInfo.getScope()) == null ? null : (Class)scope.getClazz())) == null) {
                m = new LinkedHashMap<QName, ElementBeanInfoImpl>();
                this.elements.put(clazz, m);
            }
            m.put(runtimeElementInfo.getElementName(), (ElementBeanInfoImpl)bi2);
        }
        this.beanInfoMap.put(JAXBElement.class, new ElementBeanInfoImpl(this));
        this.beanInfoMap.put(CompositeStructure.class, new CompositeStructureBeanInfo(this));
        this.getOrCreate(typeSet.getAnyTypeInfo());
        for (JaxBeanInfo jaxBeanInfo : this.beanInfos.values()) {
            jaxBeanInfo.link(this);
        }
        for (Map.Entry entry : RuntimeUtil.primitiveToBox.entrySet()) {
            this.beanInfoMap.put((Class)entry.getKey(), this.beanInfoMap.get(entry.getValue()));
        }
        Navigator nav = typeSet.getNavigator();
        for (TypeReference tr : typeRefs) {
            void var9_38;
            XmlJavaTypeAdapter xjta = tr.get(XmlJavaTypeAdapter.class);
            Object var9_34 = null;
            XmlList xl = tr.get(XmlList.class);
            Class erasedType = (Class)nav.erasure(tr.type);
            if (xjta != null) {
                Adapter adapter = new Adapter(xjta.value(), nav);
            }
            if (tr.get(XmlAttachmentRef.class) != null) {
                Adapter adapter = new Adapter(SwaRefAdapter.class, nav);
                this.hasSwaRef = true;
            }
            if (var9_38 != null) {
                erasedType = (Class)nav.erasure((Type)var9_38.defaultType);
            }
            Name name = this.nameBuilder.createElementName(tr.tagName);
            InternalBridge bridge = xl == null ? new BridgeImpl(this, name, this.getBeanInfo(erasedType, true), tr) : new BridgeImpl(this, name, new ValueListBeanInfoImpl(this, erasedType), tr);
            if (var9_38 != null) {
                bridge = new BridgeAdapter(bridge, (Class)var9_38.adapterType);
            }
            this.bridges.put(tr, bridge);
        }
        this.nameList = this.nameBuilder.conclude();
        for (JaxBeanInfo bi2 : this.beanInfos.values()) {
            bi2.wrapUp();
        }
        this.nameBuilder = null;
        this.beanInfos = null;
    }

    @Override
    public boolean hasSwaRef() {
        return this.hasSwaRef;
    }

    @Override
    public RuntimeTypeInfoSet getRuntimeTypeInfoSet() {
        try {
            return this.getTypeInfoSet();
        }
        catch (IllegalAnnotationsException e) {
            throw new AssertionError((Object)e);
        }
    }

    public RuntimeTypeInfoSet getTypeInfoSet() throws IllegalAnnotationsException {
        RuntimeTypeInfoSet r;
        if (this.typeInfoSetCache != null && (r = (RuntimeTypeInfoSet)this.typeInfoSetCache.get()) != null) {
            return r;
        }
        RuntimeModelBuilder builder = new RuntimeModelBuilder(this, this.annotationReader, this.subclassReplacements, this.defaultNsUri);
        IllegalAnnotationsException.Builder errorHandler = new IllegalAnnotationsException.Builder();
        builder.setErrorHandler(errorHandler);
        for (Class c : this.classes) {
            if (c == CompositeStructure.class) continue;
            builder.getTypeInfo(new Ref(c));
        }
        this.hasSwaRef |= builder.hasSwaRef;
        RuntimeTypeInfoSet r2 = builder.link();
        errorHandler.check();
        assert (r2 != null) : "if no error was reported, the link must be a success";
        this.typeInfoSetCache = new WeakReference<RuntimeTypeInfoSet>(r2);
        return r2;
    }

    public ElementBeanInfoImpl getElement(Class scope, QName name) {
        ElementBeanInfoImpl bi;
        Map<QName, ElementBeanInfoImpl> m = this.elements.get(scope);
        if (m != null && (bi = m.get(name)) != null) {
            return bi;
        }
        m = this.elements.get(null);
        return m.get(name);
    }

    private ElementBeanInfoImpl getOrCreate(RuntimeElementInfo rei) {
        JaxBeanInfo bi = this.beanInfos.get(rei);
        if (bi != null) {
            return (ElementBeanInfoImpl)bi;
        }
        return new ElementBeanInfoImpl(this, rei);
    }

    protected JaxBeanInfo getOrCreate(RuntimeEnumLeafInfo eli) {
        LeafBeanInfoImpl bi = this.beanInfos.get(eli);
        if (bi != null) {
            return bi;
        }
        bi = new LeafBeanInfoImpl(this, eli);
        this.beanInfoMap.put(bi.jaxbType, bi);
        return bi;
    }

    protected ClassBeanInfoImpl getOrCreate(RuntimeClassInfo ci) {
        ClassBeanInfoImpl bi = (ClassBeanInfoImpl)this.beanInfos.get(ci);
        if (bi != null) {
            return bi;
        }
        bi = new ClassBeanInfoImpl(this, ci);
        this.beanInfoMap.put(bi.jaxbType, bi);
        return bi;
    }

    protected JaxBeanInfo getOrCreate(RuntimeArrayInfo ai) {
        JaxBeanInfo abi = this.beanInfos.get(ai);
        if (abi != null) {
            return abi;
        }
        abi = new ArrayBeanInfoImpl(this, ai);
        this.beanInfoMap.put(ai.getType(), abi);
        return abi;
    }

    public JaxBeanInfo getOrCreate(RuntimeTypeInfo e) {
        if (e instanceof RuntimeElementInfo) {
            return this.getOrCreate((RuntimeElementInfo)e);
        }
        if (e instanceof RuntimeClassInfo) {
            return this.getOrCreate((RuntimeClassInfo)e);
        }
        if (e instanceof RuntimeLeafInfo) {
            JaxBeanInfo bi = this.beanInfos.get(e);
            assert (bi != null);
            return bi;
        }
        if (e instanceof RuntimeArrayInfo) {
            return this.getOrCreate((RuntimeArrayInfo)e);
        }
        if (e.getType() == Object.class) {
            JaxBeanInfo bi = this.beanInfoMap.get(Object.class);
            if (bi == null) {
                bi = new AnyTypeBeanInfo(this, e);
                this.beanInfoMap.put(Object.class, bi);
            }
            return bi;
        }
        throw new IllegalArgumentException();
    }

    public final JaxBeanInfo getBeanInfo(Object o) {
        for (Class<?> c = o.getClass(); c != Object.class; c = c.getSuperclass()) {
            JaxBeanInfo bi = this.beanInfoMap.get(c);
            if (bi == null) continue;
            return bi;
        }
        if (o instanceof Element) {
            return this.beanInfoMap.get(Object.class);
        }
        for (Class<?> c : o.getClass().getInterfaces()) {
            JaxBeanInfo bi = this.beanInfoMap.get(c);
            if (bi == null) continue;
            return bi;
        }
        return null;
    }

    public final JaxBeanInfo getBeanInfo(Object o, boolean fatal) throws JAXBException {
        JaxBeanInfo bi = this.getBeanInfo(o);
        if (bi != null) {
            return bi;
        }
        if (fatal) {
            if (o instanceof Document) {
                throw new JAXBException(Messages.ELEMENT_NEEDED_BUT_FOUND_DOCUMENT.format(o.getClass()));
            }
            throw new JAXBException(Messages.UNKNOWN_CLASS.format(o.getClass()));
        }
        return null;
    }

    public final <T> JaxBeanInfo<T> getBeanInfo(Class<T> clazz) {
        return this.beanInfoMap.get(clazz);
    }

    public final <T> JaxBeanInfo<T> getBeanInfo(Class<T> clazz, boolean fatal) throws JAXBException {
        JaxBeanInfo<T> bi = this.getBeanInfo(clazz);
        if (bi != null) {
            return bi;
        }
        if (fatal) {
            throw new JAXBException(clazz.getName() + " is not known to this context");
        }
        return null;
    }

    public final Loader selectRootLoader(UnmarshallingContext.State state, TagName tag) {
        JaxBeanInfo beanInfo = this.rootMap.get(tag.uri, tag.local);
        if (beanInfo == null) {
            return null;
        }
        return beanInfo.getLoader(this, true);
    }

    public JaxBeanInfo getGlobalType(QName name) {
        return this.typeMap.get(name);
    }

    public String getNearestTypeName(QName name) {
        String[] all = new String[this.typeMap.size()];
        int i = 0;
        for (QName qn : this.typeMap.keySet()) {
            if (qn.getLocalPart().equals(name.getLocalPart())) {
                return qn.toString();
            }
            all[i++] = qn.toString();
        }
        String nearest = EditDistance.findNearest(name.toString(), all);
        if (EditDistance.editDistance(nearest, name.toString()) > 10) {
            return null;
        }
        return nearest;
    }

    public Set<QName> getValidRootNames() {
        TreeSet<QName> r = new TreeSet<QName>(QNAME_COMPARATOR);
        for (QNameMap.Entry<JaxBeanInfo> e : this.rootMap.entrySet()) {
            r.add(e.createQName());
        }
        return r;
    }

    public synchronized Encoded[] getUTF8NameTable() {
        if (this.utf8nameTable == null) {
            Encoded[] x = new Encoded[this.nameList.localNames.length];
            for (int i = 0; i < x.length; ++i) {
                Encoded e = new Encoded(this.nameList.localNames[i]);
                e.compact();
                x[i] = e;
            }
            this.utf8nameTable = x;
        }
        return this.utf8nameTable;
    }

    public int getNumberOfLocalNames() {
        return this.nameList.localNames.length;
    }

    public int getNumberOfElementNames() {
        return this.nameList.numberOfElementNames;
    }

    public int getNumberOfAttributeNames() {
        return this.nameList.numberOfAttributeNames;
    }

    static Transformer createTransformer(boolean disableSecureProcessing) {
        try {
            SAXTransformerFactory tf = (SAXTransformerFactory)XmlFactory.createTransformerFactory(disableSecureProcessing);
            return tf.newTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new Error(e);
        }
    }

    public static TransformerHandler createTransformerHandler(boolean disableSecureProcessing) {
        try {
            SAXTransformerFactory tf = (SAXTransformerFactory)XmlFactory.createTransformerFactory(disableSecureProcessing);
            return tf.newTransformerHandler();
        }
        catch (TransformerConfigurationException e) {
            throw new Error(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Document createDom(boolean disableSecurityProcessing) {
        Class<JAXBContextImpl> clazz = JAXBContextImpl.class;
        synchronized (JAXBContextImpl.class) {
            if (db == null) {
                try {
                    DocumentBuilderFactory dbf = XmlFactory.createDocumentBuilderFactory(disableSecurityProcessing);
                    db = dbf.newDocumentBuilder();
                }
                catch (ParserConfigurationException e) {
                    throw new FactoryConfigurationError(e);
                }
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return db.newDocument();
        }
    }

    @Override
    public MarshallerImpl createMarshaller() {
        return new MarshallerImpl(this, null);
    }

    @Override
    public UnmarshallerImpl createUnmarshaller() {
        return new UnmarshallerImpl(this, null);
    }

    @Override
    public Validator createValidator() {
        throw new UnsupportedOperationException(Messages.NOT_IMPLEMENTED_IN_2_0.format(new Object[0]));
    }

    @Override
    public JAXBIntrospector createJAXBIntrospector() {
        return new JAXBIntrospector(){

            @Override
            public boolean isElement(Object object) {
                return this.getElementName(object) != null;
            }

            @Override
            public QName getElementName(Object jaxbElement) {
                try {
                    return JAXBContextImpl.this.getElementName(jaxbElement);
                }
                catch (JAXBException e) {
                    return null;
                }
            }
        };
    }

    private NonElement<Type, Class> getXmlType(RuntimeTypeInfoSet tis, TypeReference tr) {
        if (tr == null) {
            throw new IllegalArgumentException();
        }
        XmlJavaTypeAdapter xjta = tr.get(XmlJavaTypeAdapter.class);
        XmlList xl = tr.get(XmlList.class);
        Ref<Type, Class> ref = new Ref<Type, Class>(this.annotationReader, tis.getNavigator(), tr.type, xjta, xl);
        return tis.getTypeInfo(ref);
    }

    @Override
    public void generateEpisode(Result output) {
        if (output == null) {
            throw new IllegalArgumentException();
        }
        this.createSchemaGenerator().writeEpisodeFile(ResultFactory.createSerializer((Result)output));
    }

    @Override
    public void generateSchema(SchemaOutputResolver outputResolver) throws IOException {
        if (outputResolver == null) {
            throw new IOException(Messages.NULL_OUTPUT_RESOLVER.format(new Object[0]));
        }
        final SAXParseException[] e = new SAXParseException[1];
        final SAXParseException[] w = new SAXParseException[1];
        this.createSchemaGenerator().write(outputResolver, new ErrorListener(){

            @Override
            public void error(SAXParseException exception) {
                e[0] = exception;
            }

            @Override
            public void fatalError(SAXParseException exception) {
                e[0] = exception;
            }

            @Override
            public void warning(SAXParseException exception) {
                w[0] = exception;
            }

            @Override
            public void info(SAXParseException exception) {
            }
        });
        if (e[0] != null) {
            IOException x = new IOException(Messages.FAILED_TO_GENERATE_SCHEMA.format(new Object[0]));
            x.initCause(e[0]);
            throw x;
        }
        if (w[0] != null) {
            IOException x = new IOException(Messages.ERROR_PROCESSING_SCHEMA.format(new Object[0]));
            x.initCause(w[0]);
            throw x;
        }
    }

    private XmlSchemaGenerator<Type, Class, Field, Method> createSchemaGenerator() {
        RuntimeTypeInfoSet tis;
        try {
            tis = this.getTypeInfoSet();
        }
        catch (IllegalAnnotationsException e) {
            throw new AssertionError((Object)e);
        }
        XmlSchemaGenerator<Type, Class, Field, Method> xsdgen = new XmlSchemaGenerator<Type, Class, Field, Method>(tis.getNavigator(), tis);
        HashSet<QName> rootTagNames = new HashSet<QName>();
        for (RuntimeElementInfo runtimeElementInfo : tis.getAllElements()) {
            rootTagNames.add(runtimeElementInfo.getElementName());
        }
        for (RuntimeClassInfo runtimeClassInfo : tis.beans().values()) {
            if (!runtimeClassInfo.isElement()) continue;
            rootTagNames.add(runtimeClassInfo.asElement().getElementName());
        }
        for (TypeReference typeReference : this.bridges.keySet()) {
            if (rootTagNames.contains(typeReference.tagName)) continue;
            if (typeReference.type == Void.TYPE || typeReference.type == Void.class) {
                xsdgen.add(typeReference.tagName, false, null);
                continue;
            }
            if (typeReference.type == CompositeStructure.class) continue;
            NonElement<Type, Class> typeInfo = this.getXmlType(tis, typeReference);
            xsdgen.add(typeReference.tagName, !tis.getNavigator().isPrimitive(typeReference.type), typeInfo);
        }
        return xsdgen;
    }

    @Override
    public QName getTypeName(TypeReference tr) {
        try {
            NonElement<Type, Class> xt = this.getXmlType(this.getTypeInfoSet(), tr);
            if (xt == null) {
                throw new IllegalArgumentException();
            }
            return xt.getTypeName();
        }
        catch (IllegalAnnotationsException e) {
            throw new AssertionError((Object)e);
        }
    }

    @Override
    public <T> Binder<T> createBinder(Class<T> domType) {
        if (domType == Node.class) {
            return this.createBinder();
        }
        return super.createBinder(domType);
    }

    @Override
    public Binder<Node> createBinder() {
        return new BinderImpl<Node>(this, new DOMScanner());
    }

    @Override
    public QName getElementName(Object o) throws JAXBException {
        JaxBeanInfo bi = this.getBeanInfo(o, true);
        if (!bi.isElement()) {
            return null;
        }
        return new QName(bi.getElementNamespaceURI(o), bi.getElementLocalName(o));
    }

    @Override
    public QName getElementName(Class o) throws JAXBException {
        JaxBeanInfo bi = this.getBeanInfo(o, true);
        if (!bi.isElement()) {
            return null;
        }
        return new QName(bi.getElementNamespaceURI(o), bi.getElementLocalName(o));
    }

    @Override
    public Bridge createBridge(TypeReference ref) {
        return this.bridges.get(ref);
    }

    @Override
    @NotNull
    public BridgeContext createBridgeContext() {
        return new BridgeContextImpl(this);
    }

    public RawAccessor getElementPropertyAccessor(Class wrapperBean, String nsUri, String localName) throws JAXBException {
        JaxBeanInfo bi = this.getBeanInfo(wrapperBean, true);
        if (!(bi instanceof ClassBeanInfoImpl)) {
            throw new JAXBException(wrapperBean + " is not a bean");
        }
        ClassBeanInfoImpl cb = (ClassBeanInfoImpl)bi;
        while (cb != null) {
            for (Property p : cb.properties) {
                final Accessor acc = p.getElementPropertyAccessor(nsUri, localName);
                if (acc == null) continue;
                return new RawAccessor(){

                    public Object get(Object bean) throws AccessorException {
                        return acc.getUnadapted(bean);
                    }

                    public void set(Object bean, Object value) throws AccessorException {
                        acc.setUnadapted(bean, value);
                    }
                };
            }
            cb = cb.superClazz;
        }
        throw new JAXBException(new QName(nsUri, localName) + " is not a valid property on " + wrapperBean);
    }

    @Override
    public List<String> getKnownNamespaceURIs() {
        return Arrays.asList(this.nameList.namespaceURIs);
    }

    @Override
    public String getBuildId() {
        Package pkg = this.getClass().getPackage();
        if (pkg == null) {
            return null;
        }
        return pkg.getImplementationVersion();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(Which.which(this.getClass()) + " Build-Id: " + this.getBuildId());
        buf.append("\nClasses known to this context:\n");
        TreeSet<String> names = new TreeSet<String>();
        for (Class key : this.beanInfoMap.keySet()) {
            names.add(key.getName());
        }
        for (String name : names) {
            buf.append("  ").append(name).append('\n');
        }
        return buf.toString();
    }

    public String getXMIMEContentType(Object o) {
        JaxBeanInfo bi = this.getBeanInfo(o);
        if (!(bi instanceof ClassBeanInfoImpl)) {
            return null;
        }
        ClassBeanInfoImpl cb = (ClassBeanInfoImpl)bi;
        for (Property p : cb.properties) {
            if (!(p instanceof AttributeProperty)) continue;
            AttributeProperty ap = (AttributeProperty)p;
            if (!ap.attName.equals("http://www.w3.org/2005/05/xmlmime", "contentType")) continue;
            try {
                return (String)ap.xacc.print(o);
            }
            catch (AccessorException e) {
                return null;
            }
            catch (SAXException e) {
                return null;
            }
            catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    public JAXBContextImpl createAugmented(Class<?> clazz) throws JAXBException {
        Class[] newList = new Class[this.classes.length + 1];
        System.arraycopy(this.classes, 0, newList, 0, this.classes.length);
        newList[this.classes.length] = clazz;
        JAXBContextBuilder builder = new JAXBContextBuilder(this);
        builder.setClasses(newList);
        return builder.build();
    }

    static {
        QNAME_COMPARATOR = new Comparator<QName>(){

            @Override
            public int compare(QName lhs, QName rhs) {
                int r = lhs.getLocalPart().compareTo(rhs.getLocalPart());
                if (r != 0) {
                    return r;
                }
                return lhs.getNamespaceURI().compareTo(rhs.getNamespaceURI());
            }
        };
    }

    public static class JAXBContextBuilder {
        private boolean retainPropertyInfo = false;
        private boolean supressAccessorWarnings = false;
        private String defaultNsUri = "";
        @NotNull
        private RuntimeAnnotationReader annotationReader = new RuntimeInlineAnnotationReader();
        @NotNull
        private Map<Class, Class> subclassReplacements = Collections.emptyMap();
        private boolean c14nSupport = false;
        private Class[] classes;
        private Collection<TypeReference> typeRefs;
        private boolean xmlAccessorFactorySupport = false;
        private boolean allNillable;
        private boolean improvedXsiTypeHandling = true;
        private boolean disableSecurityProcessing = true;
        private Boolean backupWithParentNamespace = null;

        public JAXBContextBuilder() {
        }

        public JAXBContextBuilder(JAXBContextImpl baseImpl) {
            this.supressAccessorWarnings = baseImpl.supressAccessorWarnings;
            this.retainPropertyInfo = baseImpl.retainPropertyInfo;
            this.defaultNsUri = baseImpl.defaultNsUri;
            this.annotationReader = baseImpl.annotationReader;
            this.subclassReplacements = baseImpl.subclassReplacements;
            this.c14nSupport = baseImpl.c14nSupport;
            this.classes = baseImpl.classes;
            this.typeRefs = baseImpl.bridges.keySet();
            this.xmlAccessorFactorySupport = baseImpl.xmlAccessorFactorySupport;
            this.allNillable = baseImpl.allNillable;
            this.disableSecurityProcessing = baseImpl.disableSecurityProcessing;
            this.backupWithParentNamespace = baseImpl.backupWithParentNamespace;
        }

        public JAXBContextBuilder setRetainPropertyInfo(boolean val) {
            this.retainPropertyInfo = val;
            return this;
        }

        public JAXBContextBuilder setSupressAccessorWarnings(boolean val) {
            this.supressAccessorWarnings = val;
            return this;
        }

        public JAXBContextBuilder setC14NSupport(boolean val) {
            this.c14nSupport = val;
            return this;
        }

        public JAXBContextBuilder setXmlAccessorFactorySupport(boolean val) {
            this.xmlAccessorFactorySupport = val;
            return this;
        }

        public JAXBContextBuilder setDefaultNsUri(String val) {
            this.defaultNsUri = val;
            return this;
        }

        public JAXBContextBuilder setAllNillable(boolean val) {
            this.allNillable = val;
            return this;
        }

        public JAXBContextBuilder setClasses(Class[] val) {
            this.classes = val;
            return this;
        }

        public JAXBContextBuilder setAnnotationReader(RuntimeAnnotationReader val) {
            this.annotationReader = val;
            return this;
        }

        public JAXBContextBuilder setSubclassReplacements(Map<Class, Class> val) {
            this.subclassReplacements = val;
            return this;
        }

        public JAXBContextBuilder setTypeRefs(Collection<TypeReference> val) {
            this.typeRefs = val;
            return this;
        }

        public JAXBContextBuilder setImprovedXsiTypeHandling(boolean val) {
            this.improvedXsiTypeHandling = val;
            return this;
        }

        public JAXBContextBuilder setDisableSecurityProcessing(boolean val) {
            this.disableSecurityProcessing = val;
            return this;
        }

        public JAXBContextBuilder setBackupWithParentNamespace(Boolean backupWithParentNamespace) {
            this.backupWithParentNamespace = backupWithParentNamespace;
            return this;
        }

        public JAXBContextImpl build() throws JAXBException {
            if (this.defaultNsUri == null) {
                this.defaultNsUri = "";
            }
            if (this.subclassReplacements == null) {
                this.subclassReplacements = Collections.emptyMap();
            }
            if (this.annotationReader == null) {
                this.annotationReader = new RuntimeInlineAnnotationReader();
            }
            if (this.typeRefs == null) {
                this.typeRefs = Collections.emptyList();
            }
            return new JAXBContextImpl(this);
        }
    }
}

