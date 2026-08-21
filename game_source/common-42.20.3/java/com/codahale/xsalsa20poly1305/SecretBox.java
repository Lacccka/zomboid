/*
 * Decompiled with CFR 0.152.
 */
package com.codahale.xsalsa20poly1305;

import com.codahale.xsalsa20poly1305.Keys;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Optional;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.engines.XSalsa20Engine;
import org.bouncycastle.crypto.macs.Poly1305;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class SecretBox {
    static final int NONCE_SIZE = 24;
    private final byte[] key;

    public SecretBox(byte[] secretKey) {
        if (secretKey.length != 32) {
            throw new IllegalArgumentException("secretKey must be 32 bytes long");
        }
        this.key = Arrays.copyOf(secretKey, secretKey.length);
    }

    public SecretBox(byte[] publicKey, byte[] privateKey) {
        this(Keys.sharedSecret(publicKey, privateKey));
    }

    public byte[] seal(byte[] nonce, byte[] plaintext) {
        XSalsa20Engine xsalsa20 = new XSalsa20Engine();
        Poly1305 poly1305 = new Poly1305();
        xsalsa20.init(true, new ParametersWithIV(new KeyParameter(this.key), nonce));
        byte[] sk = new byte[32];
        xsalsa20.processBytes(sk, 0, 32, sk, 0);
        byte[] out = new byte[plaintext.length + poly1305.getMacSize()];
        xsalsa20.processBytes(plaintext, 0, plaintext.length, out, poly1305.getMacSize());
        poly1305.init(new KeyParameter(sk));
        poly1305.update(out, poly1305.getMacSize(), plaintext.length);
        poly1305.doFinal(out, 0);
        return out;
    }

    public Optional<byte[]> open(byte[] nonce, byte[] ciphertext) {
        XSalsa20Engine xsalsa20 = new XSalsa20Engine();
        Poly1305 poly1305 = new Poly1305();
        xsalsa20.init(false, new ParametersWithIV(new KeyParameter(this.key), nonce));
        byte[] sk = new byte[32];
        xsalsa20.processBytes(sk, 0, sk.length, sk, 0);
        poly1305.init(new KeyParameter(sk));
        int len = Math.max(ciphertext.length - poly1305.getMacSize(), 0);
        poly1305.update(ciphertext, poly1305.getMacSize(), len);
        byte[] calculatedMAC = new byte[poly1305.getMacSize()];
        poly1305.doFinal(calculatedMAC, 0);
        byte[] presentedMAC = new byte[poly1305.getMacSize()];
        System.arraycopy(ciphertext, 0, presentedMAC, 0, Math.min(ciphertext.length, poly1305.getMacSize()));
        if (!MessageDigest.isEqual(calculatedMAC, presentedMAC)) {
            return Optional.empty();
        }
        byte[] plaintext = new byte[len];
        xsalsa20.processBytes(ciphertext, poly1305.getMacSize(), plaintext.length, plaintext, 0);
        return Optional.of(plaintext);
    }

    public byte[] nonce() {
        byte[] nonce = new byte[24];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);
        return nonce;
    }

    public byte[] nonce(byte[] message) {
        byte[] n1 = new byte[16];
        byte[] n2 = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(n1);
        random.nextBytes(n2);
        Blake2bDigest blake2b = new Blake2bDigest(this.key, 24, n1, n2);
        blake2b.update(message, message.length, 0);
        byte[] nonce = new byte[24];
        blake2b.doFinal(nonce, 0);
        return nonce;
    }
}

