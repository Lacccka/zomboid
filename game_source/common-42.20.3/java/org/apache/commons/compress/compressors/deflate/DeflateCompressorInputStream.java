/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.deflate;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;

public class DeflateCompressorInputStream
extends CompressorInputStream
implements InputStreamStatistics {
    private static final int MAGIC_1 = 120;
    private static final int MAGIC_2a = 1;
    private static final int MAGIC_2b = 94;
    private static final int MAGIC_2c = 156;
    private static final int MAGIC_2d = 218;
    private final BoundedInputStream countingStream;
    private final InputStream in;
    private final Inflater inflater;

    public static boolean matches(byte[] signature, int length) {
        return length > 3 && signature[0] == 120 && (signature[1] == 1 || signature[1] == 94 || signature[1] == -100 || signature[1] == -38);
    }

    public DeflateCompressorInputStream(InputStream inputStream2) {
        this(inputStream2, new DeflateParameters());
    }

    public DeflateCompressorInputStream(InputStream inputStream2, DeflateParameters parameters) {
        this.inflater = new Inflater(!parameters.withZlibHeader());
        this.countingStream = (BoundedInputStream)((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(inputStream2)).asSupplier().get();
        this.in = new InflaterInputStream(this.countingStream, this.inflater);
    }

    @Override
    public int available() throws IOException {
        return this.in.available();
    }

    @Override
    public void close() throws IOException {
        try {
            this.in.close();
        }
        finally {
            this.inflater.end();
        }
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
        if (len == 0) {
            return 0;
        }
        int ret = this.in.read(buf, off, len);
        this.count(ret);
        return ret;
    }

    @Override
    public long skip(long n) throws IOException {
        return IOUtils.skip(this.in, n);
    }
}

