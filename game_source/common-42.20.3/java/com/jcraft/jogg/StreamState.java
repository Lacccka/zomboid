/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jogg;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;

public class StreamState {
    public int endOfStream;
    int beginOfStream;
    byte[] bodyData;
    int bodyFill;
    int bodyStorage;
    long[] granuleVals;
    long granulepos;
    byte[] header = new byte[282];
    int headerFill;
    int lacingFill;
    int lacingPacket;
    int lacingReturned;
    int lacingStorage;
    int[] lacingVals;
    long packetno;
    int pageno;
    int serialno;
    private int bodyReturned;

    public StreamState() {
        this.init();
    }

    StreamState(int serialno) {
        this();
        this.init(serialno);
    }

    public void clear() {
        this.bodyData = null;
        this.lacingVals = null;
        this.granuleVals = null;
    }

    public int eof() {
        return this.endOfStream;
    }

    public int flush(Page og) {
        int i;
        int vals = 0;
        int maxvals = this.lacingFill > 255 ? 255 : this.lacingFill;
        int bytes = 0;
        int acc = 0;
        long granule_pos = this.granuleVals[0];
        if (maxvals == 0) {
            return 0;
        }
        if (this.beginOfStream == 0) {
            granule_pos = 0L;
            for (vals = 0; vals < maxvals; ++vals) {
                if ((this.lacingVals[vals] & 0xFF) >= 255) continue;
                ++vals;
                break;
            }
        } else {
            for (vals = 0; vals < maxvals && acc <= 4096; acc += this.lacingVals[vals] & 0xFF, ++vals) {
                granule_pos = this.granuleVals[vals];
            }
        }
        System.arraycopy("OggS".getBytes(), 0, this.header, 0, 4);
        this.header[4] = 0;
        this.header[5] = 0;
        if ((this.lacingVals[0] & 0x100) == 0) {
            this.header[5] = (byte)(this.header[5] | 1);
        }
        if (this.beginOfStream == 0) {
            this.header[5] = (byte)(this.header[5] | 2);
        }
        if (this.endOfStream != 0 && this.lacingFill == vals) {
            this.header[5] = (byte)(this.header[5] | 4);
        }
        this.beginOfStream = 1;
        for (i = 6; i < 14; ++i) {
            this.header[i] = (byte)granule_pos;
            granule_pos >>>= 8;
        }
        int _serialno = this.serialno;
        for (i = 14; i < 18; ++i) {
            this.header[i] = (byte)_serialno;
            _serialno >>>= 8;
        }
        if (this.pageno == -1) {
            this.pageno = 0;
        }
        int _pageno = this.pageno++;
        for (i = 18; i < 22; ++i) {
            this.header[i] = (byte)_pageno;
            _pageno >>>= 8;
        }
        this.header[22] = 0;
        this.header[23] = 0;
        this.header[24] = 0;
        this.header[25] = 0;
        this.header[26] = (byte)vals;
        for (i = 0; i < vals; ++i) {
            this.header[i + 27] = (byte)this.lacingVals[i];
            bytes += this.header[i + 27] & 0xFF;
        }
        og.headerBase = this.header;
        og.header = 0;
        og.headerLen = this.headerFill = vals + 27;
        og.bodyBase = this.bodyData;
        og.body = this.bodyReturned;
        og.bodyLen = bytes;
        this.lacingFill -= vals;
        System.arraycopy(this.lacingVals, vals, this.lacingVals, 0, this.lacingFill * 4);
        System.arraycopy(this.granuleVals, vals, this.granuleVals, 0, this.lacingFill * 8);
        this.bodyReturned += bytes;
        og.checksum();
        return 1;
    }

    public void init(int serialno) {
        if (this.bodyData == null) {
            this.init();
        } else {
            int i;
            for (i = 0; i < this.bodyData.length; ++i) {
                this.bodyData[i] = 0;
            }
            for (i = 0; i < this.lacingVals.length; ++i) {
                this.lacingVals[i] = 0;
            }
            for (i = 0; i < this.granuleVals.length; ++i) {
                this.granuleVals[i] = 0L;
            }
        }
        this.serialno = serialno;
    }

    public int packetin(Packet op) {
        int lacing_val = op.bytes / 255 + 1;
        if (this.bodyReturned != 0) {
            this.bodyFill -= this.bodyReturned;
            if (this.bodyFill != 0) {
                System.arraycopy(this.bodyData, this.bodyReturned, this.bodyData, 0, this.bodyFill);
            }
            this.bodyReturned = 0;
        }
        this.body_expand(op.bytes);
        this.lacing_expand(lacing_val);
        System.arraycopy(op.packetBase, op.packet, this.bodyData, this.bodyFill, op.bytes);
        this.bodyFill += op.bytes;
        for (int j = 0; j < lacing_val - 1; ++j) {
            this.lacingVals[this.lacingFill + j] = 255;
            this.granuleVals[this.lacingFill + j] = this.granulepos;
        }
        this.lacingVals[this.lacingFill + j] = op.bytes % 255;
        long l = op.granulepos;
        this.granuleVals[this.lacingFill + j] = l;
        this.granulepos = l;
        int n = this.lacingFill;
        this.lacingVals[n] = this.lacingVals[n] | 0x100;
        this.lacingFill += lacing_val;
        ++this.packetno;
        if (op.endOfStream != 0) {
            this.endOfStream = 1;
        }
        return 0;
    }

    public int packetout(Packet op) {
        int ptr;
        if (this.lacingPacket <= (ptr = this.lacingReturned++)) {
            return 0;
        }
        if ((this.lacingVals[ptr] & 0x400) != 0) {
            ++this.packetno;
            return -1;
        }
        int size = this.lacingVals[ptr] & 0xFF;
        int bytes = 0;
        op.packetBase = this.bodyData;
        op.packet = this.bodyReturned;
        op.endOfStream = this.lacingVals[ptr] & 0x200;
        op.beginOfStream = this.lacingVals[ptr] & 0x100;
        bytes += size;
        while (size == 255) {
            int val = this.lacingVals[++ptr];
            size = val & 0xFF;
            if ((val & 0x200) != 0) {
                op.endOfStream = 512;
            }
            bytes += size;
        }
        op.packetno = this.packetno++;
        op.granulepos = this.granuleVals[ptr];
        op.bytes = bytes;
        this.bodyReturned += bytes;
        this.lacingReturned = ptr + 1;
        return 1;
    }

    public int pagein(Page og) {
        int val;
        byte[] header_base = og.headerBase;
        int header = og.header;
        byte[] body_base = og.bodyBase;
        int body = og.body;
        int bodysize = og.bodyLen;
        int segptr = 0;
        int version = og.version();
        int continued = og.continued();
        int bos = og.bos();
        int eos = og.eos();
        long granulepos = og.granulepos();
        int _serialno = og.serialno();
        int _pageno = og.pageno();
        int segments = header_base[header + 26] & 0xFF;
        int lr = this.lacingReturned;
        int br = this.bodyReturned;
        if (br != 0) {
            this.bodyFill -= br;
            if (this.bodyFill != 0) {
                System.arraycopy(this.bodyData, br, this.bodyData, 0, this.bodyFill);
            }
            this.bodyReturned = 0;
        }
        if (lr != 0) {
            if (this.lacingFill - lr != 0) {
                System.arraycopy(this.lacingVals, lr, this.lacingVals, 0, this.lacingFill - lr);
                System.arraycopy(this.granuleVals, lr, this.granuleVals, 0, this.lacingFill - lr);
            }
            this.lacingFill -= lr;
            this.lacingPacket -= lr;
            this.lacingReturned = 0;
        }
        if (_serialno != this.serialno) {
            return -1;
        }
        if (version > 0) {
            return -1;
        }
        this.lacing_expand(segments + 1);
        if (_pageno != this.pageno) {
            for (int i = this.lacingPacket; i < this.lacingFill; ++i) {
                this.bodyFill -= this.lacingVals[i] & 0xFF;
            }
            this.lacingFill = this.lacingPacket++;
            if (this.pageno != -1) {
                this.lacingVals[this.lacingFill++] = 1024;
            }
            if (continued != 0) {
                bos = 0;
                while (segptr < segments) {
                    val = header_base[header + 27 + segptr] & 0xFF;
                    body += val;
                    bodysize -= val;
                    if (val < 255) {
                        ++segptr;
                        break;
                    }
                    ++segptr;
                }
            }
        }
        if (bodysize != 0) {
            this.body_expand(bodysize);
            System.arraycopy(body_base, body, this.bodyData, this.bodyFill, bodysize);
            this.bodyFill += bodysize;
        }
        int saved = -1;
        while (segptr < segments) {
            this.lacingVals[this.lacingFill] = val = header_base[header + 27 + segptr] & 0xFF;
            this.granuleVals[this.lacingFill] = -1L;
            if (bos != 0) {
                int n = this.lacingFill;
                this.lacingVals[n] = this.lacingVals[n] | 0x100;
                bos = 0;
            }
            if (val < 255) {
                saved = this.lacingFill;
            }
            ++this.lacingFill;
            ++segptr;
            if (val >= 255) continue;
            this.lacingPacket = this.lacingFill;
        }
        if (saved != -1) {
            this.granuleVals[saved] = granulepos;
        }
        if (eos != 0) {
            this.endOfStream = 1;
            if (this.lacingFill > 0) {
                int n = this.lacingFill - 1;
                this.lacingVals[n] = this.lacingVals[n] | 0x200;
            }
        }
        this.pageno = _pageno + 1;
        return 0;
    }

    public int pageout(Page og) {
        if (this.endOfStream != 0 && this.lacingFill != 0 || this.bodyFill - this.bodyReturned > 4096 || this.lacingFill >= 255 || this.lacingFill != 0 && this.beginOfStream == 0) {
            return this.flush(og);
        }
        return 0;
    }

    public int reset() {
        this.bodyFill = 0;
        this.bodyReturned = 0;
        this.lacingFill = 0;
        this.lacingPacket = 0;
        this.lacingReturned = 0;
        this.headerFill = 0;
        this.endOfStream = 0;
        this.beginOfStream = 0;
        this.pageno = -1;
        this.packetno = 0L;
        this.granulepos = 0L;
        return 0;
    }

    void body_expand(int needed) {
        if (this.bodyStorage <= this.bodyFill + needed) {
            this.bodyStorage += needed + 1024;
            byte[] foo = new byte[this.bodyStorage];
            System.arraycopy(this.bodyData, 0, foo, 0, this.bodyData.length);
            this.bodyData = foo;
        }
    }

    void destroy() {
        this.clear();
    }

    void init() {
        this.bodyStorage = 16384;
        this.bodyData = new byte[this.bodyStorage];
        this.lacingStorage = 1024;
        this.lacingVals = new int[this.lacingStorage];
        this.granuleVals = new long[this.lacingStorage];
    }

    void lacing_expand(int needed) {
        if (this.lacingStorage <= this.lacingFill + needed) {
            this.lacingStorage += needed + 32;
            int[] foo = new int[this.lacingStorage];
            System.arraycopy(this.lacingVals, 0, foo, 0, this.lacingVals.length);
            this.lacingVals = foo;
            long[] bar = new long[this.lacingStorage];
            System.arraycopy(this.granuleVals, 0, bar, 0, this.granuleVals.length);
            this.granuleVals = bar;
        }
    }
}

