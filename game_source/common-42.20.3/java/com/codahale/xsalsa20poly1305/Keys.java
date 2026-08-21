/*
 * Decompiled with CFR 0.152.
 */
package com.codahale.xsalsa20poly1305;

import com.codahale.xsalsa20poly1305.HSalsa20;
import java.security.SecureRandom;
import org.bouncycastle.math.ec.rfc7748.X25519;

public class Keys {
    static final int KEY_LEN = 32;
    private static final byte[] HSALSA20_SEED = new byte[16];

    private Keys() {
    }

    public static byte[] generateSecretKey() {
        byte[] k = new byte[32];
        SecureRandom random = new SecureRandom();
        random.nextBytes(k);
        return k;
    }

    public static byte[] generatePrivateKey() {
        byte[] k = Keys.generateSecretKey();
        k[0] = (byte)(k[0] & 0xFFFFFFF8);
        k[31] = (byte)(k[31] & 0x7F);
        k[31] = (byte)(k[31] | 0x40);
        return k;
    }

    public static byte[] generatePublicKey(byte[] privateKey) {
        byte[] publicKey = new byte[32];
        X25519.scalarMultBase(privateKey, 0, publicKey, 0);
        return publicKey;
    }

    public static byte[] sharedSecret(byte[] publicKey, byte[] privateKey) {
        byte[] s = new byte[32];
        X25519.scalarMult(privateKey, 0, publicKey, 0, s, 0);
        byte[] k = new byte[32];
        HSalsa20.hsalsa20(k, HSALSA20_SEED, s);
        return k;
    }
}

