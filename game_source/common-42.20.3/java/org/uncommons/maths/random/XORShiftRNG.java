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

public class XORShiftRNG
extends Random
implements RepeatableRNG {
    private static final int SEED_SIZE_BYTES = 20;
    private int state1;
    private int state2;
    private int state3;
    private int state4;
    private int state5;
    private final byte[] seed;
    private final ReentrantLock lock = new ReentrantLock();

    public XORShiftRNG() {
        this(DefaultSeedGenerator.getInstance().generateSeed(20));
    }

    public XORShiftRNG(SeedGenerator seedGenerator) throws SeedException {
        this(seedGenerator.generateSeed(20));
    }

    public XORShiftRNG(byte[] seed) {
        if (seed == null || seed.length != 20) {
            throw new IllegalArgumentException("XOR shift RNG requires 160 bits of seed data.");
        }
        this.seed = (byte[])seed.clone();
        int[] state = BinaryUtils.convertBytesToInts(seed);
        this.state1 = state[0];
        this.state2 = state[1];
        this.state3 = state[2];
        this.state4 = state[3];
        this.state5 = state[4];
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
            int t = this.state1 ^ this.state1 >> 7;
            this.state1 = this.state2;
            this.state2 = this.state3;
            this.state3 = this.state4;
            this.state4 = this.state5;
            this.state5 = this.state5 ^ this.state5 << 6 ^ (t ^ t << 13);
            int value = (this.state2 + this.state2 + 1) * this.state5;
            int n = value >>> 32 - bits;
            return n;
        }
        finally {
            this.lock.unlock();
        }
    }
}

