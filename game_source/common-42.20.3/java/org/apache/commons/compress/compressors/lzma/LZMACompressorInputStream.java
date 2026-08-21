/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.tukaani.xz.LZMAInputStream
 *  org.tukaani.xz.MemoryLimitException
 */
package org.apache.commons.compress.compressors.lzma;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.MemoryLimitException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.tukaani.xz.LZMAInputStream;

public class LZMACompressorInputStream
extends CompressorInputStream
implements InputStreamStatistics {
    private final BoundedInputStream countingStream;
    private final InputStream in;

    public static boolean matches(byte[] signature, int length) {
        return signature != null && length >= 3 && signature[0] == 93 && signature[1] == 0 && signature[2] == 0;
    }

    public LZMACompressorInputStream(InputStream inputStream2) throws IOException {
        this.countingStream = ((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(inputStream2)).get();
        this.in = new LZMAInputStream((InputStream)this.countingStream, -1);
    }

    public LZMACompressorInputStream(InputStream inputStream2, int memoryLimitInKb) throws IOException {
        try {
            this.countingStream = ((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(inputStream2)).get();
            this.in = new LZMAInputStream((InputStream)this.countingStream, memoryLimitInKb);
        }
        catch (org.tukaani.xz.MemoryLimitException e) {
            throw new MemoryLimitException(e.getMemoryNeeded(), e.getMemoryLimit(), (Exception)((Object)e));
        }
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }

    @Override
    public long getCompressedCount() {
        return this.countingStream.getCount();
    }

    @Override
    public int read() throws IOException {
        int ret = this.in.read();
        this.count(ret == -1 ? 0 : 1);
        return ret;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int ret = this.in.read(buf, off, len);
        this.count(ret);
        return ret;
    }

    @Override
    public long skip(long n) throws IOException {
        return IOUtils.skip(this.in, n);
    }
}

