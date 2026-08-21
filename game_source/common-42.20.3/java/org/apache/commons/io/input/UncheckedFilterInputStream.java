/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

public final class UncheckedFilterInputStream
extends FilterInputStream {
    public static Builder builder() {
        return new Builder();
    }

    private UncheckedFilterInputStream(InputStream inputStream2) {
        super(inputStream2);
    }

    @Override
    public int available() throws UncheckedIOException {
        return Uncheck.get(() -> super.available());
    }

    @Override
    public void close() throws UncheckedIOException {
        Uncheck.run(() -> super.close());
    }

    @Override
    public int read() throws UncheckedIOException {
        return Uncheck.get(() -> super.read());
    }

    @Override
    public int read(byte[] b) throws UncheckedIOException {
        return Uncheck.apply(x$0 -> super.read((byte[])x$0), b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws UncheckedIOException {
        return Uncheck.apply((x$0, x$1, x$2) -> super.read((byte[])x$0, (int)x$1, (int)x$2), b, off, len);
    }

    @Override
    public synchronized void reset() throws UncheckedIOException {
        Uncheck.run(() -> super.reset());
    }

    @Override
    public long skip(long n) throws UncheckedIOException {
        return Uncheck.apply(x$0 -> super.skip((long)x$0), n);
    }

    public static class Builder
    extends AbstractStreamBuilder<UncheckedFilterInputStream, Builder> {
        @Override
        public UncheckedFilterInputStream get() {
            return Uncheck.get(() -> new UncheckedFilterInputStream(this.getInputStream()));
        }
    }
}

