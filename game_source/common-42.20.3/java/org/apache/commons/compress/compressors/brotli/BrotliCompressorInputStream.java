/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.brotli.dec.BrotliInputStream
 */
package org.apache.commons.compress.compressors.brotli;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.brotli.dec.BrotliInputStream;

public class BrotliCompressorInputStream
extends CompressorInputStream
implements InputStreamStatistics {
    private final BoundedInputStream countingInputStream;
    private final BrotliInputStream brotliInputStream;

    public BrotliCompressorInputStream(InputStream inputStream2) throws IOException {
        this.countingInputStream = ((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(inputStream2)).get();
        this.brotliInputStream = new BrotliInputStream((InputStream)this.countingInputStream);
    }

    @Override
    public int available() throws IOException {
        return this.brotliInputStream.available();
    }

    @Override
    public void close() throws IOException {
        this.brotliInputStream.close();
    }

    @Override
    public long getCompressedCount() {
        return this.countingInputStream.getCount();
    }

    @Override
    public synchronized void mark(int readLimit) {
        this.brotliInputStream.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        return this.brotliInputStream.markSupported();
    }

    @Override
    public int read() throws IOException {
        int ret = this.brotliInputStream.read();
        this.count(ret == -1 ? 0 : 1);
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.brotliInputStream.read(b);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int ret = this.brotliInputStream.read(buf, off, len);
        this.count(ret);
        return ret;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.brotliInputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return IOUtils.skip((InputStream)this.brotliInputStream, n);
    }

    public String toString() {
        return this.brotliInputStream.toString();
    }
}

