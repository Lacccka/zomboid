/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationUtil {
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotation) {
        return AnnotationUtil.getAnnotation(method.getDeclaringClass(), method.getName(), method.getParameterTypes(), annotation);
    }

    private static <T extends Annotation> T getAnnotation(Class<?> clazz, String name, Class<?>[] types, Class<T> annotationType) {
        if (clazz == null) {
            return null;
        }
        try {
            Method method = clazz.getMethod(name, types);
            T annotation = method.getAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
            for (Class<?> subClass : clazz.getInterfaces()) {
                annotation = AnnotationUtil.getAnnotation(subClass, name, types, annotationType);
                if (annotation == null) continue;
                return annotation;
            }
            return AnnotationUtil.getAnnotation(clazz.getSuperclass(), name, types, annotationType);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }
}

