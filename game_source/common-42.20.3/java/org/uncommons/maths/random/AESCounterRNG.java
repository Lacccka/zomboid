/*
 * Decompiled with CFR 0.152.
 */
package org.uncommons.maths.random;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import javax.crypto.Cipher;
import org.uncommons.maths.binary.BinaryUtils;
import org.uncommons.maths.random.DefaultSeedGenerator;
import org.uncommons.maths.random.RepeatableRNG;
import org.uncommons.maths.random.SeedException;
import org.uncommons.maths.random.SeedGenerator;

public class AESCounterRNG
extends Random
implements RepeatableRNG {
    private static final int DEFAULT_SEED_SIZE_BYTES = 16;
    private final byte[] seed;
    private final Cipher cipher;
    private final byte[] counter = new byte[16];
    private final ReentrantLock lock = new ReentrantLock();
    private byte[] currentBlock = null;
    private int index = 0;

    public AESCounterRNG() throws GeneralSecurityException {
        this(16);
    }

    public AESCounterRNG(SeedGenerator seedGenerator) throws SeedException, GeneralSecurityException {
        this(seedGenerator.generateSeed(16));
    }

    public AESCounterRNG(int seedSizeBytes) throws GeneralSecurityException {
        this(DefaultSeedGenerator.getInstance().generateSeed(seedSizeBytes));
    }

    public AESCounterRNG(byte[] seed) throws GeneralSecurityException {
        if (seed == null) {
            throw new IllegalArgumentException("AES RNG requires a 128-bit, 192-bit or 256-bit seed.");
        }
        this.seed = (byte[])seed.clone();
        this.cipher = Cipher.getInstance("AES/ECB/NoPadding");
        this.cipher.init(1, new AESKey(this.seed));
    }

    public byte[] getSeed() {
        return (byte[])this.seed.clone();
    }

    private void incrementCounter() {
        for (int i = 0; i < this.counter.length; ++i) {
            int n = i;
            this.counter[n] = (byte)(this.counter[n] + 1);
            if (this.counter[i] != 0) break;
        }
    }

    private byte[] nextBlock() throws GeneralSecurityException {
        this.incrementCounter();
        return this.cipher.doFinal(this.counter);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected final int next(int bits) {
        int result;
        try {
            this.lock.lock();
            if (this.currentBlock == null || this.currentBlock.length - this.index < 4) {
                try {
                    this.currentBlock = this.nextBlock();
                    this.index = 0;
                }
                catch (GeneralSecurityException ex) {
                    throw new IllegalStateException("Failed creating next random block.", ex);
                }
            }
            result = BinaryUtils.convertBytesToInt(this.currentBlock, this.index);
            this.index += 4;
        }
        finally {
            this.lock.unlock();
        }
        return result >>> 32 - bits;
    }

    private static final class AESKey
    implements Key {
        private final byte[] keyData;

        private AESKey(byte[] keyData) {
            this.keyData = keyData;
        }

        public String getAlgorithm() {
            return "AES";
        }

        public String getFormat() {
            return "RAW";
        }

        public byte[] getEncoded() {
            return this.keyData;
        }
    }
}

