/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind;

import com.sun.xml.bind.AccessorFactory;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import javax.xml.bind.JAXBException;

public interface InternalAccessorFactory
extends AccessorFactory {
    public Accessor createFieldAccessor(Class var1, Field var2, boolean var3, boolean var4) throws JAXBException;
}

