/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration;

import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.vm.KahluaThread;

public class LuaCaller {
    private final KahluaConverterManager converterManager;

    public LuaCaller(KahluaConverterManager converterManager) {
        this.converterManager = converterManager;
    }

    public void pcallvoid(KahluaThread thread2, Object functionObject, Object arg) {
        thread2.pcallvoid(functionObject, arg);
    }

    public void pcallvoid(KahluaThread thread2, Object functionObject, Object arg, Object arg2) {
        thread2.pcallvoid(functionObject, arg, arg2);
    }

    public void pcallvoid(KahluaThread thread2, Object functionObject, Object arg, Object arg2, Object arg3) {
        thread2.pcallvoid(functionObject, arg, arg2, arg3);
    }

    public Boolean pcallBoolean(KahluaThread thread2, Object functionObject, Object arg, Object arg2) {
        return thread2.pcallBoolean(functionObject, arg, arg2);
    }

    public Boolean pcallBoolean(KahluaThread thread2, Object functionObject, Object arg, Object arg2, Object arg3) {
        return thread2.pcallBoolean(functionObject, arg, arg2, arg3);
    }

    public void pcallvoid(KahluaThread thread2, Object functionObject, Object[] args2) {
        if (args2 != null) {
            for (int i = args2.length - 1; i >= 0; --i) {
                args2[i] = this.converterManager.fromJavaToLua(args2[i]);
            }
        }
        thread2.pcallvoid(functionObject, args2);
    }

    public Object[] pcall(KahluaThread thread2, Object functionObject, Object ... args2) {
        if (args2 != null) {
            for (int i = args2.length - 1; i >= 0; --i) {
                args2[i] = this.converterManager.fromJavaToLua(args2[i]);
            }
        }
        Object[] results = thread2.pcall(functionObject, args2);
        return results;
    }

    public Object[] pcall(KahluaThread thread2, Object functionObject, Object arg) {
        if (arg != null) {
            arg = this.converterManager.fromJavaToLua(arg);
        }
        Object[] results = thread2.pcall(functionObject, new Object[]{arg});
        return results;
    }

    public Boolean protectedCallBoolean(KahluaThread thread2, Object functionObject, Object arg) {
        arg = this.converterManager.fromJavaToLua(arg);
        return thread2.pcallBoolean(functionObject, arg);
    }

    public Boolean protectedCallBoolean(KahluaThread thread2, Object functionObject, Object arg, Object arg2) {
        arg = this.converterManager.fromJavaToLua(arg);
        arg2 = this.converterManager.fromJavaToLua(arg2);
        return thread2.pcallBoolean(functionObject, arg, arg2);
    }

    public Boolean protectedCallBoolean(KahluaThread thread2, Object functionObject, Object arg, Object arg2, Object arg3) {
        arg = this.converterManager.fromJavaToLua(arg);
        arg2 = this.converterManager.fromJavaToLua(arg2);
        arg3 = this.converterManager.fromJavaToLua(arg3);
        return thread2.pcallBoolean(functionObject, arg, arg2, arg3);
    }

    public Boolean pcallBoolean(KahluaThread thread2, Object functionObject, Object[] args2) {
        if (args2 != null) {
            for (int i = args2.length - 1; i >= 0; --i) {
                args2[i] = this.converterManager.fromJavaToLua(args2[i]);
            }
        }
        return thread2.pcallBoolean(functionObject, args2);
    }

    public LuaReturn protectedCall(KahluaThread thread2, Object functionObject, Object ... args2) {
        return LuaReturn.createReturn(this.pcall(thread2, functionObject, args2));
    }

    public void protectedCallVoid(KahluaThread thread2, Object functionObject, Object arg) {
        arg = this.converterManager.fromJavaToLua(arg);
        thread2.pcallvoid(functionObject, arg);
    }

    public void protectedCallVoid(KahluaThread thread2, Object functionObject, Object arg, Object arg2) {
        arg = this.converterManager.fromJavaToLua(arg);
        arg2 = this.converterManager.fromJavaToLua(arg2);
        thread2.pcallvoid(functionObject, arg, arg2);
    }

    public void protectedCallVoid(KahluaThread thread2, Object functionObject, Object arg, Object arg2, Object arg3) {
        arg = this.converterManager.fromJavaToLua(arg);
        arg2 = this.converterManager.fromJavaToLua(arg2);
        arg3 = this.converterManager.fromJavaToLua(arg3);
        thread2.pcallvoid(functionObject, arg, arg2, arg3);
    }

    public void protectedCallVoid(KahluaThread thread2, Object functionObject, Object[] args2) {
        this.pcallvoid(thread2, functionObject, args2);
    }

    public Boolean protectedCallBoolean(KahluaThread thread2, Object functionObject, Object[] args2) {
        return this.pcallBoolean(thread2, functionObject, args2);
    }
}

