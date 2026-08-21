/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing;

import com.google.zxing.ReaderException;

public final class ChecksumException
extends ReaderException {
    private static final ChecksumException INSTANCE = new ChecksumException();

    private ChecksumException() {
    }

    private ChecksumException(Throwable cause) {
        super(cause);
    }

    public static ChecksumException getChecksumInstance() {
        return isStackTrace ? new ChecksumException() : INSTANCE;
    }

    public static ChecksumException getChecksumInstance(Throwable cause) {
        return isStackTrace ? new ChecksumException(cause) : INSTANCE;
    }

    static {
        INSTANCE.setStackTrace(NO_TRACE);
    }
}

