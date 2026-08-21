/*
 * Decompiled with CFR 0.152.
 */
package io.vavr;

public class NotImplementedError
extends Error {
    private static final long serialVersionUID = 1L;

    public NotImplementedError() {
        super("An implementation is missing.");
    }

    public NotImplementedError(String message) {
        super(message);
    }
}

