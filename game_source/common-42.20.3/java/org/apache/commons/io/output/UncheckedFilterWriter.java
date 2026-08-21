/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;

public final class UncheckedFilterWriter
extends FilterWriter {
    public static Builder builder() {
        return new Builder();
    }

    private UncheckedFilterWriter(Writer writer) {
        super(writer);
    }

    @Override
    public Writer append(char c) throws UncheckedIOException {
        return Uncheck.apply(x$0 -> super.append((char)x$0), Character.valueOf(c));
    }

    @Override
    public Writer append(CharSequence csq) throws UncheckedIOException {
        return Uncheck.apply(x$0 -> super.append((CharSequence)x$0), csq);
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws UncheckedIOException {
        return Uncheck.apply((x$0, x$1, x$2) -> super.append((CharSequence)x$0, (int)x$1, (int)x$2), csq, start, end);
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
    public void write(char[] cbuf) throws UncheckedIOException {
        Uncheck.accept(x$0 -> super.write((char[])x$0), cbuf);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws UncheckedIOException {
        Uncheck.accept((x$0, x$1, x$2) -> super.write((char[])x$0, (int)x$1, (int)x$2), cbuf, off, len);
    }

    @Override
    public void write(int c) throws UncheckedIOException {
        Uncheck.accept(x$0 -> super.write((int)x$0), c);
    }

    @Override
    public void write(String str) throws UncheckedIOException {
        Uncheck.accept(x$0 -> super.write((String)x$0), str);
    }

    @Override
    public void write(String str, int off, int len) throws UncheckedIOException {
        Uncheck.accept((x$0, x$1, x$2) -> super.write((String)x$0, (int)x$1, (int)x$2), str, off, len);
    }

    public static class Builder
    extends AbstractStreamBuilder<UncheckedFilterWriter, Builder> {
        @Override
        public UncheckedFilterWriter get() throws IOException {
            return new UncheckedFilterWriter(this.getWriter());
        }
    }
}

