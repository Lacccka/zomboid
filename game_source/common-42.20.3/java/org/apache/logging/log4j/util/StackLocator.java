/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Predicate;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.PrivateSecurityManagerStackTraceUtil;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public final class StackLocator {
    static final int JDK_7U25_OFFSET;
    private static final Method GET_CALLER_CLASS_METHOD;
    private static final StackLocator INSTANCE;
    private static final Class<?> DEFAULT_CALLER_CLASS;

    public static StackLocator getInstance() {
        return INSTANCE;
    }

    private StackLocator() {
    }

    @PerformanceSensitive
    public Class<?> getCallerClass(Class<?> sentinelClass, Predicate<Class<?>> callerPredicate) {
        Class<?> clazz;
        if (sentinelClass == null) {
            throw new IllegalArgumentException("sentinelClass cannot be null");
        }
        if (callerPredicate == null) {
            throw new IllegalArgumentException("callerPredicate cannot be null");
        }
        boolean foundSentinel = false;
        int i = 2;
        while (null != (clazz = this.getCallerClass(i))) {
            if (sentinelClass.equals(clazz)) {
                foundSentinel = true;
            } else if (foundSentinel && callerPredicate.test(clazz)) {
                return clazz;
            }
            ++i;
        }
        return DEFAULT_CALLER_CLASS;
    }

    @PerformanceSensitive
    public Class<?> getCallerClass(int depth) {
        if (depth < 0) {
            throw new IndexOutOfBoundsException(Integer.toString(depth));
        }
        if (GET_CALLER_CLASS_METHOD == null) {
            return DEFAULT_CALLER_CLASS;
        }
        try {
            return (Class)GET_CALLER_CLASS_METHOD.invoke(null, depth + 1 + JDK_7U25_OFFSET);
        }
        catch (Exception e) {
            return DEFAULT_CALLER_CLASS;
        }
    }

    @PerformanceSensitive
    public Class<?> getCallerClass(String fqcn, String pkg) {
        Class<?> clazz;
        boolean next = false;
        int i = 2;
        while (null != (clazz = this.getCallerClass(i))) {
            if (fqcn.equals(clazz.getName())) {
                next = true;
            } else if (next && clazz.getName().startsWith(pkg)) {
                return clazz;
            }
            ++i;
        }
        return DEFAULT_CALLER_CLASS;
    }

    @PerformanceSensitive
    public Class<?> getCallerClass(Class<?> anchor) {
        Class<?> clazz;
        boolean next = false;
        int i = 2;
        while (null != (clazz = this.getCallerClass(i))) {
            if (anchor.equals(clazz)) {
                next = true;
            } else if (next) {
                return clazz;
            }
            ++i;
        }
        return Object.class;
    }

    @PerformanceSensitive
    public Deque<Class<?>> getCurrentStackTrace() {
        Class<?> clazz;
        if (PrivateSecurityManagerStackTraceUtil.isEnabled()) {
            return PrivateSecurityManagerStackTraceUtil.getCurrentStackTrace();
        }
        ArrayDeque classes = new ArrayDeque();
        int i = 1;
        while (null != (clazz = this.getCallerClass(i))) {
            classes.push(clazz);
            ++i;
        }
        return classes;
    }

    public StackTraceElement calcLocation(String fqcnOfLogger) {
        if (fqcnOfLogger == null) {
            return null;
        }
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        boolean found = false;
        for (int i = 0; i < stackTrace.length; ++i) {
            String className = stackTrace[i].getClassName();
            if (fqcnOfLogger.equals(className)) {
                found = true;
                continue;
            }
            if (!found || fqcnOfLogger.equals(className)) continue;
            return stackTrace[i];
        }
        return null;
    }

    public StackTraceElement getStackTraceElement(int depth) {
        int i = 0;
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            if (!this.isValid(element)) continue;
            if (i == depth) {
                return element;
            }
            ++i;
        }
        throw new IndexOutOfBoundsException(Integer.toString(depth));
    }

    private boolean isValid(StackTraceElement element) {
        if (element.isNativeMethod()) {
            return false;
        }
        String cn = element.getClassName();
        if (cn.startsWith("sun.reflect.")) {
            return false;
        }
        String mn = element.getMethodName();
        if (cn.startsWith("java.lang.reflect.") && (mn.equals("invoke") || mn.equals("newInstance"))) {
            return false;
        }
        if (cn.startsWith("jdk.internal.reflect.")) {
            return false;
        }
        if (cn.equals("java.lang.Class") && mn.equals("newInstance")) {
            return false;
        }
        return !cn.equals("java.lang.invoke.MethodHandle") || !mn.startsWith("invoke");
    }

    static {
        Method getCallerClassMethod;
        DEFAULT_CALLER_CLASS = null;
        int java7u25CompensationOffset = 0;
        try {
            Class<?> sunReflectionClass = LoaderUtil.loadClass("sun.reflect.Reflection");
            getCallerClassMethod = sunReflectionClass.getDeclaredMethod("getCallerClass", Integer.TYPE);
            Object o = getCallerClassMethod.invoke(null, 0);
            getCallerClassMethod.invoke(null, 0);
            if (o == null || o != sunReflectionClass) {
                getCallerClassMethod = null;
                java7u25CompensationOffset = -1;
            } else {
                o = getCallerClassMethod.invoke(null, 1);
                if (o == sunReflectionClass) {
                    System.out.println("WARNING: Unexpected result from sun.reflect.Reflection.getCallerClass(int), adjusting offset for future calls.");
                    java7u25CompensationOffset = 1;
                }
            }
        }
        catch (Exception | LinkageError e) {
            System.out.println("WARNING: sun.reflect.Reflection.getCallerClass is not supported. This will impact performance.");
            getCallerClassMethod = null;
            java7u25CompensationOffset = -1;
        }
        GET_CALLER_CLASS_METHOD = getCallerClassMethod;
        JDK_7U25_OFFSET = java7u25CompensationOffset;
        INSTANCE = new StackLocator();
    }
}

