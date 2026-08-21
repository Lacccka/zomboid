/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing;

public abstract class ReaderException
extends Exception {
    protected static final boolean isStackTrace = System.getProperty("surefire.test.class.path") != null;
    protected static final StackTraceElement[] NO_TRACE = new StackTraceElement[0];

    ReaderException() {
    }

    ReaderException(Throwable cause) {
        super(cause);
    }

    @Override
    public final Throwable fillInStackTrace() {
        return null;
    }
}

