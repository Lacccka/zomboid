/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import org.bouncycastle.crypto.tls.TlsSRPUtils;
import org.bouncycastle.crypto.tls.TlsUtils;
import org.bouncycastle.util.Arrays;

public class ServerSRPParams {
    protected BigInteger N;
    protected BigInteger g;
    protected BigInteger B;
    protected byte[] s;

    public ServerSRPParams(BigInteger bigInteger, BigInteger bigInteger2, byte[] byArray, BigInteger bigInteger3) {
        this.N = bigInteger;
        this.g = bigInteger2;
        this.s = Arrays.clone(byArray);
        this.B = bigInteger3;
    }

    public BigInteger getB() {
        return this.B;
    }

    public BigInteger getG() {
        return this.g;
    }

    public BigInteger getN() {
        return this.N;
    }

    public byte[] getS() {
        return this.s;
    }

    public void encode(OutputStream outputStream2) throws IOException {
        TlsSRPUtils.writeSRPParameter(this.N, outputStream2);
        TlsSRPUtils.writeSRPParameter(this.g, outputStream2);
        TlsUtils.writeOpaque8(this.s, outputStream2);
        TlsSRPUtils.writeSRPParameter(this.B, outputStream2);
    }

    public static ServerSRPParams parse(InputStream inputStream2) throws IOException {
        BigInteger bigInteger = TlsSRPUtils.readSRPParameter(inputStream2);
        BigInteger bigInteger2 = TlsSRPUtils.readSRPParameter(inputStream2);
        byte[] byArray = TlsUtils.readOpaque8(inputStream2);
        BigInteger bigInteger3 = TlsSRPUtils.readSRPParameter(inputStream2);
        return new ServerSRPParams(bigInteger, bigInteger2, byArray, bigInteger3);
    }
}

