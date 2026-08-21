/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna;

import com.sun.jna.Function;
import com.sun.jna.FunctionResultContext;
import java.lang.reflect.Method;

public class MethodResultContext
extends FunctionResultContext {
    private final Method method;

    MethodResultContext(Class<?> resultClass, Function function, Object[] args2, Method method) {
        super(resultClass, function, args2);
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }
}

