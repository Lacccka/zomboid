/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.lz77support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.ByteUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.commons.io.input.BoundedInputStream;

public abstract class AbstractLZ77CompressorInputStream
extends CompressorInputStream
implements InputStreamStatistics {
    private final int windowSize;
    private final byte[] buf;
    private int writeIndex;
    private int readIndex;
    private final BoundedInputStream in;
    private long bytesRemaining;
    private int backReferenceOffset;
    private int size;
    private final byte[] oneByte = new byte[1];
    protected final ByteUtils.ByteSupplier supplier = this::readOneByte;

    public AbstractLZ77CompressorInputStream(InputStream is, int windowSize) {
        this.in = (BoundedInputStream)((BoundedInputStream.Builder)BoundedInputStream.builder().setInputStream(is)).asSupplier().get();
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be bigger than 0");
        }
        this.windowSize = windowSize;
        this.buf = new byte[3 * windowSize];
        this.readIndex = 0;
        this.writeIndex = 0;
        this.bytesRemaining = 0L;
    }

    @Override
    public int available() {
        return this.writeIndex - this.readIndex;
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }

    @Override
    public long getCompressedCount() {
        return this.in.getCount();
    }

    public int getSize() {
        return this.size;
    }

    protected final boolean hasMoreDataInBlock() {
        return this.bytesRemaining > 0L;
    }

    public void prefill(byte[] data) {
        if (this.writeIndex != 0) {
            throw new IllegalStateException("The stream has already been read from, can't prefill anymore");
        }
        int len = Math.min(this.windowSize, data.length);
        System.arraycopy(data, data.length - len, this.buf, 0, len);
        this.writeIndex += len;
        this.readIndex += len;
    }

    @Override
    public int read() throws IOException {
        return this.read(this.oneByte, 0, 1) == -1 ? -1 : this.oneByte[0] & 0xFF;
    }

    protected final int readBackReference(byte[] b, int off, int len) {
        int avail = this.available();
        if (len > avail) {
            this.tryToCopy(len - avail);
        }
        return this.readFromBuffer(b, off, len);
    }

    private int readFromBuffer(byte[] b, int off, int len) {
        int readable = Math.min(len, this.available());
        if (readable > 0) {
            System.arraycopy(this.buf, this.readIndex, b, off, readable);
            this.readIndex += readable;
            if (this.readIndex > 2 * this.windowSize) {
                this.slideBuffer();
            }
        }
        this.size += readable;
        return readable;
    }

    protected final int readLiteral(byte[] b, int off, int len) throws IOException {
        int avail = this.available();
        if (len > avail) {
            this.tryToReadLiteral(len - avail);
        }
        return this.readFromBuffer(b, off, len);
    }

    protected final int readOneByte() throws IOException {
        int b = this.in.read();
        if (b != -1) {
            this.count(1);
            return b & 0xFF;
        }
        return -1;
    }

    private void slideBuffer() {
        System.arraycopy(this.buf, this.windowSize, this.buf, 0, this.windowSize * 2);
        this.writeIndex -= this.windowSize;
        this.readIndex -= this.windowSize;
    }

    protected final void startBackReference(int offset, long length) {
        if (offset <= 0 || offset > this.writeIndex) {
            throw new IllegalArgumentException("offset must be bigger than 0 but not bigger than the number of bytes available for back-references");
        }
        if (length < 0L) {
            throw new IllegalArgumentException("length must not be negative");
        }
        this.backReferenceOffset = offset;
        this.bytesRemaining = length;
    }

    protected final void startLiteral(long length) {
        if (length < 0L) {
            throw new IllegalArgumentException("length must not be negative");
        }
        this.bytesRemaining = length;
    }

    private void tryToCopy(int bytesToCopy) {
        int copy = Math.min((int)Math.min((long)bytesToCopy, this.bytesRemaining), this.buf.length - this.writeIndex);
        if (copy != 0) {
            if (this.backReferenceOffset == 1) {
                byte last = this.buf[this.writeIndex - 1];
                Arrays.fill(this.buf, this.writeIndex, this.writeIndex + copy, last);
                this.writeIndex += copy;
            } else if (copy < this.backReferenceOffset) {
                System.arraycopy(this.buf, this.writeIndex - this.backReferenceOffset, this.buf, this.writeIndex, copy);
                this.writeIndex += copy;
            } else {
                int fullRots = copy / this.backReferenceOffset;
                for (int i = 0; i < fullRots; ++i) {
                    System.arraycopy(this.buf, this.writeIndex - this.backReferenceOffset, this.buf, this.writeIndex, this.backReferenceOffset);
                    this.writeIndex += this.backReferenceOffset;
                }
                int pad = copy - this.backReferenceOffset * fullRots;
                if (pad > 0) {
                    System.arraycopy(this.buf, this.writeIndex - this.backReferenceOffset, this.buf, this.writeIndex, pad);
                    this.writeIndex += pad;
                }
            }
        }
        this.bytesRemaining -= (long)copy;
    }

    private void tryToReadLiteral(int bytesToRead) throws IOException {
        int reallyTryToRead = Math.min((int)Math.min((long)bytesToRead, this.bytesRemaining), this.buf.length - this.writeIndex);
        int bytesRead = reallyTryToRead > 0 ? IOUtils.readFully(this.in, this.buf, this.writeIndex, reallyTryToRead) : 0;
        this.count(bytesRead);
        if (reallyTryToRead != bytesRead) {
            throw new IOException("Premature end of stream reading literal");
        }
        this.writeIndex += reallyTryToRead;
        this.bytesRemaining -= (long)reallyTryToRead;
    }
}

