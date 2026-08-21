/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.Util;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.v2.Messages;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.util.TypeCast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class ContextFactory {
    public static final String USE_JAXB_PROPERTIES = "_useJAXBProperties";

    public static JAXBContext createContext(Class[] classes, Map<String, Object> properties) throws JAXBException {
        Map<Class, Class> subclassReplacements;
        Boolean xmlAccessorFactorySupport;
        Boolean improvedXsiTypeHandling;
        Boolean supressAccessorWarnings;
        Boolean retainPropertyInfo;
        Boolean allNillable;
        Boolean disablesecurityProcessing;
        properties = properties == null ? Collections.emptyMap() : new HashMap<String, Object>(properties);
        String defaultNsUri = ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.defaultNamespaceRemap", String.class);
        Boolean c14nSupport = ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.c14n", Boolean.class);
        if (c14nSupport == null) {
            c14nSupport = false;
        }
        if ((disablesecurityProcessing = ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.disableXmlSecurity", Boolean.class)) == null) {
            disablesecurityProcessing = false;
        }
        if ((allNillable = ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.treatEverythingNillable", Boolean.class)) == null) {
            allNillable = false;
        }
        if ((retainPropertyInfo = ContextFactory.getPropertyValue(properties, "retainReferenceToInfo", Boolean.class)) == null) {
            retainPropertyInfo = false;
        }
        if ((supressAccessorWarnings = ContextFactory.getPropertyValue(properties, "supressAccessorWarnings", Boolean.class)) == null) {
            supressAccessorWarnings = false;
        }
        if ((improvedXsiTypeHandling = ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.improvedXsiTypeHandling", Boolean.class)) == null) {
            String improvedXsiSystemProperty = Util.getSystemProperty("com.sun.xml.bind.improvedXsiTypeHandling");
            improvedXsiTypeHandling = improvedXsiSystemProperty == null ? Boolean.valueOf(true) : Boolean.valueOf(improvedXsiSystemProperty);
        }
        if ((xmlAccessorFactorySupport = ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.XmlAccessorFactory", Boolean.class)) == null) {
            xmlAccessorFactorySupport = false;
            Util.getClassLogger().log(Level.FINE, "Property com.sun.xml.bind.XmlAccessorFactoryis not active.  Using JAXB's implementation");
        }
        Boolean backupWithParentNamespace = ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.backupWithParentNamespace", Boolean.class);
        RuntimeAnnotationReader ar = ContextFactory.getPropertyValue(properties, JAXBRIContext.ANNOTATION_READER, RuntimeAnnotationReader.class);
        List<TypeReference> tr = ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.typeReferences", Collection.class);
        if (tr == null) {
            tr = Collections.emptyList();
        }
        try {
            subclassReplacements = TypeCast.checkedCast(ContextFactory.getPropertyValue(properties, "com.sun.xml.bind.subclassReplacements", Map.class), Class.class, Class.class);
        }
        catch (ClassCastException e) {
            throw new JAXBException(Messages.INVALID_TYPE_IN_MAP.format(new Object[0]), e);
        }
        if (!properties.isEmpty()) {
            throw new JAXBException(Messages.UNSUPPORTED_PROPERTY.format(properties.keySet().iterator().next()));
        }
        JAXBContextImpl.JAXBContextBuilder builder = new JAXBContextImpl.JAXBContextBuilder();
        builder.setClasses(classes);
        builder.setTypeRefs(tr);
        builder.setSubclassReplacements(subclassReplacements);
        builder.setDefaultNsUri(defaultNsUri);
        builder.setC14NSupport(c14nSupport);
        builder.setAnnotationReader(ar);
        builder.setXmlAccessorFactorySupport(xmlAccessorFactorySupport);
        builder.setAllNillable(allNillable);
        builder.setRetainPropertyInfo(retainPropertyInfo);
        builder.setSupressAccessorWarnings(supressAccessorWarnings);
        builder.setImprovedXsiTypeHandling(improvedXsiTypeHandling);
        builder.setDisableSecurityProcessing(disablesecurityProcessing);
        builder.setBackupWithParentNamespace(backupWithParentNamespace);
        return builder.build();
    }

    private static <T> T getPropertyValue(Map<String, Object> properties, String keyName, Class<T> type) throws JAXBException {
        Object o = properties.get(keyName);
        if (o == null) {
            return null;
        }
        properties.remove(keyName);
        if (!type.isInstance(o)) {
            throw new JAXBException(Messages.INVALID_PROPERTY_VALUE.format(keyName, o));
        }
        return type.cast(o);
    }

    @Deprecated
    public static JAXBRIContext createContext(Class[] classes, Collection<TypeReference> typeRefs, Map<Class, Class> subclassReplacements, String defaultNsUri, boolean c14nSupport, RuntimeAnnotationReader ar, boolean xmlAccessorFactorySupport, boolean allNillable, boolean retainPropertyInfo) throws JAXBException {
        return ContextFactory.createContext(classes, typeRefs, subclassReplacements, defaultNsUri, c14nSupport, ar, xmlAccessorFactorySupport, allNillable, retainPropertyInfo, false);
    }

    @Deprecated
    public static JAXBRIContext createContext(Class[] classes, Collection<TypeReference> typeRefs, Map<Class, Class> subclassReplacements, String defaultNsUri, boolean c14nSupport, RuntimeAnnotationReader ar, boolean xmlAccessorFactorySupport, boolean allNillable, boolean retainPropertyInfo, boolean improvedXsiTypeHandling) throws JAXBException {
        JAXBContextImpl.JAXBContextBuilder builder = new JAXBContextImpl.JAXBContextBuilder();
        builder.setClasses(classes);
        builder.setTypeRefs(typeRefs);
        builder.setSubclassReplacements(subclassReplacements);
        builder.setDefaultNsUri(defaultNsUri);
        builder.setC14NSupport(c14nSupport);
        builder.setAnnotationReader(ar);
        builder.setXmlAccessorFactorySupport(xmlAccessorFactorySupport);
        builder.setAllNillable(allNillable);
        builder.setRetainPropertyInfo(retainPropertyInfo);
        builder.setImprovedXsiTypeHandling(improvedXsiTypeHandling);
        return builder.build();
    }

    public static JAXBContext createContext(String contextPath, ClassLoader classLoader, Map<String, Object> properties) throws JAXBException {
        FinalArrayList<Class> classes = new FinalArrayList<Class>();
        StringTokenizer tokens = new StringTokenizer(contextPath, ":");
        while (tokens.hasMoreTokens()) {
            List<Class> indexedClasses;
            boolean foundJaxbIndex = false;
            boolean foundObjectFactory = false;
            String pkg = tokens.nextToken();
            try {
                Class<?> o = classLoader.loadClass(pkg + ".ObjectFactory");
                classes.add(o);
                foundObjectFactory = true;
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
            try {
                indexedClasses = ContextFactory.loadIndexedClasses(pkg, classLoader);
            }
            catch (IOException e) {
                throw new JAXBException(e);
            }
            if (indexedClasses != null) {
                classes.addAll(indexedClasses);
                foundJaxbIndex = true;
            }
            if (foundObjectFactory || foundJaxbIndex) continue;
            throw new JAXBException(Messages.BROKEN_CONTEXTPATH.format(pkg));
        }
        return ContextFactory.createContext(classes.toArray(new Class[classes.size()]), properties);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static List<Class> loadIndexedClasses(String pkg, ClassLoader classLoader) throws IOException, JAXBException {
        String resource = pkg.replace('.', '/') + "/jaxb.index";
        InputStream resourceAsStream = classLoader.getResourceAsStream(resource);
        if (resourceAsStream == null) {
            return null;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"));){
            FinalArrayList<Class> classes = new FinalArrayList<Class>();
            String className = in.readLine();
            while (className != null) {
                if ((className = className.trim()).startsWith("#") || className.length() == 0) {
                    className = in.readLine();
                    continue;
                }
                if (className.endsWith(".class")) {
                    throw new JAXBException(Messages.ILLEGAL_ENTRY.format(className));
                }
                try {
                    classes.add(classLoader.loadClass(pkg + '.' + className));
                }
                catch (ClassNotFoundException e) {
                    throw new JAXBException(Messages.ERROR_LOADING_CLASS.format(className, resource), e);
                }
                className = in.readLine();
            }
            FinalArrayList<Class> finalArrayList = classes;
            return finalArrayList;
        }
    }
}

