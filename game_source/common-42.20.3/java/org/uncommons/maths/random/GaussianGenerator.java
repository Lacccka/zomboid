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
public class GaussianGenerator
implements NumberGenerator<Double> {
    private final Random rng;
    private final NumberGenerator<Double> mean;
    private final NumberGenerator<Double> standardDeviation;

    public GaussianGenerator(NumberGenerator<Double> mean, NumberGenerator<Double> standardDeviation, Random rng) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.rng = rng;
    }

    public GaussianGenerator(double mean, double standardDeviation, Random rng) {
        this(new ConstantGenerator<Double>(mean), new ConstantGenerator<Double>(standardDeviation), rng);
    }

    @Override
    public Double nextValue() {
        return this.rng.nextGaussian() * this.standardDeviation.nextValue() + this.mean.nextValue();
    }
}

