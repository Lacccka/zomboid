/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.property;

import com.sun.xml.bind.v2.runtime.property.UnmarshallerChain;
import com.sun.xml.bind.v2.runtime.unmarshaller.ChildLoader;
import com.sun.xml.bind.v2.util.QNameMap;
import javax.xml.namespace.QName;

public interface StructureLoaderBuilder {
    public static final QName TEXT_HANDLER = new QName("\u0000", "text");
    public static final QName CATCH_ALL = new QName("\u0000", "catchAll");

    public void buildChildElementUnmarshallers(UnmarshallerChain var1, QNameMap<ChildLoader> var2);
}

