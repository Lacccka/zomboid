/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.tls.TlsDHUtils;

public class ServerDHParams {
    protected DHPublicKeyParameters publicKey;

    public ServerDHParams(DHPublicKeyParameters dHPublicKeyParameters) {
        if (dHPublicKeyParameters == null) {
            throw new IllegalArgumentException("'publicKey' cannot be null");
        }
        this.publicKey = dHPublicKeyParameters;
    }

    public DHPublicKeyParameters getPublicKey() {
        return this.publicKey;
    }

    public void encode(OutputStream outputStream2) throws IOException {
        DHParameters dHParameters = this.publicKey.getParameters();
        BigInteger bigInteger = this.publicKey.getY();
        TlsDHUtils.writeDHParameter(dHParameters.getP(), outputStream2);
        TlsDHUtils.writeDHParameter(dHParameters.getG(), outputStream2);
        TlsDHUtils.writeDHParameter(bigInteger, outputStream2);
    }

    public static ServerDHParams parse(InputStream inputStream2) throws IOException {
        BigInteger bigInteger = TlsDHUtils.readDHParameter(inputStream2);
        BigInteger bigInteger2 = TlsDHUtils.readDHParameter(inputStream2);
        BigInteger bigInteger3 = TlsDHUtils.readDHParameter(inputStream2);
        return new ServerDHParams(TlsDHUtils.validateDHPublicKey(new DHPublicKeyParameters(bigInteger3, new DHParameters(bigInteger, bigInteger2))));
    }
}

