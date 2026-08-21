/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArraySorter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.stream.Streams;

public class ArrayUtils {
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
    public static final Field[] EMPTY_FIELD_ARRAY = new Field[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
    public static final Method[] EMPTY_METHOD_ARRAY = new Method[0];
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Throwable[] EMPTY_THROWABLE_ARRAY = new Throwable[0];
    public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
    public static final int INDEX_NOT_FOUND = -1;

    public static boolean[] add(boolean[] array, boolean element) {
        boolean[] newArray = (boolean[])ArrayUtils.copyArrayGrow1(array, Boolean.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    @Deprecated
    public static boolean[] add(boolean[] array, int index, boolean element) {
        return (boolean[])ArrayUtils.add(array, index, element, Boolean.TYPE);
    }

    public static byte[] add(byte[] array, byte element) {
        byte[] newArray = (byte[])ArrayUtils.copyArrayGrow1(array, Byte.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    @Deprecated
    public static byte[] add(byte[] array, int index, byte element) {
        return (byte[])ArrayUtils.add(array, index, element, Byte.TYPE);
    }

    public static char[] add(char[] array, char element) {
        char[] newArray = (char[])ArrayUtils.copyArrayGrow1(array, Character.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    @Deprecated
    public static char[] add(char[] array, int index, char element) {
        return (char[])ArrayUtils.add(array, index, Character.valueOf(element), Character.TYPE);
    }

    public static double[] add(double[] array, double element) {
        double[] newArray = (double[])ArrayUtils.copyArrayGrow1(array, Double.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    @Deprecated
    public static double[] add(double[] array, int index, double element) {
        return (double[])ArrayUtils.add(array, index, element, Double.TYPE);
    }

    public static float[] add(float[] array, float element) {
        float[] newArray = (float[])ArrayUtils.copyArrayGrow1(array, Float.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    @Deprecated
    public static float[] add(float[] array, int index, float element) {
        return (float[])ArrayUtils.add(array, index, Float.valueOf(element), Float.TYPE);
    }

    public static int[] add(int[] array, int element) {
        int[] newArray = (int[])ArrayUtils.copyArrayGrow1(array, Integer.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    @Deprecated
    public static int[] add(int[] array, int index, int element) {
        return (int[])ArrayUtils.add(array, index, element, Integer.TYPE);
    }

    @Deprecated
    public static long[] add(long[] array, int index, long element) {
        return (long[])ArrayUtils.add(array, index, element, Long.TYPE);
    }

    public static long[] add(long[] array, long element) {
        long[] newArray = (long[])ArrayUtils.copyArrayGrow1(array, Long.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    private static Object add(Object array, int index, Object element, Class<?> clazz) {
        if (array == null) {
            if (index != 0) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Length: 0");
            }
            Object joinedArray = Array.newInstance(clazz, 1);
            Array.set(joinedArray, 0, element);
            return joinedArray;
        }
        int length = Array.getLength(array);
        if (index > length || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }
        Object result = ArrayUtils.arraycopy(array, 0, 0, index, () -> Array.newInstance(clazz, length + 1));
        Array.set(result, index, element);
        if (index < length) {
            System.arraycopy(array, index, result, index + 1, length - index);
        }
        return result;
    }

    @Deprecated
    public static short[] add(short[] array, int index, short element) {
        return (short[])ArrayUtils.add(array, index, element, Short.TYPE);
    }

    public static short[] add(short[] array, short element) {
        short[] newArray = (short[])ArrayUtils.copyArrayGrow1(array, Short.TYPE);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    @Deprecated
    public static <T> T[] add(T[] array, int index, T element) {
        Class<T> clazz;
        if (array != null) {
            clazz = ArrayUtils.getComponentType(array);
        } else if (element != null) {
            clazz = ObjectUtils.getClass(element);
        } else {
            throw new IllegalArgumentException("Array and element cannot both be null");
        }
        return (Object[])ArrayUtils.add(array, index, element, clazz);
    }

    public static <T> T[] add(T[] array, T element) {
        Class<?> type;
        if (array != null) {
            type = array.getClass().getComponentType();
        } else if (element != null) {
            type = element.getClass();
        } else {
            throw new IllegalArgumentException("Arguments cannot both be null");
        }
        Object[] newArray = (Object[])ArrayUtils.copyArrayGrow1(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    public static boolean[] addAll(boolean[] array1, boolean ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        boolean[] joinedArray = new boolean[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static byte[] addAll(byte[] array1, byte ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static char[] addAll(char[] array1, char ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        char[] joinedArray = new char[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static double[] addAll(double[] array1, double ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        double[] joinedArray = new double[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static float[] addAll(float[] array1, float ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        float[] joinedArray = new float[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static int[] addAll(int[] array1, int ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        int[] joinedArray = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static long[] addAll(long[] array1, long ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        long[] joinedArray = new long[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static short[] addAll(short[] array1, short ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        short[] joinedArray = new short[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static <T> T[] addAll(T[] array1, T ... array2) {
        if (array1 == null) {
            return ArrayUtils.clone(array2);
        }
        if (array2 == null) {
            return ArrayUtils.clone(array1);
        }
        Class type1 = ArrayUtils.getComponentType(array1);
        Object[] joinedArray = ArrayUtils.arraycopy(array1, 0, 0, array1.length, () -> ArrayUtils.newInstance(type1, array1.length + array2.length));
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        }
        catch (ArrayStoreException ase) {
            Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)) {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of " + type1.getName(), ase);
            }
            throw ase;
        }
        return joinedArray;
    }

    public static boolean[] addFirst(boolean[] array, boolean element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static byte[] addFirst(byte[] array, byte element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static char[] addFirst(char[] array, char element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static double[] addFirst(double[] array, double element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static float[] addFirst(float[] array, float element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static int[] addFirst(int[] array, int element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static long[] addFirst(long[] array, long element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static short[] addFirst(short[] array, short element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static <T> T[] addFirst(T[] array, T element) {
        return array == null ? ArrayUtils.add(array, element) : ArrayUtils.insert(0, array, element);
    }

    public static <T> T arraycopy(T source2, int sourcePos, int destPos, int length, Function<Integer, T> allocator) {
        return ArrayUtils.arraycopy(source2, sourcePos, allocator.apply(length), destPos, length);
    }

    public static <T> T arraycopy(T source2, int sourcePos, int destPos, int length, Supplier<T> allocator) {
        return ArrayUtils.arraycopy(source2, sourcePos, allocator.get(), destPos, length);
    }

    public static <T> T arraycopy(T source2, int sourcePos, T dest, int destPos, int length) {
        System.arraycopy(source2, sourcePos, dest, destPos, length);
        return dest;
    }

    public static boolean[] clone(boolean[] array) {
        return array != null ? (boolean[])array.clone() : null;
    }

    public static byte[] clone(byte[] array) {
        return array != null ? (byte[])array.clone() : null;
    }

    public static char[] clone(char[] array) {
        return array != null ? (char[])array.clone() : null;
    }

    public static double[] clone(double[] array) {
        return array != null ? (double[])array.clone() : null;
    }

    public static float[] clone(float[] array) {
        return array != null ? (float[])array.clone() : null;
    }

    public static int[] clone(int[] array) {
        return array != null ? (int[])array.clone() : null;
    }

    public static long[] clone(long[] array) {
        return array != null ? (long[])array.clone() : null;
    }

    public static short[] clone(short[] array) {
        return array != null ? (short[])array.clone() : null;
    }

    public static <T> T[] clone(T[] array) {
        return array != null ? (Object[])array.clone() : null;
    }

    public static boolean contains(boolean[] array, boolean valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind) != -1;
    }

    public static boolean contains(byte[] array, byte valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind) != -1;
    }

    public static boolean contains(char[] array, char valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind) != -1;
    }

    public static boolean contains(double[] array, double valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind) != -1;
    }

    public static boolean contains(double[] array, double valueToFind, double tolerance) {
        return ArrayUtils.indexOf(array, valueToFind, 0, tolerance) != -1;
    }

    public static boolean contains(float[] array, float valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind) != -1;
    }

    public static boolean contains(int[] array, int valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind) != -1;
    }

    public static boolean contains(long[] array, long valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind) != -1;
    }

    public static boolean contains(Object[] array, Object objectToFind) {
        return ArrayUtils.indexOf(array, objectToFind) != -1;
    }

    public static boolean contains(short[] array, short valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind) != -1;
    }

    public static boolean containsAny(Object[] array, Object ... objectsToFind) {
        return Streams.of(objectsToFind).anyMatch(e -> ArrayUtils.contains(array, e));
    }

    private static Object copyArrayGrow1(Object array, Class<?> newArrayComponentType) {
        if (array != null) {
            int arrayLength = Array.getLength(array);
            Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
            System.arraycopy(array, 0, newArray, 0, arrayLength);
            return newArray;
        }
        return Array.newInstance(newArrayComponentType, 1);
    }

    public static <T> T get(T[] array, int index) {
        return ArrayUtils.get(array, index, null);
    }

    public static <T> T get(T[] array, int index, T defaultValue) {
        return ArrayUtils.isArrayIndexValid(array, index) ? array[index] : defaultValue;
    }

    public static <T> Class<T> getComponentType(T[] array) {
        return ClassUtils.getComponentType(ObjectUtils.getClass(array));
    }

    public static int getLength(Object array) {
        return array != null ? Array.getLength(array) : 0;
    }

    public static int hashCode(Object array) {
        return new HashCodeBuilder().append(array).toHashCode();
    }

    public static BitSet indexesOf(boolean[] array, boolean valueToFind) {
        return ArrayUtils.indexesOf(array, valueToFind, 0);
    }

    public static BitSet indexesOf(boolean[] array, boolean valueToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(byte[] array, byte valueToFind) {
        return ArrayUtils.indexesOf(array, valueToFind, 0);
    }

    public static BitSet indexesOf(byte[] array, byte valueToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(char[] array, char valueToFind) {
        return ArrayUtils.indexesOf(array, valueToFind, 0);
    }

    public static BitSet indexesOf(char[] array, char valueToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(double[] array, double valueToFind) {
        return ArrayUtils.indexesOf(array, valueToFind, 0);
    }

    public static BitSet indexesOf(double[] array, double valueToFind, double tolerance) {
        return ArrayUtils.indexesOf(array, valueToFind, 0, tolerance);
    }

    public static BitSet indexesOf(double[] array, double valueToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(double[] array, double valueToFind, int startIndex, double tolerance) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex, tolerance)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(float[] array, float valueToFind) {
        return ArrayUtils.indexesOf(array, valueToFind, 0);
    }

    public static BitSet indexesOf(float[] array, float valueToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(int[] array, int valueToFind) {
        return ArrayUtils.indexesOf(array, valueToFind, 0);
    }

    public static BitSet indexesOf(int[] array, int valueToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(long[] array, long valueToFind) {
        return ArrayUtils.indexesOf(array, valueToFind, 0);
    }

    public static BitSet indexesOf(long[] array, long valueToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(Object[] array, Object objectToFind) {
        return ArrayUtils.indexesOf(array, objectToFind, 0);
    }

    public static BitSet indexesOf(Object[] array, Object objectToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, objectToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static BitSet indexesOf(short[] array, short valueToFind) {
        return ArrayUtils.indexesOf(array, valueToFind, 0);
    }

    public static BitSet indexesOf(short[] array, short valueToFind, int startIndex) {
        BitSet bitSet = new BitSet();
        if (array == null) {
            return bitSet;
        }
        while (startIndex < array.length && (startIndex = ArrayUtils.indexOf(array, valueToFind, startIndex)) != -1) {
            bitSet.set(startIndex);
            ++startIndex;
        }
        return bitSet;
    }

    public static int indexOf(boolean[] array, boolean valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind, 0);
    }

    public static int indexOf(boolean[] array, boolean valueToFind, int startIndex) {
        if (ArrayUtils.isEmpty(array)) {
            return -1;
        }
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(byte[] array, byte valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind, 0);
    }

    public static int indexOf(byte[] array, byte valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        }
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(char[] array, char valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind, 0);
    }

    public static int indexOf(char[] array, char valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        }
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(double[] array, double valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind, 0);
    }

    public static int indexOf(double[] array, double valueToFind, double tolerance) {
        return ArrayUtils.indexOf(array, valueToFind, 0, tolerance);
    }

    public static int indexOf(double[] array, double valueToFind, int startIndex) {
        if (ArrayUtils.isEmpty(array)) {
            return -1;
        }
        boolean searchNaN = Double.isNaN(valueToFind);
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            double element = array[i];
            if (valueToFind != element && (!searchNaN || !Double.isNaN(element))) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
        if (ArrayUtils.isEmpty(array)) {
            return -1;
        }
        double min = valueToFind - tolerance;
        double max = valueToFind + tolerance;
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            if (!(array[i] >= min) || !(array[i] <= max)) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(float[] array, float valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind, 0);
    }

    public static int indexOf(float[] array, float valueToFind, int startIndex) {
        if (ArrayUtils.isEmpty(array)) {
            return -1;
        }
        boolean searchNaN = Float.isNaN(valueToFind);
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            float element = array[i];
            if (valueToFind != element && (!searchNaN || !Float.isNaN(element))) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(int[] array, int valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind, 0);
    }

    public static int indexOf(int[] array, int valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        }
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(long[] array, long valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind, 0);
    }

    public static int indexOf(long[] array, long valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        }
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int indexOf(Object[] array, Object objectToFind) {
        return ArrayUtils.indexOf(array, objectToFind, 0);
    }

    public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return -1;
        }
        startIndex = ArrayUtils.max0(startIndex);
        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; ++i) {
                if (array[i] != null) continue;
                return i;
            }
        } else {
            for (int i = startIndex; i < array.length; ++i) {
                if (!objectToFind.equals(array[i])) continue;
                return i;
            }
        }
        return -1;
    }

    public static int indexOf(short[] array, short valueToFind) {
        return ArrayUtils.indexOf(array, valueToFind, 0);
    }

    public static int indexOf(short[] array, short valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        }
        for (int i = ArrayUtils.max0(startIndex); i < array.length; ++i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static boolean[] insert(int index, boolean[] array, boolean ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        boolean[] result = new boolean[array.length + values2.length];
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    public static byte[] insert(int index, byte[] array, byte ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        byte[] result = new byte[array.length + values2.length];
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    public static char[] insert(int index, char[] array, char ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        char[] result = new char[array.length + values2.length];
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    public static double[] insert(int index, double[] array, double ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        double[] result = new double[array.length + values2.length];
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    public static float[] insert(int index, float[] array, float ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        float[] result = new float[array.length + values2.length];
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    public static int[] insert(int index, int[] array, int ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        int[] result = new int[array.length + values2.length];
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    public static long[] insert(int index, long[] array, long ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        long[] result = new long[array.length + values2.length];
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    public static short[] insert(int index, short[] array, short ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        short[] result = new short[array.length + values2.length];
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    @SafeVarargs
    public static <T> T[] insert(int index, T[] array, T ... values2) {
        if (array == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        if (index < 0 || index > array.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + array.length);
        }
        Class<T> type = ArrayUtils.getComponentType(array);
        int length = array.length + values2.length;
        T[] result = ArrayUtils.newInstance(type, length);
        System.arraycopy(values2, 0, result, index, values2.length);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index < array.length) {
            System.arraycopy(array, index, result, index + values2.length, array.length - index);
        }
        return result;
    }

    private static boolean isArrayEmpty(Object array) {
        return ArrayUtils.getLength(array) == 0;
    }

    public static <T> boolean isArrayIndexValid(T[] array, int index) {
        return index >= 0 && ArrayUtils.getLength(array) > index;
    }

    public static boolean isEmpty(boolean[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    public static boolean isEmpty(byte[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    public static boolean isEmpty(char[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    public static boolean isEmpty(double[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    public static boolean isEmpty(float[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    public static boolean isEmpty(int[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    public static boolean isEmpty(long[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    public static boolean isEmpty(Object[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    public static boolean isEmpty(short[] array) {
        return ArrayUtils.isArrayEmpty(array);
    }

    @Deprecated
    public static boolean isEquals(Object array1, Object array2) {
        return new EqualsBuilder().append(array1, array2).isEquals();
    }

    public static boolean isNotEmpty(boolean[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static boolean isNotEmpty(byte[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static boolean isNotEmpty(char[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static boolean isNotEmpty(double[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static boolean isNotEmpty(float[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static boolean isNotEmpty(int[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static boolean isNotEmpty(long[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static boolean isNotEmpty(short[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static <T> boolean isNotEmpty(T[] array) {
        return !ArrayUtils.isEmpty(array);
    }

    public static boolean isSameLength(boolean[] array1, boolean[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(byte[] array1, byte[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(char[] array1, char[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(double[] array1, double[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(float[] array1, float[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(int[] array1, int[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(long[] array1, long[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(Object array1, Object array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(Object[] array1, Object[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameLength(short[] array1, short[] array2) {
        return ArrayUtils.getLength(array1) == ArrayUtils.getLength(array2);
    }

    public static boolean isSameType(Object array1, Object array2) {
        if (array1 == null || array2 == null) {
            throw new IllegalArgumentException("The Array must not be null");
        }
        return array1.getClass().getName().equals(array2.getClass().getName());
    }

    public static boolean isSorted(boolean[] array) {
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        boolean previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            boolean current = array[i];
            if (BooleanUtils.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static boolean isSorted(byte[] array) {
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        byte previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            byte current = array[i];
            if (NumberUtils.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static boolean isSorted(char[] array) {
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        char previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            char current = array[i];
            if (CharUtils.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static boolean isSorted(double[] array) {
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        double previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            double current = array[i];
            if (Double.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static boolean isSorted(float[] array) {
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        float previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            float current = array[i];
            if (Float.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static boolean isSorted(int[] array) {
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        int previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            int current = array[i];
            if (NumberUtils.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static boolean isSorted(long[] array) {
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        long previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            long current = array[i];
            if (NumberUtils.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static boolean isSorted(short[] array) {
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        short previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            short current = array[i];
            if (NumberUtils.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static <T extends Comparable<? super T>> boolean isSorted(T[] array) {
        return ArrayUtils.isSorted(array, Comparable::compareTo);
    }

    public static <T> boolean isSorted(T[] array, Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "comparator");
        if (ArrayUtils.getLength(array) < 2) {
            return true;
        }
        T previous = array[0];
        int n = array.length;
        for (int i = 1; i < n; ++i) {
            T current = array[i];
            if (comparator.compare(previous, current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }

    public static int lastIndexOf(boolean[] array, boolean valueToFind) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(boolean[] array, boolean valueToFind, int startIndex) {
        if (ArrayUtils.isEmpty(array) || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; --i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int lastIndexOf(byte[] array, byte valueToFind) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(byte[] array, byte valueToFind, int startIndex) {
        if (array == null || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; --i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int lastIndexOf(char[] array, char valueToFind) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(char[] array, char valueToFind, int startIndex) {
        if (array == null || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; --i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int lastIndexOf(double[] array, double valueToFind) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(double[] array, double valueToFind, double tolerance) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE, tolerance);
    }

    public static int lastIndexOf(double[] array, double valueToFind, int startIndex) {
        if (ArrayUtils.isEmpty(array) || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; --i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int lastIndexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
        if (ArrayUtils.isEmpty(array) || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        double min = valueToFind - tolerance;
        double max = valueToFind + tolerance;
        for (int i = startIndex; i >= 0; --i) {
            if (!(array[i] >= min) || !(array[i] <= max)) continue;
            return i;
        }
        return -1;
    }

    public static int lastIndexOf(float[] array, float valueToFind) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(float[] array, float valueToFind, int startIndex) {
        if (ArrayUtils.isEmpty(array) || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; --i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int lastIndexOf(int[] array, int valueToFind) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(int[] array, int valueToFind, int startIndex) {
        if (array == null || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; --i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int lastIndexOf(long[] array, long valueToFind) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(long[] array, long valueToFind, int startIndex) {
        if (array == null || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; --i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    public static int lastIndexOf(Object[] array, Object objectToFind) {
        return ArrayUtils.lastIndexOf(array, objectToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(Object[] array, Object objectToFind, int startIndex) {
        block5: {
            block4: {
                if (array == null || startIndex < 0) {
                    return -1;
                }
                if (startIndex >= array.length) {
                    startIndex = array.length - 1;
                }
                if (objectToFind != null) break block4;
                for (int i = startIndex; i >= 0; --i) {
                    if (array[i] != null) continue;
                    return i;
                }
                break block5;
            }
            if (!array.getClass().getComponentType().isInstance(objectToFind)) break block5;
            for (int i = startIndex; i >= 0; --i) {
                if (!objectToFind.equals(array[i])) continue;
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(short[] array, short valueToFind) {
        return ArrayUtils.lastIndexOf(array, valueToFind, Integer.MAX_VALUE);
    }

    public static int lastIndexOf(short[] array, short valueToFind, int startIndex) {
        if (array == null || startIndex < 0) {
            return -1;
        }
        if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        for (int i = startIndex; i >= 0; --i) {
            if (valueToFind != array[i]) continue;
            return i;
        }
        return -1;
    }

    private static int max0(int other) {
        return Math.max(0, other);
    }

    public static <T> T[] newInstance(Class<T> componentType, int length) {
        return (Object[])Array.newInstance(componentType, length);
    }

    public static <T> T[] nullTo(T[] array, T[] defaultArray) {
        return ArrayUtils.isEmpty(array) ? defaultArray : array;
    }

    public static boolean[] nullToEmpty(boolean[] array) {
        return ArrayUtils.isEmpty(array) ? EMPTY_BOOLEAN_ARRAY : array;
    }

    public static Boolean[] nullToEmpty(Boolean[] array) {
        return ArrayUtils.nullTo(array, EMPTY_BOOLEAN_OBJECT_ARRAY);
    }

    public static byte[] nullToEmpty(byte[] array) {
        return ArrayUtils.isEmpty(array) ? EMPTY_BYTE_ARRAY : array;
    }

    public static Byte[] nullToEmpty(Byte[] array) {
        return ArrayUtils.nullTo(array, EMPTY_BYTE_OBJECT_ARRAY);
    }

    public static char[] nullToEmpty(char[] array) {
        return ArrayUtils.isEmpty(array) ? EMPTY_CHAR_ARRAY : array;
    }

    public static Character[] nullToEmpty(Character[] array) {
        return ArrayUtils.nullTo(array, EMPTY_CHARACTER_OBJECT_ARRAY);
    }

    public static Class<?>[] nullToEmpty(Class<?>[] array) {
        return ArrayUtils.nullTo(array, EMPTY_CLASS_ARRAY);
    }

    public static double[] nullToEmpty(double[] array) {
        return ArrayUtils.isEmpty(array) ? EMPTY_DOUBLE_ARRAY : array;
    }

    public static Double[] nullToEmpty(Double[] array) {
        return ArrayUtils.nullTo(array, EMPTY_DOUBLE_OBJECT_ARRAY);
    }

    public static float[] nullToEmpty(float[] array) {
        return ArrayUtils.isEmpty(array) ? EMPTY_FLOAT_ARRAY : array;
    }

    public static Float[] nullToEmpty(Float[] array) {
        return ArrayUtils.nullTo(array, EMPTY_FLOAT_OBJECT_ARRAY);
    }

    public static int[] nullToEmpty(int[] array) {
        return ArrayUtils.isEmpty(array) ? EMPTY_INT_ARRAY : array;
    }

    public static Integer[] nullToEmpty(Integer[] array) {
        return ArrayUtils.nullTo(array, EMPTY_INTEGER_OBJECT_ARRAY);
    }

    public static long[] nullToEmpty(long[] array) {
        return ArrayUtils.isEmpty(array) ? EMPTY_LONG_ARRAY : array;
    }

    public static Long[] nullToEmpty(Long[] array) {
        return ArrayUtils.nullTo(array, EMPTY_LONG_OBJECT_ARRAY);
    }

    public static Object[] nullToEmpty(Object[] array) {
        return ArrayUtils.nullTo(array, EMPTY_OBJECT_ARRAY);
    }

    public static short[] nullToEmpty(short[] array) {
        return ArrayUtils.isEmpty(array) ? EMPTY_SHORT_ARRAY : array;
    }

    public static Short[] nullToEmpty(Short[] array) {
        return ArrayUtils.nullTo(array, EMPTY_SHORT_OBJECT_ARRAY);
    }

    public static String[] nullToEmpty(String[] array) {
        return ArrayUtils.nullTo(array, EMPTY_STRING_ARRAY);
    }

    public static <T> T[] nullToEmpty(T[] array, Class<T[]> type) {
        if (type == null) {
            throw new IllegalArgumentException("The type must not be null");
        }
        if (array == null) {
            return type.cast(Array.newInstance(type.getComponentType(), 0));
        }
        return array;
    }

    private static ThreadLocalRandom random() {
        return ThreadLocalRandom.current();
    }

    public static boolean[] remove(boolean[] array, int index) {
        return (boolean[])ArrayUtils.remove((Object)array, index);
    }

    public static byte[] remove(byte[] array, int index) {
        return (byte[])ArrayUtils.remove((Object)array, index);
    }

    public static char[] remove(char[] array, int index) {
        return (char[])ArrayUtils.remove((Object)array, index);
    }

    public static double[] remove(double[] array, int index) {
        return (double[])ArrayUtils.remove((Object)array, index);
    }

    public static float[] remove(float[] array, int index) {
        return (float[])ArrayUtils.remove((Object)array, index);
    }

    public static int[] remove(int[] array, int index) {
        return (int[])ArrayUtils.remove((Object)array, index);
    }

    public static long[] remove(long[] array, int index) {
        return (long[])ArrayUtils.remove((Object)array, index);
    }

    private static Object remove(Object array, int index) {
        int length = ArrayUtils.getLength(array);
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }
        Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1) {
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        }
        return result;
    }

    public static short[] remove(short[] array, int index) {
        return (short[])ArrayUtils.remove((Object)array, index);
    }

    public static <T> T[] remove(T[] array, int index) {
        return (Object[])ArrayUtils.remove(array, index);
    }

    public static boolean[] removeAll(boolean[] array, int ... indices) {
        return (boolean[])ArrayUtils.removeAll((Object)array, indices);
    }

    public static byte[] removeAll(byte[] array, int ... indices) {
        return (byte[])ArrayUtils.removeAll((Object)array, indices);
    }

    public static char[] removeAll(char[] array, int ... indices) {
        return (char[])ArrayUtils.removeAll((Object)array, indices);
    }

    public static double[] removeAll(double[] array, int ... indices) {
        return (double[])ArrayUtils.removeAll((Object)array, indices);
    }

    public static float[] removeAll(float[] array, int ... indices) {
        return (float[])ArrayUtils.removeAll((Object)array, indices);
    }

    public static int[] removeAll(int[] array, int ... indices) {
        return (int[])ArrayUtils.removeAll((Object)array, indices);
    }

    public static long[] removeAll(long[] array, int ... indices) {
        return (long[])ArrayUtils.removeAll((Object)array, indices);
    }

    static Object removeAll(Object array, BitSet indices) {
        int count;
        int set;
        if (array == null) {
            return null;
        }
        int srcLength = ArrayUtils.getLength(array);
        int removals = indices.cardinality();
        Object result = Array.newInstance(array.getClass().getComponentType(), srcLength - removals);
        int srcIndex = 0;
        int destIndex = 0;
        while ((set = indices.nextSetBit(srcIndex)) != -1) {
            count = set - srcIndex;
            if (count > 0) {
                System.arraycopy(array, srcIndex, result, destIndex, count);
                destIndex += count;
            }
            srcIndex = indices.nextClearBit(set);
        }
        count = srcLength - srcIndex;
        if (count > 0) {
            System.arraycopy(array, srcIndex, result, destIndex, count);
        }
        return result;
    }

    static Object removeAll(Object array, int ... indices) {
        if (array == null) {
            return null;
        }
        int length = ArrayUtils.getLength(array);
        int diff = 0;
        int[] clonedIndices = ArraySorter.sort(ArrayUtils.clone(indices));
        if (ArrayUtils.isNotEmpty(clonedIndices)) {
            int i = clonedIndices.length;
            int prevIndex = length;
            while (--i >= 0) {
                int index = clonedIndices[i];
                if (index < 0 || index >= length) {
                    throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
                }
                if (index >= prevIndex) continue;
                ++diff;
                prevIndex = index;
            }
        }
        Object result = Array.newInstance(array.getClass().getComponentType(), length - diff);
        if (diff < length && clonedIndices != null) {
            int end = length;
            int dest = length - diff;
            for (int i = clonedIndices.length - 1; i >= 0; --i) {
                int index = clonedIndices[i];
                if (end - index > 1) {
                    int cp = end - index - 1;
                    System.arraycopy(array, index + 1, result, dest -= cp, cp);
                }
                end = index;
            }
            if (end > 0) {
                System.arraycopy(array, 0, result, 0, end);
            }
        }
        return result;
    }

    public static short[] removeAll(short[] array, int ... indices) {
        return (short[])ArrayUtils.removeAll((Object)array, indices);
    }

    public static <T> T[] removeAll(T[] array, int ... indices) {
        return (Object[])ArrayUtils.removeAll(array, indices);
    }

    @Deprecated
    public static boolean[] removeAllOccurences(boolean[] array, boolean element) {
        return (boolean[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    @Deprecated
    public static byte[] removeAllOccurences(byte[] array, byte element) {
        return (byte[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    @Deprecated
    public static char[] removeAllOccurences(char[] array, char element) {
        return (char[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    @Deprecated
    public static double[] removeAllOccurences(double[] array, double element) {
        return (double[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    @Deprecated
    public static float[] removeAllOccurences(float[] array, float element) {
        return (float[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    @Deprecated
    public static int[] removeAllOccurences(int[] array, int element) {
        return (int[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    @Deprecated
    public static long[] removeAllOccurences(long[] array, long element) {
        return (long[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    @Deprecated
    public static short[] removeAllOccurences(short[] array, short element) {
        return (short[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    @Deprecated
    public static <T> T[] removeAllOccurences(T[] array, T element) {
        return (Object[])ArrayUtils.removeAll(array, ArrayUtils.indexesOf(array, element));
    }

    public static boolean[] removeAllOccurrences(boolean[] array, boolean element) {
        return (boolean[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    public static byte[] removeAllOccurrences(byte[] array, byte element) {
        return (byte[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    public static char[] removeAllOccurrences(char[] array, char element) {
        return (char[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    public static double[] removeAllOccurrences(double[] array, double element) {
        return (double[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    public static float[] removeAllOccurrences(float[] array, float element) {
        return (float[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    public static int[] removeAllOccurrences(int[] array, int element) {
        return (int[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    public static long[] removeAllOccurrences(long[] array, long element) {
        return (long[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    public static short[] removeAllOccurrences(short[] array, short element) {
        return (short[])ArrayUtils.removeAll((Object)array, ArrayUtils.indexesOf(array, element));
    }

    public static <T> T[] removeAllOccurrences(T[] array, T element) {
        return (Object[])ArrayUtils.removeAll(array, ArrayUtils.indexesOf(array, element));
    }

    public static boolean[] removeElement(boolean[] array, boolean element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static byte[] removeElement(byte[] array, byte element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static char[] removeElement(char[] array, char element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static double[] removeElement(double[] array, double element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static float[] removeElement(float[] array, float element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static int[] removeElement(int[] array, int element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static long[] removeElement(long[] array, long element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static short[] removeElement(short[] array, short element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static <T> T[] removeElement(T[] array, Object element) {
        int index = ArrayUtils.indexOf(array, element);
        return index == -1 ? ArrayUtils.clone(array) : ArrayUtils.remove(array, index);
    }

    public static boolean[] removeElements(boolean[] array, boolean ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<Boolean, MutableInt> occurrences = new HashMap<Boolean, MutableInt>(2);
        for (boolean v : values2) {
            Boolean boxed = v;
            MutableInt count = (MutableInt)occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            boolean key = array[i];
            MutableInt count = (MutableInt)occurrences.get(key);
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(key);
            }
            toRemove.set(i);
        }
        return (boolean[])ArrayUtils.removeAll((Object)array, toRemove);
    }

    public static byte[] removeElements(byte[] array, byte ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<Byte, MutableInt> occurrences = new HashMap<Byte, MutableInt>(values2.length);
        for (byte v : values2) {
            Byte boxed = v;
            MutableInt count = (MutableInt)occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            byte key = array[i];
            MutableInt count = (MutableInt)occurrences.get(key);
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(key);
            }
            toRemove.set(i);
        }
        return (byte[])ArrayUtils.removeAll((Object)array, toRemove);
    }

    public static char[] removeElements(char[] array, char ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<Character, MutableInt> occurrences = new HashMap<Character, MutableInt>(values2.length);
        for (char v : values2) {
            Character boxed = Character.valueOf(v);
            MutableInt count = (MutableInt)occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            char key = array[i];
            MutableInt count = (MutableInt)occurrences.get(Character.valueOf(key));
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(Character.valueOf(key));
            }
            toRemove.set(i);
        }
        return (char[])ArrayUtils.removeAll((Object)array, toRemove);
    }

    public static double[] removeElements(double[] array, double ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<Double, MutableInt> occurrences = new HashMap<Double, MutableInt>(values2.length);
        for (double v : values2) {
            Double boxed = v;
            MutableInt count = (MutableInt)occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            double key = array[i];
            MutableInt count = (MutableInt)occurrences.get(key);
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(key);
            }
            toRemove.set(i);
        }
        return (double[])ArrayUtils.removeAll((Object)array, toRemove);
    }

    public static float[] removeElements(float[] array, float ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<Float, MutableInt> occurrences = new HashMap<Float, MutableInt>(values2.length);
        for (float v : values2) {
            Float boxed = Float.valueOf(v);
            MutableInt count = (MutableInt)occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            float key = array[i];
            MutableInt count = (MutableInt)occurrences.get(Float.valueOf(key));
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(Float.valueOf(key));
            }
            toRemove.set(i);
        }
        return (float[])ArrayUtils.removeAll((Object)array, toRemove);
    }

    public static int[] removeElements(int[] array, int ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<Integer, MutableInt> occurrences = new HashMap<Integer, MutableInt>(values2.length);
        for (int v : values2) {
            Integer boxed = v;
            MutableInt count = (MutableInt)occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            int key = array[i];
            MutableInt count = (MutableInt)occurrences.get(key);
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(key);
            }
            toRemove.set(i);
        }
        return (int[])ArrayUtils.removeAll((Object)array, toRemove);
    }

    public static long[] removeElements(long[] array, long ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<Long, MutableInt> occurrences = new HashMap<Long, MutableInt>(values2.length);
        for (long v : values2) {
            Long boxed = v;
            MutableInt count = (MutableInt)occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            long key = array[i];
            MutableInt count = (MutableInt)occurrences.get(key);
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(key);
            }
            toRemove.set(i);
        }
        return (long[])ArrayUtils.removeAll((Object)array, toRemove);
    }

    public static short[] removeElements(short[] array, short ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<Short, MutableInt> occurrences = new HashMap<Short, MutableInt>(values2.length);
        for (short v : values2) {
            Short boxed = v;
            MutableInt count = (MutableInt)occurrences.get(boxed);
            if (count == null) {
                occurrences.put(boxed, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            short key = array[i];
            MutableInt count = (MutableInt)occurrences.get(key);
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(key);
            }
            toRemove.set(i);
        }
        return (short[])ArrayUtils.removeAll((Object)array, toRemove);
    }

    @SafeVarargs
    public static <T> T[] removeElements(T[] array, T ... values2) {
        if (ArrayUtils.isEmpty(array) || ArrayUtils.isEmpty(values2)) {
            return ArrayUtils.clone(array);
        }
        HashMap<T, MutableInt> occurrences = new HashMap<T, MutableInt>(values2.length);
        for (T v : values2) {
            MutableInt count = (MutableInt)occurrences.get(v);
            if (count == null) {
                occurrences.put(v, new MutableInt(1));
                continue;
            }
            count.increment();
        }
        BitSet toRemove = new BitSet();
        for (int i = 0; i < array.length; ++i) {
            T key = array[i];
            MutableInt count = (MutableInt)occurrences.get(key);
            if (count == null) continue;
            if (count.decrementAndGet() == 0) {
                occurrences.remove(key);
            }
            toRemove.set(i);
        }
        Object[] result = (Object[])ArrayUtils.removeAll(array, toRemove);
        return result;
    }

    public static void reverse(boolean[] array) {
        if (array == null) {
            return;
        }
        ArrayUtils.reverse(array, 0, array.length);
    }

    public static void reverse(boolean[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            boolean tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static void reverse(byte[] array) {
        if (array != null) {
            ArrayUtils.reverse(array, 0, array.length);
        }
    }

    public static void reverse(byte[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            byte tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static void reverse(char[] array) {
        if (array != null) {
            ArrayUtils.reverse(array, 0, array.length);
        }
    }

    public static void reverse(char[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            char tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static void reverse(double[] array) {
        if (array != null) {
            ArrayUtils.reverse(array, 0, array.length);
        }
    }

    public static void reverse(double[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            double tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static void reverse(float[] array) {
        if (array != null) {
            ArrayUtils.reverse(array, 0, array.length);
        }
    }

    public static void reverse(float[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            float tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static void reverse(int[] array) {
        if (array != null) {
            ArrayUtils.reverse(array, 0, array.length);
        }
    }

    public static void reverse(int[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            int tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static void reverse(long[] array) {
        if (array != null) {
            ArrayUtils.reverse(array, 0, array.length);
        }
    }

    public static void reverse(long[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            long tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static void reverse(Object[] array) {
        if (array != null) {
            ArrayUtils.reverse(array, 0, array.length);
        }
    }

    public static void reverse(Object[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            Object tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static void reverse(short[] array) {
        if (array != null) {
            ArrayUtils.reverse(array, 0, array.length);
        }
    }

    public static void reverse(short[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = Math.max(startIndexInclusive, 0);
        for (int j = Math.min(array.length, endIndexExclusive) - 1; j > i; --j, ++i) {
            short tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static <T> T[] setAll(T[] array, IntFunction<? extends T> generator) {
        if (array != null && generator != null) {
            Arrays.setAll(array, generator);
        }
        return array;
    }

    public static <T> T[] setAll(T[] array, Supplier<? extends T> generator) {
        if (array != null && generator != null) {
            for (int i = 0; i < array.length; ++i) {
                array[i] = generator.get();
            }
        }
        return array;
    }

    public static void shift(boolean[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(boolean[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shift(byte[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(byte[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shift(char[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(char[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shift(double[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(double[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shift(float[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(float[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shift(int[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(int[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shift(long[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(long[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shift(Object[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(Object[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shift(short[] array, int offset) {
        if (array != null) {
            ArrayUtils.shift(array, 0, array.length, offset);
        }
    }

    public static void shift(short[] array, int startIndexInclusive, int endIndexExclusive, int offset) {
        if (array == null || startIndexInclusive >= array.length - 1 || endIndexExclusive <= 0) {
            return;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int n = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (n <= 1) {
            return;
        }
        if ((offset %= n) < 0) {
            offset += n;
        }
        while (n > 1 && offset > 0) {
            int nOffset = n - offset;
            if (offset > nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + n - nOffset, nOffset);
                n = offset;
                offset -= nOffset;
                continue;
            }
            if (offset < nOffset) {
                ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
                startIndexInclusive += offset;
                n = nOffset;
                continue;
            }
            ArrayUtils.swap(array, startIndexInclusive, startIndexInclusive + nOffset, offset);
            break;
        }
    }

    public static void shuffle(boolean[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(boolean[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static void shuffle(byte[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(byte[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static void shuffle(char[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(char[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static void shuffle(double[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(double[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static void shuffle(float[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(float[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static void shuffle(int[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(int[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static void shuffle(long[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(long[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static void shuffle(Object[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(Object[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static void shuffle(short[] array) {
        ArrayUtils.shuffle(array, (Random)ArrayUtils.random());
    }

    public static void shuffle(short[] array, Random random) {
        for (int i = array.length; i > 1; --i) {
            ArrayUtils.swap(array, i - 1, random.nextInt(i), 1);
        }
    }

    public static boolean[] subarray(boolean[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int newSize = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, boolean[]::new);
    }

    public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int newSize = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_BYTE_ARRAY;
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, byte[]::new);
    }

    public static char[] subarray(char[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int newSize = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_CHAR_ARRAY;
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, char[]::new);
    }

    public static double[] subarray(double[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int newSize = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, double[]::new);
    }

    public static float[] subarray(float[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int newSize = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, float[]::new);
    }

    public static int[] subarray(int[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int newSize = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_INT_ARRAY;
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, int[]::new);
    }

    public static long[] subarray(long[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int newSize = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_LONG_ARRAY;
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, long[]::new);
    }

    public static short[] subarray(short[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        int newSize = (endIndexExclusive = Math.min(endIndexExclusive, array.length)) - startIndexInclusive;
        if (newSize <= 0) {
            return EMPTY_SHORT_ARRAY;
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, short[]::new);
    }

    public static <T> T[] subarray(T[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        startIndexInclusive = ArrayUtils.max0(startIndexInclusive);
        endIndexExclusive = Math.min(endIndexExclusive, array.length);
        int newSize = endIndexExclusive - startIndexInclusive;
        Class type = ArrayUtils.getComponentType(array);
        if (newSize <= 0) {
            return ArrayUtils.newInstance(type, 0);
        }
        return ArrayUtils.arraycopy(array, startIndexInclusive, 0, newSize, () -> ArrayUtils.newInstance(type, newSize));
    }

    public static void swap(boolean[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(boolean[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        offset1 = ArrayUtils.max0(offset1);
        offset2 = ArrayUtils.max0(offset2);
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            boolean aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static void swap(byte[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(byte[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        offset1 = ArrayUtils.max0(offset1);
        offset2 = ArrayUtils.max0(offset2);
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            byte aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static void swap(char[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(char[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        offset1 = ArrayUtils.max0(offset1);
        offset2 = ArrayUtils.max0(offset2);
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            char aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static void swap(double[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(double[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        offset1 = ArrayUtils.max0(offset1);
        offset2 = ArrayUtils.max0(offset2);
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            double aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static void swap(float[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(float[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        offset1 = ArrayUtils.max0(offset1);
        offset2 = ArrayUtils.max0(offset2);
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            float aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static void swap(int[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(int[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        offset1 = ArrayUtils.max0(offset1);
        offset2 = ArrayUtils.max0(offset2);
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            int aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static void swap(long[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(long[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        offset1 = ArrayUtils.max0(offset1);
        offset2 = ArrayUtils.max0(offset2);
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            long aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static void swap(Object[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(Object[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        offset1 = ArrayUtils.max0(offset1);
        offset2 = ArrayUtils.max0(offset2);
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            Object aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static void swap(short[] array, int offset1, int offset2) {
        ArrayUtils.swap(array, offset1, offset2, 1);
    }

    public static void swap(short[] array, int offset1, int offset2, int len) {
        if (ArrayUtils.isEmpty(array) || offset1 >= array.length || offset2 >= array.length) {
            return;
        }
        if ((offset1 = ArrayUtils.max0(offset1)) == (offset2 = ArrayUtils.max0(offset2))) {
            return;
        }
        len = Math.min(Math.min(len, array.length - offset1), array.length - offset2);
        int i = 0;
        while (i < len) {
            short aux = array[offset1];
            array[offset1] = array[offset2];
            array[offset2] = aux;
            ++i;
            ++offset1;
            ++offset2;
        }
    }

    public static <T> T[] toArray(T ... items) {
        return items;
    }

    public static Map<Object, Object> toMap(Object[] array) {
        if (array == null) {
            return null;
        }
        HashMap<Object, Object> map = new HashMap<Object, Object>((int)((double)array.length * 1.5));
        for (int i = 0; i < array.length; ++i) {
            Object[] entry;
            Object object = array[i];
            if (object instanceof Map.Entry) {
                entry = (Object[])object;
                map.put(entry.getKey(), entry.getValue());
                continue;
            }
            if (object instanceof Object[]) {
                entry = (Object[])object;
                if (entry.length < 2) {
                    throw new IllegalArgumentException("Array element " + i + ", '" + object + "', has a length less than 2");
                }
                map.put(entry[0], entry[1]);
                continue;
            }
            throw new IllegalArgumentException("Array element " + i + ", '" + object + "', is neither of type Map.Entry nor an Array");
        }
        return map;
    }

    public static Boolean[] toObject(boolean[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_BOOLEAN_OBJECT_ARRAY;
        }
        Boolean[] result = new Boolean[array.length];
        return ArrayUtils.setAll(result, (int i) -> array[i] ? Boolean.TRUE : Boolean.FALSE);
    }

    public static Byte[] toObject(byte[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_BYTE_OBJECT_ARRAY;
        }
        return ArrayUtils.setAll(new Byte[array.length], (int i) -> array[i]);
    }

    public static Character[] toObject(char[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_CHARACTER_OBJECT_ARRAY;
        }
        return ArrayUtils.setAll(new Character[array.length], (int i) -> Character.valueOf(array[i]));
    }

    public static Double[] toObject(double[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_DOUBLE_OBJECT_ARRAY;
        }
        return ArrayUtils.setAll(new Double[array.length], (int i) -> array[i]);
    }

    public static Float[] toObject(float[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_FLOAT_OBJECT_ARRAY;
        }
        return ArrayUtils.setAll(new Float[array.length], (int i) -> Float.valueOf(array[i]));
    }

    public static Integer[] toObject(int[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_INTEGER_OBJECT_ARRAY;
        }
        return ArrayUtils.setAll(new Integer[array.length], (int i) -> array[i]);
    }

    public static Long[] toObject(long[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_LONG_OBJECT_ARRAY;
        }
        return ArrayUtils.setAll(new Long[array.length], (int i) -> array[i]);
    }

    public static Short[] toObject(short[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_SHORT_OBJECT_ARRAY;
        }
        return ArrayUtils.setAll(new Short[array.length], (int i) -> array[i]);
    }

    public static boolean[] toPrimitive(Boolean[] array) {
        return ArrayUtils.toPrimitive(array, false);
    }

    public static boolean[] toPrimitive(Boolean[] array, boolean valueForNull) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_BOOLEAN_ARRAY;
        }
        boolean[] result = new boolean[array.length];
        for (int i = 0; i < array.length; ++i) {
            Boolean b = array[i];
            result[i] = b == null ? valueForNull : b;
        }
        return result;
    }

    public static byte[] toPrimitive(Byte[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static byte[] toPrimitive(Byte[] array, byte valueForNull) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; ++i) {
            Byte b = array[i];
            result[i] = b == null ? valueForNull : b;
        }
        return result;
    }

    public static char[] toPrimitive(Character[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] result = new char[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i].charValue();
        }
        return result;
    }

    public static char[] toPrimitive(Character[] array, char valueForNull) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        char[] result = new char[array.length];
        for (int i = 0; i < array.length; ++i) {
            Character b = array[i];
            result[i] = b == null ? valueForNull : b.charValue();
        }
        return result;
    }

    public static double[] toPrimitive(Double[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static double[] toPrimitive(Double[] array, double valueForNull) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        double[] result = new double[array.length];
        for (int i = 0; i < array.length; ++i) {
            Double b = array[i];
            result[i] = b == null ? valueForNull : b;
        }
        return result;
    }

    public static float[] toPrimitive(Float[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i].floatValue();
        }
        return result;
    }

    public static float[] toPrimitive(Float[] array, float valueForNull) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        }
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; ++i) {
            Float b = array[i];
            result[i] = b == null ? valueForNull : b.floatValue();
        }
        return result;
    }

    public static int[] toPrimitive(Integer[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_INT_ARRAY;
        }
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static int[] toPrimitive(Integer[] array, int valueForNull) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_INT_ARRAY;
        }
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; ++i) {
            Integer b = array[i];
            result[i] = b == null ? valueForNull : b;
        }
        return result;
    }

    public static long[] toPrimitive(Long[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static long[] toPrimitive(Long[] array, long valueForNull) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_LONG_ARRAY;
        }
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; ++i) {
            Long b = array[i];
            result[i] = b == null ? valueForNull : b;
        }
        return result;
    }

    public static Object toPrimitive(Object array) {
        if (array == null) {
            return null;
        }
        Class<?> ct = array.getClass().getComponentType();
        Class<?> pt = ClassUtils.wrapperToPrimitive(ct);
        if (Boolean.TYPE.equals(pt)) {
            return ArrayUtils.toPrimitive((Boolean[])array);
        }
        if (Character.TYPE.equals(pt)) {
            return ArrayUtils.toPrimitive((Character[])array);
        }
        if (Byte.TYPE.equals(pt)) {
            return ArrayUtils.toPrimitive((Byte[])array);
        }
        if (Integer.TYPE.equals(pt)) {
            return ArrayUtils.toPrimitive((Integer[])array);
        }
        if (Long.TYPE.equals(pt)) {
            return ArrayUtils.toPrimitive((Long[])array);
        }
        if (Short.TYPE.equals(pt)) {
            return ArrayUtils.toPrimitive((Short[])array);
        }
        if (Double.TYPE.equals(pt)) {
            return ArrayUtils.toPrimitive((Double[])array);
        }
        if (Float.TYPE.equals(pt)) {
            return ArrayUtils.toPrimitive((Float[])array);
        }
        return array;
    }

    public static short[] toPrimitive(Short[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        short[] result = new short[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i];
        }
        return result;
    }

    public static short[] toPrimitive(Short[] array, short valueForNull) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_SHORT_ARRAY;
        }
        short[] result = new short[array.length];
        for (int i = 0; i < array.length; ++i) {
            Short b = array[i];
            result[i] = b == null ? valueForNull : b;
        }
        return result;
    }

    public static String toString(Object array) {
        return ArrayUtils.toString(array, "{}");
    }

    public static String toString(Object array, String stringIfNull) {
        if (array == null) {
            return stringIfNull;
        }
        return new ToStringBuilder(array, ToStringStyle.SIMPLE_STYLE).append(array).toString();
    }

    public static String[] toStringArray(Object[] array) {
        if (array == null) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = array[i].toString();
        }
        return result;
    }

    public static String[] toStringArray(Object[] array, String valueForNullElements) {
        if (null == array) {
            return null;
        }
        if (array.length == 0) {
            return EMPTY_STRING_ARRAY;
        }
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[i] = Objects.toString(array[i], valueForNullElements);
        }
        return result;
    }

    @Deprecated
    public ArrayUtils() {
    }
}

