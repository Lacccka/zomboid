/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.IOException;
import java.io.OutputStream;

abstract class RandomAccessOutputStream
extends OutputStream {
    RandomAccessOutputStream() {
    }

    public abstract long position() throws IOException;

    @Override
    public void write(int b) throws IOException {
        this.write(new byte[]{(byte)b});
    }

    abstract void writeFully(byte[] var1, int var2, int var3, long var4) throws IOException;

    public void writeFully(byte[] b, long position) throws IOException {
        this.writeFully(b, 0, b.length, position);
    }
}

