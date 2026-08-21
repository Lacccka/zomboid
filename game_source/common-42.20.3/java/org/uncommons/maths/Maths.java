/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class Maths {
    private static final int MAX_LONG_FACTORIAL = 20;
    private static final int CACHE_SIZE = 256;
    private static final ConcurrentMap<Integer, BigInteger> BIG_FACTORIALS = new ConcurrentHashMap<Integer, BigInteger>();

    private Maths() {
    }

    public static long factorial(int n) {
        if (n < 0 || n > 20) {
            throw new IllegalArgumentException("Argument must be in the range 0 - 20.");
        }
        long factorial = 1L;
        for (int i = n; i > 1; --i) {
            factorial *= (long)i;
        }
        return factorial;
    }

    public static BigInteger bigFactorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Argument must greater than or equal to zero.");
        }
        BigInteger factorial = null;
        if (n < 256) {
            factorial = (BigInteger)BIG_FACTORIALS.get(n);
        }
        if (factorial == null) {
            factorial = BigInteger.ONE;
            for (int i = n; i > 1; --i) {
                factorial = factorial.multiply(BigInteger.valueOf(i));
            }
            if (n < 256) {
                BIG_FACTORIALS.putIfAbsent(n, factorial);
            }
        }
        return factorial;
    }

    public static long raiseToPower(int value, int power) {
        if (power < 0) {
            throw new IllegalArgumentException("This method does not support negative powers.");
        }
        long result = 1L;
        for (int i = 0; i < power; ++i) {
            result *= (long)value;
        }
        return result;
    }

    public static double log(double base, double arg) {
        return Math.log(arg) / Math.log(base);
    }

    public static boolean approxEquals(double value1, double value2, double tolerance) {
        if (tolerance < 0.0 || tolerance > 1.0) {
            throw new IllegalArgumentException("Tolerance must be between 0 and 1.");
        }
        return Math.abs(value1 - value2) <= value1 * tolerance;
    }

    public static int restrictRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static long restrictRange(long value, long min, long max) {
        return Math.min(Math.max(value, min), max);
    }

    public static double restrictRange(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static long greatestCommonDivisor(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0L) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}

