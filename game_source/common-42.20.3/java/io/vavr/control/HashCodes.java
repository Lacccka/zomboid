/*
 * Decompiled with CFR 0.152.
 */
package io.vavr.control;

import java.util.Objects;

@Deprecated
public interface HashCodes {
    public static int hash(int value) {
        return Integer.hashCode(value);
    }

    public static int hash(int v1, int v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(long value) {
        return Long.hashCode(value);
    }

    public static int hash(byte value) {
        return Byte.hashCode(value);
    }

    public static int hash(short value) {
        return Short.hashCode(value);
    }

    public static int hash(char value) {
        return Character.hashCode(value);
    }

    public static int hash(boolean value) {
        return Boolean.hashCode(value);
    }

    public static int hash(float value) {
        return Float.hashCode(value);
    }

    public static int hash(double value) {
        return Double.hashCode(value);
    }

    public static int hash(Object value) {
        return Objects.hashCode(value);
    }

    public static int hash(int v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(long v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(byte v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(short v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(char v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(boolean v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(float v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(double v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(Object v1, Object v2) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        return result;
    }

    public static int hash(Object v1, Object v2, Object v3) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        result = 31 * result + HashCodes.hash(v3);
        return result;
    }

    public static int hash(Object v1, Object v2, Object v3, Object v4) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        result = 31 * result + HashCodes.hash(v3);
        result = 31 * result + HashCodes.hash(v4);
        return result;
    }

    public static int hash(Object v1, Object v2, Object v3, Object v4, Object v5) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        result = 31 * result + HashCodes.hash(v3);
        result = 31 * result + HashCodes.hash(v4);
        result = 31 * result + HashCodes.hash(v5);
        return result;
    }

    public static int hash(Object v1, Object v2, Object v3, Object v4, Object v5, Object v6) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        result = 31 * result + HashCodes.hash(v3);
        result = 31 * result + HashCodes.hash(v4);
        result = 31 * result + HashCodes.hash(v5);
        result = 31 * result + HashCodes.hash(v6);
        return result;
    }

    public static int hash(Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        result = 31 * result + HashCodes.hash(v3);
        result = 31 * result + HashCodes.hash(v4);
        result = 31 * result + HashCodes.hash(v5);
        result = 31 * result + HashCodes.hash(v6);
        result = 31 * result + HashCodes.hash(v7);
        return result;
    }

    public static int hash(Object v1, Object v2, Object v3, Object v4, Object v5, Object v6, Object v7, Object v8) {
        int result = 1;
        result = 31 * result + HashCodes.hash(v1);
        result = 31 * result + HashCodes.hash(v2);
        result = 31 * result + HashCodes.hash(v3);
        result = 31 * result + HashCodes.hash(v4);
        result = 31 * result + HashCodes.hash(v5);
        result = 31 * result + HashCodes.hash(v6);
        result = 31 * result + HashCodes.hash(v7);
        result = 31 * result + HashCodes.hash(v8);
        return result;
    }
}

