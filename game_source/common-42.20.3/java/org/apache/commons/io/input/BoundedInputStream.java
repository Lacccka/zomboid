/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.ProxyInputStream;

public class BoundedInputStream
extends ProxyInputStream {
    private long count;
    private long mark;
    private final long maxCount;
    private boolean propagateClose = true;

    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public BoundedInputStream(InputStream in) {
        this(in, -1L);
    }

    @Deprecated
    public BoundedInputStream(InputStream inputStream2, long maxCount) {
        this(inputStream2, 0L, maxCount, true);
    }

    BoundedInputStream(InputStream inputStream2, long count, long maxCount, boolean propagateClose) {
        super(inputStream2);
        this.count = count;
        this.maxCount = maxCount;
        this.propagateClose = propagateClose;
    }

    @Override
    protected synchronized void afterRead(int n) throws IOException {
        if (n != -1) {
            this.count += (long)n;
        }
    }

    @Override
    public int available() throws IOException {
        if (this.isMaxCount()) {
            this.onMaxLength(this.maxCount, this.getCount());
            return 0;
        }
        return this.in.available();
    }

    @Override
    public void close() throws IOException {
        if (this.propagateClose) {
            this.in.close();
        }
    }

    public synchronized long getCount() {
        return this.count;
    }

    public long getMaxCount() {
        return this.maxCount;
    }

    @Deprecated
    public long getMaxLength() {
        return this.maxCount;
    }

    public long getRemaining() {
        return Math.max(0L, this.getMaxCount() - this.getCount());
    }

    private boolean isMaxCount() {
        return this.maxCount >= 0L && this.getCount() >= this.maxCount;
    }

    public boolean isPropagateClose() {
        return this.propagateClose;
    }

    @Override
    public synchronized void mark(int readLimit) {
        this.in.mark(readLimit);
        this.mark = this.count;
    }

    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }

    protected void onMaxLength(long maxLength, long count) throws IOException {
    }

    @Override
    public int read() throws IOException {
        if (this.isMaxCount()) {
            this.onMaxLength(this.maxCount, this.getCount());
            return -1;
        }
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.isMaxCount()) {
            this.onMaxLength(this.maxCount, this.getCount());
            return -1;
        }
        return super.read(b, off, (int)this.toReadLen(len));
    }

    @Override
    public synchronized void reset() throws IOException {
        this.in.reset();
        this.count = this.mark;
    }

    @Deprecated
    public void setPropagateClose(boolean propagateClose) {
        this.propagateClose = propagateClose;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        long skip = super.skip(this.toReadLen(n));
        this.count += skip;
        return skip;
    }

    private long toReadLen(long len) {
        return this.maxCount >= 0L ? Math.min(len, this.maxCount - this.getCount()) : len;
    }

    public String toString() {
        return this.in.toString();
    }

    public static class Builder
    extends AbstractBuilder<Builder> {
        @Override
        public BoundedInputStream get() throws IOException {
            return new BoundedInputStream(this.getInputStream(), this.getCount(), this.getMaxCount(), this.isPropagateClose());
        }
    }

    static abstract class AbstractBuilder<T extends AbstractBuilder<T>>
    extends AbstractStreamBuilder<BoundedInputStream, T> {
        private long count;
        private long maxCount = -1L;
        private boolean propagateClose = true;

        AbstractBuilder() {
        }

        long getCount() {
            return this.count;
        }

        long getMaxCount() {
            return this.maxCount;
        }

        boolean isPropagateClose() {
            return this.propagateClose;
        }

        public T setCount(long count) {
            this.count = Math.max(0L, count);
            return (T)((AbstractBuilder)this.asThis());
        }

        public T setMaxCount(long maxCount) {
            this.maxCount = Math.max(-1L, maxCount);
            return (T)((AbstractBuilder)this.asThis());
        }

        public T setPropagateClose(boolean propagateClose) {
            this.propagateClose = propagateClose;
            return (T)((AbstractBuilder)this.asThis());
        }
    }
}

