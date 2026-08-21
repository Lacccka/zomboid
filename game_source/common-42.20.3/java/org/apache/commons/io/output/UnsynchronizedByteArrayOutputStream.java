/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.function.Uncheck;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.AbstractByteArrayOutputStream;

public final class UnsynchronizedByteArrayOutputStream
extends AbstractByteArrayOutputStream {
    public static Builder builder() {
        return new Builder();
    }

    public static InputStream toBufferedInputStream(InputStream input) throws IOException {
        return UnsynchronizedByteArrayOutputStream.toBufferedInputStream(input, 1024);
    }

    public static InputStream toBufferedInputStream(InputStream input, int size) throws IOException {
        try (UnsynchronizedByteArrayOutputStream output = ((Builder)UnsynchronizedByteArrayOutputStream.builder().setBufferSize(size)).get();){
            output.write(input);
            InputStream inputStream2 = output.toInputStream();
            return inputStream2;
        }
    }

    @Deprecated
    public UnsynchronizedByteArrayOutputStream() {
        this(1024);
    }

    @Deprecated
    public UnsynchronizedByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        this.needNewBuffer(size);
    }

    @Override
    public void reset() {
        this.resetImpl();
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public byte[] toByteArray() {
        return this.toByteArrayImpl();
    }

    @Override
    public InputStream toInputStream() {
        return this.toInputStream((buffer, offset, length) -> Uncheck.get(() -> UnsynchronizedByteArrayInputStream.builder().setByteArray(buffer).setOffset(offset).setLength(length).get()));
    }

    @Override
    public void write(byte[] b, int off, int len) {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException(String.format("offset=%,d, length=%,d", off, len));
        }
        if (len == 0) {
            return;
        }
        this.writeImpl(b, off, len);
    }

    @Override
    public int write(InputStream in) throws IOException {
        return this.writeImpl(in);
    }

    @Override
    public void write(int b) {
        this.writeImpl(b);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        this.writeToImpl(out);
    }

    public static class Builder
    extends AbstractStreamBuilder<UnsynchronizedByteArrayOutputStream, Builder> {
        @Override
        public UnsynchronizedByteArrayOutputStream get() {
            return new UnsynchronizedByteArrayOutputStream(this.getBufferSize());
        }
    }
}

