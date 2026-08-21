/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.DefaultSeedGenerator;
import org.uncommons.maths.random.RepeatableRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

public class MersenneTwisterRNG
extends Random
implements RepeatableRNG {
    private static final int SEED_SIZE_BYTES = 16;
    private static final int N = 624;
    private static final int M = 397;
    private static final int[] MAG01 = new int[]{0, -1727483681};
    private static final int UPPER_MASK = Integer.MIN_VALUE;
    private static final int LOWER_MASK = Integer.MAX_VALUE;
    private static final int BOOTSTRAP_SEED = 19650218;
    private static final int BOOTSTRAP_FACTOR = 1812433253;
    private static final int SEED_FACTOR1 = 1664525;
    private static final int SEED_FACTOR2 = 1566083941;
    private static final int GENERATE_MASK1 = -1658038656;
    private static final int GENERATE_MASK2 = -272236544;
    private final byte[] seed;
    private final ReentrantLock lock = new ReentrantLock();
    private final int[] mt = new int[624];
    private int mtIndex = 0;

    public MersenneTwisterRNG() {
        this(DefaultSeedGenerator.getInstance().generateSeed(16));
    }

    public MersenneTwisterRNG(SeedGenerator seedGenerator) throws SeedException {
        this(seedGenerator.generateSeed(16));
    }

    public MersenneTwisterRNG(byte[] seed) {
        int k;
        if (seed == null || seed.length != 16) {
            throw new IllegalArgumentException("Mersenne Twister RNG requires a 128-bit (16-byte) seed.");
        }
        this.seed = (byte[])seed.clone();
        int[] seedInts = BinaryUtils.convertBytesToInts(this.seed);
        this.mt[0] = 19650218;
        this.mtIndex = 1;
        while (this.mtIndex < 624) {
            this.mt[this.mtIndex] = 1812433253 * (this.mt[this.mtIndex - 1] ^ this.mt[this.mtIndex - 1] >>> 30) + this.mtIndex;
            ++this.mtIndex;
        }
        int i = 1;
        int j = 0;
        for (k = Math.max(624, seedInts.length); k > 0; --k) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * 1664525) + seedInts[j] + j;
            ++j;
            if (++i >= 624) {
                this.mt[0] = this.mt[623];
                i = 1;
            }
            if (j < seedInts.length) continue;
            j = 0;
        }
        for (k = 623; k > 0; --k) {
            this.mt[i] = (this.mt[i] ^ (this.mt[i - 1] ^ this.mt[i - 1] >>> 30) * 1566083941) - i;
            if (++i < 624) continue;
            this.mt[0] = this.mt[623];
            i = 1;
        }
        this.mt[0] = Integer.MIN_VALUE;
    }

    public byte[] getSeed() {
        return (byte[])this.seed.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final int next(int bits) {
        int y;
        try {
            this.lock.lock();
            if (this.mtIndex >= 624) {
                int kk;
                for (kk = 0; kk < 227; ++kk) {
                    y = this.mt[kk] & Integer.MIN_VALUE | this.mt[kk + 1] & Integer.MAX_VALUE;
                    this.mt[kk] = this.mt[kk + 397] ^ y >>> 1 ^ MAG01[y & 1];
                }
                while (kk < 623) {
                    y = this.mt[kk] & Integer.MIN_VALUE | this.mt[kk + 1] & Integer.MAX_VALUE;
                    this.mt[kk] = this.mt[kk + -227] ^ y >>> 1 ^ MAG01[y & 1];
                    ++kk;
                }
                y = this.mt[623] & Integer.MIN_VALUE | this.mt[0] & Integer.MAX_VALUE;
                this.mt[623] = this.mt[396] ^ y >>> 1 ^ MAG01[y & 1];
                this.mtIndex = 0;
            }
            y = this.mt[this.mtIndex++];
        }
        finally {
            this.lock.unlock();
        }
        y ^= y >>> 11;
        y ^= y << 7 & 0x9D2C5680;
        y ^= y << 15 & 0xEFC60000;
        y ^= y >>> 18;
        return y >>> 32 - bits;
    }
}

