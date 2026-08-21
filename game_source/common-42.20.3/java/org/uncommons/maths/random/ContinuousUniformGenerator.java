/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.util.Random;
import org.uncommons.maths.number.NumberGenerator;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ContinuousUniformGenerator
implements NumberGenerator<Double> {
    private final Random rng;
    private final double range;
    private final double minimumValue;

    public ContinuousUniformGenerator(double minimumValue, double maximumValue, Random rng) {
        this.rng = rng;
        this.minimumValue = minimumValue;
        this.range = maximumValue - minimumValue;
    }

    @Override
    public Double nextValue() {
        return this.rng.nextDouble() * this.range + this.minimumValue;
    }
}

