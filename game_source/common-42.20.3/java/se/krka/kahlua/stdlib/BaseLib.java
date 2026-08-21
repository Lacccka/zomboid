/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.stdlib;

import java.util.function.Consumer;
import se.krka.kahlua.vm.Coroutine;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaException;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;

public final class BaseLib
implements JavaFunction {
    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final int PCALL = 0;
    private static final int PRINT = 1;
    private static final int SELECT = 2;
    private static final int TYPE = 3;
    private static final int TOSTRING = 4;
    private static final int TONUMBER = 5;
    private static final int GETMETATABLE = 6;
    private static final int SETMETATABLE = 7;
    private static final int ERROR = 8;
    private static final int UNPACK = 9;
    private static final int SETFENV = 10;
    private static final int GETFENV = 11;
    private static final int RAWEQUAL = 12;
    private static final int RAWSET = 13;
    private static final int RAWGET = 14;
    private static final int COLLECTGARBAGE = 15;
    private static final int DEBUGSTACKTRACE = 16;
    private static final int BYTECODELOADER = 17;
    private static final int NUM_FUNCTIONS = 18;
    private static final String[] names;
    private static final Object DOUBLE_ONE;
    private static final BaseLib[] functions;
    private final int index;
    private static Consumer<String> printCallback;

    public BaseLib(int index) {
        this.index = index;
    }

    public static void register(KahluaTable environment) {
        for (int i = 0; i < 18; ++i) {
            environment.rawset(names[i], (Object)functions[i]);
        }
    }

    public String toString() {
        return names[this.index];
    }

    @Override
    public int call(LuaCallFrame callFrame, int nArguments) {
        switch (this.index) {
            case 0: {
                return BaseLib.pcall(callFrame, nArguments);
            }
            case 1: {
                return BaseLib.print(callFrame, nArguments);
            }
            case 2: {
                return BaseLib.select(callFrame, nArguments);
            }
            case 3: {
                return BaseLib.type(callFrame, nArguments);
            }
            case 4: {
                return BaseLib.tostring(callFrame, nArguments);
            }
            case 5: {
                return BaseLib.tonumber(callFrame, nArguments);
            }
            case 6: {
                return BaseLib.getmetatable(callFrame, nArguments);
            }
            case 7: {
                return BaseLib.setmetatable(callFrame, nArguments);
            }
            case 8: {
                return this.error(callFrame, nArguments);
            }
            case 9: {
                return this.unpack(callFrame, nArguments);
            }
            case 10: {
                return this.setfenv(callFrame, nArguments);
            }
            case 11: {
                return this.getfenv(callFrame, nArguments);
            }
            case 12: {
                return this.rawequal(callFrame, nArguments);
            }
            case 13: {
                return this.rawset(callFrame, nArguments);
            }
            case 14: {
                return this.rawget(callFrame, nArguments);
            }
            case 15: {
                return BaseLib.collectgarbage(callFrame, nArguments);
            }
            case 16: {
                return this.debugstacktrace(callFrame, nArguments);
            }
            case 17: {
                return BaseLib.bytecodeloader(callFrame, nArguments);
            }
        }
        return 0;
    }

    private int debugstacktrace(LuaCallFrame callFrame, int nArguments) {
        Coroutine coroutine = (Coroutine)KahluaUtil.getOptionalArg(callFrame, 1);
        if (coroutine == null) {
            coroutine = callFrame.coroutine;
        }
        Double levelDouble = KahluaUtil.getOptionalNumberArg(callFrame, 2);
        int level = 0;
        if (levelDouble != null) {
            level = levelDouble.intValue();
        }
        Double countDouble = KahluaUtil.getOptionalNumberArg(callFrame, 3);
        int count = Integer.MAX_VALUE;
        if (countDouble != null) {
            count = countDouble.intValue();
        }
        Double haltAtDouble = KahluaUtil.getOptionalNumberArg(callFrame, 4);
        int haltAt = 0;
        if (haltAtDouble != null) {
            haltAt = haltAtDouble.intValue();
        }
        return callFrame.push(coroutine.getCurrentStackTrace(level, count, haltAt));
    }

    private int rawget(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
        KahluaTable t = (KahluaTable)callFrame.get(0);
        Object key = callFrame.get(1);
        callFrame.push(t.rawget(key));
        return 1;
    }

    private int rawset(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 3, "Not enough arguments");
        KahluaTable t = (KahluaTable)callFrame.get(0);
        Object key = callFrame.get(1);
        Object value = callFrame.get(2);
        t.rawset(key, value);
        callFrame.setTop(1);
        return 1;
    }

    private int rawequal(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
        Object o1 = callFrame.get(0);
        Object o2 = callFrame.get(1);
        callFrame.push(KahluaUtil.toBoolean(BaseLib.luaEquals(o1, o2)));
        return 1;
    }

    private int setfenv(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
        KahluaTable newEnv = (KahluaTable)callFrame.get(1);
        KahluaUtil.luaAssert(newEnv != null, "expected a table");
        LuaClosure closure = null;
        Object o = callFrame.get(0);
        if (o instanceof LuaClosure) {
            LuaClosure luaClosure;
            closure = luaClosure = (LuaClosure)o;
        } else {
            KahluaUtil.luaAssert((o = KahluaUtil.rawTonumber(o)) != null, "expected a lua function or a number");
            int level = ((Double)o).intValue();
            if (level == 0) {
                callFrame.coroutine.environment = newEnv;
                return 0;
            }
            LuaCallFrame parentCallFrame = callFrame.coroutine.getParent(level);
            if (!parentCallFrame.isLua()) {
                KahluaUtil.fail("No closure found at this level: " + level);
            }
            closure = parentCallFrame.closure;
        }
        closure.env = newEnv;
        callFrame.setTop(1);
        return 1;
    }

    private int getfenv(LuaCallFrame callFrame, int nArguments) {
        Object o = DOUBLE_ONE;
        if (nArguments >= 1) {
            o = callFrame.get(0);
        }
        KahluaTable res = null;
        if (o == null || o instanceof JavaFunction) {
            res = callFrame.coroutine.environment;
        } else if (o instanceof LuaClosure) {
            LuaClosure closure = (LuaClosure)o;
            res = closure.env;
        } else {
            Double d = KahluaUtil.rawTonumber(o);
            KahluaUtil.luaAssert(d != null, "Expected number");
            int level = d.intValue();
            KahluaUtil.luaAssert(level >= 0, "level must be non-negative");
            LuaCallFrame callFrame2 = callFrame.coroutine.getParent(level);
            res = callFrame2.getEnvironment();
        }
        callFrame.push(res);
        return 1;
    }

    private int unpack(LuaCallFrame callFrame, int nArguments) {
        int i;
        int j;
        int nReturnValues;
        KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
        KahluaTable t = (KahluaTable)callFrame.get(0);
        Object di = null;
        Object dj = null;
        if (nArguments >= 2) {
            di = callFrame.get(1);
        }
        if (nArguments >= 3) {
            dj = callFrame.get(2);
        }
        if ((nReturnValues = 1 + (j = dj != null ? (int)KahluaUtil.fromDouble(dj) : t.len()) - (i = di != null ? (int)KahluaUtil.fromDouble(di) : 1)) <= 0) {
            callFrame.setTop(0);
            return 0;
        }
        callFrame.setTop(nReturnValues);
        for (int b = 0; b < nReturnValues; ++b) {
            callFrame.set(b, t.rawget(KahluaUtil.toDouble(i + b)));
        }
        return nReturnValues;
    }

    private int error(LuaCallFrame callFrame, int nArguments) {
        if (nArguments >= 1) {
            String stacktrace = KahluaUtil.getOptionalStringArg(callFrame, 2);
            if (stacktrace == null) {
                stacktrace = "";
            }
            callFrame.coroutine.stackTrace = stacktrace;
            throw new KahluaException(callFrame.get(0));
        }
        return 0;
    }

    public static int pcall(LuaCallFrame callFrame, int nArguments) {
        return callFrame.getThread().pcall(nArguments - 1);
    }

    private static int print(LuaCallFrame callFrame, int nArguments) {
        KahluaThread thread2 = callFrame.getThread();
        KahluaTable env = thread2.getEnvironment();
        Object toStringFun = thread2.tableget(env, "tostring");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nArguments; ++i) {
            if (i > 0) {
                sb.append("\t");
            }
            Object res = thread2.call(toStringFun, callFrame.get(i), null, null);
            sb.append(res);
        }
        String str = sb.toString();
        thread2.getOut().println(str);
        if (printCallback != null) {
            printCallback.accept(str);
        }
        return 0;
    }

    public static void setPrintCallback(Consumer<String> callback) {
        printCallback = callback;
    }

    private static int select(LuaCallFrame callFrame, int nArguments) {
        String s;
        KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
        Object arg1 = callFrame.get(0);
        if (arg1 instanceof String && (s = (String)arg1).startsWith("#")) {
            callFrame.push(KahluaUtil.toDouble(nArguments - 1));
            return 1;
        }
        Double d_indexDouble = KahluaUtil.rawTonumber(arg1);
        double d_index = KahluaUtil.fromDouble(d_indexDouble);
        int index = (int)d_index;
        if (index >= 1 && index <= nArguments - 1) {
            int nResults = nArguments - index;
            return nResults;
        }
        return 0;
    }

    private static int getmetatable(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
        Object o = callFrame.get(0);
        Object metatable = callFrame.getThread().getmetatable(o, false);
        callFrame.push(metatable);
        return 1;
    }

    private static int setmetatable(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 2, "Not enough arguments");
        Object o = callFrame.get(0);
        KahluaTable newMeta = (KahluaTable)callFrame.get(1);
        BaseLib.setmetatable(callFrame.getThread(), o, newMeta, false);
        callFrame.setTop(1);
        return 1;
    }

    public static void setmetatable(KahluaThread thread2, Object o, KahluaTable newMeta, boolean raw) {
        KahluaUtil.luaAssert(o != null, "Expected table, got nil");
        Object oldMeta = thread2.getmetatable(o, true);
        if (!raw && oldMeta != null && thread2.tableget(oldMeta, "__metatable") != null) {
            throw new RuntimeException("cannot change a protected metatable");
        }
        thread2.setmetatable(o, newMeta);
    }

    private static int type(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
        Object o = callFrame.get(0);
        callFrame.push(KahluaUtil.type(o));
        return 1;
    }

    private static int tostring(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
        Object o = callFrame.get(0);
        String res = KahluaUtil.tostring(o, callFrame.getThread());
        callFrame.push(res);
        return 1;
    }

    private static int tonumber(LuaCallFrame callFrame, int nArguments) {
        KahluaUtil.luaAssert(nArguments >= 1, "Not enough arguments");
        Object o = callFrame.get(0);
        if (nArguments == 1) {
            callFrame.push(KahluaUtil.rawTonumber(o));
            return 1;
        }
        String s = (String)o;
        Object radixObj = callFrame.get(1);
        Double radixDouble = KahluaUtil.rawTonumber(radixObj);
        if (radixDouble == null) {
            callFrame.push(null);
            return 1;
        }
        KahluaUtil.luaAssert(radixDouble != null, "Argument 2 must be a number");
        double dradix = KahluaUtil.fromDouble(radixDouble);
        int radix = (int)dradix;
        if ((double)radix != dradix) {
            callFrame.push(null);
            return 1;
        }
        if ((double)radix != dradix) {
            throw new RuntimeException("base is not an integer");
        }
        Double res = KahluaUtil.tonumber(s, radix);
        callFrame.push(res);
        return 1;
    }

    public static int collectgarbage(LuaCallFrame callFrame, int nArguments) {
        Object option = null;
        if (nArguments > 0) {
            option = callFrame.get(0);
        }
        if (option == null || option.equals("step") || option.equals("collect")) {
            System.gc();
            return 0;
        }
        if (option.equals("count")) {
            long freeMemory = RUNTIME.freeMemory();
            long totalMemory = RUNTIME.totalMemory();
            callFrame.setTop(3);
            callFrame.set(0, BaseLib.toKiloBytes(totalMemory - freeMemory));
            callFrame.set(1, BaseLib.toKiloBytes(freeMemory));
            callFrame.set(2, BaseLib.toKiloBytes(totalMemory));
            return 3;
        }
        throw new RuntimeException("invalid option: " + String.valueOf(option));
    }

    private static Double toKiloBytes(long freeMemory) {
        return KahluaUtil.toDouble((double)freeMemory / 1024.0);
    }

    private static int bytecodeloader(LuaCallFrame callFrame, int nArguments) {
        String modname = KahluaUtil.getStringArg(callFrame, 1, "loader");
        KahluaTable packageTable = (KahluaTable)callFrame.getEnvironment().rawget("package");
        String classpath = (String)packageTable.rawget("classpath");
        int index = 0;
        while (index < classpath.length()) {
            Object path;
            int nextIndex = classpath.indexOf(";", index);
            if (nextIndex == -1) {
                nextIndex = classpath.length();
            }
            if (!((String)(path = classpath.substring(index, nextIndex))).isEmpty()) {
                LuaClosure closure;
                if (!((String)path).endsWith("/")) {
                    path = (String)path + "/";
                }
                if ((closure = KahluaUtil.loadByteCodeFromResource((String)path + modname, callFrame.getEnvironment())) != null) {
                    return callFrame.push(closure);
                }
            }
            index = nextIndex;
        }
        return callFrame.push("Could not find the bytecode for '" + modname + "' in classpath");
    }

    public static boolean luaEquals(Object a, Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a instanceof Double) {
            Double ad = (Double)a;
            if (b instanceof Double) {
                Double bd = (Double)b;
                return ad.doubleValue() == bd.doubleValue();
            }
        }
        return a == b;
    }

    static {
        DOUBLE_ONE = 1.0;
        names = new String[18];
        BaseLib.names[0] = "pcall";
        BaseLib.names[1] = "print";
        BaseLib.names[2] = "select";
        BaseLib.names[3] = "type";
        BaseLib.names[4] = "tostring";
        BaseLib.names[5] = "tonumber";
        BaseLib.names[6] = "getmetatable";
        BaseLib.names[7] = "setmetatable";
        BaseLib.names[8] = "error";
        BaseLib.names[9] = "unpack";
        BaseLib.names[10] = "setfenv";
        BaseLib.names[11] = "getfenv";
        BaseLib.names[12] = "rawequal";
        BaseLib.names[13] = "rawset";
        BaseLib.names[14] = "rawget";
        BaseLib.names[15] = "collectgarbage";
        BaseLib.names[16] = "debugstacktrace";
        BaseLib.names[17] = "bytecodeloader";
        functions = new BaseLib[18];
        for (int i = 0; i < 18; ++i) {
            BaseLib.functions[i] = new BaseLib(i);
        }
    }
}

