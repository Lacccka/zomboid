/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind;

import com.sun.xml.bind.InternalAccessorFactory;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AccessorFactoryImpl
implements InternalAccessorFactory {
    private static AccessorFactoryImpl instance = new AccessorFactoryImpl();

    private AccessorFactoryImpl() {
    }

    public static AccessorFactoryImpl getInstance() {
        return instance;
    }

    @Override
    public Accessor createFieldAccessor(Class bean, Field field, boolean readOnly) {
        return readOnly ? new Accessor.ReadOnlyFieldReflection(field) : new Accessor.FieldReflection(field);
    }

    @Override
    public Accessor createFieldAccessor(Class bean, Field field, boolean readOnly, boolean supressWarning) {
        return readOnly ? new Accessor.ReadOnlyFieldReflection(field, supressWarning) : new Accessor.FieldReflection(field, supressWarning);
    }

    @Override
    public Accessor createPropertyAccessor(Class bean, Method getter, Method setter) {
        if (getter == null) {
            return new Accessor.SetterOnlyReflection(setter);
        }
        if (setter == null) {
            return new Accessor.GetterOnlyReflection(getter);
        }
        return new Accessor.GetterSetterReflection(getter, setter);
    }
}

