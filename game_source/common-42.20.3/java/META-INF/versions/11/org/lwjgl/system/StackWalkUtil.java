/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system;

import java.util.Arrays;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.APIUtil;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
final class StackWalkUtil {
    private static final StackWalker STACKWALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private StackWalkUtil() {
    }

    static StackTraceElement[] stackWalkArray(Object[] a) {
        return (StackTraceElement[])Arrays.stream((StackWalker.StackFrame[])a).map(StackWalker.StackFrame::toStackTraceElement).toArray(StackTraceElement[]::new);
    }

    static Object stackWalkGetMethod(Class<?> after) {
        return STACKWALKER.walk(s -> {
            StackWalker.StackFrame frame;
            Iterator iter = s.iterator();
            iter.next();
            iter.next();
            while ((frame = (StackWalker.StackFrame)iter.next()).getDeclaringClass() == after && iter.hasNext()) {
            }
            return frame;
        });
    }

    private static boolean isSameMethod(StackWalker.StackFrame a, StackWalker.StackFrame b) {
        return StackWalkUtil.isSameMethod(a, b, b.getMethodName());
    }

    private static boolean isSameMethod(StackWalker.StackFrame a, StackWalker.StackFrame b, String methodName) {
        return a.getDeclaringClass() == b.getDeclaringClass() && a.getMethodName().equals(methodName);
    }

    private static boolean isAutoCloseable(StackWalker.StackFrame element, StackWalker.StackFrame pushed) {
        if (StackWalkUtil.isSameMethod(element, pushed, "$closeResource")) {
            return true;
        }
        return "kotlin.jdk7.AutoCloseableKt".equals(element.getClassName()) && "closeFinally".equals(element.getMethodName());
    }

    static @Nullable Object stackWalkCheckPop(Class<?> after, Object pushedObj) {
        StackWalker.StackFrame pushed = (StackWalker.StackFrame)pushedObj;
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s -> {
            StackWalker.StackFrame element;
            Iterator iter = s.iterator();
            iter.next();
            iter.next();
            while ((element = (StackWalker.StackFrame)iter.next()).getDeclaringClass() == after && iter.hasNext()) {
            }
            if (StackWalkUtil.isSameMethod(element, pushed)) {
                return null;
            }
            if (iter.hasNext() && StackWalkUtil.isAutoCloseable(element, pushed) && StackWalkUtil.isSameMethod(element = (StackWalker.StackFrame)iter.next(), pushed)) {
                return null;
            }
            return element;
        });
    }

    static Object[] stackWalkGetTrace() {
        return StackWalker.getInstance().walk(s -> (StackWalker.StackFrame[])s.skip(2L).dropWhile(f -> f.getClassName().startsWith("org.lwjgl.system.Memory")).toArray(StackWalker.StackFrame[]::new));
    }

    static {
        APIUtil.apiLog("Java 11 stack walker enabled");
    }
}

