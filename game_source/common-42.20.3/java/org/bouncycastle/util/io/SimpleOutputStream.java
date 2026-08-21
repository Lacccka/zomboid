/*
 * Decompiled with CFR 0.152.
 */
package org.bouncycastle.util.io;

import java.io.IOException;
import java.io.OutputStream;

public abstract class SimpleOutputStream
extends OutputStream {
    public void close() {
    }

    public void flush() {
    }

    public void write(int n) throws IOException {
        byte[] byArray = new byte[]{(byte)n};
        this.write(byArray, 0, 1);
    }
}

