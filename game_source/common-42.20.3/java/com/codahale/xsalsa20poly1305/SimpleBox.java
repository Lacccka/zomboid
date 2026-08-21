/*
 * Decompiled with CFR 0.152.
 */
package com.codahale.xsalsa20poly1305;

import com.codahale.xsalsa20poly1305.SecretBox;
import java.util.Arrays;
import java.util.Optional;

public class SimpleBox {
    private final SecretBox box;

    public SimpleBox(byte[] secretKey) {
        this.box = new SecretBox(secretKey);
    }

    public SimpleBox(byte[] publicKey, byte[] privateKey) {
        this.box = new SecretBox(publicKey, privateKey);
    }

    public byte[] seal(byte[] plaintext) {
        byte[] nonce = this.box.nonce(plaintext);
        byte[] ciphertext = this.box.seal(nonce, plaintext);
        byte[] combined = new byte[nonce.length + ciphertext.length];
        System.arraycopy(nonce, 0, combined, 0, nonce.length);
        System.arraycopy(ciphertext, 0, combined, nonce.length, ciphertext.length);
        return combined;
    }

    public Optional<byte[]> open(byte[] ciphertext) {
        if (ciphertext.length < 24) {
            return Optional.empty();
        }
        byte[] nonce = Arrays.copyOfRange(ciphertext, 0, 24);
        byte[] x = Arrays.copyOfRange(ciphertext, 24, ciphertext.length);
        return this.box.open(nonce, x);
    }
}

