/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.binary;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.apache.commons.codec.binary.BaseNCodec;

public class BaseNCodecInputStream
extends FilterInputStream {
    private final BaseNCodec baseNCodec;
    private final boolean doEncode;
    private final byte[] singleByte = new byte[1];
    private final byte[] buf;
    private final BaseNCodec.Context context = new BaseNCodec.Context();

    protected BaseNCodecInputStream(InputStream inputStream2, BaseNCodec baseNCodec, boolean doEncode) {
        super(inputStream2);
        this.doEncode = doEncode;
        this.baseNCodec = baseNCodec;
        this.buf = new byte[doEncode ? 4096 : 8192];
    }

    @Override
    public int available() throws IOException {
        return this.context.eof ? 0 : 1;
    }

    public boolean isStrictDecoding() {
        return this.baseNCodec.isStrictDecoding();
    }

    @Override
    public synchronized void mark(int readLimit) {
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        int r = this.read(this.singleByte, 0, 1);
        while (r == 0) {
            r = this.read(this.singleByte, 0, 1);
        }
        if (r > 0) {
            int b = this.singleByte[0];
            return b < 0 ? 256 + b : b;
        }
        return -1;
    }

    @Override
    public int read(byte[] array, int offset, int len) throws IOException {
        int readLen;
        int read;
        Objects.requireNonNull(array, "array");
        if (offset < 0 || len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (offset > array.length || offset + len > array.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        for (readLen = 0; readLen < len; readLen += read) {
            if (!this.baseNCodec.hasData(this.context)) {
                int c = this.in.read(this.buf);
                if (this.doEncode) {
                    this.baseNCodec.encode(this.buf, 0, c, this.context);
                } else {
                    this.baseNCodec.decode(this.buf, 0, c, this.context);
                }
            }
            if ((read = this.baseNCodec.readResults(array, offset + readLen, len - readLen, this.context)) >= 0) continue;
            return readLen != 0 ? readLen : -1;
        }
        return readLen;
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public long skip(long n) throws IOException {
        long todo;
        int len;
        if (n < 0L) {
            throw new IllegalArgumentException("Negative skip length: " + n);
        }
        byte[] b = new byte[512];
        for (todo = n; todo > 0L; todo -= (long)len) {
            len = (int)Math.min((long)b.length, todo);
            if ((len = this.read(b, 0, len)) == -1) break;
        }
        return n - todo;
    }
}

