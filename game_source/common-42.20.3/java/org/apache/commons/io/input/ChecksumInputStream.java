/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.CountingInputStream;

public final class ChecksumInputStream
extends CountingInputStream {
    private final long expectedChecksumValue;
    private final long countThreshold;

    public static Builder builder() {
        return new Builder();
    }

    private ChecksumInputStream(InputStream in, Checksum checksum, long expectedChecksumValue, long countThreshold) {
        super(new CheckedInputStream(in, checksum));
        this.countThreshold = countThreshold;
        this.expectedChecksumValue = expectedChecksumValue;
    }

    @Override
    protected synchronized void afterRead(int n) throws IOException {
        super.afterRead(n);
        if ((this.countThreshold > 0L && this.getByteCount() >= this.countThreshold || n == -1) && this.expectedChecksumValue != this.getChecksum().getValue()) {
            throw new IOException("Checksum verification failed.");
        }
    }

    private Checksum getChecksum() {
        return ((CheckedInputStream)this.in).getChecksum();
    }

    public long getRemaining() {
        return this.countThreshold - this.getByteCount();
    }

    public static class Builder
    extends AbstractStreamBuilder<ChecksumInputStream, Builder> {
        private Checksum checksum;
        private long countThreshold = -1L;
        private long expectedChecksumValue;

        @Override
        public ChecksumInputStream get() throws IOException {
            return new ChecksumInputStream(this.getInputStream(), this.checksum, this.expectedChecksumValue, this.countThreshold);
        }

        public Builder setChecksum(Checksum checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder setCountThreshold(long countThreshold) {
            this.countThreshold = countThreshold;
            return this;
        }

        public Builder setExpectedChecksumValue(long expectedChecksumValue) {
            this.expectedChecksumValue = expectedChecksumValue;
            return this;
        }
    }
}

