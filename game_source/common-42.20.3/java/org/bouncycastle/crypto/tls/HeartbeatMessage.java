/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.crypto.tls.HeartbeatMessageType;
import org.bouncycastle.crypto.tls.TlsContext;
import org.bouncycastle.crypto.tls.TlsFatalAlert;
import org.bouncycastle.crypto.tls.TlsUtils;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.io.Streams;

public class HeartbeatMessage {
    protected short type;
    protected byte[] payload;
    protected int paddingLength;

    public HeartbeatMessage(short s, byte[] byArray, int n) {
        if (!HeartbeatMessageType.isValid(s)) {
            throw new IllegalArgumentException("'type' is not a valid HeartbeatMessageType value");
        }
        if (byArray == null || byArray.length >= 65536) {
            throw new IllegalArgumentException("'payload' must have length < 2^16");
        }
        if (n < 16) {
            throw new IllegalArgumentException("'paddingLength' must be at least 16");
        }
        this.type = s;
        this.payload = byArray;
        this.paddingLength = n;
    }

    public void encode(TlsContext tlsContext, OutputStream outputStream2) throws IOException {
        TlsUtils.writeUint8(this.type, outputStream2);
        TlsUtils.checkUint16(this.payload.length);
        TlsUtils.writeUint16(this.payload.length, outputStream2);
        outputStream2.write(this.payload);
        byte[] byArray = new byte[this.paddingLength];
        tlsContext.getNonceRandomGenerator().nextBytes(byArray);
        outputStream2.write(byArray);
    }

    public static HeartbeatMessage parse(InputStream inputStream2) throws IOException {
        short s = TlsUtils.readUint8(inputStream2);
        if (!HeartbeatMessageType.isValid(s)) {
            throw new TlsFatalAlert(47);
        }
        int n = TlsUtils.readUint16(inputStream2);
        PayloadBuffer payloadBuffer = new PayloadBuffer();
        Streams.pipeAll(inputStream2, payloadBuffer);
        byte[] byArray = payloadBuffer.toTruncatedByteArray(n);
        if (byArray == null) {
            return null;
        }
        int n2 = payloadBuffer.size() - byArray.length;
        return new HeartbeatMessage(s, byArray, n2);
    }

    static class PayloadBuffer
    extends ByteArrayOutputStream {
        PayloadBuffer() {
        }

        byte[] toTruncatedByteArray(int n) {
            int n2 = n + 16;
            if (this.count < n2) {
                return null;
            }
            return Arrays.copyOf(this.buf, n);
        }
    }
}

