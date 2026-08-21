/*
 * Decompiled with CFR 0.152.
 */
package zombie.debug;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Predicate;
import zombie.Lua.LuaManager;
import zombie.debug.LogSeverity;
import zombie.util.list.PZArrayUtil;

public class StackTraceContainer {
    private final int depthStart;
    private final int depthCount;
    private final StackTraceElement[] stackTraceElements;
    private final String indent;

    public StackTraceContainer(StackTraceElement[] stackTraceElements, String indent, int depthStart, int depthCount) {
        this.depthStart = Math.max(depthStart, 0);
        this.depthCount = depthCount;
        this.stackTraceElements = stackTraceElements;
        this.indent = indent;
    }

    public String toString() {
        return StackTraceContainer.getStackTraceString(this.stackTraceElements, this.indent, this.depthStart, this.depthCount);
    }

    public static String getStackTraceString(StackTraceElement[] stackTraceElements, String indent, int depthStart, int depthCount) {
        StringBuilder result = new StringBuilder();
        depthCount = depthCount <= 0 ? stackTraceElements.length : depthCount;
        int numElements = 0;
        for (int d = depthStart; numElements < depthCount && d < stackTraceElements.length; ++d) {
            StackTraceElement stackTraceElement = stackTraceElements[d];
            String stackTraceElementString = stackTraceElement.toString();
            if (numElements > 0 && stackTraceElementString.startsWith("zombie.core.profiling.PerformanceProbes$")) continue;
            if (numElements > 0) {
                result.append(System.lineSeparator());
            }
            result.append(indent).append(stackTraceElementString);
            ++numElements;
        }
        return result.toString();
    }

    public static StringBuilder getStackTraceString(StringBuilder result, Throwable throwable, String indent, int depthStart, int depthCount) {
        StackTraceElement[] trace = StackTraceContainer.getStackTrace(throwable);
        depthCount = depthCount <= 0 ? trace.length : depthCount;
        int numElements = 0;
        for (int d = depthStart; numElements < depthCount && d < trace.length; ++d) {
            StackTraceElement stackTraceElement = trace[d];
            String stackTraceElementString = stackTraceElement.toString();
            if (numElements > 0 && stackTraceElementString.startsWith("zombie.core.profiling.PerformanceProbes$")) continue;
            result.append(indent).append(stackTraceElementString).append(System.lineSeparator());
            ++numElements;
        }
        return result;
    }

    public static StringBuilder getStackTraceString(StringBuilder result, Throwable throwable, String caption, String prefix, int depthStart, int depthCount) {
        if (caption != null) {
            result.append(prefix).append(caption).append(System.lineSeparator());
        }
        Set<Throwable> done = Collections.newSetFromMap(new IdentityHashMap());
        done.add(throwable);
        Throwable cause = throwable.getCause();
        StackTraceElement[] trace = StackTraceContainer.getStackTrace(throwable);
        if (cause != null) {
            StackTraceContainer.getEnclosedStackTraceString(result, trace, "Caused by: ", prefix, cause, done);
        }
        StackTraceContainer.getStackTraceString(result, throwable, prefix + "\t", depthStart, depthCount);
        for (Throwable se : throwable.getSuppressed()) {
            StackTraceContainer.getEnclosedStackTraceString(result, trace, "Suppressed: ", prefix, se, done);
        }
        return result;
    }

    public static StackTraceElement[] getStackTrace(Throwable throwable) {
        try {
            StackTraceElement[] stackTraceElementsRaw = throwable.getStackTrace();
            return LuaManager.insertAnyLuaTraceElements(stackTraceElementsRaw);
        }
        catch (SecurityException secEx) {
            return new StackTraceElement[0];
        }
    }

    public static StackTraceElement[] getStackTrace() {
        try {
            StackTraceElement[] stackTraceElementsRaw = PZArrayUtil.trimLeft(Thread.currentThread().getStackTrace(), 2);
            return LuaManager.insertAnyLuaTraceElements(stackTraceElementsRaw);
        }
        catch (SecurityException secEx) {
            return new StackTraceElement[0];
        }
    }

    public static StackTraceElement[] getStackTrace(int maxDepth, Predicate<StackTraceElement> predicate) {
        try {
            StackTraceElement[] stackTraceElementsRaw = PZArrayUtil.trimLeft(Thread.currentThread().getStackTrace(), 2);
            StackTraceElement[] completeStackTraceElements = LuaManager.insertAnyLuaTraceElements(stackTraceElementsRaw);
            return PZArrayUtil.filtered(completeStackTraceElements, maxDepth, predicate);
        }
        catch (SecurityException secEx) {
            return new StackTraceElement[0];
        }
    }

    public static void getEnclosedStackTraceString(StringBuilder result, StackTraceElement[] enclosingTrace, String caption, String prefix, Throwable throwable, Set<Throwable> done) {
        if (done.contains(throwable)) {
            result.append(prefix).append(caption).append("[CIRCULAR REFERENCE: ").append(throwable).append("]");
            return;
        }
        done.add(throwable);
        Throwable cause = throwable.getCause();
        StackTraceElement[] trace = StackTraceContainer.getStackTrace(throwable);
        if (cause != null) {
            StackTraceContainer.getEnclosedStackTraceString(result, trace, "Caused by: ", prefix, cause, done);
        }
        int m = trace.length - 1;
        for (int n = enclosingTrace.length - 1; m >= 0 && n >= 0 && trace[m].equals(enclosingTrace[n]); --m, --n) {
        }
        result.append(prefix).append(caption).append(throwable).append(System.lineSeparator());
        StackTraceContainer.getStackTraceString(result, throwable, prefix + "\t", 0, m + 1);
        for (Throwable se : throwable.getSuppressed()) {
            StackTraceContainer.getEnclosedStackTraceString(result, trace, "Suppressed: ", prefix, se, done);
        }
    }

    public static class Filters {
        public static boolean all(StackTraceElement stackTraceElement) {
            return true;
        }

        public static boolean excludeNativesAndLuaCalls(StackTraceElement stackTraceElement) {
            return !(stackTraceElement.isNativeMethod() || stackTraceElement.getClassName().startsWith("jdk.internal.reflect.") || stackTraceElement.getClassName().startsWith("java.lang.reflect.") || stackTraceElement.getClassName().startsWith("se.krka.kahlua.integration.expose.") || stackTraceElement.getClassName().equals("zombie.Lua.LuaManager$GlobalObject") || stackTraceElement.getClassName().equals("zombie.Lua.LuaManager") && stackTraceElement.getMethodName().equals("RunLua") || stackTraceElement.getClassName().equals("se.krka.kahlua.vm.KahluaThread") && stackTraceElement.getMethodName().equals("callJava"));
        }

        public static Predicate<StackTraceElement> forSeverity(LogSeverity severity) {
            return severity.ordinal() >= LogSeverity.Error.ordinal() ? Filters::all : Filters::excludeNativesAndLuaCalls;
        }
    }
}

