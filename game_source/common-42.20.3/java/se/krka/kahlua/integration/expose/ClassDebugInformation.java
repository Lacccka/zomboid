/*
 * Decompiled with CFR 0.152.
 */
package se.krka.kahlua.integration.expose;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import se.krka.kahlua.integration.annotations.Desc;
import se.krka.kahlua.integration.annotations.LuaConstructor;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.AnnotationUtil;
import se.krka.kahlua.integration.expose.MethodDebugInformation;
import se.krka.kahlua.integration.expose.MethodParameter;
import se.krka.kahlua.integration.expose.TypeUtil;
import se.krka.kahlua.integration.processor.ClassParameterInformation;
import se.krka.kahlua.integration.processor.DescriptorUtil;
import se.krka.kahlua.integration.processor.MethodParameterInformation;

public class ClassDebugInformation {
    private final Map<String, MethodDebugInformation> methods = new HashMap<String, MethodDebugInformation>();

    public ClassDebugInformation(Class<?> clazz, ClassParameterInformation parameterInfo) {
        this.addContent(clazz, parameterInfo);
        this.addConstructors(clazz, parameterInfo);
    }

    private void addContent(Class<?> clazz, ClassParameterInformation parameterInfo) {
        if (clazz == null) {
            return;
        }
        this.addContent(clazz.getSuperclass(), parameterInfo);
        for (Class<?> clazz2 : clazz.getInterfaces()) {
            this.addContent(clazz2, parameterInfo);
        }
        for (GenericDeclaration genericDeclaration : clazz.getDeclaredMethods()) {
            LuaMethod methodAnnotation = AnnotationUtil.getAnnotation((Method)genericDeclaration, LuaMethod.class);
            String defaultName = ((Method)genericDeclaration).getName();
            int modifiers = ((Method)genericDeclaration).getModifiers();
            Type[] parameterTypes = ((Method)genericDeclaration).getGenericParameterTypes();
            String descriptor = DescriptorUtil.getDescriptor((Method)genericDeclaration);
            Type returnTypeClass = ((Method)genericDeclaration).getGenericReturnType();
            Annotation[][] parameterAnnotations = ((Method)genericDeclaration).getParameterAnnotations();
            Desc descriptionAnnotation = AnnotationUtil.getAnnotation((Method)genericDeclaration, Desc.class);
            this.addMethod(parameterInfo, parameterTypes, descriptor, returnTypeClass, parameterAnnotations, ClassDebugInformation.getName(methodAnnotation, defaultName), !ClassDebugInformation.isGlobal(methodAnnotation, ClassDebugInformation.isStatic(modifiers)), descriptionAnnotation);
        }
    }

    private void addConstructors(Class<?> clazz, ClassParameterInformation parameterInfo) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            LuaConstructor methodAnnotation = constructor.getAnnotation(LuaConstructor.class);
            String defaultName = "new";
            Type[] parameterTypes = constructor.getGenericParameterTypes();
            String descriptor = DescriptorUtil.getDescriptor(constructor);
            Class<?> returnTypeClass = clazz;
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            Desc descriptionAnnotation = constructor.getAnnotation(Desc.class);
            this.addMethod(parameterInfo, parameterTypes, descriptor, returnTypeClass, parameterAnnotations, ClassDebugInformation.getName(methodAnnotation, "new"), true, descriptionAnnotation);
        }
    }

    private void addMethod(ClassParameterInformation parameterInfo, Type[] parameterTypes, String descriptor, Type returnTypeClass, Annotation[][] parameterAnnotations, String luaName, boolean method, Desc descriptionAnnotation) {
        MethodParameterInformation parameterNames = parameterInfo.methods.get(descriptor);
        if (this.methods.containsKey(descriptor)) {
            return;
        }
        if (parameterNames == null) {
            return;
        }
        ArrayList<MethodParameter> parameters = new ArrayList<MethodParameter>();
        for (int i = 0; i < parameterTypes.length; ++i) {
            Type type = parameterTypes[i];
            String name = parameterNames.getName(i);
            String typeName = TypeUtil.getClassName(type);
            String description = this.getDescription(parameterAnnotations[i]);
            parameters.add(new MethodParameter(name, typeName, description));
        }
        String returnType = TypeUtil.getClassName(returnTypeClass);
        String returnDescription = ClassDebugInformation.getDescription(descriptionAnnotation);
        MethodDebugInformation debugInfo = new MethodDebugInformation(luaName, method, parameters, returnType, returnDescription);
        this.methods.put(descriptor, debugInfo);
    }

    private String getDescription(Annotation[] parameterAnnotation) {
        for (Annotation annotation : parameterAnnotation) {
            if (annotation == null || !(annotation instanceof Desc)) continue;
            Desc desc = (Desc)annotation;
            return ClassDebugInformation.getDescription(desc);
        }
        return null;
    }

    private static String getDescription(Desc annotation) {
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }

    private static boolean isStatic(int modifiers) {
        return (modifiers & 8) != 0;
    }

    private static boolean isGlobal(LuaMethod annotation, boolean defaultValue) {
        if (annotation != null) {
            return annotation.global();
        }
        return defaultValue;
    }

    private static String getName(LuaMethod annotation, String defaultName) {
        String name;
        if (annotation != null && (name = annotation.name()) != null && !name.isEmpty()) {
            return name;
        }
        return defaultName;
    }

    private static String getName(LuaConstructor annotation, String defaultName) {
        String name;
        if (annotation != null && (name = annotation.name()) != null && !name.isEmpty()) {
            return name;
        }
        return defaultName;
    }

    public Map<String, MethodDebugInformation> getMethods() {
        return this.methods;
    }
}

