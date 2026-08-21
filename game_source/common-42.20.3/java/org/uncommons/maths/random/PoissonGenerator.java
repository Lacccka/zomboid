/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.util.Random;
import org.uncommons.maths.number.ConstantGenerator;
import org.uncommons.maths.number.NumberGenerator;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class PoissonGenerator
implements NumberGenerator<Integer> {
    private final Random rng;
    private final NumberGenerator<Double> mean;

    public PoissonGenerator(NumberGenerator<Double> mean, Random rng) {
        this.mean = mean;
        this.rng = rng;
    }

    public PoissonGenerator(double mean, Random rng) {
        this(new ConstantGenerator<Double>(mean), rng);
        if (mean <= 0.0) {
            throw new IllegalArgumentException("Mean must be a positive value.");
        }
    }

    @Override
    public Integer nextValue() {
        int x = 0;
        double t = 0.0;
        while (!((t -= Math.log(this.rng.nextDouble()) / this.mean.nextValue()) > 1.0)) {
            ++x;
        }
        return x;
    }
}

