/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Objects;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.charset.CharsetEncoders;
import org.apache.commons.io.function.Uncheck;
import org.apache.commons.io.input.ReaderInputStream;

public class CharSequenceInputStream
extends InputStream {
    private static final int NO_MARK = -1;
    private final ByteBuffer bBuf;
    private int bBufMark;
    private final CharBuffer cBuf;
    private int cBufMark;
    private final CharsetEncoder charsetEncoder;

    public static Builder builder() {
        return new Builder();
    }

    private static CharsetEncoder newEncoder(Charset charset) {
        return Charsets.toCharset(charset).newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    @Deprecated
    public CharSequenceInputStream(CharSequence cs, Charset charset) {
        this(cs, charset, 8192);
    }

    @Deprecated
    public CharSequenceInputStream(CharSequence cs, Charset charset, int bufferSize) {
        this(cs, bufferSize, CharSequenceInputStream.newEncoder(charset));
    }

    private CharSequenceInputStream(CharSequence cs, int bufferSize, CharsetEncoder charsetEncoder) {
        this.charsetEncoder = charsetEncoder;
        this.bBuf = ByteBuffer.allocate(ReaderInputStream.checkMinBufferSize(charsetEncoder, bufferSize));
        this.bBuf.flip();
        this.cBuf = CharBuffer.wrap(cs);
        this.cBufMark = -1;
        this.bBufMark = -1;
        try {
            this.fillBuffer();
        }
        catch (CharacterCodingException ex) {
            this.bBuf.clear();
            this.bBuf.flip();
            this.cBuf.rewind();
        }
    }

    @Deprecated
    public CharSequenceInputStream(CharSequence cs, String charset) {
        this(cs, charset, 8192);
    }

    @Deprecated
    public CharSequenceInputStream(CharSequence cs, String charset, int bufferSize) {
        this(cs, Charsets.toCharset(charset), bufferSize);
    }

    @Override
    public int available() throws IOException {
        return this.bBuf.remaining();
    }

    @Override
    public void close() throws IOException {
    }

    private void fillBuffer() throws CharacterCodingException {
        this.bBuf.compact();
        CoderResult result = this.charsetEncoder.encode(this.cBuf, this.bBuf, true);
        if (result.isError()) {
            result.throwException();
        }
        this.bBuf.flip();
    }

    CharsetEncoder getCharsetEncoder() {
        return this.charsetEncoder;
    }

    @Override
    public synchronized void mark(int readLimit) {
        this.cBufMark = this.cBuf.position();
        this.bBufMark = this.bBuf.position();
        this.cBuf.mark();
        this.bBuf.mark();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        do {
            if (this.bBuf.hasRemaining()) {
                return this.bBuf.get() & 0xFF;
            }
            this.fillBuffer();
        } while (this.bBuf.hasRemaining() || this.cBuf.hasRemaining());
        return -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] array, int off, int len) throws IOException {
        Objects.requireNonNull(array, "array");
        if (len < 0 || off + len > array.length) {
            throw new IndexOutOfBoundsException("Array Size=" + array.length + ", offset=" + off + ", length=" + len);
        }
        if (len == 0) {
            return 0;
        }
        if (!this.bBuf.hasRemaining() && !this.cBuf.hasRemaining()) {
            return -1;
        }
        int bytesRead = 0;
        while (len > 0) {
            if (this.bBuf.hasRemaining()) {
                int chunk = Math.min(this.bBuf.remaining(), len);
                this.bBuf.get(array, off, chunk);
                off += chunk;
                len -= chunk;
                bytesRead += chunk;
                continue;
            }
            this.fillBuffer();
            if (this.bBuf.hasRemaining() || this.cBuf.hasRemaining()) continue;
        }
        return bytesRead == 0 && !this.cBuf.hasRemaining() ? -1 : bytesRead;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (this.cBufMark != -1) {
            if (this.cBuf.position() != 0) {
                this.charsetEncoder.reset();
                this.cBuf.rewind();
                this.bBuf.rewind();
                this.bBuf.limit(0);
                while (this.cBuf.position() < this.cBufMark) {
                    this.bBuf.rewind();
                    this.bBuf.limit(0);
                    this.fillBuffer();
                }
            }
            if (this.cBuf.position() != this.cBufMark) {
                throw new IllegalStateException("Unexpected CharBuffer position: actual=" + this.cBuf.position() + " expected=" + this.cBufMark);
            }
            this.bBuf.position(this.bBufMark);
            this.cBufMark = -1;
            this.bBufMark = -1;
        }
        this.mark(0);
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = 0L;
        while (n > 0L && this.available() > 0) {
            this.read();
            --n;
            ++skipped;
        }
        return skipped;
    }

    public static class Builder
    extends AbstractStreamBuilder<CharSequenceInputStream, Builder> {
        private CharsetEncoder charsetEncoder = CharSequenceInputStream.access$000(this.getCharset());

        @Override
        public CharSequenceInputStream get() {
            return Uncheck.get(() -> new CharSequenceInputStream(this.getCharSequence(), this.getBufferSize(), this.charsetEncoder));
        }

        CharsetEncoder getCharsetEncoder() {
            return this.charsetEncoder;
        }

        @Override
        public Builder setCharset(Charset charset) {
            super.setCharset(charset);
            this.charsetEncoder = CharSequenceInputStream.newEncoder(this.getCharset());
            return this;
        }

        public Builder setCharsetEncoder(CharsetEncoder newEncoder) {
            this.charsetEncoder = CharsetEncoders.toCharsetEncoder(newEncoder, () -> CharSequenceInputStream.newEncoder(this.getCharsetDefault()));
            super.setCharset(this.charsetEncoder.charset());
            return this;
        }
    }
}

