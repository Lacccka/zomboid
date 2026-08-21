/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import javax.lang.model.element.VariableElement;

public class DescriptorUtil {
    public static String getDescriptor(String methodName, List<? extends VariableElement> parameters) {
        Object parameterString = "";
        for (VariableElement variableElement : parameters) {
            parameterString = (String)parameterString + ":" + variableElement.asType().toString();
        }
        return methodName + (String)parameterString;
    }

    public static String getDescriptor(Constructor<?> constructor) {
        String parameters = DescriptorUtil.getParameters(constructor.getParameterTypes());
        return "new" + parameters;
    }

    public static String getDescriptor(Method method) {
        String parameters = DescriptorUtil.getParameters(method.getParameterTypes());
        return method.getName() + parameters;
    }

    private static String getParameters(Class<?>[] parameterTypes) {
        Object parameters = "";
        for (Class<?> clazz : parameterTypes) {
            parameters = (String)parameters + ":" + clazz.getName();
        }
        return parameters;
    }
}

