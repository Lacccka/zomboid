/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

public final class UncheckedFilterOutputStream
extends FilterOutputStream {
    public static Builder builder() {
        return new Builder();
    }

    private UncheckedFilterOutputStream(OutputStream outputStream2) {
        super(outputStream2);
    }

    @Override
    public void close() throws UncheckedIOException {
        Uncheck.run(() -> super.close());
    }

    @Override
    public void flush() throws UncheckedIOException {
        Uncheck.run(() -> super.flush());
    }

    @Override
    public void write(byte[] b) throws UncheckedIOException {
        Uncheck.accept(x$0 -> super.write((byte[])x$0), b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws UncheckedIOException {
        Uncheck.accept((x$0, x$1, x$2) -> super.write((byte[])x$0, (int)x$1, (int)x$2), b, off, len);
    }

    @Override
    public void write(int b) throws UncheckedIOException {
        Uncheck.accept(x$0 -> super.write((int)x$0), b);
    }

    public static class Builder
    extends AbstractStreamBuilder<UncheckedFilterOutputStream, Builder> {
        @Override
        public UncheckedFilterOutputStream get() throws IOException {
            return new UncheckedFilterOutputStream(this.getOutputStream());
        }
    }
}

