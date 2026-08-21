/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

public abstract class ProxyInputStream
extends FilterInputStream {
    public ProxyInputStream(InputStream proxy) {
        super(proxy);
    }

    protected void afterRead(int n) throws IOException {
    }

    @Override
    public int available() throws IOException {
        try {
            return super.available();
        }
        catch (IOException e) {
            this.handleIOException(e);
            return 0;
        }
    }

    protected void beforeRead(int n) throws IOException {
    }

    @Override
    public void close() throws IOException {
        IOUtils.close((Closeable)this.in, this::handleIOException);
    }

    protected void handleIOException(IOException e) throws IOException {
        throw e;
    }

    @Override
    public synchronized void mark(int readLimit) {
        this.in.mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        return this.in.markSupported();
    }

    @Override
    public int read() throws IOException {
        try {
            this.beforeRead(1);
            int b = this.in.read();
            this.afterRead(b != -1 ? 1 : -1);
            return b;
        }
        catch (IOException e) {
            this.handleIOException(e);
            return -1;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            this.beforeRead(IOUtils.length(b));
            int n = this.in.read(b);
            this.afterRead(n);
            return n;
        }
        catch (IOException e) {
            this.handleIOException(e);
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            this.beforeRead(len);
            int n = this.in.read(b, off, len);
            this.afterRead(n);
            return n;
        }
        catch (IOException e) {
            this.handleIOException(e);
            return -1;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        try {
            this.in.reset();
        }
        catch (IOException e) {
            this.handleIOException(e);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            return this.in.skip(n);
        }
        catch (IOException e) {
            this.handleIOException(e);
            return 0L;
        }
    }

    public InputStream unwrap() {
        return this.in;
    }
}

