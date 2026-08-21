/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.crypto.tls;

import java.io.IOException;

public interface TlsCipher {
    public int getPlaintextLimit(int var1);

    public byte[] encodePlaintext(long var1, short var3, byte[] var4, int var5, int var6) throws IOException;

    public byte[] decodeCiphertext(long var1, short var3, byte[] var4, int var5, int var6) throws IOException;
}

