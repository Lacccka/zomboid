/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.io.IOException;

public interface ILogEncoder {
    public String contentEncoding();

    public byte[] encode(byte[] var1) throws IOException;
}

