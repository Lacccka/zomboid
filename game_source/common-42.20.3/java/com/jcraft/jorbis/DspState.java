/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.CodeBook;
import com.jcraft.jorbis.FuncMapping;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.Mdct;
import com.jcraft.jorbis.Util;

public class DspState {
    static final float M_PI = (float)Math.PI;
    static final int VI_TRANSFORMB = 1;
    static final int VI_WINDOWB = 1;
    int analysisp;
    int centerW;
    int envelopeCurrent;
    int envelopeStorage;
    int eofflag;
    long floorBits;
    CodeBook[] fullbooks;
    long glueBits;
    long granulepos;
    byte[] header;
    byte[] header1;
    byte[] header2;
    int lW;
    Object[] mode;
    int modebits;
    float[] multipliers;
    int nW;
    float[][] pcm;
    int pcmCurrent;
    int pcmReturned;
    int pcmStorage;
    long resBits;
    long sequence;
    long timeBits;
    Object[][] transform = new Object[2][];
    Info vi;
    int w;
    float[][][][][] window = new float[2][][][][];

    public DspState() {
        this.window[0] = new float[2][][][];
        this.window[0][0] = new float[2][][];
        this.window[0][1] = new float[2][][];
        this.window[0][0][0] = new float[2][];
        this.window[0][0][1] = new float[2][];
        this.window[0][1][0] = new float[2][];
        this.window[0][1][1] = new float[2][];
        this.window[1] = new float[2][][][];
        this.window[1][0] = new float[2][][];
        this.window[1][1] = new float[2][][];
        this.window[1][0][0] = new float[2][];
        this.window[1][0][1] = new float[2][];
        this.window[1][1][0] = new float[2][];
        this.window[1][1][1] = new float[2][];
    }

    DspState(Info vi) {
        this();
        this.init(vi, false);
        this.pcmReturned = this.centerW;
        this.centerW -= vi.blocksizes[this.w] / 4 + vi.blocksizes[this.lW] / 4;
        this.granulepos = -1L;
        this.sequence = -1L;
    }

    static float[] window(int type, int window, int left, int right) {
        float[] ret = new float[window];
        switch (type) {
            case 0: {
                float x;
                int i;
                int leftbegin = window / 4 - left / 2;
                int rightbegin = window - window / 4 - right / 2;
                for (i = 0; i < left; ++i) {
                    x = (float)(((double)i + 0.5) / (double)left * 3.1415927410125732 / 2.0);
                    x = (float)Math.sin(x);
                    x *= x;
                    x = (float)((double)x * 1.5707963705062866);
                    ret[i + leftbegin] = x = (float)Math.sin(x);
                }
                for (i = leftbegin + left; i < rightbegin; ++i) {
                    ret[i] = 1.0f;
                }
                for (i = 0; i < right; ++i) {
                    x = (float)(((double)(right - i) - 0.5) / (double)right * 3.1415927410125732 / 2.0);
                    x = (float)Math.sin(x);
                    x *= x;
                    x = (float)((double)x * 1.5707963705062866);
                    ret[i + rightbegin] = x = (float)Math.sin(x);
                }
                break;
            }
            default: {
                return null;
            }
        }
        return ret;
    }

    public void clear() {
    }

    public int synthesis_blockin(Block vb) {
        if (this.centerW > this.vi.blocksizes[1] / 2 && this.pcmReturned > 8192) {
            int shiftPCM = this.centerW - this.vi.blocksizes[1] / 2;
            shiftPCM = this.pcmReturned < shiftPCM ? this.pcmReturned : shiftPCM;
            this.pcmCurrent -= shiftPCM;
            this.centerW -= shiftPCM;
            this.pcmReturned -= shiftPCM;
            if (shiftPCM != 0) {
                for (int i = 0; i < this.vi.channels; ++i) {
                    System.arraycopy(this.pcm[i], shiftPCM, this.pcm[i], 0, this.pcmCurrent);
                }
            }
        }
        this.lW = this.w;
        this.w = vb.w;
        this.nW = -1;
        this.glueBits += (long)vb.glueBits;
        this.timeBits += (long)vb.timeBits;
        this.floorBits += (long)vb.floorBits;
        this.resBits += (long)vb.resBits;
        if (this.sequence + 1L != vb.sequence) {
            this.granulepos = -1L;
        }
        this.sequence = vb.sequence;
        int sizeW = this.vi.blocksizes[this.w];
        int _centerW = this.centerW + this.vi.blocksizes[this.lW] / 4 + sizeW / 4;
        int beginW = _centerW - sizeW / 2;
        int endW = beginW + sizeW;
        int beginSl = 0;
        int endSl = 0;
        if (endW > this.pcmStorage) {
            this.pcmStorage = endW + this.vi.blocksizes[1];
            for (int i = 0; i < this.vi.channels; ++i) {
                float[] foo = new float[this.pcmStorage];
                System.arraycopy(this.pcm[i], 0, foo, 0, this.pcm[i].length);
                this.pcm[i] = foo;
            }
        }
        switch (this.w) {
            case 0: {
                beginSl = 0;
                endSl = this.vi.blocksizes[0] / 2;
                break;
            }
            case 1: {
                beginSl = this.vi.blocksizes[1] / 4 - this.vi.blocksizes[this.lW] / 4;
                endSl = beginSl + this.vi.blocksizes[this.lW] / 2;
            }
        }
        for (int j = 0; j < this.vi.channels; ++j) {
            int _pcm = beginW;
            int i = 0;
            for (i = beginSl; i < endSl; ++i) {
                float[] fArray = this.pcm[j];
                int n = _pcm + i;
                fArray[n] = fArray[n] + vb.pcm[j][i];
            }
            while (i < sizeW) {
                this.pcm[j][_pcm + i] = vb.pcm[j][i];
                ++i;
            }
        }
        if (this.granulepos == -1L) {
            this.granulepos = vb.granulepos;
        } else {
            this.granulepos += (long)(_centerW - this.centerW);
            if (vb.granulepos != -1L && this.granulepos != vb.granulepos) {
                if (this.granulepos > vb.granulepos && vb.eofflag != 0) {
                    _centerW = (int)((long)_centerW - (this.granulepos - vb.granulepos));
                }
                this.granulepos = vb.granulepos;
            }
        }
        this.centerW = _centerW;
        this.pcmCurrent = endW;
        if (vb.eofflag != 0) {
            this.eofflag = 1;
        }
        return 0;
    }

    public int synthesis_init(Info vi) {
        this.init(vi, false);
        this.pcmReturned = this.centerW;
        this.centerW -= vi.blocksizes[this.w] / 4 + vi.blocksizes[this.lW] / 4;
        this.granulepos = -1L;
        this.sequence = -1L;
        return 0;
    }

    public int synthesis_pcmout(float[][][] _pcm, int[] index) {
        if (this.pcmReturned < this.centerW) {
            if (_pcm != null) {
                for (int i = 0; i < this.vi.channels; ++i) {
                    index[i] = this.pcmReturned;
                }
                _pcm[0] = this.pcm;
            }
            return this.centerW - this.pcmReturned;
        }
        return 0;
    }

    public int synthesis_read(int bytes) {
        if (bytes != 0 && this.pcmReturned + bytes > this.centerW) {
            return -1;
        }
        this.pcmReturned += bytes;
        return 0;
    }

    int init(Info vi, boolean encp) {
        int i;
        this.vi = vi;
        this.modebits = Util.ilog2(vi.modes);
        this.transform[0] = new Object[1];
        this.transform[1] = new Object[1];
        this.transform[0][0] = new Mdct();
        this.transform[1][0] = new Mdct();
        ((Mdct)this.transform[0][0]).init(vi.blocksizes[0]);
        ((Mdct)this.transform[1][0]).init(vi.blocksizes[1]);
        this.window[0][0][0] = new float[1][];
        this.window[0][0][1] = this.window[0][0][0];
        this.window[0][1][0] = this.window[0][0][0];
        this.window[0][1][1] = this.window[0][0][0];
        this.window[1][0][0] = new float[1][];
        this.window[1][0][1] = new float[1][];
        this.window[1][1][0] = new float[1][];
        this.window[1][1][1] = new float[1][];
        for (i = 0; i < 1; ++i) {
            this.window[0][0][0][i] = DspState.window(i, vi.blocksizes[0], vi.blocksizes[0] / 2, vi.blocksizes[0] / 2);
            this.window[1][0][0][i] = DspState.window(i, vi.blocksizes[1], vi.blocksizes[0] / 2, vi.blocksizes[0] / 2);
            this.window[1][0][1][i] = DspState.window(i, vi.blocksizes[1], vi.blocksizes[0] / 2, vi.blocksizes[1] / 2);
            this.window[1][1][0][i] = DspState.window(i, vi.blocksizes[1], vi.blocksizes[1] / 2, vi.blocksizes[0] / 2);
            this.window[1][1][1][i] = DspState.window(i, vi.blocksizes[1], vi.blocksizes[1] / 2, vi.blocksizes[1] / 2);
        }
        this.fullbooks = new CodeBook[vi.books];
        for (i = 0; i < vi.books; ++i) {
            this.fullbooks[i] = new CodeBook();
            this.fullbooks[i].init_decode(vi.bookParam[i]);
        }
        this.pcmStorage = 8192;
        this.pcm = new float[vi.channels][];
        for (i = 0; i < vi.channels; ++i) {
            this.pcm[i] = new float[this.pcmStorage];
        }
        this.lW = 0;
        this.w = 0;
        this.pcmCurrent = this.centerW = vi.blocksizes[1] / 2;
        this.mode = new Object[vi.modes];
        for (i = 0; i < vi.modes; ++i) {
            int mapnum = vi.modeParam[i].mapping;
            int maptype = vi.mapType[mapnum];
            this.mode[i] = FuncMapping.mappingP[maptype].look(this, vi.modeParam[i], vi.mapParam[mapnum]);
        }
        return 0;
    }
}

