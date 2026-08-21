/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.collection;

import io.vavr.collection.Array;
import java.io.Serializable;
import java.util.Iterator;

interface ArrayType<T> {
    public static <T> ArrayType<T> obj() {
        return ObjectArrayType.INSTANCE;
    }

    public Class<T> type();

    public int lengthOf(Object var1);

    public T getAt(Object var1, int var2);

    public Object empty();

    public void setAt(Object var1, int var2, T var3) throws ClassCastException;

    public Object copy(Object var1, int var2, int var3, int var4, int var5);

    public static <T> ArrayType<T> of(Object array) {
        return ArrayType.of(array.getClass().getComponentType());
    }

    public static <T> ArrayType<T> of(Class<T> type) {
        return !type.isPrimitive() ? ArrayType.obj() : ArrayType.ofPrimitive(type);
    }

    public static <T> ArrayType<T> ofPrimitive(Class<T> type) {
        if (Boolean.TYPE == type) {
            return BooleanArrayType.INSTANCE;
        }
        if (Byte.TYPE == type) {
            return ByteArrayType.INSTANCE;
        }
        if (Character.TYPE == type) {
            return CharArrayType.INSTANCE;
        }
        if (Double.TYPE == type) {
            return DoubleArrayType.INSTANCE;
        }
        if (Float.TYPE == type) {
            return FloatArrayType.INSTANCE;
        }
        if (Integer.TYPE == type) {
            return IntArrayType.INSTANCE;
        }
        if (Long.TYPE == type) {
            return LongArrayType.INSTANCE;
        }
        if (Short.TYPE == type) {
            return ShortArrayType.INSTANCE;
        }
        throw new IllegalArgumentException(String.valueOf(type));
    }

    default public Object newInstance(int length) {
        return this.copy(this.empty(), length);
    }

    default public Object copyRange(Object array, int from, int to) {
        int length = to - from;
        return this.copy(array, length, from, 0, length);
    }

    default public Object grouped(Object array, int groupSize) {
        int arrayLength = this.lengthOf(array);
        Object results = ArrayType.obj().newInstance(1 + (arrayLength - 1) / groupSize);
        ArrayType.obj().setAt(results, 0, this.copyRange(array, 0, groupSize));
        int start = groupSize;
        int i = 1;
        while (start < arrayLength) {
            int nextLength = Math.min(groupSize, arrayLength - i * groupSize);
            ArrayType.obj().setAt(results, i, this.copyRange(array, start, start + nextLength));
            start += nextLength;
            ++i;
        }
        return results;
    }

    default public Object copyUpdate(Object array, int index, T element) {
        Object copy = this.copy(array, index + 1);
        this.setAt(copy, index, element);
        return copy;
    }

    default public Object copy(Object array, int minLength) {
        int arrayLength = this.lengthOf(array);
        int length = Math.max(arrayLength, minLength);
        return this.copy(array, length, 0, 0, arrayLength);
    }

    default public Object copyDrop(Object array, int index) {
        int length = this.lengthOf(array);
        return this.copy(array, length, index, index, length - index);
    }

    default public Object copyTake(Object array, int lastIndex) {
        return this.copyRange(array, 0, lastIndex + 1);
    }

    default public Object asArray(T element) {
        Object result = this.newInstance(1);
        this.setAt(result, 0, element);
        return result;
    }

    public static Object[] asArray(Iterator<?> it, int length) {
        Object[] array = new Object[length];
        for (int i = 0; i < length; ++i) {
            array[i] = it.next();
        }
        return array;
    }

    public static <T> T asPrimitives(Class<?> primitiveClass, Iterable<?> values2) {
        Object[] array = Array.ofAll(values2).toJavaArray();
        ArrayType<?> type = ArrayType.of(primitiveClass);
        Object results = type.newInstance(array.length);
        for (int i = 0; i < array.length; ++i) {
            type.setAt(results, i, array[i]);
        }
        return (T)results;
    }

    public static final class ObjectArrayType
    implements ArrayType<Object>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final ObjectArrayType INSTANCE = new ObjectArrayType();
        static final Object[] EMPTY = new Object[0];

        private static Object[] cast(Object array) {
            return (Object[])array;
        }

        @Override
        public Class<Object> type() {
            return Object.class;
        }

        public Object[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? ObjectArrayType.cast(array).length : 0;
        }

        @Override
        public Object getAt(Object array, int index) {
            return ObjectArrayType.cast(array)[index];
        }

        @Override
        public void setAt(Object array, int index, Object value) {
            ObjectArrayType.cast((Object)array)[index] = value;
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? ObjectArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new Object[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            Object[] result = new Object[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }

    public static final class ShortArrayType
    implements ArrayType<Short>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final ShortArrayType INSTANCE = new ShortArrayType();
        static final short[] EMPTY = new short[0];

        private static short[] cast(Object array) {
            return (short[])array;
        }

        @Override
        public Class<Short> type() {
            return Short.TYPE;
        }

        public short[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? ShortArrayType.cast(array).length : 0;
        }

        @Override
        public Short getAt(Object array, int index) {
            return ShortArrayType.cast(array)[index];
        }

        @Override
        public void setAt(Object array, int index, Short value) throws ClassCastException {
            if (value == null) {
                throw new ClassCastException();
            }
            ShortArrayType.cast((Object)array)[index] = value;
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? (Object)ShortArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new short[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            short[] result = new short[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }

    public static final class LongArrayType
    implements ArrayType<Long>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final LongArrayType INSTANCE = new LongArrayType();
        static final long[] EMPTY = new long[0];

        private static long[] cast(Object array) {
            return (long[])array;
        }

        @Override
        public Class<Long> type() {
            return Long.TYPE;
        }

        public long[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? LongArrayType.cast(array).length : 0;
        }

        @Override
        public Long getAt(Object array, int index) {
            return LongArrayType.cast(array)[index];
        }

        @Override
        public void setAt(Object array, int index, Long value) throws ClassCastException {
            if (value == null) {
                throw new ClassCastException();
            }
            LongArrayType.cast((Object)array)[index] = value;
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? (Object)LongArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new long[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            long[] result = new long[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }

    public static final class IntArrayType
    implements ArrayType<Integer>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final IntArrayType INSTANCE = new IntArrayType();
        static final int[] EMPTY = new int[0];

        private static int[] cast(Object array) {
            return (int[])array;
        }

        @Override
        public Class<Integer> type() {
            return Integer.TYPE;
        }

        public int[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? IntArrayType.cast(array).length : 0;
        }

        @Override
        public Integer getAt(Object array, int index) {
            return IntArrayType.cast(array)[index];
        }

        @Override
        public void setAt(Object array, int index, Integer value) throws ClassCastException {
            if (value == null) {
                throw new ClassCastException();
            }
            IntArrayType.cast((Object)array)[index] = value;
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? (Object)IntArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new int[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            int[] result = new int[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }

    public static final class FloatArrayType
    implements ArrayType<Float>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final FloatArrayType INSTANCE = new FloatArrayType();
        static final float[] EMPTY = new float[0];

        private static float[] cast(Object array) {
            return (float[])array;
        }

        @Override
        public Class<Float> type() {
            return Float.TYPE;
        }

        public float[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? FloatArrayType.cast(array).length : 0;
        }

        @Override
        public Float getAt(Object array, int index) {
            return Float.valueOf(FloatArrayType.cast(array)[index]);
        }

        @Override
        public void setAt(Object array, int index, Float value) throws ClassCastException {
            if (value == null) {
                throw new ClassCastException();
            }
            FloatArrayType.cast((Object)array)[index] = value.floatValue();
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? (Object)FloatArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new float[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            float[] result = new float[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }

    public static final class DoubleArrayType
    implements ArrayType<Double>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final DoubleArrayType INSTANCE = new DoubleArrayType();
        static final double[] EMPTY = new double[0];

        private static double[] cast(Object array) {
            return (double[])array;
        }

        @Override
        public Class<Double> type() {
            return Double.TYPE;
        }

        public double[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? DoubleArrayType.cast(array).length : 0;
        }

        @Override
        public Double getAt(Object array, int index) {
            return DoubleArrayType.cast(array)[index];
        }

        @Override
        public void setAt(Object array, int index, Double value) throws ClassCastException {
            if (value == null) {
                throw new ClassCastException();
            }
            DoubleArrayType.cast((Object)array)[index] = value;
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? (Object)DoubleArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new double[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            double[] result = new double[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }

    public static final class CharArrayType
    implements ArrayType<Character>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final CharArrayType INSTANCE = new CharArrayType();
        static final char[] EMPTY = new char[0];

        private static char[] cast(Object array) {
            return (char[])array;
        }

        @Override
        public Class<Character> type() {
            return Character.TYPE;
        }

        public char[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? CharArrayType.cast(array).length : 0;
        }

        @Override
        public Character getAt(Object array, int index) {
            return Character.valueOf(CharArrayType.cast(array)[index]);
        }

        @Override
        public void setAt(Object array, int index, Character value) throws ClassCastException {
            if (value == null) {
                throw new ClassCastException();
            }
            CharArrayType.cast((Object)array)[index] = value.charValue();
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? (Object)CharArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new char[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            char[] result = new char[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }

    public static final class ByteArrayType
    implements ArrayType<Byte>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final ByteArrayType INSTANCE = new ByteArrayType();
        static final byte[] EMPTY = new byte[0];

        private static byte[] cast(Object array) {
            return (byte[])array;
        }

        @Override
        public Class<Byte> type() {
            return Byte.TYPE;
        }

        public byte[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? ByteArrayType.cast(array).length : 0;
        }

        @Override
        public Byte getAt(Object array, int index) {
            return ByteArrayType.cast(array)[index];
        }

        @Override
        public void setAt(Object array, int index, Byte value) throws ClassCastException {
            if (value == null) {
                throw new ClassCastException();
            }
            ByteArrayType.cast((Object)array)[index] = value;
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? (Object)ByteArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new byte[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            byte[] result = new byte[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }

    public static final class BooleanArrayType
    implements ArrayType<Boolean>,
    Serializable {
        private static final long serialVersionUID = 1L;
        static final BooleanArrayType INSTANCE = new BooleanArrayType();
        static final boolean[] EMPTY = new boolean[0];

        private static boolean[] cast(Object array) {
            return (boolean[])array;
        }

        @Override
        public Class<Boolean> type() {
            return Boolean.TYPE;
        }

        public boolean[] empty() {
            return EMPTY;
        }

        @Override
        public int lengthOf(Object array) {
            return array != null ? BooleanArrayType.cast(array).length : 0;
        }

        @Override
        public Boolean getAt(Object array, int index) {
            return BooleanArrayType.cast(array)[index];
        }

        @Override
        public void setAt(Object array, int index, Boolean value) throws ClassCastException {
            if (value == null) {
                throw new ClassCastException();
            }
            BooleanArrayType.cast((Object)array)[index] = value;
        }

        @Override
        public Object copy(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            return size > 0 ? (Object)BooleanArrayType.copyNonEmpty(array, arraySize, sourceFrom, destinationFrom, size) : new boolean[arraySize];
        }

        private static Object copyNonEmpty(Object array, int arraySize, int sourceFrom, int destinationFrom, int size) {
            boolean[] result = new boolean[arraySize];
            System.arraycopy(array, sourceFrom, result, destinationFrom, size);
            return result;
        }
    }
}

