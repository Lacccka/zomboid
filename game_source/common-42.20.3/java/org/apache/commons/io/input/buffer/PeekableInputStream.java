/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.apache.commons.io.input.buffer.CircularBufferInputStream;

public class PeekableInputStream
extends CircularBufferInputStream {
    public PeekableInputStream(InputStream inputStream2) {
        super(inputStream2);
    }

    public PeekableInputStream(InputStream inputStream2, int bufferSize) {
        super(inputStream2, bufferSize);
    }

    public boolean peek(byte[] sourceBuffer) throws IOException {
        Objects.requireNonNull(sourceBuffer, "sourceBuffer");
        return this.peek(sourceBuffer, 0, sourceBuffer.length);
    }

    public boolean peek(byte[] sourceBuffer, int offset, int length) throws IOException {
        Objects.requireNonNull(sourceBuffer, "sourceBuffer");
        if (sourceBuffer.length > this.bufferSize) {
            throw new IllegalArgumentException("Peek request size of " + sourceBuffer.length + " bytes exceeds buffer size of " + this.bufferSize + " bytes");
        }
        if (this.buffer.getCurrentNumberOfBytes() < sourceBuffer.length) {
            this.fillBuffer();
        }
        return this.buffer.peek(sourceBuffer, offset, length);
    }
}

