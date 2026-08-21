/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing;

import com.google.zxing.ReaderException;

public final class NotFoundException
extends ReaderException {
    private static final NotFoundException INSTANCE = new NotFoundException();

    private NotFoundException() {
    }

    public static NotFoundException getNotFoundInstance() {
        return INSTANCE;
    }

    static {
        INSTANCE.setStackTrace(NO_TRACE);
    }
}

