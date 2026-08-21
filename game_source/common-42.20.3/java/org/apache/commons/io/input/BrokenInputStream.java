/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;
import org.apache.commons.io.function.Erase;

public class BrokenInputStream
extends InputStream {
    public static final BrokenInputStream INSTANCE = new BrokenInputStream();
    private final Supplier<Throwable> exceptionSupplier;

    public BrokenInputStream() {
        this(() -> new IOException("Broken input stream"));
    }

    @Deprecated
    public BrokenInputStream(IOException exception) {
        this(() -> exception);
    }

    public BrokenInputStream(Supplier<Throwable> exceptionSupplier) {
        this.exceptionSupplier = exceptionSupplier;
    }

    public BrokenInputStream(Throwable exception) {
        this(() -> exception);
    }

    @Override
    public int available() throws IOException {
        throw this.rethrow();
    }

    @Override
    public void close() throws IOException {
        throw this.rethrow();
    }

    @Override
    public int read() throws IOException {
        throw this.rethrow();
    }

    @Override
    public synchronized void reset() throws IOException {
        throw this.rethrow();
    }

    private RuntimeException rethrow() {
        return Erase.rethrow(this.exceptionSupplier.get());
    }

    @Override
    public long skip(long n) throws IOException {
        throw this.rethrow();
    }
}

