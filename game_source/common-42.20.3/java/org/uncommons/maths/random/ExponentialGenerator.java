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
public class ExponentialGenerator
implements NumberGenerator<Double> {
    private final NumberGenerator<Double> rate;
    private final Random rng;

    public ExponentialGenerator(NumberGenerator<Double> rate, Random rng) {
        this.rate = rate;
        this.rng = rng;
    }

    public ExponentialGenerator(double rate, Random rng) {
        this(new ConstantGenerator<Double>(rate), rng);
    }

    @Override
    public Double nextValue() {
        double u;
        while ((u = this.rng.nextDouble()) == 0.0) {
        }
        return -Math.log(u) / this.rate.nextValue();
    }
}

