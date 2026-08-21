/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

public final class ByteArrayDataSource
implements DataSource {
    private final String contentType;
    private final byte[] buf;
    private final int len;

    public ByteArrayDataSource(byte[] buf, String contentType) {
        this(buf, buf.length, contentType);
    }

    public ByteArrayDataSource(byte[] buf, int length, String contentType) {
        this.buf = buf;
        this.len = length;
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        if (this.contentType == null) {
            return "application/octet-stream";
        }
        return this.contentType;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.buf, 0, this.len);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }
}

