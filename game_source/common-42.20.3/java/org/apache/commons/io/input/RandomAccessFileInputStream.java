/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;
import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.build.AbstractStreamBuilder;

public class RandomAccessFileInputStream
extends InputStream {
    private final boolean closeOnClose;
    private final RandomAccessFile randomAccessFile;

    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public RandomAccessFileInputStream(RandomAccessFile file) {
        this(file, false);
    }

    @Deprecated
    public RandomAccessFileInputStream(RandomAccessFile file, boolean closeOnClose) {
        this.randomAccessFile = Objects.requireNonNull(file, "file");
        this.closeOnClose = closeOnClose;
    }

    @Override
    public int available() throws IOException {
        long avail = this.availableLong();
        if (avail > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int)avail;
    }

    public long availableLong() throws IOException {
        return this.randomAccessFile.length() - this.randomAccessFile.getFilePointer();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (this.closeOnClose) {
            this.randomAccessFile.close();
        }
    }

    public RandomAccessFile getRandomAccessFile() {
        return this.randomAccessFile;
    }

    public boolean isCloseOnClose() {
        return this.closeOnClose;
    }

    @Override
    public int read() throws IOException {
        return this.randomAccessFile.read();
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return this.randomAccessFile.read(bytes);
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        return this.randomAccessFile.read(bytes, offset, length);
    }

    @Override
    public long skip(long skipCount) throws IOException {
        long newPos;
        long fileLength;
        if (skipCount <= 0L) {
            return 0L;
        }
        long filePointer = this.randomAccessFile.getFilePointer();
        if (filePointer >= (fileLength = this.randomAccessFile.length())) {
            return 0L;
        }
        long targetPos = filePointer + skipCount;
        long l = newPos = targetPos > fileLength ? fileLength - 1L : targetPos;
        if (newPos > 0L) {
            this.randomAccessFile.seek(newPos);
        }
        return this.randomAccessFile.getFilePointer() - filePointer;
    }

    public static class Builder
    extends AbstractStreamBuilder<RandomAccessFileInputStream, Builder> {
        private RandomAccessFile randomAccessFile;
        private boolean closeOnClose;

        @Override
        public RandomAccessFileInputStream get() throws IOException {
            if (this.randomAccessFile != null) {
                if (this.getOrigin() != null) {
                    throw new IllegalStateException(String.format("Only set one of RandomAccessFile (%s) or origin (%s)", this.randomAccessFile, this.getOrigin()));
                }
                return new RandomAccessFileInputStream(this.randomAccessFile, this.closeOnClose);
            }
            return new RandomAccessFileInputStream(RandomAccessFileMode.READ_ONLY.create(this.checkOrigin().getFile()), this.closeOnClose);
        }

        public Builder setCloseOnClose(boolean closeOnClose) {
            this.closeOnClose = closeOnClose;
            return this;
        }

        public Builder setRandomAccessFile(RandomAccessFile randomAccessFile) {
            this.randomAccessFile = randomAccessFile;
            return this;
        }
    }
}

