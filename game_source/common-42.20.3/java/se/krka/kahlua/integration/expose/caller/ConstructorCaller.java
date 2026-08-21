/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose.caller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import se.krka.kahlua.integration.expose.ReturnValues;
import se.krka.kahlua.integration.expose.caller.AbstractCaller;
import se.krka.kahlua.integration.processor.DescriptorUtil;

public class ConstructorCaller
extends AbstractCaller {
    private final Constructor<?> constructor;

    public ConstructorCaller(Constructor<?> constructor) {
        super(constructor.getParameterTypes(), constructor.isVarArgs());
        this.constructor = constructor;
        constructor.setAccessible(true);
        if (this.needsMultipleReturnValues()) {
            throw new RuntimeException("Constructor can not return multiple values");
        }
    }

    @Override
    public void call(Object self, ReturnValues rv, Object[] params) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        rv.push(this.constructor.newInstance(params));
    }

    @Override
    public boolean hasSelf() {
        return false;
    }

    @Override
    public String getDescriptor() {
        return DescriptorUtil.getDescriptor(this.constructor);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ConstructorCaller that = (ConstructorCaller)o;
        return this.constructor.equals(that.constructor);
    }

    public int hashCode() {
        return this.constructor.hashCode();
    }
}

