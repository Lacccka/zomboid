/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.compress.utils.ByteUtils;

public class SeekableInMemoryByteChannel
implements SeekableByteChannel {
    private static final int NAIVE_RESIZE_LIMIT = 0x3FFFFFFF;
    private byte[] data;
    private final AtomicBoolean closed = new AtomicBoolean();
    private int position;
    private int size;

    public SeekableInMemoryByteChannel() {
        this(ByteUtils.EMPTY_BYTE_ARRAY);
    }

    public SeekableInMemoryByteChannel(byte[] data) {
        this.data = data;
        this.size = data.length;
    }

    public SeekableInMemoryByteChannel(int size) {
        this(new byte[size]);
    }

    public byte[] array() {
        return this.data;
    }

    @Override
    public void close() {
        this.closed.set(true);
    }

    private void ensureOpen() throws ClosedChannelException {
        if (!this.isOpen()) {
            throw new ClosedChannelException();
        }
    }

    @Override
    public boolean isOpen() {
        return !this.closed.get();
    }

    @Override
    public long position() {
        return this.position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        this.ensureOpen();
        if (newPosition < 0L || newPosition > Integer.MAX_VALUE) {
            throw new IOException("Position has to be in range 0.. 2147483647");
        }
        this.position = (int)newPosition;
        return this;
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        this.ensureOpen();
        int wanted = buf.remaining();
        int possible = this.size - this.position;
        if (possible <= 0) {
            return -1;
        }
        if (wanted > possible) {
            wanted = possible;
        }
        buf.put(this.data, this.position, wanted);
        this.position += wanted;
        return wanted;
    }

    private void resize(int newLength) {
        int len = this.data.length;
        if (len <= 0) {
            len = 1;
        }
        if (newLength < 0x3FFFFFFF) {
            while (len < newLength) {
                len <<= 1;
            }
        } else {
            len = newLength;
        }
        this.data = Arrays.copyOf(this.data, len);
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public SeekableByteChannel truncate(long newSize) {
        if (newSize < 0L || newSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size has to be in range 0.. 2147483647");
        }
        if ((long)this.size > newSize) {
            this.size = (int)newSize;
        }
        if ((long)this.position > newSize) {
            this.position = (int)newSize;
        }
        return this;
    }

    @Override
    public int write(ByteBuffer b) throws IOException {
        this.ensureOpen();
        int wanted = b.remaining();
        int possibleWithoutResize = this.size - this.position;
        if (wanted > possibleWithoutResize) {
            int newSize = this.position + wanted;
            if (newSize < 0) {
                this.resize(Integer.MAX_VALUE);
                wanted = Integer.MAX_VALUE - this.position;
            } else {
                this.resize(newSize);
            }
        }
        b.get(this.data, this.position, wanted);
        this.position += wanted;
        if (this.size < this.position) {
            this.size = this.position;
        }
        return wanted;
    }
}

