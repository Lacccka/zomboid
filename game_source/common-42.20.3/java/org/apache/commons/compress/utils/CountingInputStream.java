/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

@Deprecated
public class CountingInputStream
extends FilterInputStream {
    private long bytesRead;

    public CountingInputStream(InputStream in) {
        super(in);
    }

    protected final void count(long read) {
        if (read != -1L) {
            this.bytesRead += read;
        }
    }

    public long getBytesRead() {
        return this.bytesRead;
    }

    @Override
    public int read() throws IOException {
        int r = this.in.read();
        if (r >= 0) {
            this.count(1L);
        }
        return r;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int r = this.in.read(b, off, len);
        if (r >= 0) {
            this.count(r);
        }
        return r;
    }
}

