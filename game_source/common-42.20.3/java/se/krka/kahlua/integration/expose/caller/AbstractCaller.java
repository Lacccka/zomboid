/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose.caller;

import se.krka.kahlua.integration.expose.ReturnValues;
import se.krka.kahlua.integration.expose.caller.Caller;

public abstract class AbstractCaller
implements Caller {
    protected final Class<?>[] parameters;
    protected final boolean needsMultipleReturnValues;
    protected final Class<?> varargType;

    protected AbstractCaller(Class<?>[] parameters, boolean isVarArgs) {
        Class<?> firstType;
        boolean needsMultipleReturnValues = false;
        if (parameters.length > 0 && (firstType = parameters[0]) == ReturnValues.class) {
            needsMultipleReturnValues = true;
        }
        if (isVarArgs) {
            Class<?> lastType = parameters[parameters.length - 1];
            this.varargType = lastType.getComponentType();
        } else {
            this.varargType = null;
        }
        this.needsMultipleReturnValues = needsMultipleReturnValues;
        int from = needsMultipleReturnValues ? 1 : 0;
        int to = parameters.length - (this.varargType == null ? 0 : 1);
        int len = to - from;
        this.parameters = new Class[len];
        System.arraycopy(parameters, from, this.parameters, 0, len);
    }

    @Override
    public final Class<?>[] getParameterTypes() {
        return this.parameters;
    }

    @Override
    public final Class<?> getVarargType() {
        return this.varargType;
    }

    @Override
    public final boolean hasVararg() {
        return this.varargType != null;
    }

    @Override
    public final boolean needsMultipleReturnValues() {
        return this.needsMultipleReturnValues;
    }
}

