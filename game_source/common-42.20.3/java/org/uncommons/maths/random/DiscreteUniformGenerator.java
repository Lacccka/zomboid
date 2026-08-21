/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.util.Random;
import org.uncommons.maths.number.NumberGenerator;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class DiscreteUniformGenerator
implements NumberGenerator<Integer> {
    private final Random rng;
    private final int range;
    private final int minimumValue;

    public DiscreteUniformGenerator(int minimumValue, int maximumValue, Random rng) {
        this.rng = rng;
        this.minimumValue = minimumValue;
        this.range = maximumValue - minimumValue + 1;
    }

    @Override
    public Integer nextValue() {
        return this.rng.nextInt(this.range) + this.minimumValue;
    }
}

