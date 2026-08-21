/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

public class EnumUtils {
    private static final String CANNOT_STORE_S_S_VALUES_IN_S_BITS = "Cannot store %s %s values in %s bits";
    private static final String ENUM_CLASS_MUST_BE_DEFINED = "EnumClass must be defined.";
    private static final String NULL_ELEMENTS_NOT_PERMITTED = "null elements not permitted";
    private static final String S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE = "%s does not seem to be an Enum type";

    private static <E extends Enum<E>> Class<E> asEnum(Class<E> enumClass) {
        Objects.requireNonNull(enumClass, ENUM_CLASS_MUST_BE_DEFINED);
        Validate.isTrue(enumClass.isEnum(), S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE, enumClass);
        return enumClass;
    }

    private static <E extends Enum<E>> Class<E> checkBitVectorable(Class<E> enumClass) {
        Enum[] constants = (Enum[])EnumUtils.asEnum(enumClass).getEnumConstants();
        Validate.isTrue(constants.length <= 64, CANNOT_STORE_S_S_VALUES_IN_S_BITS, constants.length, enumClass.getSimpleName(), 64);
        return enumClass;
    }

    @SafeVarargs
    public static <E extends Enum<E>> long generateBitVector(Class<E> enumClass, E ... values2) {
        Validate.noNullElements(values2);
        return EnumUtils.generateBitVector(enumClass, Arrays.asList(values2));
    }

    public static <E extends Enum<E>> long generateBitVector(Class<E> enumClass, Iterable<? extends E> values2) {
        EnumUtils.checkBitVectorable(enumClass);
        Objects.requireNonNull(values2, "values");
        long total = 0L;
        for (Enum constant : values2) {
            Objects.requireNonNull(constant, NULL_ELEMENTS_NOT_PERMITTED);
            total |= 1L << constant.ordinal();
        }
        return total;
    }

    @SafeVarargs
    public static <E extends Enum<E>> long[] generateBitVectors(Class<E> enumClass, E ... values2) {
        EnumUtils.asEnum(enumClass);
        Validate.noNullElements(values2);
        EnumSet<Enum> condensed = EnumSet.noneOf(enumClass);
        Collections.addAll(condensed, values2);
        long[] result = new long[(((Enum[])enumClass.getEnumConstants()).length - 1) / 64 + 1];
        for (Enum value : condensed) {
            int n = value.ordinal() / 64;
            result[n] = result[n] | 1L << value.ordinal() % 64;
        }
        ArrayUtils.reverse(result);
        return result;
    }

    public static <E extends Enum<E>> long[] generateBitVectors(Class<E> enumClass, Iterable<? extends E> values2) {
        EnumUtils.asEnum(enumClass);
        Objects.requireNonNull(values2, "values");
        EnumSet<Enum> condensed = EnumSet.noneOf(enumClass);
        values2.forEach(constant -> condensed.add(Objects.requireNonNull(constant, NULL_ELEMENTS_NOT_PERMITTED)));
        long[] result = new long[(((Enum[])enumClass.getEnumConstants()).length - 1) / 64 + 1];
        for (Enum value : condensed) {
            int n = value.ordinal() / 64;
            result[n] = result[n] | 1L << value.ordinal() % 64;
        }
        ArrayUtils.reverse(result);
        return result;
    }

    public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String enumName) {
        return EnumUtils.getEnum(enumClass, enumName, null);
    }

    public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String enumName, E defaultEnum) {
        if (enumName == null) {
            return defaultEnum;
        }
        try {
            return Enum.valueOf(enumClass, enumName);
        }
        catch (IllegalArgumentException ex) {
            return defaultEnum;
        }
    }

    public static <E extends Enum<E>> E getEnumIgnoreCase(Class<E> enumClass, String enumName) {
        return EnumUtils.getEnumIgnoreCase(enumClass, enumName, null);
    }

    public static <E extends Enum<E>> E getEnumIgnoreCase(Class<E> enumClass, String enumName, E defaultEnum) {
        return (E)EnumUtils.getFirstEnumIgnoreCase(enumClass, enumName, Enum::name, defaultEnum);
    }

    public static <E extends Enum<E>> List<E> getEnumList(Class<E> enumClass) {
        return new ArrayList<Enum>(Arrays.asList((Enum[])enumClass.getEnumConstants()));
    }

    public static <E extends Enum<E>> Map<String, E> getEnumMap(Class<E> enumClass) {
        return EnumUtils.getEnumMap(enumClass, Enum::name);
    }

    public static <E extends Enum<E>, K> Map<K, E> getEnumMap(Class<E> enumClass, Function<E, K> keyFunction) {
        return Stream.of((Enum[])enumClass.getEnumConstants()).collect(Collectors.toMap(keyFunction::apply, Function.identity()));
    }

    public static <E extends Enum<E>> E getEnumSystemProperty(Class<E> enumClass, String propName, E defaultEnum) {
        return enumClass == null || propName == null ? defaultEnum : EnumUtils.getEnum(enumClass, System.getProperty(propName), defaultEnum);
    }

    public static <E extends Enum<E>> E getFirstEnumIgnoreCase(Class<E> enumClass, String enumName, Function<E, String> stringFunction, E defaultEnum) {
        if (enumName == null || !enumClass.isEnum()) {
            return defaultEnum;
        }
        return (E)Stream.of((Enum[])enumClass.getEnumConstants()).filter(e -> enumName.equalsIgnoreCase((String)stringFunction.apply(e))).findFirst().orElse(defaultEnum);
    }

    public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String enumName) {
        return EnumUtils.getEnum(enumClass, enumName) != null;
    }

    public static <E extends Enum<E>> boolean isValidEnumIgnoreCase(Class<E> enumClass, String enumName) {
        return EnumUtils.getEnumIgnoreCase(enumClass, enumName) != null;
    }

    public static <E extends Enum<E>> EnumSet<E> processBitVector(Class<E> enumClass, long value) {
        EnumUtils.checkBitVectorable(enumClass).getEnumConstants();
        return EnumUtils.processBitVectors(enumClass, value);
    }

    public static <E extends Enum<E>> EnumSet<E> processBitVectors(Class<E> enumClass, long ... values2) {
        EnumSet<Enum> results = EnumSet.noneOf(EnumUtils.asEnum(enumClass));
        long[] lvalues = ArrayUtils.clone(Objects.requireNonNull(values2, "values"));
        ArrayUtils.reverse(lvalues);
        for (Enum constant : (Enum[])enumClass.getEnumConstants()) {
            int block = constant.ordinal() / 64;
            if (block >= lvalues.length || (lvalues[block] & 1L << constant.ordinal() % 64) == 0L) continue;
            results.add(constant);
        }
        return results;
    }

    @Deprecated
    public EnumUtils() {
    }
}

