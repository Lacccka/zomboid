/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.test;

import java.util.Vector;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.Platform;

public class UserdataArray
implements JavaFunction {
    private static final int LENGTH = 0;
    private static final int INDEX = 1;
    private static final int NEWINDEX = 2;
    private static final int NEW = 3;
    private static final int PUSH = 4;
    private static final Class<?> VECTOR_CLASS = new Vector().getClass();
    private static KahluaTable metatable;
    private final int index;

    public static synchronized void register(Platform platform, KahluaTable env) {
        if (metatable == null) {
            metatable = platform.newTable();
            metatable.rawset("__metatable", (Object)"restricted");
            metatable.rawset("__len", (Object)new UserdataArray(0));
            metatable.rawset("__index", (Object)new UserdataArray(1));
            metatable.rawset("__newindex", (Object)new UserdataArray(2));
            metatable.rawset("new", (Object)new UserdataArray(3));
            metatable.rawset("push", (Object)new UserdataArray(4));
        }
        KahluaTable metatables = KahluaUtil.getClassMetatables(platform, env);
        metatables.rawset(VECTOR_CLASS, (Object)metatable);
        env.rawset("array", (Object)metatable);
    }

    private UserdataArray(int index) {
        this.index = index;
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        switch (this.index) {
            case 0: {
                return this.length(callFrame, nArguments);
            }
            case 1: {
                return this.index(callFrame, nArguments);
            }
            case 2: {
                return this.newindex(callFrame, nArguments);
            }
            case 3: {
                return this.newVector(callFrame, nArguments);
            }
            case 4: {
                return this.push(callFrame, nArguments);
            }
        }
        return 0;
    }

    private int push(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 2, "not enough parameters");
        Vector v = (Vector)callFrame.get(0);
        Object value = callFrame.get(1);
        v.addElement(value);
        callFrame.push(v);
        return 1;
    }

    private int newVector(LuaCallFrame callFrame, int nArguments) {
        callFrame.push(new Vector());
        return 1;
    }

    private int newindex(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 3, "not enough parameters");
        Vector v = (Vector)callFrame.get(0);
        Object key = callFrame.get(1);
        Object value = callFrame.get(2);
        v.setElementAt(value, (int)KahluaUtil.fromDouble(key));
        return 0;
    }

    private int index(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 2, "not enough parameters");
        Object obj = callFrame.get(0);
        if (obj == null || !(obj instanceof Vector)) {
            return 0;
        }
        Vector v = (Vector)obj;
        Object key = callFrame.get(1);
        Object res = key instanceof Double ? v.elementAt((int)KahluaUtil.fromDouble(key)) : metatable.rawget(key);
        callFrame.push(res);
        return 1;
    }

    private int length(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "not enough parameters");
        Vector v = (Vector)callFrame.get(0);
        double size = v.size();
        callFrame.push(KahluaUtil.toDouble(size));
        return 1;
    }
}

