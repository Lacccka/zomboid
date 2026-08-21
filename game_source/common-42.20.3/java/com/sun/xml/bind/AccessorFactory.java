/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.xml.bind.JAXBException;

public interface AccessorFactory {
    public Accessor createFieldAccessor(Class var1, Field var2, boolean var3) throws JAXBException;

    public Accessor createPropertyAccessor(Class var1, Method var2, Method var3) throws JAXBException;
}

