/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.crypto.tls.TlsUtils;

public class SignatureAndHashAlgorithm {
    protected short hash;
    protected short signature;

    public SignatureAndHashAlgorithm(short s, short s2) {
        if (!TlsUtils.isValidUint8(s)) {
            throw new IllegalArgumentException("'hash' should be a uint8");
        }
        if (!TlsUtils.isValidUint8(s2)) {
            throw new IllegalArgumentException("'signature' should be a uint8");
        }
        if (s2 == 0) {
            throw new IllegalArgumentException("'signature' MUST NOT be \"anonymous\"");
        }
        this.hash = s;
        this.signature = s2;
    }

    public short getHash() {
        return this.hash;
    }

    public short getSignature() {
        return this.signature;
    }

    public boolean equals(Object object) {
        if (!(object instanceof SignatureAndHashAlgorithm)) {
            return false;
        }
        SignatureAndHashAlgorithm signatureAndHashAlgorithm = (SignatureAndHashAlgorithm)object;
        return signatureAndHashAlgorithm.getHash() == this.getHash() && signatureAndHashAlgorithm.getSignature() == this.getSignature();
    }

    public int hashCode() {
        return this.getHash() << 16 | this.getSignature();
    }

    public void encode(OutputStream outputStream2) throws IOException {
        TlsUtils.writeUint8(this.getHash(), outputStream2);
        TlsUtils.writeUint8(this.getSignature(), outputStream2);
    }

    public static SignatureAndHashAlgorithm parse(InputStream inputStream2) throws IOException {
        short s = TlsUtils.readUint8(inputStream2);
        short s2 = TlsUtils.readUint8(inputStream2);
        return new SignatureAndHashAlgorithm(s, s2);
    }
}

