/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration;

import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.vm.KahluaUtil;

public class LuaFail
extends LuaReturn {
    LuaFail(Object[] returnValues) {
        super(returnValues);
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Object getErrorObject() {
        if (this.returnValues.length >= 2) {
            return this.returnValues[1];
        }
        return null;
    }

    @Override
    public String getErrorString() {
        if (this.returnValues.length >= 2 && this.returnValues[1] != null) {
            return KahluaUtil.rawTostring(this.returnValues[1]);
        }
        return "";
    }

    @Override
    public String getLuaStackTrace() {
        if (this.returnValues.length >= 3 && this.returnValues[2] instanceof String) {
            return (String)this.returnValues[2];
        }
        return "";
    }

    @Override
    public RuntimeException getJavaException() {
        if (this.returnValues.length >= 4 && this.returnValues[3] instanceof RuntimeException) {
            return (RuntimeException)this.returnValues[3];
        }
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String toString() {
        return this.getErrorString() + "\n" + this.getLuaStackTrace();
    }
}

