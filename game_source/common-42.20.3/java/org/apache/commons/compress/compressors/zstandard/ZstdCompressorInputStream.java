/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.luben.zstd.BufferPool
 *  com.github.luben.zstd.ZstdInputStream
 */
package org.apache.commons.compress.compressors.zstandard;

import com.github.luben.zstd.BufferPool;
import com.github.luben.zstd.ZstdInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;

public class ZstdCompressorInputStream
extends CompressorInputStream
implements InputStreamStatistics {
    private final BoundedInputStream countingStream;
    private final ZstdInputStream decIS;

    public ZstdCompressorInputStream(InputStream in) throws IOException {
        this.countingStream = ((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(in)).get();
        this.decIS = new ZstdInputStream((InputStream)this.countingStream);
    }

    public ZstdCompressorInputStream(InputStream in, BufferPool bufferPool) throws IOException {
        this.countingStream = ((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(in)).get();
        this.decIS = new ZstdInputStream((InputStream)this.countingStream, bufferPool);
    }

    @Override
    public int available() throws IOException {
        return this.decIS.available();
    }

    @Override
    public void close() throws IOException {
        this.decIS.close();
    }

    @Override
    public long getCompressedCount() {
        return this.countingStream.getCount();
    }

    @Override
    public synchronized void mark(int readLimit) {
        this.decIS.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        return this.decIS.markSupported();
    }

    @Override
    public int read() throws IOException {
        int ret = this.decIS.read();
        this.count(ret == -1 ? 0 : 1);
        return ret;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int ret = this.decIS.read(buf, off, len);
        this.count(ret);
        return ret;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.decIS.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return IOUtils.skip((InputStream)this.decIS, n);
    }

    public String toString() {
        return this.decIS.toString();
    }
}

