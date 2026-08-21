/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;

class InflaterInputStreamWithStatistics
extends InflaterInputStream
implements InputStreamStatistics {
    private long compressedCount;
    private long uncompressedCount;

    InflaterInputStreamWithStatistics(InputStream in) {
        super(in);
    }

    InflaterInputStreamWithStatistics(InputStream in, Inflater inf) {
        super(in, inf);
    }

    InflaterInputStreamWithStatistics(InputStream in, Inflater inf, int size) {
        super(in, inf, size);
    }

    @Override
    protected void fill() throws IOException {
        super.fill();
        this.compressedCount += (long)this.inf.getRemaining();
    }

    @Override
    public long getCompressedCount() {
        return this.compressedCount;
    }

    @Override
    public long getUncompressedCount() {
        return this.uncompressedCount;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b > -1) {
            ++this.uncompressedCount;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytes = super.read(b, off, len);
        if (bytes > -1) {
            this.uncompressedCount += (long)bytes;
        }
        return bytes;
    }
}

