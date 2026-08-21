/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.util.Which;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.annotation.ClassLocatable;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ErrorHandler;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.Ref;
import com.sun.xml.bind.v2.model.core.RegistryInfo;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.core.TypeInfoSet;
import com.sun.xml.bind.v2.model.impl.ArrayInfoImpl;
import com.sun.xml.bind.v2.model.impl.BuiltinLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.ClassInfoImpl;
import com.sun.xml.bind.v2.model.impl.ElementInfoImpl;
import com.sun.xml.bind.v2.model.impl.EnumLeafInfoImpl;
import com.sun.xml.bind.v2.model.impl.Messages;
import com.sun.xml.bind.v2.model.impl.ModelBuilderI;
import com.sun.xml.bind.v2.model.impl.RegistryInfoImpl;
import com.sun.xml.bind.v2.model.impl.SecureLoader;
import com.sun.xml.bind.v2.model.impl.TypeInfoSetImpl;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

public class ModelBuilder<T, C, F, M>
implements ModelBuilderI<T, C, F, M> {
    private static final Logger logger;
    final TypeInfoSetImpl<T, C, F, M> typeInfoSet;
    public final AnnotationReader<T, C, F, M> reader;
    public final Navigator<T, C, F, M> nav;
    private final Map<QName, TypeInfo> typeNames = new HashMap<QName, TypeInfo>();
    public final String defaultNsUri;
    final Map<String, RegistryInfoImpl<T, C, F, M>> registries = new HashMap<String, RegistryInfoImpl<T, C, F, M>>();
    private final Map<C, C> subclassReplacements;
    private ErrorHandler errorHandler;
    private boolean hadError;
    public boolean hasSwaRef;
    private final ErrorHandler proxyErrorHandler = new ErrorHandler(){

        @Override
        public void error(IllegalAnnotationException e) {
            ModelBuilder.this.reportError(e);
        }
    };
    private boolean linked;

    public ModelBuilder(AnnotationReader<T, C, F, M> reader, Navigator<T, C, F, M> navigator, Map<C, C> subclassReplacements, String defaultNamespaceRemap) {
        this.reader = reader;
        this.nav = navigator;
        this.subclassReplacements = subclassReplacements;
        if (defaultNamespaceRemap == null) {
            defaultNamespaceRemap = "";
        }
        this.defaultNsUri = defaultNamespaceRemap;
        reader.setErrorHandler(this.proxyErrorHandler);
        this.typeInfoSet = this.createTypeInfoSet();
    }

    protected TypeInfoSetImpl<T, C, F, M> createTypeInfoSet() {
        return new TypeInfoSetImpl<T, C, F, M>(this.nav, this.reader, BuiltinLeafInfoImpl.createLeaves(this.nav));
    }

    public NonElement<T, C> getClassInfo(C clazz, Locatable upstream) {
        return this.getClassInfo(clazz, false, upstream);
    }

    public NonElement<T, C> getClassInfo(C clazz, boolean searchForSuperClass, Locatable upstream) {
        assert (clazz != null);
        NonElement<T, C> r = this.typeInfoSet.getClassInfo(clazz);
        if (r != null) {
            return r;
        }
        if (this.nav.isEnum(clazz)) {
            EnumLeafInfoImpl<T, C, F, M> li = this.createEnumLeafInfo(clazz, upstream);
            this.typeInfoSet.add(li);
            r = li;
            this.addTypeName(r);
        } else {
            boolean isReplaced = this.subclassReplacements.containsKey(clazz);
            if (isReplaced && !searchForSuperClass) {
                r = this.getClassInfo(this.subclassReplacements.get(clazz), upstream);
            } else if (this.reader.hasClassAnnotation(clazz, XmlTransient.class) || isReplaced) {
                r = this.getClassInfo(this.nav.getSuperClass(clazz), searchForSuperClass, new ClassLocatable<C>(upstream, clazz, this.nav));
            } else {
                ClassInfoImpl<T, C, F, M> ci = this.createClassInfo(clazz, upstream);
                this.typeInfoSet.add(ci);
                for (PropertyInfo<T, C> p : ci.getProperties()) {
                    if (p.kind() == PropertyKind.REFERENCE) {
                        this.addToRegistry(clazz, (Locatable)((Object)p));
                        Class[] prmzdClasses = this.getParametrizedTypes(p);
                        if (prmzdClasses != null) {
                            for (Class prmzdClass : prmzdClasses) {
                                if (prmzdClass == clazz) continue;
                                this.addToRegistry(prmzdClass, (Locatable)((Object)p));
                            }
                        }
                    }
                    for (TypeInfo typeInfo : p.ref()) {
                    }
                }
                ci.getBaseClass();
                r = ci;
                this.addTypeName(r);
            }
        }
        XmlSeeAlso sa = this.reader.getClassAnnotation(XmlSeeAlso.class, clazz, upstream);
        if (sa != null) {
            for (T t : this.reader.getClassArrayValue(sa, "value")) {
                this.getTypeInfo(t, (Locatable)((Object)sa));
            }
        }
        return r;
    }

    private void addToRegistry(C clazz, Locatable p) {
        C c;
        String pkg = this.nav.getPackageName(clazz);
        if (!this.registries.containsKey(pkg) && (c = this.nav.loadObjectFactory(clazz, pkg)) != null) {
            this.addRegistry(c, p);
        }
    }

    private Class[] getParametrizedTypes(PropertyInfo p) {
        try {
            ParameterizedType prmzdType;
            Type pType = ((RuntimePropertyInfo)p).getIndividualType();
            if (pType instanceof ParameterizedType && (prmzdType = (ParameterizedType)pType).getRawType() == JAXBElement.class) {
                Type[] actualTypes = prmzdType.getActualTypeArguments();
                Class[] result = new Class[actualTypes.length];
                for (int i = 0; i < actualTypes.length; ++i) {
                    result[i] = (Class)actualTypes[i];
                }
                return result;
            }
        }
        catch (Exception e) {
            logger.log(Level.FINE, "Error in ModelBuilder.getParametrizedTypes. " + e.getMessage());
        }
        return null;
    }

    private void addTypeName(NonElement<T, C> r) {
        QName t = r.getTypeName();
        if (t == null) {
            return;
        }
        TypeInfo old = this.typeNames.put(t, r);
        if (old != null) {
            this.reportError(new IllegalAnnotationException(Messages.CONFLICTING_XML_TYPE_MAPPING.format(r.getTypeName()), old, r));
        }
    }

    public NonElement<T, C> getTypeInfo(T t, Locatable upstream) {
        NonElement<T, C> r = this.typeInfoSet.getTypeInfo(t);
        if (r != null) {
            return r;
        }
        if (this.nav.isArray(t)) {
            ArrayInfoImpl<T, C, F, M> ai = this.createArrayInfo(upstream, t);
            this.addTypeName(ai);
            this.typeInfoSet.add(ai);
            return ai;
        }
        C c = this.nav.asDecl(t);
        assert (c != null) : t.toString() + " must be a leaf, but we failed to recognize it.";
        return this.getClassInfo(c, upstream);
    }

    public NonElement<T, C> getTypeInfo(Ref<T, C> ref) {
        assert (!ref.valueList);
        C c = this.nav.asDecl(ref.type);
        if (c != null && this.reader.getClassAnnotation(XmlRegistry.class, c, null) != null) {
            if (!this.registries.containsKey(this.nav.getPackageName(c))) {
                this.addRegistry(c, null);
            }
            return null;
        }
        return this.getTypeInfo(ref.type, null);
    }

    protected EnumLeafInfoImpl<T, C, F, M> createEnumLeafInfo(C clazz, Locatable upstream) {
        return new EnumLeafInfoImpl(this, upstream, clazz, this.nav.use(clazz));
    }

    protected ClassInfoImpl<T, C, F, M> createClassInfo(C clazz, Locatable upstream) {
        return new ClassInfoImpl(this, upstream, clazz);
    }

    protected ElementInfoImpl<T, C, F, M> createElementInfo(RegistryInfoImpl<T, C, F, M> registryInfo, M m) throws IllegalAnnotationException {
        return new ElementInfoImpl<T, C, F, M>(this, registryInfo, m);
    }

    protected ArrayInfoImpl<T, C, F, M> createArrayInfo(Locatable upstream, T arrayType) {
        return new ArrayInfoImpl(this, upstream, arrayType);
    }

    public RegistryInfo<T, C> addRegistry(C registryClass, Locatable upstream) {
        return new RegistryInfoImpl(this, upstream, registryClass);
    }

    public RegistryInfo<T, C> getRegistry(String packageName) {
        return this.registries.get(packageName);
    }

    public TypeInfoSet<T, C, F, M> link() {
        assert (!this.linked);
        this.linked = true;
        for (ElementInfoImpl<T, C, F, M> elementInfoImpl : this.typeInfoSet.getAllElements()) {
            elementInfoImpl.link();
        }
        for (ClassInfoImpl classInfoImpl : this.typeInfoSet.beans().values()) {
            classInfoImpl.link();
        }
        for (EnumLeafInfoImpl enumLeafInfoImpl : this.typeInfoSet.enums().values()) {
            enumLeafInfoImpl.link();
        }
        if (this.hadError) {
            return null;
        }
        return this.typeInfoSet;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public final void reportError(IllegalAnnotationException e) {
        this.hadError = true;
        if (this.errorHandler != null) {
            this.errorHandler.error(e);
        }
    }

    public boolean isReplaced(C sc) {
        return this.subclassReplacements.containsKey(sc);
    }

    @Override
    public Navigator<T, C, F, M> getNavigator() {
        return this.nav;
    }

    @Override
    public AnnotationReader<T, C, F, M> getReader() {
        return this.reader;
    }

    static {
        try {
            XmlSchema s = null;
            s.location();
        }
        catch (NullPointerException s) {
        }
        catch (NoSuchMethodError e) {
            Messages res = SecureLoader.getClassClassLoader(XmlSchema.class) == null ? Messages.INCOMPATIBLE_API_VERSION_MUSTANG : Messages.INCOMPATIBLE_API_VERSION;
            throw new LinkageError(res.format(Which.which(XmlSchema.class), Which.which(ModelBuilder.class)));
        }
        try {
            WhiteSpaceProcessor.isWhiteSpace("xyz");
        }
        catch (NoSuchMethodError e) {
            throw new LinkageError(Messages.RUNNING_WITH_1_0_RUNTIME.format(Which.which(WhiteSpaceProcessor.class), Which.which(ModelBuilder.class)));
        }
        logger = Logger.getLogger(ModelBuilder.class.getName());
    }
}

