/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.tls.TlsContext;

public interface TlsHandshakeHash
extends Digest {
    public void init(TlsContext var1);

    public TlsHandshakeHash notifyPRFDetermined();

    public void trackHashAlgorithm(short var1);

    public void sealHashAlgorithms();

    public TlsHandshakeHash stopTracking();

    public Digest forkPRFHash();

    public byte[] getFinalHash(short var1);
}

