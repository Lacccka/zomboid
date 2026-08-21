/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassHelper {
    private ClassHelper() {
        throw new UnsupportedOperationException("You cannot create an instance of this class");
    }

    public static List<Class<?>> getInterfaces(Class<?> clazz) {
        return ClassHelper.getInterfacesAsStream(clazz).collect(Collectors.toList());
    }

    public static Stream<Class<?>> getInterfacesAsStream(Class<?> clazz) {
        return ClassHelper.getSuperclassesAsStream(clazz, true).flatMap(superClass -> Stream.concat(superClass.isInterface() ? Stream.of(superClass) : Stream.empty(), Arrays.stream(superClass.getInterfaces()).flatMap(ClassHelper::getInterfacesAsStream))).distinct();
    }

    public static List<Class<?>> getSuperclasses(Class<?> clazz) {
        return ClassHelper.getSuperclassesAsStream(clazz).collect(Collectors.toList());
    }

    public static Stream<Class<?>> getSuperclassesAsStream(Class<?> clazz) {
        return ClassHelper.getSuperclassesAsStream(clazz, false);
    }

    private static Stream<Class<?>> getSuperclassesAsStream(Class<?> clazz, boolean includeArgument) {
        return Stream.concat(includeArgument ? Stream.of(clazz) : Stream.empty(), Optional.ofNullable(clazz.getSuperclass()).map(Stream::of).orElseGet(Stream::empty).flatMap(superclass -> ClassHelper.getSuperclassesAsStream(superclass, true)));
    }
}

