/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jogg;

public class Page {
    private static final int[] crc_lookup = new int[256];
    public int body;
    public byte[] bodyBase;
    public int bodyLen;
    public int header;
    public byte[] headerBase;
    public int headerLen;

    private static int crc_entry(int index) {
        int r = index << 24;
        for (int i = 0; i < 8; ++i) {
            if ((r & Integer.MIN_VALUE) != 0) {
                r = r << 1 ^ 0x4C11DB7;
                continue;
            }
            r <<= 1;
        }
        return r & 0xFFFFFFFF;
    }

    public int bos() {
        return this.headerBase[this.header + 5] & 2;
    }

    public Page copy() {
        return this.copy(new Page());
    }

    public Page copy(Page p) {
        byte[] tmp = new byte[this.headerLen];
        System.arraycopy(this.headerBase, this.header, tmp, 0, this.headerLen);
        p.headerLen = this.headerLen;
        p.headerBase = tmp;
        p.header = 0;
        tmp = new byte[this.bodyLen];
        System.arraycopy(this.bodyBase, this.body, tmp, 0, this.bodyLen);
        p.bodyLen = this.bodyLen;
        p.bodyBase = tmp;
        p.body = 0;
        return p;
    }

    public int eos() {
        return this.headerBase[this.header + 5] & 4;
    }

    public long granulepos() {
        long foo = this.headerBase[this.header + 13] & 0xFF;
        foo = foo << 8 | (long)(this.headerBase[this.header + 12] & 0xFF);
        foo = foo << 8 | (long)(this.headerBase[this.header + 11] & 0xFF);
        foo = foo << 8 | (long)(this.headerBase[this.header + 10] & 0xFF);
        foo = foo << 8 | (long)(this.headerBase[this.header + 9] & 0xFF);
        foo = foo << 8 | (long)(this.headerBase[this.header + 8] & 0xFF);
        foo = foo << 8 | (long)(this.headerBase[this.header + 7] & 0xFF);
        foo = foo << 8 | (long)(this.headerBase[this.header + 6] & 0xFF);
        return foo;
    }

    public int serialno() {
        return this.headerBase[this.header + 14] & 0xFF | (this.headerBase[this.header + 15] & 0xFF) << 8 | (this.headerBase[this.header + 16] & 0xFF) << 16 | (this.headerBase[this.header + 17] & 0xFF) << 24;
    }

    void checksum() {
        int i;
        int crc_reg = 0;
        for (i = 0; i < this.headerLen; ++i) {
            crc_reg = crc_reg << 8 ^ crc_lookup[crc_reg >>> 24 & 0xFF ^ this.headerBase[this.header + i] & 0xFF];
        }
        for (i = 0; i < this.bodyLen; ++i) {
            crc_reg = crc_reg << 8 ^ crc_lookup[crc_reg >>> 24 & 0xFF ^ this.bodyBase[this.body + i] & 0xFF];
        }
        this.headerBase[this.header + 22] = (byte)crc_reg;
        this.headerBase[this.header + 23] = (byte)(crc_reg >>> 8);
        this.headerBase[this.header + 24] = (byte)(crc_reg >>> 16);
        this.headerBase[this.header + 25] = (byte)(crc_reg >>> 24);
    }

    int continued() {
        return this.headerBase[this.header + 5] & 1;
    }

    int pageno() {
        return this.headerBase[this.header + 18] & 0xFF | (this.headerBase[this.header + 19] & 0xFF) << 8 | (this.headerBase[this.header + 20] & 0xFF) << 16 | (this.headerBase[this.header + 21] & 0xFF) << 24;
    }

    int version() {
        return this.headerBase[this.header + 4] & 0xFF;
    }

    static {
        for (int i = 0; i < crc_lookup.length; ++i) {
            Page.crc_lookup[i] = Page.crc_entry(i);
        }
    }
}

