/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.core;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.nav.Navigator;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class Adapter<TypeT, ClassDeclT> {
    public final ClassDeclT adapterType;
    public final TypeT defaultType;
    public final TypeT customType;

    public Adapter(XmlJavaTypeAdapter spec, AnnotationReader<TypeT, ClassDeclT, ?, ?> reader, Navigator<TypeT, ClassDeclT, ?, ?> nav) {
        this(nav.asDecl(reader.getClassValue(spec, "value")), nav);
    }

    public Adapter(ClassDeclT adapterType, Navigator<TypeT, ClassDeclT, ?, ?> nav) {
        this.adapterType = adapterType;
        TypeT baseClass = nav.getBaseClass(nav.use(adapterType), nav.asDecl(XmlAdapter.class));
        assert (baseClass != null);
        this.defaultType = nav.isParameterizedType(baseClass) ? nav.getTypeArgument(baseClass, 0) : nav.ref(Object.class);
        this.customType = nav.isParameterizedType(baseClass) ? nav.getTypeArgument(baseClass, 1) : nav.ref(Object.class);
    }
}

