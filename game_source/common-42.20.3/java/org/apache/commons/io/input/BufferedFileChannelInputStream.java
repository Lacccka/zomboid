/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.ByteBufferCleaner;

public final class BufferedFileChannelInputStream
extends InputStream {
    private final ByteBuffer byteBuffer;
    private final FileChannel fileChannel;

    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public BufferedFileChannelInputStream(File file) throws IOException {
        this(file, 8192);
    }

    @Deprecated
    public BufferedFileChannelInputStream(File file, int bufferSize) throws IOException {
        this(file.toPath(), bufferSize);
    }

    @Deprecated
    public BufferedFileChannelInputStream(Path path) throws IOException {
        this(path, 8192);
    }

    @Deprecated
    public BufferedFileChannelInputStream(Path path, int bufferSize) throws IOException {
        Objects.requireNonNull(path, "path");
        this.fileChannel = FileChannel.open(path, StandardOpenOption.READ);
        this.byteBuffer = ByteBuffer.allocateDirect(bufferSize);
        this.byteBuffer.flip();
    }

    @Override
    public synchronized int available() throws IOException {
        return this.byteBuffer.remaining();
    }

    private void clean(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            this.cleanDirectBuffer(buffer);
        }
    }

    private void cleanDirectBuffer(ByteBuffer buffer) {
        if (ByteBufferCleaner.isSupported()) {
            ByteBufferCleaner.clean(buffer);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            this.fileChannel.close();
        }
        finally {
            this.clean(this.byteBuffer);
        }
    }

    @Override
    public synchronized int read() throws IOException {
        if (!this.refill()) {
            return -1;
        }
        return this.byteBuffer.get() & 0xFF;
    }

    @Override
    public synchronized int read(byte[] b, int offset, int len) throws IOException {
        if (offset < 0 || len < 0 || offset + len < 0 || offset + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (!this.refill()) {
            return -1;
        }
        len = Math.min(len, this.byteBuffer.remaining());
        this.byteBuffer.get(b, offset, len);
        return len;
    }

    private boolean refill() throws IOException {
        if (!this.byteBuffer.hasRemaining()) {
            this.byteBuffer.clear();
            int nRead = 0;
            while (nRead == 0) {
                nRead = this.fileChannel.read(this.byteBuffer);
            }
            this.byteBuffer.flip();
            return nRead >= 0;
        }
        return true;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        if (n <= 0L) {
            return 0L;
        }
        if ((long)this.byteBuffer.remaining() >= n) {
            this.byteBuffer.position(this.byteBuffer.position() + (int)n);
            return n;
        }
        long skippedFromBuffer = this.byteBuffer.remaining();
        long toSkipFromFileChannel = n - skippedFromBuffer;
        this.byteBuffer.position(0);
        this.byteBuffer.flip();
        return skippedFromBuffer + this.skipFromFileChannel(toSkipFromFileChannel);
    }

    private long skipFromFileChannel(long n) throws IOException {
        long currentFilePosition = this.fileChannel.position();
        long size = this.fileChannel.size();
        if (n > size - currentFilePosition) {
            this.fileChannel.position(size);
            return size - currentFilePosition;
        }
        this.fileChannel.position(currentFilePosition + n);
        return n;
    }

    public static class Builder
    extends AbstractStreamBuilder<BufferedFileChannelInputStream, Builder> {
        @Override
        public BufferedFileChannelInputStream get() throws IOException {
            return new BufferedFileChannelInputStream(this.getPath(), this.getBufferSize());
        }
    }
}

