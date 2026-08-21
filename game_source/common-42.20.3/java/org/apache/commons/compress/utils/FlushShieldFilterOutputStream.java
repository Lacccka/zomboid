/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FlushShieldFilterOutputStream
extends FilterOutputStream {
    public FlushShieldFilterOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void flush() throws IOException {
    }
}

