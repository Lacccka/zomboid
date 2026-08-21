/*
 * Decompiled with CFR 0.152.
 */
package org.json;

import java.io.IOException;
import java.io.Writer;

public class StringBuilderWriter
extends Writer {
    private final StringBuilder builder;

    public StringBuilderWriter() {
        this.builder = new StringBuilder();
        this.lock = this.builder;
    }

    public StringBuilderWriter(int initialSize) {
        this.builder = new StringBuilder(initialSize);
        this.lock = this.builder;
    }

    @Override
    public void write(int c) {
        this.builder.append((char)c);
    }

    @Override
    public void write(char[] cbuf, int offset, int length) {
        if (offset < 0 || offset > cbuf.length || length < 0 || offset + length > cbuf.length || offset + length < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) {
            return;
        }
        this.builder.append(cbuf, offset, length);
    }

    @Override
    public void write(String str) {
        this.builder.append(str);
    }

    @Override
    public void write(String str, int offset, int length) {
        this.builder.append(str, offset, offset + length);
    }

    @Override
    public StringBuilderWriter append(CharSequence csq) {
        this.write(String.valueOf(csq));
        return this;
    }

    @Override
    public StringBuilderWriter append(CharSequence csq, int start, int end) {
        if (csq == null) {
            csq = "null";
        }
        return this.append(csq.subSequence(start, end));
    }

    @Override
    public StringBuilderWriter append(char c) {
        this.write(c);
        return this;
    }

    public String toString() {
        return this.builder.toString();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws IOException {
    }
}

