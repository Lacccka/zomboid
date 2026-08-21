/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.vm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jspecify.annotations.Nullable;
import se.krka.kahlua.integration.expose.LuaJavaInvoker;
import se.krka.kahlua.integration.expose.MethodDebugInformation;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.Coroutine;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaTableIterator;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import se.krka.kahlua.vm.Prototype;
import zombie.Lua.LuaManager;
import zombie.UsedFromLua;
import zombie.core.BoxedStaticValues;
import zombie.core.Core;
import zombie.core.textures.Texture;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.ui.UIManager;

@UsedFromLua
public class KahluaUtil {
    private static final Object WORKER_THREAD_KEY = new Object();
    private static final String TYPE_NIL = "nil";
    private static final String TYPE_STRING = "string";
    private static final String TYPE_NUMBER = "number";
    private static final String TYPE_BOOLEAN = "boolean";
    private static final String TYPE_FUNCTION = "function";
    private static final String TYPE_TABLE = "table";
    private static final String TYPE_COROUTINE = "coroutine";
    private static final String TYPE_USERDATA = "userdata";

    public static double fromDouble(Object o) {
        return (Double)o;
    }

    public static Double toDouble(double d) {
        return BoxedStaticValues.toDouble(d);
    }

    public static Double toDouble(long d) {
        return BoxedStaticValues.toDouble(d);
    }

    public static Boolean toBoolean(boolean b) {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

    public static boolean boolEval(Object o) {
        return o != null && o != Boolean.FALSE;
    }

    public static @Nullable LuaClosure loadByteCodeFromFile(File file, KahluaTable environment) {
        LuaClosure luaClosure;
        FileInputStream stream = new FileInputStream(file);
        try {
            luaClosure = KahluaUtil.getLuaClosure(environment, stream);
        }
        catch (Throwable throwable) {
            try {
                try {
                    ((InputStream)stream).close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        ((InputStream)stream).close();
        return luaClosure;
    }

    public static @Nullable LuaClosure loadByteCodeFromResource(String name, KahluaTable environment) {
        LuaClosure luaClosure;
        block8: {
            InputStream stream = environment.getClass().getResourceAsStream(name + ".lbc");
            try {
                luaClosure = KahluaUtil.getLuaClosure(environment, stream);
                if (stream == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            stream.close();
        }
        return luaClosure;
    }

    private static @Nullable LuaClosure getLuaClosure(KahluaTable environment, InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        return Prototype.loadByteCode(stream, environment);
    }

    public static void luaAssert(boolean b, String msg) {
        if (!b) {
            KahluaUtil.fail(msg);
        }
    }

    public static void fail(String msg) {
        if (Core.debug && UIManager.defaultthread == LuaManager.thread) {
            DebugType.Lua.printStackTrace(LogSeverity.Error, -1, "Lua fail. Message: %s", msg);
            UIManager.debugBreakpoint(LuaManager.thread.currentfile, LuaManager.thread.currentLine - 1);
        }
        throw new RuntimeException(msg);
    }

    public static double round(double x) {
        if (x < 0.0) {
            return -KahluaUtil.round(-x);
        }
        double x2 = Math.floor(x += 0.5);
        if (x2 == x) {
            return x2 - (double)((long)x2 & 1L);
        }
        return x2;
    }

    public static long ipow(long base, int exponent) {
        if (exponent <= 0) {
            return 1L;
        }
        long b = 1L;
        b = (exponent & 1) != 0 ? base : 1L;
        exponent >>= 1;
        while (exponent != 0) {
            base *= base;
            if ((exponent & 1) != 0) {
                b *= base;
            }
            exponent >>= 1;
        }
        return b;
    }

    public static boolean isNegative(double vDouble) {
        return Double.doubleToLongBits(vDouble) < 0L;
    }

    public static KahluaTable getClassMetatables(Platform platform, KahluaTable env) {
        return KahluaUtil.getOrCreateTable(platform, env, "__classmetatables");
    }

    public static KahluaThread getWorkerThread(Platform platform, KahluaTable env) {
        Object workerThread = env.rawget(WORKER_THREAD_KEY);
        if (workerThread == null) {
            workerThread = new KahluaThread(platform, env);
            env.rawset(WORKER_THREAD_KEY, workerThread);
        }
        return (KahluaThread)workerThread;
    }

    public static void setWorkerThread(KahluaTable env, KahluaThread thread2) {
        env.rawset(WORKER_THREAD_KEY, (Object)thread2);
    }

    public static KahluaTable getOrCreateTable(Platform platform, KahluaTable env, String name) {
        Object t = env.rawget(name);
        if (t == null || !(t instanceof KahluaTable)) {
            t = platform.newTable();
            env.rawset(name, t);
        }
        return (KahluaTable)t;
    }

    public static void setupLibrary(KahluaTable env, KahluaThread workerThread, File library) {
        LuaClosure closure = KahluaUtil.loadByteCodeFromFile(library, env);
        if (closure == null) {
            KahluaUtil.fail("Could not load " + String.valueOf(library));
        }
        workerThread.call(closure, null, null, null);
    }

    public static void setupLibraryText(KahluaTable env, KahluaThread workerThread, File library) {
        try (FileInputStream fis = new FileInputStream(library);){
            LuaClosure closure = LuaCompiler.loadis(fis, "stdlib.lua", env);
            workerThread.call(closure, null, null, null);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String numberToString(Double num) {
        if (num.isNaN()) {
            return "nan";
        }
        if (num.isInfinite()) {
            if (KahluaUtil.isNegative(num)) {
                return "-inf";
            }
            return "inf";
        }
        double n = num;
        if (Math.floor(n) == n && Math.abs(n) < 1.0E14) {
            return String.valueOf(num.longValue());
        }
        return num.toString();
    }

    public static String type(Object o) {
        if (o == null) {
            return TYPE_NIL;
        }
        if (o instanceof String) {
            return TYPE_STRING;
        }
        if (o instanceof Double) {
            return TYPE_NUMBER;
        }
        if (o instanceof Boolean) {
            return TYPE_BOOLEAN;
        }
        if (o instanceof JavaFunction || o instanceof LuaClosure) {
            return TYPE_FUNCTION;
        }
        if (o instanceof KahluaTable) {
            return TYPE_TABLE;
        }
        if (o instanceof Coroutine) {
            return TYPE_COROUTINE;
        }
        return TYPE_USERDATA;
    }

    public static boolean isUserdata(Object o) {
        return KahluaUtil.type(o) == TYPE_USERDATA;
    }

    public static boolean isTable(Object o) {
        return KahluaUtil.type(o) == TYPE_TABLE;
    }

    public static boolean isFunction(Object o) {
        return KahluaUtil.type(o) == TYPE_FUNCTION;
    }

    public static String tostring(Object o, KahluaThread thread2) {
        if (o == null) {
            return TYPE_NIL;
        }
        if (o instanceof String) {
            String s = (String)o;
            return s;
        }
        if (o instanceof Double) {
            return KahluaUtil.rawTostring(o);
        }
        if (o instanceof Boolean) {
            return o == Boolean.TRUE ? "true" : "false";
        }
        if (o instanceof LuaClosure) {
            return "closure 0x" + System.identityHashCode(o);
        }
        if (o instanceof JavaFunction) {
            return "function 0x" + System.identityHashCode(o);
        }
        if (thread2 != null) {
            Object toStringMember;
            if (o instanceof KahluaTable && KahluaUtil.type(toStringMember = thread2.tableget(o, "tostring")) == TYPE_FUNCTION) {
                return String.valueOf(thread2.call(toStringMember, o, null, null));
            }
            Object tostringFun = thread2.getMetaOp(o, "__tostring");
            if (tostringFun != null) {
                return String.valueOf(thread2.call(tostringFun, o, null, null));
            }
            if (o instanceof KahluaTable) {
                KahluaTable table = (KahluaTable)o;
                StringBuilder toStringBuilder = new StringBuilder();
                toStringBuilder.append(KahluaUtil.tableToString(table, thread2));
                toStringBuilder.append("{ ");
                boolean anyFound = false;
                KahluaTableIterator it = table.iterator();
                while (it.advance()) {
                    Object key = it.getKey();
                    Object value = it.getValue();
                    if (!anyFound) {
                        anyFound = true;
                    } else {
                        toStringBuilder.append(", ");
                    }
                    toStringBuilder.append(KahluaUtil.isTable(key) ? KahluaUtil.tableToString(key, thread2) : KahluaUtil.tostring(key, thread2));
                    toStringBuilder.append("=");
                    toStringBuilder.append(KahluaUtil.isTable(value) ? KahluaUtil.tableToString(value, thread2) : KahluaUtil.tostring(value, thread2));
                }
                toStringBuilder.append(" }");
                return toStringBuilder.toString();
            }
        }
        return o.toString();
    }

    private static String tableToString(Object o, KahluaThread thread2) {
        Object object = thread2.tableget(o, "Type");
        if (object instanceof String) {
            String type = (String)object;
            return type + " 0x" + System.identityHashCode(o);
        }
        return KahluaUtil.type(o) + " 0x" + System.identityHashCode(o);
    }

    public static String identityHashCode(Object o) {
        return Integer.toHexString(System.identityHashCode(o));
    }

    public static Double tonumber(String s) {
        return KahluaUtil.tonumber(s, 10);
    }

    public static Double tonumber(String s, int radix) {
        if (radix < 2 || radix > 36) {
            throw new RuntimeException("base out of range");
        }
        try {
            if (radix == 10) {
                return BoxedStaticValues.toDouble(Double.parseDouble(s));
            }
            return KahluaUtil.toDouble(Integer.parseInt(s, radix));
        }
        catch (NumberFormatException e) {
            s = s.toLowerCase();
            if (s.endsWith("nan")) {
                return KahluaUtil.toDouble(Double.NaN);
            }
            if (s.endsWith("inf")) {
                if (s.charAt(0) == '-') {
                    return KahluaUtil.toDouble(Double.NEGATIVE_INFINITY);
                }
                return KahluaUtil.toDouble(Double.POSITIVE_INFINITY);
            }
            return null;
        }
    }

    public static String rawTostring(Object o) {
        if (o instanceof String) {
            String s = (String)o;
            return s;
        }
        if (o instanceof Double) {
            Double d = (Double)o;
            return KahluaUtil.numberToString(d);
        }
        return null;
    }

    public static String rawTostring2(Object o) {
        if (o instanceof String) {
            return "\"" + String.valueOf(o) + "\"";
        }
        if (o instanceof Texture) {
            Texture texture = (Texture)o;
            return "Texture: \"" + texture.getName() + "\"";
        }
        if (o instanceof Double) {
            Double d = (Double)o;
            return KahluaUtil.numberToString(d);
        }
        if (o instanceof LuaClosure) {
            LuaClosure closure = (LuaClosure)o;
            return closure.toString2(0);
        }
        if (o instanceof LuaCallFrame) {
            LuaCallFrame callFrame = (LuaCallFrame)o;
            return callFrame.toString2();
        }
        if (o instanceof LuaJavaInvoker) {
            LuaJavaInvoker invoker = (LuaJavaInvoker)o;
            if (o.toString().equals("breakpoint")) {
                return null;
            }
            MethodDebugInformation ooo = invoker.getMethodDebugData();
            Object params = "";
            for (int n = 0; n < ooo.getParameters().size(); ++n) {
                if (ooo.getParameters().get(n) == null) continue;
                params = (String)params + String.valueOf(ooo.getParameters().get(n));
            }
            return "Java: " + ooo.getReturnType() + " " + o.toString() + "(" + (String)params + ")";
        }
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    public static StackTraceElement rawToStackTraceElement(Object o) {
        if (o instanceof LuaClosure) {
            LuaClosure closure = (LuaClosure)o;
            return closure.toStackTraceElement(0);
        }
        if (o instanceof LuaCallFrame) {
            LuaCallFrame callFrame = (LuaCallFrame)o;
            return callFrame.toStackTraceElement();
        }
        if (o instanceof LuaJavaInvoker) {
            LuaJavaInvoker invoker = (LuaJavaInvoker)o;
            if (o.toString().equals("breakpoint")) {
                return null;
            }
            MethodDebugInformation ooo = invoker.getMethodDebugData();
            StringBuilder params = new StringBuilder();
            for (int n = 0; n < ooo.getParameters().size(); ++n) {
                if (ooo.getParameters().get(n) == null) continue;
                params.append(ooo.getParameters().get(n));
            }
            return new StackTraceElement("LuaJavaInvoker", ooo.getReturnType() + " " + o.toString() + "(" + String.valueOf(params) + ")", "(LuaJavaInvoker)", -1);
        }
        return null;
    }

    public static Double rawTonumber(Object o) {
        if (o instanceof Double) {
            Double d = (Double)o;
            return d;
        }
        if (o instanceof String) {
            String s = (String)o;
            return KahluaUtil.tonumber(s);
        }
        return null;
    }

    public static String getStringArg(LuaCallFrame callFrame, int n, String function) {
        Object o = KahluaUtil.getArg(callFrame, n, function);
        String res = KahluaUtil.rawTostring(o);
        if (res == null) {
            KahluaUtil.fail(n, function, TYPE_STRING, KahluaUtil.type(res));
        }
        return res;
    }

    public static String getOptionalStringArg(LuaCallFrame callFrame, int n) {
        Object o = KahluaUtil.getOptionalArg(callFrame, n);
        return KahluaUtil.rawTostring(o);
    }

    public static Double getNumberArg(LuaCallFrame callFrame, int n, String function) {
        Object o = KahluaUtil.getArg(callFrame, n, function);
        Double res = KahluaUtil.rawTonumber(o);
        if (res == null) {
            KahluaUtil.fail(n, function, "double", KahluaUtil.type(res));
        }
        return res;
    }

    public static Double getOptionalNumberArg(LuaCallFrame callFrame, int n) {
        Object o = KahluaUtil.getOptionalArg(callFrame, n);
        return KahluaUtil.rawTonumber(o);
    }

    private static void fail(int n, String function, String wantedType, String gotten) {
        throw new RuntimeException("bad argument #" + n + " to '" + function + "' (" + wantedType + " expected, got " + gotten + ")");
    }

    public static void assertArgNotNull(Object o, int n, String type, String function) {
        if (o == null) {
            KahluaUtil.fail(n, function, type, "null");
        }
    }

    public static Object getOptionalArg(LuaCallFrame callFrame, int n) {
        int index = n - 1;
        int top = callFrame.getTop();
        if (index >= top) {
            return null;
        }
        return callFrame.get(n - 1);
    }

    public static Object getArg(LuaCallFrame callFrame, int n, String function) {
        Object res = KahluaUtil.getOptionalArg(callFrame, n);
        if (res == null) {
            throw new RuntimeException("missing argument #" + n + " to '" + function + "'");
        }
        return res;
    }

    public static int len(KahluaTable kahluaTable, int low, int high) {
        while (low < high) {
            int middle = high + low + 1 >> 1;
            Object value = kahluaTable.rawget(middle);
            if (value == null) {
                high = middle - 1;
                continue;
            }
            low = middle;
        }
        while (kahluaTable.rawget(low + 1) != null) {
            ++low;
        }
        return low;
    }

    public static double getDoubleArg(LuaCallFrame callFrame, int i, String name) {
        return KahluaUtil.getNumberArg(callFrame, i, name);
    }
}

