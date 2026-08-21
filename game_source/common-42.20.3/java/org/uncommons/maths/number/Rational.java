/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.uncommons.maths.Maths;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class Rational
extends Number
implements Comparable<Rational> {
    public static final Rational ZERO = new Rational(0L);
    public static final Rational QUARTER = new Rational(1L, 4L);
    public static final Rational THIRD = new Rational(1L, 3L);
    public static final Rational HALF = new Rational(1L, 2L);
    public static final Rational TWO_THIRDS = new Rational(2L, 3L);
    public static final Rational THREE_QUARTERS = new Rational(3L, 4L);
    public static final Rational ONE = new Rational(1L);
    private final long numerator;
    private final long denominator;

    public Rational(long numerator, long denominator) {
        if (denominator < 1L) {
            throw new IllegalArgumentException("Denominator must be non-zero and positive.");
        }
        long gcd = Maths.greatestCommonDivisor(numerator, denominator);
        this.numerator = numerator / gcd;
        this.denominator = denominator / gcd;
    }

    public Rational(long value) {
        this(value, 1L);
    }

    public Rational(BigDecimal value) {
        BigDecimal trimmedValue = value.stripTrailingZeros();
        BigInteger denominator = BigInteger.TEN.pow(trimmedValue.scale());
        BigInteger numerator = trimmedValue.unscaledValue();
        BigInteger gcd = numerator.gcd(denominator);
        this.numerator = numerator.divide(gcd).longValue();
        this.denominator = denominator.divide(gcd).longValue();
    }

    public long getNumerator() {
        return this.numerator;
    }

    public long getDenominator() {
        return this.denominator;
    }

    public Rational add(Rational value) {
        if (this.denominator == value.getDenominator()) {
            return new Rational(this.numerator + value.getNumerator(), this.denominator);
        }
        return new Rational(this.numerator * value.getDenominator() + value.getNumerator() * this.denominator, this.denominator * value.getDenominator());
    }

    public Rational subtract(Rational value) {
        if (this.denominator == value.getDenominator()) {
            return new Rational(this.numerator - value.getNumerator(), this.denominator);
        }
        return new Rational(this.numerator * value.getDenominator() - value.getNumerator() * this.denominator, this.denominator * value.getDenominator());
    }

    public Rational multiply(Rational value) {
        return new Rational(this.numerator * value.getNumerator(), this.denominator * value.getDenominator());
    }

    public Rational divide(Rational value) {
        return new Rational(this.numerator * value.getDenominator(), this.denominator * value.getNumerator());
    }

    @Override
    public int intValue() {
        return (int)this.longValue();
    }

    @Override
    public long longValue() {
        return (long)this.doubleValue();
    }

    @Override
    public float floatValue() {
        return (float)this.doubleValue();
    }

    @Override
    public double doubleValue() {
        return (double)this.numerator / (double)this.denominator;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        Rational rational = (Rational)other;
        return this.denominator == rational.getDenominator() && this.numerator == rational.getNumerator();
    }

    public int hashCode() {
        int result = (int)(this.numerator ^ this.numerator >>> 32);
        result = 31 * result + (int)(this.denominator ^ this.denominator >>> 32);
        return result;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.numerator);
        if (this.denominator != 1L) {
            buffer.append('/');
            buffer.append(this.denominator);
        }
        return buffer.toString();
    }

    @Override
    public int compareTo(Rational other) {
        if (this.denominator == other.getDenominator()) {
            return Long.valueOf(this.numerator).compareTo(other.getNumerator());
        }
        Long adjustedNumerator = this.numerator * other.getDenominator();
        Long otherAdjustedNumerator = other.getNumerator() * this.denominator;
        return adjustedNumerator.compareTo(otherAdjustedNumerator);
    }
}

