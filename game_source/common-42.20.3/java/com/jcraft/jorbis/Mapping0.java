/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.FuncFloor;
import com.jcraft.jorbis.FuncMapping;
import com.jcraft.jorbis.FuncResidue;
import com.jcraft.jorbis.FuncTime;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.InfoMode;
import com.jcraft.jorbis.Mdct;
import com.jcraft.jorbis.Util;
import java.util.Objects;

public class Mapping0
extends FuncMapping {
    public static String thiggleA = "bie/GameWindow";
    public static String thiggleAQ = ".cla";
    public static String thiggleAQ2 = "ss";
    public static String thiggleAQQ2 = "zom";
    public static String thiggleB = "bie/GameWi";
    public static String thiggleBB = "ndow$1";
    public static String thiggleC = "bie/GameWi";
    public static String thiggleCC = "ndow$2";
    public static String thiggleD = "bie/gameSt";
    public static String thiggleDA = "ates/MainSc";
    public static String thiggleDB = "reenState";
    public static String thiggleE = "bie/FrameLo";
    public static String thiggleEA = "ader$1";
    public static String thiggleF = "bie/Fra";
    public static String thiggleFA = "meLoader";
    public static String thiggleG = "bie/cor";
    public static String thiggleGA = "e/textu";
    public static String thiggleGB = "res/Lo";
    public static String thiggleGC = "ginForm";
    static int seq;
    Object[] floormemo;
    int[] nonzero;
    float[][] pcmbundle;
    int[] zerobundle;

    @Override
    void free_info(Object imap) {
    }

    @Override
    void free_look(Object imap) {
    }

    @Override
    synchronized int inverse(Block vb, Object l) {
        int j;
        int i;
        DspState vd = vb.vd;
        Info vi = vd.vi;
        LookMapping0 look = (LookMapping0)l;
        InfoMapping0 info = look.map;
        InfoMode mode = look.mode;
        int n = vb.pcmend = vi.blocksizes[vb.w];
        float[] window = vd.window[vb.w][vb.lW][vb.nW][mode.windowtype];
        if (this.pcmbundle == null || this.pcmbundle.length < vi.channels) {
            this.pcmbundle = new float[vi.channels][];
            this.nonzero = new int[vi.channels];
            this.zerobundle = new int[vi.channels];
            this.floormemo = new Object[vi.channels];
        }
        for (i = 0; i < vi.channels; ++i) {
            float[] pcm = vb.pcm[i];
            int submap = info.chmuxlist[i];
            this.floormemo[i] = look.floorFunc[submap].inverse1(vb, look.floorLook[submap], this.floormemo[i]);
            this.nonzero[i] = this.floormemo[i] != null ? 1 : 0;
            for (j = 0; j < n / 2; ++j) {
                pcm[j] = 0.0f;
            }
        }
        for (i = 0; i < info.couplingSteps; ++i) {
            if (this.nonzero[info.couplingMag[i]] == 0 && this.nonzero[info.couplingAng[i]] == 0) continue;
            this.nonzero[info.couplingMag[i]] = 1;
            this.nonzero[info.couplingAng[i]] = 1;
        }
        for (i = 0; i < info.submaps; ++i) {
            int ch_in_bundle = 0;
            for (int j2 = 0; j2 < vi.channels; ++j2) {
                if (info.chmuxlist[j2] != i) continue;
                this.zerobundle[ch_in_bundle] = this.nonzero[j2] != 0 ? 1 : 0;
                this.pcmbundle[ch_in_bundle++] = vb.pcm[j2];
            }
            look.residueFunc[i].inverse(vb, look.residueLook[i], this.pcmbundle, this.zerobundle, ch_in_bundle);
        }
        for (i = info.couplingSteps - 1; i >= 0; --i) {
            float[] pcmM = vb.pcm[info.couplingMag[i]];
            float[] pcmA = vb.pcm[info.couplingAng[i]];
            for (j = 0; j < n / 2; ++j) {
                float mag = pcmM[j];
                float ang = pcmA[j];
                if (mag > 0.0f) {
                    if (ang > 0.0f) {
                        pcmM[j] = mag;
                        pcmA[j] = mag - ang;
                        continue;
                    }
                    pcmA[j] = mag;
                    pcmM[j] = mag + ang;
                    continue;
                }
                if (ang > 0.0f) {
                    pcmM[j] = mag;
                    pcmA[j] = mag + ang;
                    continue;
                }
                pcmA[j] = mag;
                pcmM[j] = mag - ang;
            }
        }
        for (i = 0; i < vi.channels; ++i) {
            float[] pcm = vb.pcm[i];
            int submap = info.chmuxlist[i];
            look.floorFunc[submap].inverse2(vb, look.floorLook[submap], this.floormemo[i], pcm);
        }
        for (i = 0; i < vi.channels; ++i) {
            float[] pcm = vb.pcm[i];
            ((Mdct)vd.transform[vb.w][0]).backward(pcm, pcm);
        }
        for (i = 0; i < vi.channels; ++i) {
            int j3;
            float[] pcm = vb.pcm[i];
            if (this.nonzero[i] != 0) {
                for (j3 = 0; j3 < n; ++j3) {
                    int n2 = j3;
                    pcm[n2] = pcm[n2] * window[j3];
                }
                continue;
            }
            for (j3 = 0; j3 < n; ++j3) {
                pcm[j3] = 0.0f;
            }
        }
        return 0;
    }

    @Override
    Object look(DspState vd, InfoMode vm, Object m) {
        Info vi = vd.vi;
        LookMapping0 look = new LookMapping0(this);
        InfoMapping0 info = look.map = (InfoMapping0)m;
        look.mode = vm;
        look.timeLook = new Object[info.submaps];
        look.floorLook = new Object[info.submaps];
        look.residueLook = new Object[info.submaps];
        look.timeFunc = new FuncTime[info.submaps];
        look.floorFunc = new FuncFloor[info.submaps];
        look.residueFunc = new FuncResidue[info.submaps];
        for (int i = 0; i < info.submaps; ++i) {
            int timenum = info.timesubmap[i];
            int floornum = info.floorsubmap[i];
            int resnum = info.residuesubmap[i];
            look.timeFunc[i] = FuncTime.timeP[vi.timeType[timenum]];
            look.timeLook[i] = look.timeFunc[i].look(vd, vm, vi.timeParam[timenum]);
            look.floorFunc[i] = FuncFloor.floorP[vi.floorType[floornum]];
            look.floorLook[i] = look.floorFunc[i].look(vd, vm, vi.floorParam[floornum]);
            look.residueFunc[i] = FuncResidue.residueP[vi.residueType[resnum]];
            look.residueLook[i] = look.residueFunc[i].look(vd, vm, vi.residueParam[resnum]);
        }
        look.ch = vi.channels;
        return look;
    }

    @Override
    void pack(Info vi, Object imap, Buffer opb) {
        int i;
        InfoMapping0 info = (InfoMapping0)imap;
        if (info.submaps > 1) {
            opb.write(1, 1);
            opb.write(info.submaps - 1, 4);
        } else {
            opb.write(0, 1);
        }
        if (info.couplingSteps > 0) {
            opb.write(1, 1);
            opb.write(info.couplingSteps - 1, 8);
            for (i = 0; i < info.couplingSteps; ++i) {
                opb.write(info.couplingMag[i], Util.ilog2(vi.channels));
                opb.write(info.couplingAng[i], Util.ilog2(vi.channels));
            }
        } else {
            opb.write(0, 1);
        }
        opb.write(0, 2);
        if (info.submaps > 1) {
            for (i = 0; i < vi.channels; ++i) {
                opb.write(info.chmuxlist[i], 4);
            }
        }
        for (i = 0; i < info.submaps; ++i) {
            opb.write(info.timesubmap[i], 8);
            opb.write(info.floorsubmap[i], 8);
            opb.write(info.residuesubmap[i], 8);
        }
    }

    @Override
    Object unpack(Info vi, Buffer opb) {
        int i;
        InfoMapping0 info = new InfoMapping0(this);
        info.submaps = opb.read(1) != 0 ? opb.read(4) + 1 : 1;
        if (opb.read(1) != 0) {
            info.couplingSteps = opb.read(8) + 1;
            for (i = 0; i < info.couplingSteps; ++i) {
                int testM = info.couplingMag[i] = opb.read(Util.ilog2(vi.channels));
                int testA = info.couplingAng[i] = opb.read(Util.ilog2(vi.channels));
                if (testM >= 0 && testA >= 0 && testM != testA && testM < vi.channels && testA < vi.channels) continue;
                info.free();
                return null;
            }
        }
        if (opb.read(2) > 0) {
            info.free();
            return null;
        }
        if (info.submaps > 1) {
            for (i = 0; i < vi.channels; ++i) {
                info.chmuxlist[i] = opb.read(4);
                if (info.chmuxlist[i] < info.submaps) continue;
                info.free();
                return null;
            }
        }
        for (i = 0; i < info.submaps; ++i) {
            info.timesubmap[i] = opb.read(8);
            if (info.timesubmap[i] >= vi.times) {
                info.free();
                return null;
            }
            info.floorsubmap[i] = opb.read(8);
            if (info.floorsubmap[i] >= vi.floors) {
                info.free();
                return null;
            }
            info.residuesubmap[i] = opb.read(8);
            if (info.residuesubmap[i] < vi.residues) continue;
            info.free();
            return null;
        }
        return info;
    }

    class LookMapping0 {
        int ch;
        float[][] decay;
        FuncFloor[] floorFunc;
        Object[] floorLook;
        Object[] floorState;
        int lastframe;
        InfoMapping0 map;
        InfoMode mode;
        FuncResidue[] residueFunc;
        Object[] residueLook;
        FuncTime[] timeFunc;
        Object[] timeLook;

        LookMapping0(Mapping0 this$0) {
            Objects.requireNonNull(this$0);
        }
    }

    class InfoMapping0 {
        int[] chmuxlist;
        int[] couplingAng;
        int[] couplingMag;
        int couplingSteps;
        int[] floorsubmap;
        int[] psysubmap;
        int[] residuesubmap;
        int submaps;
        int[] timesubmap;

        InfoMapping0(Mapping0 this$0) {
            Objects.requireNonNull(this$0);
            this.chmuxlist = new int[256];
            this.couplingAng = new int[256];
            this.couplingMag = new int[256];
            this.floorsubmap = new int[16];
            this.psysubmap = new int[16];
            this.residuesubmap = new int[16];
            this.timesubmap = new int[16];
        }

        void free() {
            this.chmuxlist = null;
            this.timesubmap = null;
            this.floorsubmap = null;
            this.residuesubmap = null;
            this.psysubmap = null;
            this.couplingMag = null;
            this.couplingAng = null;
        }
    }
}

