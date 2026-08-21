/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public abstract class CompressorOutputStream<T extends OutputStream>
extends FilterOutputStream {
    public CompressorOutputStream() {
        super(null);
    }

    public CompressorOutputStream(T out) {
        super((OutputStream)out);
    }

    protected T out() {
        return (T)this.out;
    }
}

