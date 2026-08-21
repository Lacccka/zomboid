/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

@Deprecated
public class ChecksumVerifyingInputStream
extends CheckedInputStream {
    private long remaining;
    private final long expected;

    public ChecksumVerifyingInputStream(Checksum checksum, InputStream in, long size, long expectedChecksum) {
        super(in, checksum);
        this.expected = expectedChecksum;
        this.remaining = size;
    }

    public long getBytesRemaining() {
        return this.remaining;
    }

    @Override
    public int read() throws IOException {
        if (this.remaining <= 0L) {
            return -1;
        }
        int data = super.read();
        if (data >= 0) {
            --this.remaining;
        }
        this.verify();
        return data;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int readCount = super.read(b, off, len);
        if (readCount >= 0) {
            this.remaining -= (long)readCount;
        }
        this.verify();
        return readCount;
    }

    private void verify() throws IOException {
        if (this.remaining <= 0L && this.expected != this.getChecksum().getValue()) {
            throw new IOException("Checksum verification failed");
        }
    }
}

