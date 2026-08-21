/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration;

import java.util.AbstractList;
import se.krka.kahlua.integration.LuaFail;
import se.krka.kahlua.integration.LuaSuccess;

public abstract class LuaReturn
extends AbstractList<Object> {
    protected final Object[] returnValues;

    protected LuaReturn(Object[] returnValues) {
        this.returnValues = returnValues;
    }

    public abstract boolean isSuccess();

    public abstract Object getErrorObject();

    public abstract String getErrorString();

    public abstract String getLuaStackTrace();

    public abstract RuntimeException getJavaException();

    @Override
    public Object getFirst() {
        return this.get(0);
    }

    public Object getSecond() {
        return this.get(1);
    }

    public Object getThird() {
        return this.get(2);
    }

    @Override
    public Object get(int index) {
        int n = this.size();
        if (index < 0 || index >= n) {
            throw new IndexOutOfBoundsException("The index " + index + " is outside the bounds [0, " + n + ")");
        }
        return this.returnValues[index + 1];
    }

    @Override
    public int size() {
        return this.returnValues.length - 1;
    }

    public static LuaReturn createReturn(Object[] returnValues) {
        Boolean success = (Boolean)returnValues[0];
        if (success.booleanValue()) {
            return new LuaSuccess(returnValues);
        }
        return new LuaFail(returnValues);
    }
}

