/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator$Namespace.ElementDeclaration
 *  com.sun.xml.txw2.TXW
 *  com.sun.xml.txw2.TxwException
 *  com.sun.xml.txw2.TypedXmlWriter
 *  com.sun.xml.txw2.output.ResultFactory
 *  com.sun.xml.txw2.output.XmlSerializer
 */
package com.sun.xml.bind.v2.schemagen;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.api.ErrorListener;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.Adapter;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.EnumConstant;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.MapPropertyInfo;
import com.sun.xml.bind.v2.model.core.MaybeElement;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.NonElementRef;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.core.TypeRef;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.SwaRefAdapter;
import com.sun.xml.bind.v2.schemagen.FoolProofResolver;
import com.sun.xml.bind.v2.schemagen.Form;
import com.sun.xml.bind.v2.schemagen.GroupKind;
import com.sun.xml.bind.v2.schemagen.Messages;
import com.sun.xml.bind.v2.schemagen.MultiMap;
import com.sun.xml.bind.v2.schemagen.Tree;
import com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator;
import com.sun.xml.bind.v2.schemagen.episode.Bindings;
import com.sun.xml.bind.v2.schemagen.xmlschema.Any;
import com.sun.xml.bind.v2.schemagen.xmlschema.AttrDecls;
import com.sun.xml.bind.v2.schemagen.xmlschema.AttributeType;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexExtension;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexType;
import com.sun.xml.bind.v2.schemagen.xmlschema.ComplexTypeHost;
import com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer;
import com.sun.xml.bind.v2.schemagen.xmlschema.ExplicitGroup;
import com.sun.xml.bind.v2.schemagen.xmlschema.Import;
import com.sun.xml.bind.v2.schemagen.xmlschema.List;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.LocalElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.Schema;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleExtension;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleRestriction;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleType;
import com.sun.xml.bind.v2.schemagen.xmlschema.SimpleTypeHost;
import com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelAttribute;
import com.sun.xml.bind.v2.schemagen.xmlschema.TopLevelElement;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeHost;
import com.sun.xml.bind.v2.util.CollisionCheckStack;
import com.sun.xml.bind.v2.util.StackRecorder;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.TxwException;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.output.ResultFactory;
import com.sun.xml.txw2.output.XmlSerializer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.MimeType;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.SAXParseException;

public final class XmlSchemaGenerator<T, C, F, M> {
    private static final Logger logger = Util.getClassLogger();
    private final Map<String, Namespace> namespaces = new TreeMap<String, Namespace>(NAMESPACE_COMPARATOR);
    private ErrorListener errorListener;
    private Navigator<T, C, F, M> navigator;
    private final TypeInfoSet<T, C, F, M> types;
    private final NonElement<T, C> stringType;
    private final NonElement<T, C> anyType;
    private final CollisionCheckStack<ClassInfo<T, C>> collisionChecker = new CollisionCheckStack();
    private static final Comparator<String> NAMESPACE_COMPARATOR = new Comparator<String>(){

        @Override
        public int compare(String lhs, String rhs) {
            return -lhs.compareTo(rhs);
        }
    };
    private static final String newline = "\n";

    public XmlSchemaGenerator(Navigator<T, C, F, M> navigator, TypeInfoSet<T, C, F, M> types) {
        this.navigator = navigator;
        this.types = types;
        this.stringType = types.getTypeInfo(navigator.ref(String.class));
        this.anyType = types.getAnyTypeInfo();
        for (ClassInfo<T, Object> classInfo : types.beans().values()) {
            this.add(classInfo);
        }
        for (ElementInfo elementInfo : types.getElementMappings(null).values()) {
            this.add(elementInfo);
        }
        for (EnumLeafInfo enumLeafInfo : types.enums().values()) {
            this.add(enumLeafInfo);
        }
        for (ArrayInfo arrayInfo : types.arrays().values()) {
            this.add(arrayInfo);
        }
    }

    private Namespace getNamespace(String uri) {
        Namespace n = this.namespaces.get(uri);
        if (n == null) {
            n = new Namespace(uri);
            this.namespaces.put(uri, n);
        }
        return n;
    }

    public void add(ClassInfo<T, C> clazz) {
        QName tn;
        assert (clazz != null);
        String nsUri = null;
        if (clazz.getClazz() == this.navigator.asDecl(CompositeStructure.class)) {
            return;
        }
        if (clazz.isElement()) {
            nsUri = clazz.getElementName().getNamespaceURI();
            Namespace ns = this.getNamespace(nsUri);
            ns.classes.add(clazz);
            ns.addDependencyTo(clazz.getTypeName());
            this.add(clazz.getElementName(), false, clazz);
        }
        if ((tn = clazz.getTypeName()) != null) {
            nsUri = tn.getNamespaceURI();
        } else if (nsUri == null) {
            return;
        }
        Namespace n = this.getNamespace(nsUri);
        n.classes.add(clazz);
        for (PropertyInfo<T, C> p : clazz.getProperties()) {
            MimeType mimeType;
            AttributePropertyInfo ap;
            String aUri;
            n.processForeignNamespaces(p, 1);
            if (p instanceof AttributePropertyInfo && (aUri = (ap = (AttributePropertyInfo)p).getXmlName().getNamespaceURI()).length() > 0) {
                this.getNamespace(aUri).addGlobalAttribute(ap);
                n.addDependencyTo(ap.getXmlName());
            }
            if (p instanceof ElementPropertyInfo) {
                ElementPropertyInfo ep = (ElementPropertyInfo)p;
                for (TypeRef tref : ep.getTypes()) {
                    String eUri = tref.getTagName().getNamespaceURI();
                    if (eUri.length() <= 0 || eUri.equals(n.uri)) continue;
                    this.getNamespace(eUri).addGlobalElement(tref);
                    n.addDependencyTo(tref.getTagName());
                }
            }
            if (this.generateSwaRefAdapter(p)) {
                n.useSwaRef = true;
            }
            if ((mimeType = p.getExpectedMimeType()) == null) continue;
            n.useMimeNs = true;
        }
        ClassInfo<T, C> bc = clazz.getBaseClass();
        if (bc != null) {
            this.add(bc);
            n.addDependencyTo(bc.getTypeName());
        }
    }

    public void add(ElementInfo<T, C> elem) {
        assert (elem != null);
        boolean nillable = false;
        QName name = elem.getElementName();
        Namespace n = this.getNamespace(name.getNamespaceURI());
        ElementInfo<T, Object> ei = elem.getScope() != null ? this.types.getElementInfo(elem.getScope().getClazz(), name) : this.types.getElementInfo(null, name);
        XmlElement xmlElem = ei.getProperty().readAnnotation(XmlElement.class);
        nillable = xmlElem == null ? false : xmlElem.nillable();
        MultiMap multiMap = n.elementDecls;
        String string = name.getLocalPart();
        Namespace namespace = n;
        Objects.requireNonNull(namespace);
        multiMap.put(string, namespace.new Namespace.ElementWithType(nillable, elem.getContentType()));
        n.processForeignNamespaces(elem.getProperty(), 1);
    }

    public void add(EnumLeafInfo<T, C> envm) {
        QName typeName;
        assert (envm != null);
        String nsUri = null;
        if (envm.isElement()) {
            nsUri = envm.getElementName().getNamespaceURI();
            Namespace ns = this.getNamespace(nsUri);
            ns.enums.add(envm);
            ns.addDependencyTo(envm.getTypeName());
            this.add(envm.getElementName(), false, envm);
        }
        if ((typeName = envm.getTypeName()) != null) {
            nsUri = typeName.getNamespaceURI();
        } else if (nsUri == null) {
            return;
        }
        Namespace n = this.getNamespace(nsUri);
        n.enums.add(envm);
        n.addDependencyTo(envm.getBaseType().getTypeName());
    }

    public void add(ArrayInfo<T, C> a) {
        assert (a != null);
        String namespaceURI = a.getTypeName().getNamespaceURI();
        Namespace n = this.getNamespace(namespaceURI);
        n.arrays.add(a);
        n.addDependencyTo(a.getItemType().getTypeName());
    }

    public void add(QName tagName, boolean isNillable, NonElement<T, C> type) {
        if (type != null && type.getType() == this.navigator.ref(CompositeStructure.class)) {
            return;
        }
        Namespace n = this.getNamespace(tagName.getNamespaceURI());
        MultiMap multiMap = n.elementDecls;
        String string = tagName.getLocalPart();
        Namespace namespace = n;
        Objects.requireNonNull(namespace);
        multiMap.put(string, namespace.new Namespace.ElementWithType(isNillable, type));
        if (type != null) {
            n.addDependencyTo(type.getTypeName());
        }
    }

    public void writeEpisodeFile(XmlSerializer out) {
        Bindings root = (Bindings)TXW.create(Bindings.class, (XmlSerializer)out);
        if (this.namespaces.containsKey("")) {
            root._namespace("http://java.sun.com/xml/ns/jaxb", "jaxb");
        }
        root.version("2.1");
        for (Map.Entry<String, Namespace> e : this.namespaces.entrySet()) {
            Bindings child;
            String prefix;
            Bindings group = root.bindings();
            String tns = e.getKey();
            if (!tns.equals("")) {
                group._namespace(tns, "tns");
                prefix = "tns:";
            } else {
                prefix = "";
            }
            group.scd("x-schema::" + (tns.equals("") ? "" : "tns"));
            group.schemaBindings().map(false);
            for (ClassInfo ci : e.getValue().classes) {
                if (ci.getTypeName() == null) continue;
                if (ci.getTypeName().getNamespaceURI().equals(tns)) {
                    child = group.bindings();
                    child.scd('~' + prefix + ci.getTypeName().getLocalPart());
                    child.klass().ref(ci.getName());
                }
                if (!ci.isElement() || !ci.getElementName().getNamespaceURI().equals(tns)) continue;
                child = group.bindings();
                child.scd(prefix + ci.getElementName().getLocalPart());
                child.klass().ref(ci.getName());
            }
            for (EnumLeafInfo en : e.getValue().enums) {
                if (en.getTypeName() == null) continue;
                child = group.bindings();
                child.scd('~' + prefix + en.getTypeName().getLocalPart());
                child.klass().ref(this.navigator.getClassName(en.getClazz()));
            }
            group.commit(true);
        }
        root.commit();
    }

    public void write(SchemaOutputResolver resolver, ErrorListener errorListener) throws IOException {
        if (resolver == null) {
            throw new IllegalArgumentException();
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Writing XML Schema for " + this.toString(), new StackRecorder());
        }
        resolver = new FoolProofResolver(resolver);
        this.errorListener = errorListener;
        Map<String, String> schemaLocations = this.types.getSchemaLocations();
        HashMap<Namespace, Result> out = new HashMap<Namespace, Result>();
        HashMap<Namespace, String> systemIds = new HashMap<Namespace, String>();
        this.namespaces.remove("http://www.w3.org/2001/XMLSchema");
        for (Namespace namespace : this.namespaces.values()) {
            String schemaLocation = schemaLocations.get(namespace.uri);
            if (schemaLocation != null) {
                systemIds.put(namespace, schemaLocation);
            } else {
                Result output = resolver.createOutput(namespace.uri, "schema" + (out.size() + 1) + ".xsd");
                if (output != null) {
                    out.put(namespace, output);
                    systemIds.put(namespace, output.getSystemId());
                }
            }
            namespace.resetWritten();
        }
        for (Map.Entry entry : out.entrySet()) {
            Result result = (Result)entry.getValue();
            ((Namespace)entry.getKey()).writeTo(result, systemIds);
            if (!(result instanceof StreamResult)) continue;
            OutputStream outputStream2 = ((StreamResult)result).getOutputStream();
            if (outputStream2 != null) {
                outputStream2.close();
                continue;
            }
            Writer writer = ((StreamResult)result).getWriter();
            if (writer == null) continue;
            writer.close();
        }
    }

    private boolean generateSwaRefAdapter(NonElementRef<T, C> typeRef) {
        return this.generateSwaRefAdapter(typeRef.getSource());
    }

    private boolean generateSwaRefAdapter(PropertyInfo<T, C> prop) {
        Adapter<T, C> adapter = prop.getAdapter();
        if (adapter == null) {
            return false;
        }
        C o = this.navigator.asDecl(SwaRefAdapter.class);
        if (o == null) {
            return false;
        }
        return o.equals(adapter.adapterType);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Namespace ns : this.namespaces.values()) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(ns.uri).append('=').append(ns);
        }
        return super.toString() + '[' + buf + ']';
    }

    private static String getProcessContentsModeName(WildcardMode wc) {
        switch (wc) {
            case LAX: 
            case SKIP: {
                return wc.name().toLowerCase();
            }
            case STRICT: {
                return null;
            }
        }
        throw new IllegalStateException();
    }

    protected static String relativize(String uri, String baseUri) {
        try {
            assert (uri != null);
            if (baseUri == null) {
                return uri;
            }
            URI theUri = new URI(com.sun.xml.bind.v2.schemagen.Util.escapeURI(uri));
            URI theBaseUri = new URI(com.sun.xml.bind.v2.schemagen.Util.escapeURI(baseUri));
            if (theUri.isOpaque() || theBaseUri.isOpaque()) {
                return uri;
            }
            if (!com.sun.xml.bind.v2.schemagen.Util.equalsIgnoreCase(theUri.getScheme(), theBaseUri.getScheme()) || !com.sun.xml.bind.v2.schemagen.Util.equal(theUri.getAuthority(), theBaseUri.getAuthority())) {
                return uri;
            }
            String uriPath = theUri.getPath();
            String basePath = theBaseUri.getPath();
            if (!basePath.endsWith("/")) {
                basePath = com.sun.xml.bind.v2.schemagen.Util.normalizeUriPath(basePath);
            }
            if (uriPath.equals(basePath)) {
                return ".";
            }
            String relPath = XmlSchemaGenerator.calculateRelativePath(uriPath, basePath, XmlSchemaGenerator.fixNull(theUri.getScheme()).equals("file"));
            if (relPath == null) {
                return uri;
            }
            StringBuilder relUri = new StringBuilder();
            relUri.append(relPath);
            if (theUri.getQuery() != null) {
                relUri.append('?').append(theUri.getQuery());
            }
            if (theUri.getFragment() != null) {
                relUri.append('#').append(theUri.getFragment());
            }
            return relUri.toString();
        }
        catch (URISyntaxException e) {
            throw new InternalError("Error escaping one of these uris:\n\t" + uri + "\n\t" + baseUri);
        }
    }

    private static String fixNull(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    private static String calculateRelativePath(String uri, String base, boolean fileUrl) {
        boolean onWindows;
        boolean bl = onWindows = File.pathSeparatorChar == ';';
        if (base == null) {
            return null;
        }
        if (fileUrl && onWindows && XmlSchemaGenerator.startsWithIgnoreCase(uri, base) || uri.startsWith(base)) {
            return uri.substring(base.length());
        }
        return "../" + XmlSchemaGenerator.calculateRelativePath(uri, com.sun.xml.bind.v2.schemagen.Util.getParentUriPath(base), fileUrl);
    }

    private static boolean startsWithIgnoreCase(String s, String t) {
        return s.toUpperCase().startsWith(t.toUpperCase());
    }

    static /* synthetic */ NonElement access$900(XmlSchemaGenerator x0) {
        return x0.anyType;
    }

    private class Namespace {
        @NotNull
        final String uri;
        private final Set<Namespace> depends = new LinkedHashSet<Namespace>();
        private boolean selfReference;
        private final Set<ClassInfo<T, C>> classes = new LinkedHashSet();
        private final Set<EnumLeafInfo<T, C>> enums = new LinkedHashSet();
        private final Set<ArrayInfo<T, C>> arrays = new LinkedHashSet();
        private final MultiMap<String, AttributePropertyInfo<T, C>> attributeDecls = new MultiMap(null);
        private final MultiMap<String, com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator$Namespace.ElementDeclaration> elementDecls = new MultiMap(new ElementWithType(true, XmlSchemaGenerator.access$900(XmlSchemaGenerator.this)));
        private Form attributeFormDefault;
        private Form elementFormDefault;
        private boolean useSwaRef;
        private boolean useMimeNs;
        private final Set<ClassInfo> written = new HashSet<ClassInfo>();

        public Namespace(String uri) {
            this.uri = uri;
            assert (!XmlSchemaGenerator.this.namespaces.containsKey(uri));
            XmlSchemaGenerator.this.namespaces.put(uri, this);
        }

        void resetWritten() {
            this.written.clear();
        }

        private void processForeignNamespaces(PropertyInfo<T, C> p, int processingDepth) {
            for (TypeInfo t : p.ref()) {
                if (t instanceof ClassInfo && processingDepth > 0) {
                    java.util.List l = ((ClassInfo)t).getProperties();
                    for (PropertyInfo subp : l) {
                        this.processForeignNamespaces(subp, --processingDepth);
                    }
                }
                if (t instanceof Element) {
                    this.addDependencyTo(((Element)t).getElementName());
                }
                if (!(t instanceof NonElement)) continue;
                this.addDependencyTo(((NonElement)t).getTypeName());
            }
        }

        private void addDependencyTo(@Nullable QName qname) {
            if (qname == null) {
                return;
            }
            String nsUri = qname.getNamespaceURI();
            if (nsUri.equals("http://www.w3.org/2001/XMLSchema")) {
                return;
            }
            if (nsUri.equals(this.uri)) {
                this.selfReference = true;
                return;
            }
            this.depends.add(XmlSchemaGenerator.this.getNamespace(nsUri));
        }

        private void writeTo(Result result, Map<Namespace, String> systemIds) throws IOException {
            try {
                Schema schema = (Schema)TXW.create(Schema.class, (XmlSerializer)ResultFactory.createSerializer((Result)result));
                Map<String, String> xmlNs = XmlSchemaGenerator.this.types.getXmlNs(this.uri);
                for (Map.Entry<String, String> entry : xmlNs.entrySet()) {
                    schema._namespace(entry.getValue(), entry.getKey());
                }
                if (this.useSwaRef) {
                    schema._namespace("http://ws-i.org/profiles/basic/1.1/xsd", "swaRef");
                }
                if (this.useMimeNs) {
                    schema._namespace("http://www.w3.org/2005/05/xmlmime", "xmime");
                }
                this.attributeFormDefault = Form.get(XmlSchemaGenerator.this.types.getAttributeFormDefault(this.uri));
                this.attributeFormDefault.declare("attributeFormDefault", schema);
                this.elementFormDefault = Form.get(XmlSchemaGenerator.this.types.getElementFormDefault(this.uri));
                this.elementFormDefault.declare("elementFormDefault", schema);
                if (!xmlNs.containsValue("http://www.w3.org/2001/XMLSchema") && !xmlNs.containsKey("xs")) {
                    schema._namespace("http://www.w3.org/2001/XMLSchema", "xs");
                }
                schema.version("1.0");
                if (this.uri.length() != 0) {
                    schema.targetNamespace(this.uri);
                }
                for (Namespace namespace : this.depends) {
                    schema._namespace(namespace.uri);
                }
                if (this.selfReference && this.uri.length() != 0) {
                    schema._namespace(this.uri, "tns");
                }
                schema._pcdata(XmlSchemaGenerator.newline);
                for (Namespace namespace : this.depends) {
                    String refSystemId;
                    Import imp = schema._import();
                    if (namespace.uri.length() != 0) {
                        imp.namespace(namespace.uri);
                    }
                    if ((refSystemId = systemIds.get(namespace)) != null && !refSystemId.equals("")) {
                        imp.schemaLocation(XmlSchemaGenerator.relativize(refSystemId, result.getSystemId()));
                    }
                    schema._pcdata(XmlSchemaGenerator.newline);
                }
                if (this.useSwaRef) {
                    schema._import().namespace("http://ws-i.org/profiles/basic/1.1/xsd").schemaLocation("http://ws-i.org/profiles/basic/1.1/swaref.xsd");
                }
                if (this.useMimeNs) {
                    schema._import().namespace("http://www.w3.org/2005/05/xmlmime").schemaLocation("http://www.w3.org/2005/05/xmlmime");
                }
                for (Map.Entry entry : this.elementDecls.entrySet()) {
                    ((ElementDeclaration)entry.getValue()).writeTo((String)entry.getKey(), schema);
                    schema._pcdata(XmlSchemaGenerator.newline);
                }
                for (ClassInfo classInfo : this.classes) {
                    if (classInfo.getTypeName() == null) continue;
                    if (this.uri.equals(classInfo.getTypeName().getNamespaceURI())) {
                        this.writeClass(classInfo, schema);
                    }
                    schema._pcdata(XmlSchemaGenerator.newline);
                }
                for (EnumLeafInfo enumLeafInfo : this.enums) {
                    if (enumLeafInfo.getTypeName() == null) continue;
                    if (this.uri.equals(enumLeafInfo.getTypeName().getNamespaceURI())) {
                        this.writeEnum(enumLeafInfo, schema);
                    }
                    schema._pcdata(XmlSchemaGenerator.newline);
                }
                for (ArrayInfo arrayInfo : this.arrays) {
                    this.writeArray(arrayInfo, schema);
                    schema._pcdata(XmlSchemaGenerator.newline);
                }
                for (Map.Entry entry : this.attributeDecls.entrySet()) {
                    TopLevelAttribute a = schema.attribute();
                    a.name((String)entry.getKey());
                    if (entry.getValue() == null) {
                        this.writeTypeRef((TypeHost)a, XmlSchemaGenerator.this.stringType, "type");
                    } else {
                        this.writeAttributeTypeRef((AttributePropertyInfo)entry.getValue(), a);
                    }
                    schema._pcdata(XmlSchemaGenerator.newline);
                }
                schema.commit();
            }
            catch (TxwException e) {
                logger.log(Level.INFO, e.getMessage(), e);
                throw new IOException(e.getMessage());
            }
        }

        private void writeTypeRef(TypeHost th, NonElementRef<T, C> typeRef, String refAttName) {
            switch (typeRef.getSource().id()) {
                case ID: {
                    th._attribute(refAttName, new QName("http://www.w3.org/2001/XMLSchema", "ID"));
                    return;
                }
                case IDREF: {
                    th._attribute(refAttName, new QName("http://www.w3.org/2001/XMLSchema", "IDREF"));
                    return;
                }
                case NONE: {
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
            MimeType mimeType = typeRef.getSource().getExpectedMimeType();
            if (mimeType != null) {
                th._attribute(new QName("http://www.w3.org/2005/05/xmlmime", "expectedContentTypes", "xmime"), mimeType.toString());
            }
            if (XmlSchemaGenerator.this.generateSwaRefAdapter(typeRef)) {
                th._attribute(refAttName, new QName("http://ws-i.org/profiles/basic/1.1/xsd", "swaRef", "ref"));
                return;
            }
            if (typeRef.getSource().getSchemaType() != null) {
                th._attribute(refAttName, typeRef.getSource().getSchemaType());
                return;
            }
            this.writeTypeRef(th, typeRef.getTarget(), refAttName);
        }

        private void writeTypeRef(TypeHost th, NonElement<T, C> type, String refAttName) {
            MaybeElement me;
            boolean isElement;
            Element e = null;
            if (type instanceof MaybeElement && (isElement = (me = (MaybeElement)type).isElement())) {
                e = me.asElement();
            }
            if (type instanceof Element) {
                e = (Element)((Object)type);
            }
            if (type.getTypeName() == null) {
                if (e != null && e.getElementName() != null) {
                    th.block();
                    if (type instanceof ClassInfo) {
                        this.writeClass((ClassInfo)type, th);
                    } else {
                        this.writeEnum((EnumLeafInfo)type, (SimpleTypeHost)th);
                    }
                } else {
                    th.block();
                    if (type instanceof ClassInfo) {
                        if (XmlSchemaGenerator.this.collisionChecker.push((ClassInfo)type)) {
                            XmlSchemaGenerator.this.errorListener.warning(new SAXParseException(Messages.ANONYMOUS_TYPE_CYCLE.format(XmlSchemaGenerator.this.collisionChecker.getCycleString()), null));
                        } else {
                            this.writeClass((ClassInfo)type, th);
                        }
                        XmlSchemaGenerator.this.collisionChecker.pop();
                    } else {
                        this.writeEnum((EnumLeafInfo)type, (SimpleTypeHost)th);
                    }
                }
            } else {
                th._attribute(refAttName, type.getTypeName());
            }
        }

        private void writeArray(ArrayInfo<T, C> a, Schema schema) {
            ComplexType ct = schema.complexType().name(a.getTypeName().getLocalPart());
            ct._final("#all");
            LocalElement le = ct.sequence().element().name("item");
            le.type(a.getItemType().getTypeName());
            le.minOccurs(0).maxOccurs("unbounded");
            le.nillable(true);
            ct.commit();
        }

        private void writeEnum(EnumLeafInfo<T, C> e, SimpleTypeHost th) {
            SimpleType st = th.simpleType();
            this.writeName(e, st);
            SimpleRestriction base = st.restriction();
            this.writeTypeRef((TypeHost)base, e.getBaseType(), "base");
            for (EnumConstant c : e.getConstants()) {
                base.enumeration().value(c.getLexicalValue());
            }
            st.commit();
        }

        private void writeClass(ClassInfo<T, C> c, TypeHost parent) {
            if (this.written.contains(c)) {
                return;
            }
            this.written.add(c);
            if (this.containsValueProp(c)) {
                if (c.getProperties().size() == 1) {
                    ValuePropertyInfo vp = (ValuePropertyInfo)c.getProperties().get(0);
                    SimpleType st = ((SimpleTypeHost)parent).simpleType();
                    this.writeName(c, st);
                    if (vp.isCollection()) {
                        this.writeTypeRef((TypeHost)st.list(), vp.getTarget(), "itemType");
                    } else {
                        this.writeTypeRef((TypeHost)st.restriction(), vp.getTarget(), "base");
                    }
                    return;
                }
                ComplexType ct = ((ComplexTypeHost)parent).complexType();
                this.writeName(c, ct);
                if (c.isFinal()) {
                    ct._final("extension restriction");
                }
                SimpleExtension se = ct.simpleContent().extension();
                se.block();
                block4: for (PropertyInfo p : c.getProperties()) {
                    switch (p.kind()) {
                        case ATTRIBUTE: {
                            this.handleAttributeProp((AttributePropertyInfo)p, se);
                            continue block4;
                        }
                        case VALUE: {
                            TODO.checkSpec("what if vp.isCollection() == true?");
                            ValuePropertyInfo vp = (ValuePropertyInfo)p;
                            se.base(vp.getTarget().getTypeName());
                            continue block4;
                        }
                    }
                    assert (false);
                    throw new IllegalStateException();
                }
                se.commit();
                TODO.schemaGenerator("figure out what to do if bc != null");
                TODO.checkSpec("handle sec 8.9.5.2, bullet #4");
                return;
            }
            ComplexType ct = ((ComplexTypeHost)parent).complexType();
            this.writeName(c, ct);
            if (c.isFinal()) {
                ct._final("extension restriction");
            }
            if (c.isAbstract()) {
                ct._abstract(true);
            }
            AttrDecls contentModel = ct;
            AttrDecls contentModelOwner = ct;
            ClassInfo bc = c.getBaseClass();
            if (bc != null) {
                if (bc.hasValueProperty()) {
                    SimpleExtension se = ct.simpleContent().extension();
                    contentModel = se;
                    contentModelOwner = null;
                    se.base(bc.getTypeName());
                } else {
                    ComplexExtension ce = ct.complexContent().extension();
                    contentModel = ce;
                    contentModelOwner = ce;
                    ce.base(bc.getTypeName());
                }
            }
            if (contentModelOwner != null) {
                ArrayList<Tree> children = new ArrayList<Tree>();
                for (PropertyInfo p : c.getProperties()) {
                    Tree t;
                    if (p instanceof ReferencePropertyInfo && ((ReferencePropertyInfo)p).isMixed()) {
                        ct.mixed(true);
                    }
                    if ((t = this.buildPropertyContentModel(p)) == null) continue;
                    children.add(t);
                }
                Tree top = Tree.makeGroup(c.isOrdered() ? GroupKind.SEQUENCE : GroupKind.ALL, children);
                top.write((TypeDefParticle)((Object)contentModelOwner));
            }
            for (PropertyInfo p : c.getProperties()) {
                if (!(p instanceof AttributePropertyInfo)) continue;
                this.handleAttributeProp((AttributePropertyInfo)p, contentModel);
            }
            if (c.hasAttributeWildcard()) {
                contentModel.anyAttribute().namespace("##other").processContents("skip");
            }
            ct.commit();
        }

        private void writeName(NonElement<T, C> c, TypedXmlWriter xw) {
            QName tn = c.getTypeName();
            if (tn != null) {
                xw._attribute("name", (Object)tn.getLocalPart());
            }
        }

        private boolean containsValueProp(ClassInfo<T, C> c) {
            for (PropertyInfo p : c.getProperties()) {
                if (!(p instanceof ValuePropertyInfo)) continue;
                return true;
            }
            return false;
        }

        private Tree buildPropertyContentModel(PropertyInfo<T, C> p) {
            switch (p.kind()) {
                case ELEMENT: {
                    return this.handleElementProp((ElementPropertyInfo)p);
                }
                case ATTRIBUTE: {
                    return null;
                }
                case REFERENCE: {
                    return this.handleReferenceProp((ReferencePropertyInfo)p);
                }
                case MAP: {
                    return this.handleMapProp((MapPropertyInfo)p);
                }
                case VALUE: {
                    assert (false);
                    throw new IllegalStateException();
                }
            }
            assert (false);
            throw new IllegalStateException();
        }

        private Tree handleElementProp(final ElementPropertyInfo<T, C> ep) {
            if (ep.isValueList()) {
                return new Tree.Term(){

                    @Override
                    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
                        TypeRef t = ep.getTypes().get(0);
                        LocalElement e = parent.element();
                        e.block();
                        QName tn = t.getTagName();
                        e.name(tn.getLocalPart());
                        List lst = e.simpleType().list();
                        Namespace.this.writeTypeRef(lst, t, "itemType");
                        Namespace.this.elementFormDefault.writeForm(e, tn);
                        this.writeOccurs(e, isOptional || !ep.isRequired(), repeated);
                    }
                };
            }
            ArrayList<Tree> children = new ArrayList<Tree>();
            for (final TypeRef t : ep.getTypes()) {
                children.add(new Tree.Term(){

                    @Override
                    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
                        TypeInfo parentInfo;
                        LocalElement e = parent.element();
                        QName tn = t.getTagName();
                        PropertyInfo propInfo = t.getSource();
                        TypeInfo typeInfo = parentInfo = propInfo == null ? null : propInfo.parent();
                        if (Namespace.this.canBeDirectElementRef(t, tn, parentInfo)) {
                            if (!t.getTarget().isSimpleType() && t.getTarget() instanceof ClassInfo && XmlSchemaGenerator.this.collisionChecker.findDuplicate((ClassInfo)t.getTarget())) {
                                e.ref(new QName(Namespace.this.uri, tn.getLocalPart()));
                            } else {
                                Collection refs;
                                QName elemName = null;
                                if (t.getTarget() instanceof Element) {
                                    Element te = (Element)((Object)t.getTarget());
                                    elemName = te.getElementName();
                                }
                                if ((refs = propInfo.ref()) != null && !refs.isEmpty() && elemName != null) {
                                    ClassInfoImpl cImpl = null;
                                    for (TypeInfo ref : refs) {
                                        if (ref != null && !(ref instanceof ClassInfoImpl) || !elemName.equals(((ClassInfoImpl)ref).getElementName())) continue;
                                        cImpl = (ClassInfoImpl)ref;
                                        break;
                                    }
                                    if (cImpl != null) {
                                        if (tn.getNamespaceURI() != null && tn.getNamespaceURI().trim().length() != 0) {
                                            e.ref(new QName(tn.getNamespaceURI(), tn.getLocalPart()));
                                        } else {
                                            e.ref(new QName(cImpl.getElementName().getNamespaceURI(), tn.getLocalPart()));
                                        }
                                    } else {
                                        e.ref(new QName("", tn.getLocalPart()));
                                    }
                                } else {
                                    e.ref(tn);
                                }
                            }
                        } else {
                            e.name(tn.getLocalPart());
                            Namespace.this.writeTypeRef(e, t, "type");
                            Namespace.this.elementFormDefault.writeForm(e, tn);
                        }
                        if (t.isNillable()) {
                            e.nillable(true);
                        }
                        if (t.getDefaultValue() != null) {
                            e._default(t.getDefaultValue());
                        }
                        this.writeOccurs(e, isOptional, repeated);
                    }
                });
            }
            final Tree choice = Tree.makeGroup(GroupKind.CHOICE, children).makeOptional(!ep.isRequired()).makeRepeated(ep.isCollection());
            final QName ename = ep.getXmlName();
            if (ename != null) {
                return new Tree.Term(){

                    @Override
                    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
                        LocalElement e = parent.element();
                        if (ename.getNamespaceURI().length() > 0 && !ename.getNamespaceURI().equals(Namespace.this.uri)) {
                            e.ref(new QName(ename.getNamespaceURI(), ename.getLocalPart()));
                            return;
                        }
                        e.name(ename.getLocalPart());
                        Namespace.this.elementFormDefault.writeForm(e, ename);
                        if (ep.isCollectionNillable()) {
                            e.nillable(true);
                        }
                        this.writeOccurs(e, !ep.isCollectionRequired(), repeated);
                        ComplexType p = e.complexType();
                        choice.write(p);
                    }
                };
            }
            return choice;
        }

        private boolean canBeDirectElementRef(TypeRef<T, C> t, QName tn, TypeInfo parentInfo) {
            String nsUri;
            Element te = null;
            ClassInfo ci = null;
            QName targetTagName = null;
            if (t.isNillable() || t.getDefaultValue() != null) {
                return false;
            }
            if (t.getTarget() instanceof Element) {
                te = (Element)((Object)t.getTarget());
                targetTagName = te.getElementName();
                if (te instanceof ClassInfo) {
                    ci = (ClassInfo)((Object)te);
                }
            }
            if (!((nsUri = tn.getNamespaceURI()).equals(this.uri) || nsUri.length() <= 0 || parentInfo instanceof ClassInfo && ((ClassInfo)parentInfo).getTypeName() == null)) {
                return true;
            }
            if (ci != null && targetTagName != null && te.getScope() == null && targetTagName.getNamespaceURI() == null && targetTagName.equals(tn)) {
                return true;
            }
            if (te != null) {
                return targetTagName != null && targetTagName.equals(tn);
            }
            return false;
        }

        private void handleAttributeProp(AttributePropertyInfo<T, C> ap, AttrDecls attr) {
            LocalAttribute localAttribute = attr.attribute();
            String attrURI = ap.getXmlName().getNamespaceURI();
            if (attrURI.equals("")) {
                localAttribute.name(ap.getXmlName().getLocalPart());
                this.writeAttributeTypeRef(ap, localAttribute);
                this.attributeFormDefault.writeForm(localAttribute, ap.getXmlName());
            } else {
                localAttribute.ref(ap.getXmlName());
            }
            if (ap.isRequired()) {
                localAttribute.use("required");
            }
        }

        private void writeAttributeTypeRef(AttributePropertyInfo<T, C> ap, AttributeType a) {
            if (ap.isCollection()) {
                this.writeTypeRef((TypeHost)a.simpleType().list(), ap, "itemType");
            } else {
                this.writeTypeRef((TypeHost)a, ap, "type");
            }
        }

        private Tree handleReferenceProp(final ReferencePropertyInfo<T, C> rp) {
            ArrayList<Tree> children = new ArrayList<Tree>();
            for (final Element e : rp.getElements()) {
                children.add(new Tree.Term(){

                    @Override
                    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
                        LocalElement eref = parent.element();
                        boolean local = false;
                        QName en = e.getElementName();
                        if (e.getScope() != null) {
                            boolean qualified = en.getNamespaceURI().equals(Namespace.this.uri);
                            boolean unqualified = en.getNamespaceURI().equals("");
                            if (qualified || unqualified) {
                                if (unqualified) {
                                    if (((Namespace)Namespace.this).elementFormDefault.isEffectivelyQualified) {
                                        eref.form("unqualified");
                                    }
                                } else if (!((Namespace)Namespace.this).elementFormDefault.isEffectivelyQualified) {
                                    eref.form("qualified");
                                }
                                local = true;
                                eref.name(en.getLocalPart());
                                if (e instanceof ClassInfo) {
                                    Namespace.this.writeTypeRef(eref, (ClassInfo)((Object)e), "type");
                                } else {
                                    Namespace.this.writeTypeRef(eref, ((ElementInfo)e).getContentType(), "type");
                                }
                            }
                        }
                        if (!local) {
                            eref.ref(en);
                        }
                        this.writeOccurs(eref, isOptional, repeated);
                    }
                });
            }
            final WildcardMode wc = rp.getWildcard();
            if (wc != null) {
                children.add(new Tree.Term(){

                    @Override
                    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
                        Any any = parent.any();
                        String pcmode = XmlSchemaGenerator.getProcessContentsModeName(wc);
                        if (pcmode != null) {
                            any.processContents(pcmode);
                        }
                        any.namespace("##other");
                        this.writeOccurs(any, isOptional, repeated);
                    }
                });
            }
            final Tree choice = Tree.makeGroup(GroupKind.CHOICE, children).makeRepeated(rp.isCollection()).makeOptional(!rp.isRequired());
            final QName ename = rp.getXmlName();
            if (ename != null) {
                return new Tree.Term(){

                    @Override
                    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
                        LocalElement e = parent.element().name(ename.getLocalPart());
                        Namespace.this.elementFormDefault.writeForm(e, ename);
                        if (rp.isCollectionNillable()) {
                            e.nillable(true);
                        }
                        this.writeOccurs(e, true, repeated);
                        ComplexType p = e.complexType();
                        choice.write(p);
                    }
                };
            }
            return choice;
        }

        private Tree handleMapProp(final MapPropertyInfo<T, C> mp) {
            return new Tree.Term(){

                @Override
                protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated) {
                    QName ename = mp.getXmlName();
                    LocalElement e = parent.element();
                    Namespace.this.elementFormDefault.writeForm(e, ename);
                    if (mp.isCollectionNillable()) {
                        e.nillable(true);
                    }
                    e = e.name(ename.getLocalPart());
                    this.writeOccurs(e, isOptional, repeated);
                    ComplexType p = e.complexType();
                    e = p.sequence().element();
                    e.name("entry").minOccurs(0).maxOccurs("unbounded");
                    ExplicitGroup seq = e.complexType().sequence();
                    Namespace.this.writeKeyOrValue(seq, "key", mp.getKeyType());
                    Namespace.this.writeKeyOrValue(seq, "value", mp.getValueType());
                }
            };
        }

        private void writeKeyOrValue(ExplicitGroup seq, String tagName, NonElement<T, C> typeRef) {
            LocalElement key = seq.element().name(tagName);
            key.minOccurs(0);
            this.writeTypeRef((TypeHost)key, typeRef, "type");
        }

        public void addGlobalAttribute(AttributePropertyInfo<T, C> ap) {
            this.attributeDecls.put(ap.getXmlName().getLocalPart(), ap);
            this.addDependencyTo(ap.getTarget().getTypeName());
        }

        public void addGlobalElement(TypeRef<T, C> tref) {
            this.elementDecls.put(tref.getTagName().getLocalPart(), (com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator$Namespace.ElementDeclaration)new ElementWithType(false, tref.getTarget()));
            this.addDependencyTo(tref.getTarget().getTypeName());
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("[classes=").append(this.classes);
            buf.append(",elementDecls=").append(this.elementDecls);
            buf.append(",enums=").append(this.enums);
            buf.append("]");
            return super.toString();
        }

        /*
         * Signature claims super is com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator$Namespace.ElementDeclaration, not com.sun.xml.bind.v2.schemagen.XmlSchemaGenerator$Namespace$ElementDeclaration - discarding signature.
         */
        class ElementWithType
        extends ElementDeclaration {
            private final boolean nillable;
            private final NonElement<T, C> type;

            public ElementWithType(boolean nillable, NonElement<T, C> type) {
                this.type = type;
                this.nillable = nillable;
            }

            @Override
            public void writeTo(String localName, Schema schema) {
                TopLevelElement e = schema.element().name(localName);
                if (this.nillable) {
                    e.nillable(true);
                }
                if (this.type != null) {
                    Namespace.this.writeTypeRef(e, this.type, "type");
                } else {
                    e.complexType();
                }
                e.commit();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || this.getClass() != o.getClass()) {
                    return false;
                }
                ElementWithType that = (ElementWithType)o;
                return this.type.equals(that.type);
            }

            @Override
            public int hashCode() {
                return this.type.hashCode();
            }
        }

        abstract class ElementDeclaration {
            ElementDeclaration() {
            }

            public abstract boolean equals(Object var1);

            public abstract int hashCode();

            public abstract void writeTo(String var1, Schema var2);
        }
    }
}

