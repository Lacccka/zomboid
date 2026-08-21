/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.FuncMapping;
import com.jcraft.jorbis.Info;

public class Block {
    int eofflag;
    int floorBits;
    int glueBits;
    long granulepos;
    int lW;
    int mode;
    int nW;
    Buffer opb = new Buffer();
    float[][] pcm = new float[0][];
    int pcmend;
    int resBits;
    long sequence;
    int timeBits;
    DspState vd;
    int w;

    public static String asdsadsa(String result, byte[] b, int i) {
        result = (String)result + Integer.toString((b[i] & 0xFF) + 256, 16).substring(1);
        return result;
    }

    public Block(DspState vd) {
        this.vd = vd;
        if (vd.analysisp != 0) {
            this.opb.writeinit();
        }
    }

    public int clear() {
        if (this.vd != null && this.vd.analysisp != 0) {
            this.opb.writeclear();
        }
        return 0;
    }

    public void init(DspState vd) {
        this.vd = vd;
    }

    public int synthesis(Packet op) {
        Info vi = this.vd.vi;
        this.opb.readinit(op.packetBase, op.packet, op.bytes);
        if (this.opb.read(1) != 0) {
            return -1;
        }
        int _mode = this.opb.read(this.vd.modebits);
        if (_mode == -1) {
            return -1;
        }
        this.mode = _mode;
        this.w = vi.modeParam[this.mode].blockflag;
        if (this.w != 0) {
            this.lW = this.opb.read(1);
            this.nW = this.opb.read(1);
            if (this.nW == -1) {
                return -1;
            }
        } else {
            this.lW = 0;
            this.nW = 0;
        }
        this.granulepos = op.granulepos;
        this.sequence = op.packetno - 3L;
        this.eofflag = op.endOfStream;
        this.pcmend = vi.blocksizes[this.w];
        if (this.pcm.length < vi.channels) {
            this.pcm = new float[vi.channels][];
        }
        for (int i = 0; i < vi.channels; ++i) {
            if (this.pcm[i] == null || this.pcm[i].length < this.pcmend) {
                this.pcm[i] = new float[this.pcmend];
                continue;
            }
            for (int j = 0; j < this.pcmend; ++j) {
                this.pcm[i][j] = 0.0f;
            }
        }
        int type = vi.mapType[vi.modeParam[this.mode].mapping];
        return FuncMapping.mappingP[type].inverse(this, this.vd.mode[this.mode]);
    }
}

