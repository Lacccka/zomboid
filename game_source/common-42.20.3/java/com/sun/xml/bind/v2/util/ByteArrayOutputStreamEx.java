/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.util;

import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ByteArrayOutputStreamEx
extends ByteArrayOutputStream {
    public ByteArrayOutputStreamEx() {
    }

    public ByteArrayOutputStreamEx(int size) {
        super(size);
    }

    public void set(Base64Data dt, String mimeType) {
        dt.set(this.buf, this.count, mimeType);
    }

    public byte[] getBuffer() {
        return this.buf;
    }

    public void readFrom(InputStream is) throws IOException {
        while (true) {
            int sz;
            if (this.count == this.buf.length) {
                byte[] data = new byte[this.buf.length * 2];
                System.arraycopy(this.buf, 0, data, 0, this.buf.length);
                this.buf = data;
            }
            if ((sz = is.read(this.buf, this.count, this.buf.length - this.count)) < 0) {
                return;
            }
            this.count += sz;
        }
    }
}

