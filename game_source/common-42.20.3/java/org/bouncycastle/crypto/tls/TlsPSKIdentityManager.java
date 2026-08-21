/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

public interface TlsPSKIdentityManager {
    public byte[] getHint();

    public byte[] getPSK(byte[] var1);
}

