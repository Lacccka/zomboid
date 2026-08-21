/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.util.Random;
import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.binary.BitString;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class BinomialGenerator
implements NumberGenerator<Integer> {
    private final Random rng;
    private final NumberGenerator<Integer> n;
    private final NumberGenerator<Double> p;
    private transient BitString pBits;
    private transient double lastP;

    public BinomialGenerator(NumberGenerator<Integer> n, NumberGenerator<Double> p, Random rng) {
        this.n = n;
        this.p = p;
        this.rng = rng;
    }

    public BinomialGenerator(int n, double p, Random rng) {
        this(new ConstantGenerator<Integer>(n), new ConstantGenerator<Double>(p), rng);
        if (n <= 0) {
            throw new IllegalArgumentException("n must be a positive integer.");
        }
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("p must be between 0 and 1.");
        }
    }

    @Override
    public Integer nextValue() {
        double newP = this.p.nextValue();
        if (this.pBits == null || newP != this.lastP) {
            this.lastP = newP;
            this.pBits = BinaryUtils.convertDoubleToFixedPointBits(newP);
        }
        int trials = this.n.nextValue();
        int totalSuccesses = 0;
        for (int pIndex = this.pBits.getLength() - 1; trials > 0 && pIndex >= 0; --pIndex) {
            int successes = this.binomialWithEvenProbability(trials);
            trials -= successes;
            if (!this.pBits.getBit(pIndex)) continue;
            totalSuccesses += successes;
        }
        return totalSuccesses;
    }

    private int binomialWithEvenProbability(int n) {
        BitString bits = new BitString(n, this.rng);
        return bits.countSetBits();
    }
}

