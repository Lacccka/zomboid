/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import java.io.IOException;

public final class Encoded {
    public byte[] buf;
    public int len;
    private static final byte[][] entities = new byte[128][];
    private static final byte[][] attributeEntities = new byte[128][];

    public Encoded() {
    }

    public Encoded(String text) {
        this.set(text);
    }

    public void ensureSize(int size) {
        if (this.buf == null || this.buf.length < size) {
            this.buf = new byte[size];
        }
    }

    public final void set(String text) {
        int length = text.length();
        this.ensureSize(length * 3 + 1);
        int ptr = 0;
        for (int i = 0; i < length; ++i) {
            char chr = text.charAt(i);
            if (chr > '\u007f') {
                if (chr > '\u07ff') {
                    if ('\ud800' <= chr && chr <= '\udfff') {
                        int uc = ((chr & 0x3FF) << 10 | text.charAt(++i) & 0x3FF) + 65536;
                        this.buf[ptr++] = (byte)(0xF0 | uc >> 18);
                        this.buf[ptr++] = (byte)(0x80 | uc >> 12 & 0x3F);
                        this.buf[ptr++] = (byte)(0x80 | uc >> 6 & 0x3F);
                        this.buf[ptr++] = (byte)(128 + (uc & 0x3F));
                        continue;
                    }
                    this.buf[ptr++] = (byte)(224 + (chr >> 12));
                    this.buf[ptr++] = (byte)(128 + (chr >> 6 & 0x3F));
                } else {
                    this.buf[ptr++] = (byte)(192 + (chr >> 6));
                }
                this.buf[ptr++] = (byte)(128 + (chr & 0x3F));
                continue;
            }
            this.buf[ptr++] = (byte)chr;
        }
        this.len = ptr;
    }

    public final void setEscape(String text, boolean isAttribute) {
        int length = text.length();
        this.ensureSize(length * 6 + 1);
        int ptr = 0;
        for (int i = 0; i < length; ++i) {
            char chr = text.charAt(i);
            int ptr1 = ptr;
            if (chr > '\u007f') {
                if (chr > '\u07ff') {
                    if ('\ud800' <= chr && chr <= '\udfff') {
                        int uc = ((chr & 0x3FF) << 10 | text.charAt(++i) & 0x3FF) + 65536;
                        this.buf[ptr++] = (byte)(0xF0 | uc >> 18);
                        this.buf[ptr++] = (byte)(0x80 | uc >> 12 & 0x3F);
                        this.buf[ptr++] = (byte)(0x80 | uc >> 6 & 0x3F);
                        this.buf[ptr++] = (byte)(128 + (uc & 0x3F));
                        continue;
                    }
                    this.buf[ptr1++] = (byte)(224 + (chr >> 12));
                    this.buf[ptr1++] = (byte)(128 + (chr >> 6 & 0x3F));
                } else {
                    this.buf[ptr1++] = (byte)(192 + (chr >> 6));
                }
                this.buf[ptr1++] = (byte)(128 + (chr & 0x3F));
            } else {
                byte[] ent = attributeEntities[chr];
                if (ent != null) {
                    if (isAttribute || entities[chr] != null) {
                        ptr1 = this.writeEntity(ent, ptr1);
                    } else {
                        this.buf[ptr1++] = (byte)chr;
                    }
                } else {
                    this.buf[ptr1++] = (byte)chr;
                }
            }
            ptr = ptr1;
        }
        this.len = ptr;
    }

    private int writeEntity(byte[] entity, int ptr) {
        System.arraycopy(entity, 0, this.buf, ptr, entity.length);
        return ptr + entity.length;
    }

    public final void write(UTF8XmlOutput out) throws IOException {
        out.write(this.buf, 0, this.len);
    }

    public void append(char b) {
        this.buf[this.len++] = (byte)b;
    }

    public void compact() {
        byte[] b = new byte[this.len];
        System.arraycopy(this.buf, 0, b, 0, this.len);
        this.buf = b;
    }

    private static void add(char c, String s, boolean attOnly) {
        byte[] image = UTF8XmlOutput.toBytes(s);
        Encoded.attributeEntities[c] = image;
        if (!attOnly) {
            Encoded.entities[c] = image;
        }
    }

    static {
        Encoded.add('&', "&amp;", false);
        Encoded.add('<', "&lt;", false);
        Encoded.add('>', "&gt;", false);
        Encoded.add('\"', "&quot;", true);
        Encoded.add('\t', "&#x9;", true);
        Encoded.add('\r', "&#xD;", false);
        Encoded.add('\n', "&#xA;", true);
    }
}

