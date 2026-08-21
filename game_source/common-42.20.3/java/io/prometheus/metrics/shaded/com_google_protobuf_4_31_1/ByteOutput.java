/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.shaded.com_google_protobuf_4_31_1;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ByteOutput {
    public abstract void write(byte var1) throws IOException;

    public abstract void write(byte[] var1, int var2, int var3) throws IOException;

    public abstract void writeLazy(byte[] var1, int var2, int var3) throws IOException;

    public abstract void write(ByteBuffer var1) throws IOException;

    public abstract void writeLazy(ByteBuffer var1) throws IOException;
}

