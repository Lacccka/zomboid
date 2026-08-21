/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.UnsynchronizedFilterInputStream;

public final class UnsynchronizedBufferedInputStream
extends UnsynchronizedFilterInputStream {
    protected volatile byte[] buffer;
    protected int count;
    protected int markLimit;
    protected int markPos = -1;
    protected int pos;

    private UnsynchronizedBufferedInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be > 0");
        }
        this.buffer = new byte[size];
    }

    @Override
    public int available() throws IOException {
        InputStream localIn = this.inputStream;
        if (this.buffer == null || localIn == null) {
            throw new IOException("Stream is closed");
        }
        return this.count - this.pos + localIn.available();
    }

    @Override
    public void close() throws IOException {
        this.buffer = null;
        InputStream localIn = this.inputStream;
        this.inputStream = null;
        if (localIn != null) {
            localIn.close();
        }
    }

    private int fillBuffer(InputStream localIn, byte[] localBuf) throws IOException {
        if (this.markPos == -1 || this.pos - this.markPos >= this.markLimit) {
            int result = localIn.read(localBuf);
            if (result > 0) {
                this.markPos = -1;
                this.pos = 0;
                this.count = result;
            }
            return result;
        }
        if (this.markPos == 0 && this.markLimit > localBuf.length) {
            int newLength = localBuf.length * 2;
            if (newLength > this.markLimit) {
                newLength = this.markLimit;
            }
            byte[] newbuf = new byte[newLength];
            System.arraycopy(localBuf, 0, newbuf, 0, localBuf.length);
            this.buffer = newbuf;
            localBuf = newbuf;
        } else if (this.markPos > 0) {
            System.arraycopy(localBuf, this.markPos, localBuf, 0, localBuf.length - this.markPos);
        }
        this.pos -= this.markPos;
        this.markPos = 0;
        this.count = 0;
        int bytesread = localIn.read(localBuf, this.pos, localBuf.length - this.pos);
        this.count = bytesread <= 0 ? this.pos : this.pos + bytesread;
        return bytesread;
    }

    byte[] getBuffer() {
        return this.buffer;
    }

    @Override
    public void mark(int readLimit) {
        this.markLimit = readLimit;
        this.markPos = this.pos;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        byte[] localBuf = this.buffer;
        InputStream localIn = this.inputStream;
        if (localBuf == null || localIn == null) {
            throw new IOException("Stream is closed");
        }
        if (this.pos >= this.count && this.fillBuffer(localIn, localBuf) == -1) {
            return -1;
        }
        if (localBuf != this.buffer && (localBuf = this.buffer) == null) {
            throw new IOException("Stream is closed");
        }
        if (this.count - this.pos > 0) {
            return localBuf[this.pos++] & 0xFF;
        }
        return -1;
    }

    @Override
    public int read(byte[] dest, int offset, int length) throws IOException {
        int required;
        byte[] localBuf = this.buffer;
        if (localBuf == null) {
            throw new IOException("Stream is closed");
        }
        if (offset > dest.length - length || offset < 0 || length < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return 0;
        }
        InputStream localIn = this.inputStream;
        if (localIn == null) {
            throw new IOException("Stream is closed");
        }
        if (this.pos < this.count) {
            int copylength = this.count - this.pos >= length ? length : this.count - this.pos;
            System.arraycopy(localBuf, this.pos, dest, offset, copylength);
            this.pos += copylength;
            if (copylength == length || localIn.available() == 0) {
                return copylength;
            }
            offset += copylength;
            required = length - copylength;
        } else {
            required = length;
        }
        while (true) {
            int read;
            if (this.markPos == -1 && required >= localBuf.length) {
                read = localIn.read(dest, offset, required);
                if (read == -1) {
                    return required == length ? -1 : length - required;
                }
            } else {
                if (this.fillBuffer(localIn, localBuf) == -1) {
                    return required == length ? -1 : length - required;
                }
                if (localBuf != this.buffer && (localBuf = this.buffer) == null) {
                    throw new IOException("Stream is closed");
                }
                read = this.count - this.pos >= required ? required : this.count - this.pos;
                System.arraycopy(localBuf, this.pos, dest, offset, read);
                this.pos += read;
            }
            if ((required -= read) == 0) {
                return length;
            }
            if (localIn.available() == 0) {
                return length - required;
            }
            offset += read;
        }
    }

    @Override
    public void reset() throws IOException {
        if (this.buffer == null) {
            throw new IOException("Stream is closed");
        }
        if (-1 == this.markPos) {
            throw new IOException("Mark has been invalidated");
        }
        this.pos = this.markPos;
    }

    @Override
    public long skip(long amount) throws IOException {
        byte[] localBuf = this.buffer;
        InputStream localIn = this.inputStream;
        if (localBuf == null) {
            throw new IOException("Stream is closed");
        }
        if (amount < 1L) {
            return 0L;
        }
        if (localIn == null) {
            throw new IOException("Stream is closed");
        }
        if ((long)(this.count - this.pos) >= amount) {
            this.pos += (int)amount;
            return amount;
        }
        int read = this.count - this.pos;
        this.pos = this.count;
        if (this.markPos != -1 && amount <= (long)this.markLimit) {
            if (this.fillBuffer(localIn, localBuf) == -1) {
                return read;
            }
            if ((long)(this.count - this.pos) >= amount - (long)read) {
                this.pos += (int)amount - read;
                return amount;
            }
            this.pos = this.count;
            return read += this.count - this.pos;
        }
        return (long)read + localIn.skip(amount - (long)read);
    }

    public static class Builder
    extends AbstractStreamBuilder<UnsynchronizedBufferedInputStream, Builder> {
        @Override
        public UnsynchronizedBufferedInputStream get() throws IOException {
            return new UnsynchronizedBufferedInputStream(this.getInputStream(), this.getBufferSize());
        }
    }
}

