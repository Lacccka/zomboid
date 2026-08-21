/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.CloneFailedException;
import org.apache.commons.lang3.function.Suppliers;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.stream.Streams;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.time.DurationUtils;

public class ObjectUtils {
    private static final char AT_SIGN = '@';
    public static final Null NULL = new Null();

    public static boolean allNotNull(Object ... values2) {
        return values2 != null && Stream.of(values2).noneMatch(Objects::isNull);
    }

    public static boolean allNull(Object ... values2) {
        return !ObjectUtils.anyNotNull(values2);
    }

    public static boolean anyNotNull(Object ... values2) {
        return ObjectUtils.firstNonNull(values2) != null;
    }

    public static boolean anyNull(Object ... values2) {
        return !ObjectUtils.allNotNull(values2);
    }

    public static <T> T clone(T obj) {
        if (obj instanceof Cloneable) {
            Object result;
            Class<?> objClass = obj.getClass();
            if (ObjectUtils.isArray(obj)) {
                Class<?> componentType = objClass.getComponentType();
                if (componentType.isPrimitive()) {
                    int length = Array.getLength(obj);
                    result = Array.newInstance(componentType, length);
                    while (length-- > 0) {
                        Array.set(result, length, Array.get(obj, length));
                    }
                } else {
                    result = ((Object[])obj).clone();
                }
            } else {
                try {
                    result = objClass.getMethod("clone", new Class[0]).invoke(obj, new Object[0]);
                }
                catch (ReflectiveOperationException e) {
                    throw new CloneFailedException("Exception cloning Cloneable type " + objClass.getName(), e);
                }
            }
            return (T)result;
        }
        return null;
    }

    public static <T> T cloneIfPossible(T obj) {
        T clone = ObjectUtils.clone(obj);
        return clone == null ? obj : clone;
    }

    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
        return ObjectUtils.compare(c1, c2, false);
    }

    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        }
        if (c1 == null) {
            return nullGreater ? 1 : -1;
        }
        if (c2 == null) {
            return nullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    public static boolean CONST(boolean v) {
        return v;
    }

    public static byte CONST(byte v) {
        return v;
    }

    public static char CONST(char v) {
        return v;
    }

    public static double CONST(double v) {
        return v;
    }

    public static float CONST(float v) {
        return v;
    }

    public static int CONST(int v) {
        return v;
    }

    public static long CONST(long v) {
        return v;
    }

    public static short CONST(short v) {
        return v;
    }

    public static <T> T CONST(T v) {
        return v;
    }

    public static byte CONST_BYTE(int v) {
        if (v < -128 || v > 127) {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -128 and 127: [" + v + "]");
        }
        return (byte)v;
    }

    public static short CONST_SHORT(int v) {
        if (v < Short.MIN_VALUE || v > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Supplied value must be a valid byte literal between -32768 and 32767: [" + v + "]");
        }
        return (short)v;
    }

    public static <T> T defaultIfNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }

    @Deprecated
    public static boolean equals(Object object1, Object object2) {
        return Objects.equals(object1, object2);
    }

    @SafeVarargs
    public static <T> T firstNonNull(T ... values2) {
        return Streams.of(values2).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static <T> Class<T> getClass(T object) {
        return object == null ? null : object.getClass();
    }

    @SafeVarargs
    public static <T> T getFirstNonNull(Supplier<T> ... suppliers) {
        return Streams.of(suppliers).filter(Objects::nonNull).map(Supplier::get).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static <T> T getIfNull(T object, Supplier<T> defaultSupplier) {
        return object != null ? object : Suppliers.get(defaultSupplier);
    }

    @Deprecated
    public static int hashCode(Object obj) {
        return Objects.hashCode(obj);
    }

    public static String hashCodeHex(Object object) {
        return Integer.toHexString(Objects.hashCode(object));
    }

    @Deprecated
    public static int hashCodeMulti(Object ... objects) {
        int hash = 1;
        if (objects != null) {
            for (Object object : objects) {
                int tmpHash = Objects.hashCode(object);
                hash = hash * 31 + tmpHash;
            }
        }
        return hash;
    }

    public static String identityHashCodeHex(Object object) {
        return Integer.toHexString(System.identityHashCode(object));
    }

    public static void identityToString(Appendable appendable, Object object) throws IOException {
        Objects.requireNonNull(object, "object");
        appendable.append(object.getClass().getName()).append('@').append(ObjectUtils.identityHashCodeHex(object));
    }

    public static String identityToString(Object object) {
        if (object == null) {
            return null;
        }
        String name = object.getClass().getName();
        String hexString = ObjectUtils.identityHashCodeHex(object);
        StringBuilder builder = new StringBuilder(name.length() + 1 + hexString.length());
        builder.append(name).append('@').append(hexString);
        return builder.toString();
    }

    @Deprecated
    public static void identityToString(StrBuilder builder, Object object) {
        Objects.requireNonNull(object, "object");
        String name = object.getClass().getName();
        String hexString = ObjectUtils.identityHashCodeHex(object);
        builder.ensureCapacity(builder.length() + name.length() + 1 + hexString.length());
        builder.append(name).append('@').append(hexString);
    }

    public static void identityToString(StringBuffer buffer, Object object) {
        Objects.requireNonNull(object, "object");
        String name = object.getClass().getName();
        String hexString = ObjectUtils.identityHashCodeHex(object);
        buffer.ensureCapacity(buffer.length() + name.length() + 1 + hexString.length());
        buffer.append(name).append('@').append(hexString);
    }

    public static void identityToString(StringBuilder builder, Object object) {
        Objects.requireNonNull(object, "object");
        String name = object.getClass().getName();
        String hexString = ObjectUtils.identityHashCodeHex(object);
        builder.ensureCapacity(builder.length() + name.length() + 1 + hexString.length());
        builder.append(name).append('@').append(hexString);
    }

    public static boolean isArray(Object object) {
        return object != null && object.getClass().isArray();
    }

    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }
        if (object instanceof CharSequence) {
            return ((CharSequence)object).length() == 0;
        }
        if (ObjectUtils.isArray(object)) {
            return Array.getLength(object) == 0;
        }
        if (object instanceof Collection) {
            return ((Collection)object).isEmpty();
        }
        if (object instanceof Map) {
            return ((Map)object).isEmpty();
        }
        if (object instanceof Optional) {
            return !((Optional)object).isPresent();
        }
        return false;
    }

    public static boolean isNotEmpty(Object object) {
        return !ObjectUtils.isEmpty(object);
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> T max(T ... values2) {
        T result = null;
        if (values2 != null) {
            for (T value : values2) {
                if (ObjectUtils.compare(value, result, false) <= 0) continue;
                result = value;
            }
        }
        return result;
    }

    @SafeVarargs
    public static <T> T median(Comparator<T> comparator, T ... items) {
        Validate.notEmpty(items, "null/empty items", new Object[0]);
        Validate.noNullElements(items);
        Objects.requireNonNull(comparator, "comparator");
        TreeSet<T> treeSet = new TreeSet<T>(comparator);
        Collections.addAll(treeSet, items);
        return (T)treeSet.toArray()[(treeSet.size() - 1) / 2];
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> T median(T ... items) {
        Validate.notEmpty(items);
        Validate.noNullElements(items);
        TreeSet sort = new TreeSet();
        Collections.addAll(sort, items);
        return (T)((Comparable)sort.toArray()[(sort.size() - 1) / 2]);
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> T min(T ... values2) {
        T result = null;
        if (values2 != null) {
            for (T value : values2) {
                if (ObjectUtils.compare(value, result, true) >= 0) continue;
                result = value;
            }
        }
        return result;
    }

    @SafeVarargs
    public static <T> T mode(T ... items) {
        if (ArrayUtils.isNotEmpty(items)) {
            HashMap<T, MutableInt> occurrences = new HashMap<T, MutableInt>(items.length);
            for (T t : items) {
                MutableInt count = (MutableInt)occurrences.get(t);
                if (count == null) {
                    occurrences.put(t, new MutableInt(1));
                    continue;
                }
                count.increment();
            }
            Object result = null;
            int max = 0;
            for (Map.Entry e : occurrences.entrySet()) {
                int cmp = ((MutableInt)e.getValue()).intValue();
                if (cmp == max) {
                    result = null;
                    continue;
                }
                if (cmp <= max) continue;
                max = cmp;
                result = e.getKey();
            }
            return result;
        }
        return null;
    }

    public static boolean notEqual(Object object1, Object object2) {
        return !Objects.equals(object1, object2);
    }

    public static <T> T requireNonEmpty(T obj) {
        return ObjectUtils.requireNonEmpty(obj, "object");
    }

    public static <T> T requireNonEmpty(T obj, String message) {
        Objects.requireNonNull(obj, message);
        if (ObjectUtils.isEmpty(obj)) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    @Deprecated
    public static String toString(Object obj) {
        return Objects.toString(obj, "");
    }

    @Deprecated
    public static String toString(Object obj, String nullStr) {
        return Objects.toString(obj, nullStr);
    }

    public static String toString(Supplier<Object> obj, Supplier<String> supplier) {
        return obj == null ? Suppliers.get(supplier) : ObjectUtils.toString(obj.get(), supplier);
    }

    public static <T> String toString(T obj, Supplier<String> supplier) {
        return obj == null ? Suppliers.get(supplier) : obj.toString();
    }

    public static void wait(Object obj, Duration duration) throws InterruptedException {
        DurationUtils.accept(obj::wait, DurationUtils.zeroIfNull(duration));
    }

    @Deprecated
    public ObjectUtils() {
    }

    public static class Null
    implements Serializable {
        private static final long serialVersionUID = 7092611880189329093L;

        Null() {
        }

        private Object readResolve() {
            return NULL;
        }
    }
}

