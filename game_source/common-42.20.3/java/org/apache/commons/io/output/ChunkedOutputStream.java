/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.build.AbstractStreamBuilder;

public class ChunkedOutputStream
extends FilterOutputStream {
    private final int chunkSize;

    public static Builder builder() {
        return new Builder();
    }

    @Deprecated
    public ChunkedOutputStream(OutputStream stream) {
        this(stream, 8192);
    }

    @Deprecated
    public ChunkedOutputStream(OutputStream stream, int chunkSize) {
        super(stream);
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize <= 0");
        }
        this.chunkSize = chunkSize;
    }

    int getChunkSize() {
        return this.chunkSize;
    }

    @Override
    public void write(byte[] data, int srcOffset, int length) throws IOException {
        int bytes = length;
        int dstOffset = srcOffset;
        while (bytes > 0) {
            int chunk = Math.min(bytes, this.chunkSize);
            this.out.write(data, dstOffset, chunk);
            bytes -= chunk;
            dstOffset += chunk;
        }
    }

    public static class Builder
    extends AbstractStreamBuilder<ChunkedOutputStream, Builder> {
        @Override
        public ChunkedOutputStream get() throws IOException {
            return new ChunkedOutputStream(this.getOutputStream(), this.getBufferSize());
        }
    }
}

