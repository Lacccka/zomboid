/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

public final class UncheckedBufferedReader
extends BufferedReader {
    public static Builder builder() {
        return new Builder();
    }

    private UncheckedBufferedReader(Reader reader, int bufferSize) {
        super(reader, bufferSize);
    }

    @Override
    public void close() throws UncheckedIOException {
        Uncheck.run(() -> super.close());
    }

    @Override
    public void mark(int readAheadLimit) throws UncheckedIOException {
        Uncheck.accept(x$0 -> super.mark((int)x$0), readAheadLimit);
    }

    @Override
    public int read() throws UncheckedIOException {
        return Uncheck.get(() -> super.read());
    }

    @Override
    public int read(char[] cbuf) throws UncheckedIOException {
        return Uncheck.apply(x$0 -> super.read((char[])x$0), cbuf);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws UncheckedIOException {
        return Uncheck.apply((x$0, x$1, x$2) -> super.read((char[])x$0, (int)x$1, (int)x$2), cbuf, off, len);
    }

    @Override
    public int read(CharBuffer target) throws UncheckedIOException {
        return Uncheck.apply(x$0 -> super.read((CharBuffer)x$0), target);
    }

    @Override
    public String readLine() throws UncheckedIOException {
        return Uncheck.get(() -> super.readLine());
    }

    @Override
    public boolean ready() throws UncheckedIOException {
        return Uncheck.get(() -> super.ready());
    }

    @Override
    public void reset() throws UncheckedIOException {
        Uncheck.run(() -> super.reset());
    }

    @Override
    public long skip(long n) throws UncheckedIOException {
        return Uncheck.apply(x$0 -> super.skip((long)x$0), n);
    }

    public static class Builder
    extends AbstractStreamBuilder<UncheckedBufferedReader, Builder> {
        @Override
        public UncheckedBufferedReader get() {
            return Uncheck.get(() -> new UncheckedBufferedReader(this.getReader(), this.getBufferSize()));
        }
    }
}

