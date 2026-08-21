/*
 * Decompiled with CFR 0.152.
 */
package generation.builders.validation;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class SerializableMethod {
    public static String asLuaString(Serializable handle) {
        try {
            Method writeReplace2 = handle.getClass().getDeclaredMethod("writeReplace", new Class[0]);
            writeReplace2.setAccessible(true);
            Object invoke2 = writeReplace2.invoke((Object)handle, new Object[0]);
            if (invoke2 instanceof SerializedLambda) {
                SerializedLambda lambda2 = (SerializedLambda)invoke2;
                String implClass = lambda2.getImplClass();
                String implMethod = lambda2.getImplMethodName();
                Class<?> clazz = Class.forName(implClass.replace('/', '.'));
                int classModifiers = clazz.getModifiers();
                if (!(clazz.isMemberClass() && (classModifiers & 8) == 0 || (classModifiers & 1) == 0 || lambda2.getImplMethodName().contains("$") || lambda2.getCapturedArgCount() != 0 || !Arrays.stream(clazz.getDeclaredMethods()).anyMatch(m -> {
                    int methodModifiers = m.getModifiers();
                    return m.getName().equals(implMethod) && (methodModifiers & 8) != 0 && (methodModifiers & 1) != 0;
                }))) {
                    String[] parts = implClass.split("[/$]");
                    return parts[parts.length - 1] + "." + implMethod;
                }
            }
        }
        catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException reflectiveOperationException) {
            // empty catch block
        }
        throw new IllegalArgumentException("Invalid method");
    }

    @FunctionalInterface
    public static interface Consumer3<T, U, V>
    extends Serializable {
        public void accept(T var1, U var2, V var3);
    }

    @FunctionalInterface
    public static interface Consumer2<T, U>
    extends BiConsumer<T, U>,
    Serializable {
    }

    @FunctionalInterface
    public static interface Consumer<T>
    extends java.util.function.Consumer<T>,
    Serializable {
    }

    @FunctionalInterface
    public static interface Function3<T, U, V, R>
    extends Serializable {
        public R apply(T var1, U var2, V var3);
    }

    @FunctionalInterface
    public static interface Function2<T, U, R>
    extends BiFunction<T, U, R>,
    Serializable {
    }

    @FunctionalInterface
    public static interface Function<T, R>
    extends java.util.function.Function<T, R>,
    Serializable {
    }
}

