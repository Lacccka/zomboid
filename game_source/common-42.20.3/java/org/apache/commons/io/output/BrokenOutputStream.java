/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;
import org.apache.commons.io.function.Erase;

public class BrokenOutputStream
extends OutputStream {
    public static final BrokenOutputStream INSTANCE = new BrokenOutputStream();
    private final Supplier<Throwable> exceptionSupplier;

    public BrokenOutputStream() {
        this(() -> new IOException("Broken output stream"));
    }

    @Deprecated
    public BrokenOutputStream(IOException exception) {
        this(() -> exception);
    }

    public BrokenOutputStream(Supplier<Throwable> exceptionSupplier) {
        this.exceptionSupplier = exceptionSupplier;
    }

    public BrokenOutputStream(Throwable exception) {
        this(() -> exception);
    }

    @Override
    public void close() throws IOException {
        throw this.rethrow();
    }

    @Override
    public void flush() throws IOException {
        throw this.rethrow();
    }

    private RuntimeException rethrow() {
        return Erase.rethrow(this.exceptionSupplier.get());
    }

    @Override
    public void write(int b) throws IOException {
        throw this.rethrow();
    }
}

