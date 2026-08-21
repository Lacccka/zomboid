/*
 * Decompiled with CFR 0.152.
 */
package com.sun.jna;

import com.sun.jna.Function;
import com.sun.jna.FunctionParameterContext;
import java.lang.reflect.Method;

public class MethodParameterContext
extends FunctionParameterContext {
    private Method method;

    MethodParameterContext(Function f, Object[] args2, int index, Method m) {
        super(f, args2, index);
        this.method = m;
    }

    public Method getMethod() {
        return this.method;
    }
}

