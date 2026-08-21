/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeClassInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeElement;
import com.sun.xml.bind.v2.model.runtime.RuntimeElementPropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeNonElement;
import java.lang.reflect.Type;
import javax.xml.bind.JAXBElement;

public interface RuntimeElementInfo
extends ElementInfo<Type, Class>,
RuntimeElement {
    public RuntimeClassInfo getScope();

    public RuntimeElementPropertyInfo getProperty();

    @Override
    public Class<? extends JAXBElement> getType();

    public RuntimeNonElement getContentType();
}

