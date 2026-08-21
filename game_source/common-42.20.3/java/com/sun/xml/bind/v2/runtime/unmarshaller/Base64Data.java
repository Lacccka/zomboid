/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.istack.Nullable;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import com.sun.xml.bind.v2.util.ByteArrayOutputStreamEx;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public final class Base64Data
extends Pcdata {
    private DataHandler dataHandler;
    private byte[] data;
    private int dataLen;
    @Nullable
    private String mimeType;

    public void set(byte[] data, int len, @Nullable String mimeType) {
        this.data = data;
        this.dataLen = len;
        this.dataHandler = null;
        this.mimeType = mimeType;
    }

    public void set(byte[] data, @Nullable String mimeType) {
        this.set(data, data.length, mimeType);
    }

    public void set(DataHandler data) {
        assert (data != null);
        this.dataHandler = data;
        this.data = null;
    }

    public DataHandler getDataHandler() {
        if (this.dataHandler == null) {
            this.dataHandler = new DataHandler(new DataSource(){

                @Override
                public String getContentType() {
                    return Base64Data.this.getMimeType();
                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(Base64Data.this.data, 0, Base64Data.this.dataLen);
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public OutputStream getOutputStream() {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return this.dataHandler;
    }

    public byte[] getExact() {
        this.get();
        if (this.dataLen != this.data.length) {
            byte[] buf = new byte[this.dataLen];
            System.arraycopy(this.data, 0, buf, 0, this.dataLen);
            this.data = buf;
        }
        return this.data;
    }

    public InputStream getInputStream() throws IOException {
        if (this.dataHandler != null) {
            return this.dataHandler.getInputStream();
        }
        return new ByteArrayInputStream(this.data, 0, this.dataLen);
    }

    public boolean hasData() {
        return this.data != null;
    }

    public byte[] get() {
        if (this.data == null) {
            try {
                ByteArrayOutputStreamEx baos = new ByteArrayOutputStreamEx(1024);
                InputStream is = this.dataHandler.getDataSource().getInputStream();
                baos.readFrom(is);
                is.close();
                this.data = baos.getBuffer();
                this.dataLen = baos.size();
            }
            catch (IOException e) {
                this.dataLen = 0;
            }
        }
        return this.data;
    }

    public int getDataLen() {
        return this.dataLen;
    }

    public String getMimeType() {
        if (this.mimeType == null) {
            return "application/octet-stream";
        }
        return this.mimeType;
    }

    @Override
    public int length() {
        this.get();
        return (this.dataLen + 2) / 3 * 4;
    }

    @Override
    public char charAt(int index) {
        int offset = index % 4;
        int base = index / 4 * 3;
        switch (offset) {
            case 0: {
                return DatatypeConverterImpl.encode(this.data[base] >> 2);
            }
            case 1: {
                byte b1 = base + 1 < this.dataLen ? this.data[base + 1] : (byte)0;
                return DatatypeConverterImpl.encode((this.data[base] & 3) << 4 | b1 >> 4 & 0xF);
            }
            case 2: {
                if (base + 1 < this.dataLen) {
                    byte b1 = this.data[base + 1];
                    byte b2 = base + 2 < this.dataLen ? this.data[base + 2] : (byte)0;
                    return DatatypeConverterImpl.encode((b1 & 0xF) << 2 | b2 >> 6 & 3);
                }
                return '=';
            }
            case 3: {
                if (base + 2 < this.dataLen) {
                    return DatatypeConverterImpl.encode(this.data[base + 2] & 0x3F);
                }
                return '=';
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        StringBuilder buf = new StringBuilder();
        this.get();
        for (int i = start; i < end; ++i) {
            buf.append(this.charAt(i));
        }
        return buf;
    }

    @Override
    public String toString() {
        this.get();
        return DatatypeConverterImpl._printBase64Binary(this.data, 0, this.dataLen);
    }

    @Override
    public void writeTo(char[] buf, int start) {
        this.get();
        DatatypeConverterImpl._printBase64Binary(this.data, 0, this.dataLen, buf, start);
    }

    @Override
    public void writeTo(UTF8XmlOutput output) throws IOException {
        this.get();
        output.text(this.data, this.dataLen);
    }

    public void writeTo(XMLStreamWriter output) throws IOException, XMLStreamException {
        this.get();
        DatatypeConverterImpl._printBase64Binary(this.data, 0, this.dataLen, output);
    }
}

