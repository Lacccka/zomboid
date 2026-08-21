/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.output.AbstractByteArrayOutputStream;

public class ByteArrayOutputStream
extends AbstractByteArrayOutputStream {
    public static InputStream toBufferedInputStream(InputStream input) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input, 1024);
    }

    public static InputStream toBufferedInputStream(InputStream input, int size) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(size);){
            output.write(input);
            InputStream inputStream2 = output.toInputStream();
            return inputStream2;
        }
    }

    public ByteArrayOutputStream() {
        this(1024);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        ByteArrayOutputStream byteArrayOutputStream = this;
        synchronized (byteArrayOutputStream) {
            this.needNewBuffer(size);
        }
    }

    @Override
    public synchronized void reset() {
        this.resetImpl();
    }

    @Override
    public synchronized int size() {
        return this.count;
    }

    @Override
    public synchronized byte[] toByteArray() {
        return this.toByteArrayImpl();
    }

    @Override
    public synchronized InputStream toInputStream() {
        return this.toInputStream(ByteArrayInputStream::new);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(byte[] b, int off, int len) {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        ByteArrayOutputStream byteArrayOutputStream = this;
        synchronized (byteArrayOutputStream) {
            this.writeImpl(b, off, len);
        }
    }

    @Override
    public synchronized int write(InputStream in) throws IOException {
        return this.writeImpl(in);
    }

    @Override
    public synchronized void write(int b) {
        this.writeImpl(b);
    }

    @Override
    public synchronized void writeTo(OutputStream out) throws IOException {
        this.writeToImpl(out);
    }
}

