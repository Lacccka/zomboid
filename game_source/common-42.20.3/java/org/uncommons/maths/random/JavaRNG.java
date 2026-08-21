/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.util.Random;
import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.DefaultSeedGenerator;
import org.uncommons.maths.random.RepeatableRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

public class JavaRNG
extends Random
implements RepeatableRNG {
    private static final int SEED_SIZE_BYTES = 8;
    private final byte[] seed;

    public JavaRNG() {
        this(DefaultSeedGenerator.getInstance().generateSeed(8));
    }

    public JavaRNG(SeedGenerator seedGenerator) throws SeedException {
        this(seedGenerator.generateSeed(8));
    }

    public JavaRNG(byte[] seed) {
        super(JavaRNG.createLongSeed(seed));
        this.seed = (byte[])seed.clone();
    }

    private static long createLongSeed(byte[] seed) {
        if (seed == null || seed.length != 8) {
            throw new IllegalArgumentException("Java RNG requires a 64-bit (8-byte) seed.");
        }
        return BinaryUtils.convertBytesToLong(seed, 0);
    }

    public byte[] getSeed() {
        return (byte[])this.seed.clone();
    }
}

