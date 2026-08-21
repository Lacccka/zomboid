/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.ByteBufferCleaner;

public final class MemoryMappedFileInputStream
extends InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 262144;
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]).asReadOnlyBuffer();
    private final int bufferSize;
    private final FileChannel channel;
    private ByteBuffer buffer = EMPTY_BUFFER;
    private boolean closed;
    private long nextBufferPosition;

    public static Builder builder() {
        return new Builder();
    }

    private MemoryMappedFileInputStream(Path file, int bufferSize) throws IOException {
        this.bufferSize = bufferSize;
        this.channel = FileChannel.open(file, StandardOpenOption.READ);
    }

    @Override
    public int available() throws IOException {
        return this.buffer.remaining();
    }

    private void cleanBuffer() {
        if (ByteBufferCleaner.isSupported() && this.buffer.isDirect()) {
            ByteBufferCleaner.clean(this.buffer);
        }
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.cleanBuffer();
            this.buffer = null;
            this.channel.close();
            this.closed = true;
        }
    }

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    int getBufferSize() {
        return this.bufferSize;
    }

    private void nextBuffer() throws IOException {
        long remainingInFile = this.channel.size() - this.nextBufferPosition;
        if (remainingInFile > 0L) {
            long amountToMap = Math.min(remainingInFile, (long)this.bufferSize);
            this.cleanBuffer();
            this.buffer = this.channel.map(FileChannel.MapMode.READ_ONLY, this.nextBufferPosition, amountToMap);
            this.nextBufferPosition += amountToMap;
        } else {
            this.buffer = EMPTY_BUFFER;
        }
    }

    @Override
    public int read() throws IOException {
        this.ensureOpen();
        if (!this.buffer.hasRemaining()) {
            this.nextBuffer();
            if (!this.buffer.hasRemaining()) {
                return -1;
            }
        }
        return Short.toUnsignedInt(this.buffer.get());
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.ensureOpen();
        if (!this.buffer.hasRemaining()) {
            this.nextBuffer();
            if (!this.buffer.hasRemaining()) {
                return -1;
            }
        }
        int numBytes = Math.min(this.buffer.remaining(), len);
        this.buffer.get(b, off, numBytes);
        return numBytes;
    }

    @Override
    public long skip(long n) throws IOException {
        this.ensureOpen();
        if (n <= 0L) {
            return 0L;
        }
        if (n <= (long)this.buffer.remaining()) {
            this.buffer.position((int)((long)this.buffer.position() + n));
            return n;
        }
        long remainingInFile = this.channel.size() - this.nextBufferPosition;
        long skipped = (long)this.buffer.remaining() + Math.min(remainingInFile, n - (long)this.buffer.remaining());
        this.nextBufferPosition += skipped - (long)this.buffer.remaining();
        this.nextBuffer();
        return skipped;
    }

    public static class Builder
    extends AbstractStreamBuilder<MemoryMappedFileInputStream, Builder> {
        public Builder() {
            this.setBufferSizeDefault(262144);
            this.setBufferSize(262144);
        }

        @Override
        public MemoryMappedFileInputStream get() throws IOException {
            return new MemoryMappedFileInputStream(this.getPath(), this.getBufferSize());
        }
    }
}

