/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class CloseShieldFilterInputStream
extends FilterInputStream {
    public CloseShieldFilterInputStream(InputStream in) {
        super(in);
    }

    @Override
    public void close() throws IOException {
    }
}

