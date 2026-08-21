/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;
import org.apache.logging.log4j.util.PrivateSecurityManagerStackTraceUtil;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public class StackLocator {
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static final StackWalker STACK_WALKER = StackWalker.getInstance();
    private static final StackLocator INSTANCE = new StackLocator();

    public static StackLocator getInstance() {
        return INSTANCE;
    }

    private StackLocator() {
    }

    public Class<?> getCallerClass(Class<?> sentinelClass, Predicate<Class<?>> callerPredicate) {
        if (sentinelClass == null) {
            throw new IllegalArgumentException("sentinelClass cannot be null");
        }
        if (callerPredicate == null) {
            throw new IllegalArgumentException("callerPredicate cannot be null");
        }
        return WALKER.walk(s -> s.map(StackWalker.StackFrame::getDeclaringClass).dropWhile(clazz -> !sentinelClass.equals(clazz)).dropWhile(clazz -> sentinelClass.equals(clazz) || !callerPredicate.test((Class<?>)clazz)).findFirst().orElse(null));
    }

    public Class<?> getCallerClass(String fqcn) {
        return this.getCallerClass(fqcn, "");
    }

    public Class<?> getCallerClass(String fqcn, String pkg) {
        return WALKER.walk(s -> s.dropWhile(f -> !f.getClassName().equals(fqcn)).dropWhile(f -> f.getClassName().equals(fqcn)).dropWhile(f -> !f.getClassName().startsWith(pkg)).findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public Class<?> getCallerClass(Class<?> anchor) {
        return WALKER.walk(s -> s.dropWhile(f -> !f.getDeclaringClass().equals(anchor)).dropWhile(f -> f.getDeclaringClass().equals(anchor)).findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public Class<?> getCallerClass(int depth) {
        return WALKER.walk(s -> s.skip(depth).findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public Deque<Class<?>> getCurrentStackTrace() {
        if (PrivateSecurityManagerStackTraceUtil.isEnabled()) {
            return PrivateSecurityManagerStackTraceUtil.getCurrentStackTrace();
        }
        ArrayDeque stack = new ArrayDeque();
        return WALKER.walk(s -> {
            s.forEach(f -> stack.add(f.getDeclaringClass()));
            return stack;
        });
    }

    public StackTraceElement calcLocation(String fqcnOfLogger) {
        return STACK_WALKER.walk(s -> s.dropWhile(f -> !f.getClassName().equals(fqcnOfLogger)).dropWhile(f -> f.getClassName().equals(fqcnOfLogger)).findFirst()).map(StackWalker.StackFrame::toStackTraceElement).orElse(null);
    }

    public StackTraceElement getStackTraceElement(int depth) {
        return STACK_WALKER.walk(s -> s.skip(depth).findFirst()).map(StackWalker.StackFrame::toStackTraceElement).orElse(null);
    }
}

