/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.snappy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.codec.digest.PureJavaCrc32C;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.lz77support.Parameters;
import org.apache.commons.compress.compressors.snappy.FramedSnappyCompressorInputStream;
import org.apache.commons.compress.compressors.snappy.SnappyCompressorOutputStream;
import org.apache.commons.compress.utils.ByteUtils;

public class FramedSnappyCompressorOutputStream
extends CompressorOutputStream<OutputStream> {
    private static final int MAX_COMPRESSED_BUFFER_SIZE = 65536;
    private final Parameters params;
    private final PureJavaCrc32C checksum = new PureJavaCrc32C();
    private final byte[] oneByte = new byte[1];
    private final byte[] buffer = new byte[65536];
    private int currentIndex;
    private final ByteUtils.ByteConsumer consumer;

    static long mask(long x) {
        x = x >> 15 | x << 17;
        x += 2726488792L;
        return x &= 0xFFFFFFFFL;
    }

    public FramedSnappyCompressorOutputStream(OutputStream out) throws IOException {
        this(out, SnappyCompressorOutputStream.createParameterBuilder(32768).build());
    }

    public FramedSnappyCompressorOutputStream(OutputStream out, Parameters params) throws IOException {
        super(out);
        this.params = params;
        this.consumer = new ByteUtils.OutputStreamByteConsumer(out);
        out.write(FramedSnappyCompressorInputStream.SZ_SIGNATURE);
    }

    @Override
    public void close() throws IOException {
        try {
            this.finish();
        }
        finally {
            this.out.close();
        }
    }

    public void finish() throws IOException {
        this.flushBuffer();
    }

    private void flushBuffer() throws IOException {
        if (this.currentIndex == 0) {
            return;
        }
        this.out.write(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SnappyCompressorOutputStream o = new SnappyCompressorOutputStream((OutputStream)baos, (long)this.currentIndex, this.params);){
            ((OutputStream)o).write(this.buffer, 0, this.currentIndex);
        }
        byte[] b = baos.toByteArray();
        this.writeLittleEndian(3, (long)b.length + 4L);
        this.writeCrc();
        this.out.write(b);
        this.currentIndex = 0;
    }

    @Override
    public void write(byte[] data, int off, int len) throws IOException {
        int blockDataRemaining = this.buffer.length - this.currentIndex;
        while (len > 0) {
            int copyLen = Math.min(len, blockDataRemaining);
            System.arraycopy(data, off, this.buffer, this.currentIndex, copyLen);
            off += copyLen;
            len -= copyLen;
            this.currentIndex += copyLen;
            if ((blockDataRemaining -= copyLen) != 0) continue;
            this.flushBuffer();
            blockDataRemaining = this.buffer.length;
        }
    }

    @Override
    public void write(int b) throws IOException {
        this.oneByte[0] = (byte)(b & 0xFF);
        this.write(this.oneByte);
    }

    private void writeCrc() throws IOException {
        this.checksum.update(this.buffer, 0, this.currentIndex);
        this.writeLittleEndian(4, FramedSnappyCompressorOutputStream.mask(this.checksum.getValue()));
        this.checksum.reset();
    }

    private void writeLittleEndian(int numBytes, long num) throws IOException {
        ByteUtils.toLittleEndian(this.consumer, num, numBytes);
    }
}

