/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.FuncFloor;
import com.jcraft.jorbis.FuncMapping;
import com.jcraft.jorbis.FuncResidue;
import com.jcraft.jorbis.FuncTime;
import com.jcraft.jorbis.InfoMode;
import com.jcraft.jorbis.PsyInfo;
import com.jcraft.jorbis.StaticCodeBook;
import com.jcraft.jorbis.Util;

public class Info {
    private static final int OV_EBADPACKET = -136;
    private static final int OV_ENOTAUDIO = -135;
    private static final byte[] _vorbis = "vorbis".getBytes();
    private static final int VI_TIMEB = 1;
    private static final int VI_FLOORB = 2;
    private static final int VI_RESB = 3;
    private static final int VI_MAPB = 1;
    private static final int VI_WINDOWB = 1;
    public int channels;
    public int rate;
    public int version;
    int bitrateLower;
    int bitrateNominal;
    int bitrateUpper;
    int[] blocksizes = new int[2];
    StaticCodeBook[] bookParam;
    int books;
    int envelopesa;
    Object[] floorParam;
    int[] floorType;
    int floors;
    Object[] mapParam;
    int[] mapType;
    int maps;
    InfoMode[] modeParam;
    int modes;
    float preechoClamp;
    float preechoThresh;
    PsyInfo[] psyParam = new PsyInfo[64];
    int psys;
    Object[] residueParam;
    int[] residueType;
    int residues;
    Object[] timeParam;
    int[] timeType;
    int times;

    public int blocksize(Packet op) {
        Buffer opb = new Buffer();
        opb.readinit(op.packetBase, op.packet, op.bytes);
        if (opb.read(1) != 0) {
            return -135;
        }
        int modebits = 0;
        for (int v = this.modes; v > 1; v >>>= 1) {
            ++modebits;
        }
        int mode = opb.read(modebits);
        if (mode == -1) {
            return -136;
        }
        return this.blocksizes[this.modeParam[mode].blockflag];
    }

    public void clear() {
        int i;
        for (i = 0; i < this.modes; ++i) {
            this.modeParam[i] = null;
        }
        this.modeParam = null;
        for (i = 0; i < this.maps; ++i) {
            FuncMapping.mappingP[this.mapType[i]].free_info(this.mapParam[i]);
        }
        this.mapParam = null;
        for (i = 0; i < this.times; ++i) {
            FuncTime.timeP[this.timeType[i]].free_info(this.timeParam[i]);
        }
        this.timeParam = null;
        for (i = 0; i < this.floors; ++i) {
            FuncFloor.floorP[this.floorType[i]].free_info(this.floorParam[i]);
        }
        this.floorParam = null;
        for (i = 0; i < this.residues; ++i) {
            FuncResidue.residueP[this.residueType[i]].free_info(this.residueParam[i]);
        }
        this.residueParam = null;
        for (i = 0; i < this.books; ++i) {
            if (this.bookParam[i] == null) continue;
            this.bookParam[i].clear();
            this.bookParam[i] = null;
        }
        this.bookParam = null;
        for (i = 0; i < this.psys; ++i) {
            this.psyParam[i].free();
        }
    }

    public void init() {
        this.rate = 0;
    }

    public int synthesis_headerin(Comment vc, Packet op) {
        Buffer opb = new Buffer();
        if (op != null) {
            opb.readinit(op.packetBase, op.packet, op.bytes);
            byte[] buffer = new byte[6];
            int packtype = opb.read(8);
            opb.read(buffer, 6);
            if (buffer[0] != 118 || buffer[1] != 111 || buffer[2] != 114 || buffer[3] != 98 || buffer[4] != 105 || buffer[5] != 115) {
                return -1;
            }
            switch (packtype) {
                case 1: {
                    if (op.beginOfStream == 0) {
                        return -1;
                    }
                    if (this.rate != 0) {
                        return -1;
                    }
                    return this.unpack_info(opb);
                }
                case 3: {
                    if (this.rate == 0) {
                        return -1;
                    }
                    return vc.unpack(opb);
                }
                case 5: {
                    if (this.rate == 0 || vc.vendor == null) {
                        return -1;
                    }
                    return this.unpack_books(opb);
                }
            }
        }
        return -1;
    }

    public String toString() {
        return "version:" + this.version + ", channels:" + this.channels + ", rate:" + this.rate + ", bitrate:" + this.bitrateUpper + "," + this.bitrateNominal + "," + this.bitrateLower;
    }

    int pack_books(Buffer opb) {
        int i;
        opb.write(5, 8);
        opb.write(_vorbis);
        opb.write(this.books - 1, 8);
        for (i = 0; i < this.books; ++i) {
            if (this.bookParam[i].pack(opb) == 0) continue;
            return -1;
        }
        opb.write(this.times - 1, 6);
        for (i = 0; i < this.times; ++i) {
            opb.write(this.timeType[i], 16);
            FuncTime.timeP[this.timeType[i]].pack(this.timeParam[i], opb);
        }
        opb.write(this.floors - 1, 6);
        for (i = 0; i < this.floors; ++i) {
            opb.write(this.floorType[i], 16);
            FuncFloor.floorP[this.floorType[i]].pack(this.floorParam[i], opb);
        }
        opb.write(this.residues - 1, 6);
        for (i = 0; i < this.residues; ++i) {
            opb.write(this.residueType[i], 16);
            FuncResidue.residueP[this.residueType[i]].pack(this.residueParam[i], opb);
        }
        opb.write(this.maps - 1, 6);
        for (i = 0; i < this.maps; ++i) {
            opb.write(this.mapType[i], 16);
            FuncMapping.mappingP[this.mapType[i]].pack(this, this.mapParam[i], opb);
        }
        opb.write(this.modes - 1, 6);
        for (i = 0; i < this.modes; ++i) {
            opb.write(this.modeParam[i].blockflag, 1);
            opb.write(this.modeParam[i].windowtype, 16);
            opb.write(this.modeParam[i].transformtype, 16);
            opb.write(this.modeParam[i].mapping, 8);
        }
        opb.write(1, 1);
        return 0;
    }

    int pack_info(Buffer opb) {
        opb.write(1, 8);
        opb.write(_vorbis);
        opb.write(0, 32);
        opb.write(this.channels, 8);
        opb.write(this.rate, 32);
        opb.write(this.bitrateUpper, 32);
        opb.write(this.bitrateNominal, 32);
        opb.write(this.bitrateLower, 32);
        opb.write(Util.ilog2(this.blocksizes[0]), 4);
        opb.write(Util.ilog2(this.blocksizes[1]), 4);
        opb.write(1, 1);
        return 0;
    }

    int unpack_books(Buffer opb) {
        int i;
        this.books = opb.read(8) + 1;
        if (this.bookParam == null || this.bookParam.length != this.books) {
            this.bookParam = new StaticCodeBook[this.books];
        }
        for (i = 0; i < this.books; ++i) {
            this.bookParam[i] = new StaticCodeBook();
            if (this.bookParam[i].unpack(opb) == 0) continue;
            this.clear();
            return -1;
        }
        this.times = opb.read(6) + 1;
        if (this.timeType == null || this.timeType.length != this.times) {
            this.timeType = new int[this.times];
        }
        if (this.timeParam == null || this.timeParam.length != this.times) {
            this.timeParam = new Object[this.times];
        }
        for (i = 0; i < this.times; ++i) {
            this.timeType[i] = opb.read(16);
            if (this.timeType[i] < 0 || this.timeType[i] >= 1) {
                this.clear();
                return -1;
            }
            this.timeParam[i] = FuncTime.timeP[this.timeType[i]].unpack(this, opb);
            if (this.timeParam[i] != null) continue;
            this.clear();
            return -1;
        }
        this.floors = opb.read(6) + 1;
        if (this.floorType == null || this.floorType.length != this.floors) {
            this.floorType = new int[this.floors];
        }
        if (this.floorParam == null || this.floorParam.length != this.floors) {
            this.floorParam = new Object[this.floors];
        }
        for (i = 0; i < this.floors; ++i) {
            this.floorType[i] = opb.read(16);
            if (this.floorType[i] < 0 || this.floorType[i] >= 2) {
                this.clear();
                return -1;
            }
            this.floorParam[i] = FuncFloor.floorP[this.floorType[i]].unpack(this, opb);
            if (this.floorParam[i] != null) continue;
            this.clear();
            return -1;
        }
        this.residues = opb.read(6) + 1;
        if (this.residueType == null || this.residueType.length != this.residues) {
            this.residueType = new int[this.residues];
        }
        if (this.residueParam == null || this.residueParam.length != this.residues) {
            this.residueParam = new Object[this.residues];
        }
        for (i = 0; i < this.residues; ++i) {
            this.residueType[i] = opb.read(16);
            if (this.residueType[i] < 0 || this.residueType[i] >= 3) {
                this.clear();
                return -1;
            }
            this.residueParam[i] = FuncResidue.residueP[this.residueType[i]].unpack(this, opb);
            if (this.residueParam[i] != null) continue;
            this.clear();
            return -1;
        }
        this.maps = opb.read(6) + 1;
        if (this.mapType == null || this.mapType.length != this.maps) {
            this.mapType = new int[this.maps];
        }
        if (this.mapParam == null || this.mapParam.length != this.maps) {
            this.mapParam = new Object[this.maps];
        }
        for (i = 0; i < this.maps; ++i) {
            this.mapType[i] = opb.read(16);
            if (this.mapType[i] < 0 || this.mapType[i] >= 1) {
                this.clear();
                return -1;
            }
            this.mapParam[i] = FuncMapping.mappingP[this.mapType[i]].unpack(this, opb);
            if (this.mapParam[i] != null) continue;
            this.clear();
            return -1;
        }
        this.modes = opb.read(6) + 1;
        if (this.modeParam == null || this.modeParam.length != this.modes) {
            this.modeParam = new InfoMode[this.modes];
        }
        for (i = 0; i < this.modes; ++i) {
            this.modeParam[i] = new InfoMode();
            this.modeParam[i].blockflag = opb.read(1);
            this.modeParam[i].windowtype = opb.read(16);
            this.modeParam[i].transformtype = opb.read(16);
            this.modeParam[i].mapping = opb.read(8);
            if (this.modeParam[i].windowtype < 1 && this.modeParam[i].transformtype < 1 && this.modeParam[i].mapping < this.maps) continue;
            this.clear();
            return -1;
        }
        if (opb.read(1) != 1) {
            this.clear();
            return -1;
        }
        return 0;
    }

    int unpack_info(Buffer opb) {
        this.version = opb.read(32);
        if (this.version != 0) {
            return -1;
        }
        this.channels = opb.read(8);
        this.rate = opb.read(32);
        this.bitrateUpper = opb.read(32);
        this.bitrateNominal = opb.read(32);
        this.bitrateLower = opb.read(32);
        this.blocksizes[0] = 1 << opb.read(4);
        this.blocksizes[1] = 1 << opb.read(4);
        if (this.rate < 1 || this.channels < 1 || this.blocksizes[0] < 8 || this.blocksizes[1] < this.blocksizes[0] || opb.read(1) != 1) {
            this.clear();
            return -1;
        }
        return 0;
    }
}

