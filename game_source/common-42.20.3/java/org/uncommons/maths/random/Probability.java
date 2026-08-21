/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.util.Random;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public final class Probability
extends Number
implements Comparable<Probability> {
    public static final Probability ZERO = new Probability(0.0);
    public static final Probability EVENS = new Probability(0.5);
    public static final Probability ONE = new Probability(1.0);
    private final double probability;

    public Probability(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Probability must be in the range 0..1 inclusive.");
        }
        this.probability = probability;
    }

    public boolean nextEvent(Random rng) {
        return this.probability == 1.0 || rng.nextDouble() < this.probability;
    }

    public Probability getComplement() {
        return new Probability(1.0 - this.probability);
    }

    @Override
    public int intValue() {
        if (this.probability % 1.0 == 0.0) {
            return (int)this.probability;
        }
        throw new ArithmeticException("Cannot convert probability to integer due to loss of precision.");
    }

    @Override
    public long longValue() {
        return this.intValue();
    }

    @Override
    public float floatValue() {
        return (float)this.probability;
    }

    @Override
    public double doubleValue() {
        return this.probability;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        Probability that = (Probability)other;
        return Double.compare(that.probability, this.probability) == 0;
    }

    public int hashCode() {
        long temp = this.probability == 0.0 ? 0L : Double.doubleToLongBits(this.probability);
        return (int)(temp ^ temp >>> 32);
    }

    @Override
    public int compareTo(Probability other) {
        return Double.compare(this.probability, other.probability);
    }

    public String toString() {
        return String.valueOf(this.probability);
    }
}

