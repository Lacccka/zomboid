/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import org.bouncycastle.crypto.tls.TlsSRPLoginParameters;

public interface TlsSRPIdentityManager {
    public TlsSRPLoginParameters getLoginParameters(byte[] var1);
}

