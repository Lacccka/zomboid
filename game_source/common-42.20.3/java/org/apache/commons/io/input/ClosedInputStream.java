/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;

public class ClosedInputStream
extends InputStream {
    public static final ClosedInputStream INSTANCE;
    @Deprecated
    public static final ClosedInputStream CLOSED_INPUT_STREAM;

    @Override
    public int read() {
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return -1;
    }

    static {
        CLOSED_INPUT_STREAM = INSTANCE = new ClosedInputStream();
    }
}

