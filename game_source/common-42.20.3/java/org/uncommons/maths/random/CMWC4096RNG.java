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

public class CMWC4096RNG
extends Random
implements RepeatableRNG {
    private static final int SEED_SIZE_BYTES = 16384;
    private static final long A = 18782L;
    private final byte[] seed;
    private final int[] state;
    private int carry = 362436;
    private int index = 4095;
    private final ReentrantLock lock = new ReentrantLock();

    public CMWC4096RNG() {
        this(DefaultSeedGenerator.getInstance().generateSeed(16384));
    }

    public CMWC4096RNG(SeedGenerator seedGenerator) throws SeedException {
        this(seedGenerator.generateSeed(16384));
    }

    public CMWC4096RNG(byte[] seed) {
        if (seed == null || seed.length != 16384) {
            throw new IllegalArgumentException("CMWC RNG requires 16kb of seed data.");
        }
        this.seed = (byte[])seed.clone();
        this.state = BinaryUtils.convertBytesToInts(seed);
    }

    public byte[] getSeed() {
        return (byte[])this.seed.clone();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected int next(int bits) {
        try {
            this.lock.lock();
            this.index = this.index + 1 & 0xFFF;
            long t = 18782L * ((long)this.state[this.index] & 0xFFFFFFFFL) + (long)this.carry;
            this.carry = (int)(t >> 32);
            int x = (int)t + this.carry;
            if (x < this.carry) {
                ++x;
                ++this.carry;
            }
            this.state[this.index] = -2 - x;
            int n = this.state[this.index] >>> 32 - bits;
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }
}

