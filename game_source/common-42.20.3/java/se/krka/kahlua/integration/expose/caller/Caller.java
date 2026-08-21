/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose.caller;

import java.lang.reflect.InvocationTargetException;
import se.krka.kahlua.integration.expose.ReturnValues;

public interface Caller {
    public void call(Object var1, ReturnValues var2, Object[] var3) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException;

    public Class<?>[] getParameterTypes();

    public boolean needsMultipleReturnValues();

    public boolean hasSelf();

    public Class<?> getVarargType();

    public boolean hasVararg();

    public String getDescriptor();
}

