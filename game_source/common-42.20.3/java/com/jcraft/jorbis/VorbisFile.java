/*
 * Decompiled with CFR 0.152.
 */
package com.jcraft.jorbis;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.JOrbisException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;

public class VorbisFile {
    static final int CHUNKSIZE = 8500;
    static final int SEEK_SET = 0;
    static final int SEEK_CUR = 1;
    static final int SEEK_END = 2;
    static final int OV_FALSE = -1;
    static final int OV_EOF = -2;
    static final int OV_HOLE = -3;
    static final int OV_EREAD = -128;
    static final int OV_EFAULT = -129;
    static final int OV_EIMPL = -130;
    static final int OV_EINVAL = -131;
    static final int OV_ENOTVORBIS = -132;
    static final int OV_EBADHEADER = -133;
    static final int OV_EVERSION = -134;
    static final int OV_ENOTAUDIO = -135;
    static final int OV_EBADPACKET = -136;
    static final int OV_EBADLINK = -137;
    static final int OV_ENOSEEK = -138;
    float bittrack;
    int currentLink;
    int currentSerialno;
    long[] dataoffsets;
    InputStream datasource;
    boolean decodeReady;
    long end;
    int links;
    long offset;
    long[] offsets;
    StreamState os = new StreamState();
    SyncState oy = new SyncState();
    long pcmOffset;
    long[] pcmlengths;
    float samptrack;
    boolean seekable;
    int[] serialnos;
    Comment[] vc;
    DspState vd = new DspState();
    Block vb = new Block(this.vd);
    Info[] vi;

    public VorbisFile(String file) throws JOrbisException {
        SeekableInputStream is = null;
        try {
            is = new SeekableInputStream(this, file);
            int ret = this.open(is, null, 0);
            if (ret == -1) {
                throw new JOrbisException("VorbisFile: open return -1");
            }
        }
        catch (Exception e) {
            throw new JOrbisException("VorbisFile: " + e.toString());
        }
        finally {
            if (is != null) {
                try {
                    ((InputStream)is).close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public VorbisFile(InputStream is, byte[] initial, int ibytes) throws JOrbisException {
        int ret = this.open(is, initial, ibytes);
        if (ret == -1) {
            // empty if block
        }
    }

    static int fseek(InputStream fis, long off, int whence) {
        if (fis instanceof SeekableInputStream) {
            SeekableInputStream sis = (SeekableInputStream)fis;
            try {
                if (whence == 0) {
                    sis.seek(off);
                } else if (whence == 2) {
                    sis.seek(sis.getLength() - off);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            return 0;
        }
        try {
            if (whence == 0) {
                fis.reset();
            }
            fis.skip(off);
        }
        catch (Exception e) {
            return -1;
        }
        return 0;
    }

    static long ftell(InputStream fis) {
        try {
            if (fis instanceof SeekableInputStream) {
                SeekableInputStream sis = (SeekableInputStream)fis;
                return sis.tell();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return 0L;
    }

    public int bitrate(int i) {
        if (i >= this.links) {
            return -1;
        }
        if (!this.seekable && i != 0) {
            return this.bitrate(0);
        }
        if (i < 0) {
            long bits = 0L;
            for (int j = 0; j < this.links; ++j) {
                bits += (this.offsets[j + 1] - this.dataoffsets[j]) * 8L;
            }
            return (int)Math.rint((float)bits / this.time_total(-1));
        }
        if (this.seekable) {
            return (int)Math.rint((float)((this.offsets[i + 1] - this.dataoffsets[i]) * 8L) / this.time_total(i));
        }
        if (this.vi[i].bitrateNominal > 0) {
            return this.vi[i].bitrateNominal;
        }
        if (this.vi[i].bitrateUpper > 0) {
            if (this.vi[i].bitrateLower > 0) {
                return (this.vi[i].bitrateUpper + this.vi[i].bitrateLower) / 2;
            }
            return this.vi[i].bitrateUpper;
        }
        return -1;
    }

    public int bitrate_instant() {
        int _link;
        int n = _link = this.seekable ? this.currentLink : 0;
        if (this.samptrack == 0.0f) {
            return -1;
        }
        int ret = (int)((double)(this.bittrack / this.samptrack * (float)this.vi[_link].rate) + 0.5);
        this.bittrack = 0.0f;
        this.samptrack = 0.0f;
        return ret;
    }

    public void close() throws IOException {
        this.datasource.close();
    }

    public Comment[] getComment() {
        return this.vc;
    }

    public Comment getComment(int link) {
        if (this.seekable) {
            if (link < 0) {
                if (this.decodeReady) {
                    return this.vc[this.currentLink];
                }
                return null;
            }
            if (link >= this.links) {
                return null;
            }
            return this.vc[link];
        }
        if (this.decodeReady) {
            return this.vc[0];
        }
        return null;
    }

    public Info[] getInfo() {
        return this.vi;
    }

    public Info getInfo(int link) {
        if (this.seekable) {
            if (link < 0) {
                if (this.decodeReady) {
                    return this.vi[this.currentLink];
                }
                return null;
            }
            if (link >= this.links) {
                return null;
            }
            return this.vi[link];
        }
        if (this.decodeReady) {
            return this.vi[0];
        }
        return null;
    }

    public int pcm_seek(long pos) {
        int link = -1;
        long total = this.pcm_total(-1);
        if (!this.seekable) {
            return -1;
        }
        if (pos < 0L || pos > total) {
            this.pcmOffset = -1L;
            this.decode_clear();
            return -1;
        }
        for (link = this.links - 1; link >= 0 && pos < (total -= this.pcmlengths[link]); --link) {
        }
        long target = pos - total;
        long end = this.offsets[link + 1];
        long begin = this.offsets[link];
        int best = (int)begin;
        Page og = new Page();
        while (begin < end) {
            long bisect = end - begin < 8500L ? begin : (end + begin) / 2L;
            this.seek_helper(bisect);
            int ret = this.get_next_page(og, end - bisect);
            if (ret == -1) {
                end = bisect;
                continue;
            }
            long granulepos = og.granulepos();
            if (granulepos < target) {
                best = ret;
                begin = this.offset;
                continue;
            }
            end = bisect;
        }
        if (this.raw_seek(best) != 0) {
            this.pcmOffset = -1L;
            this.decode_clear();
            return -1;
        }
        if (this.pcmOffset >= pos) {
            this.pcmOffset = -1L;
            this.decode_clear();
            return -1;
        }
        if (pos > this.pcm_total(-1)) {
            this.pcmOffset = -1L;
            this.decode_clear();
            return -1;
        }
        while (this.pcmOffset < pos) {
            int target2 = (int)(pos - this.pcmOffset);
            float[][][] _pcm = new float[1][][];
            int[] _index = new int[this.getInfo((int)-1).channels];
            int samples = this.vd.synthesis_pcmout(_pcm, _index);
            if (samples > target2) {
                samples = target2;
            }
            this.vd.synthesis_read(samples);
            this.pcmOffset += (long)samples;
            if (samples >= target2 || this.process_packet(1) != 0) continue;
            this.pcmOffset = this.pcm_total(-1);
        }
        return 0;
    }

    public long pcm_tell() {
        return this.pcmOffset;
    }

    public long pcm_total(int i) {
        if (!this.seekable || i >= this.links) {
            return -1L;
        }
        if (i < 0) {
            long acc = 0L;
            for (int j = 0; j < this.links; ++j) {
                acc += this.pcm_total(j);
            }
            return acc;
        }
        return this.pcmlengths[i];
    }

    public int raw_seek(int pos) {
        if (!this.seekable) {
            return -1;
        }
        if (pos < 0 || (long)pos > this.offsets[this.links]) {
            this.pcmOffset = -1L;
            this.decode_clear();
            return -1;
        }
        this.pcmOffset = -1L;
        this.decode_clear();
        this.seek_helper(pos);
        switch (this.process_packet(1)) {
            case 0: {
                this.pcmOffset = this.pcm_total(-1);
                return 0;
            }
            case -1: {
                this.pcmOffset = -1L;
                this.decode_clear();
                return -1;
            }
        }
        while (true) {
            switch (this.process_packet(0)) {
                case 0: {
                    return 0;
                }
                case -1: {
                    this.pcmOffset = -1L;
                    this.decode_clear();
                    return -1;
                }
            }
        }
    }

    public long raw_tell() {
        return this.offset;
    }

    public long raw_total(int i) {
        if (!this.seekable || i >= this.links) {
            return -1L;
        }
        if (i < 0) {
            long acc = 0L;
            for (int j = 0; j < this.links; ++j) {
                acc += this.raw_total(j);
            }
            return acc;
        }
        return this.offsets[i + 1] - this.offsets[i];
    }

    public boolean seekable() {
        return this.seekable;
    }

    public int serialnumber(int i) {
        if (i >= this.links) {
            return -1;
        }
        if (!this.seekable && i >= 0) {
            return this.serialnumber(-1);
        }
        if (i < 0) {
            return this.currentSerialno;
        }
        return this.serialnos[i];
    }

    public int streams() {
        return this.links;
    }

    public float time_tell() {
        int link = -1;
        long pcm_total = 0L;
        float time_total = 0.0f;
        if (this.seekable) {
            pcm_total = this.pcm_total(-1);
            time_total = this.time_total(-1);
            for (link = this.links - 1; link >= 0; --link) {
                time_total -= this.time_total(link);
                if (this.pcmOffset >= (pcm_total -= this.pcmlengths[link])) break;
            }
        }
        return time_total + (float)(this.pcmOffset - pcm_total) / (float)this.vi[link].rate;
    }

    public float time_total(int i) {
        if (!this.seekable || i >= this.links) {
            return -1.0f;
        }
        if (i < 0) {
            float acc = 0.0f;
            for (int j = 0; j < this.links; ++j) {
                acc += this.time_total(j);
            }
            return acc;
        }
        return (float)this.pcmlengths[i] / (float)this.vi[i].rate;
    }

    int bisect_forward_serialno(long begin, long searched, long end, int currentno, int m) {
        int ret;
        long endsearched = end;
        long next = end;
        Page page = new Page();
        while (searched < endsearched) {
            long bisect = endsearched - searched < 8500L ? searched : (searched + endsearched) / 2L;
            this.seek_helper(bisect);
            ret = this.get_next_page(page, -1L);
            if (ret == -128) {
                return -128;
            }
            if (ret < 0 || page.serialno() != currentno) {
                endsearched = bisect;
                if (ret < 0) continue;
                next = ret;
                continue;
            }
            searched = ret + page.headerLen + page.bodyLen;
        }
        this.seek_helper(next);
        ret = this.get_next_page(page, -1L);
        if (ret == -128) {
            return -128;
        }
        if (searched >= end || ret == -1) {
            this.links = m + 1;
            this.offsets = new long[m + 2];
            this.offsets[m + 1] = searched;
        } else {
            ret = this.bisect_forward_serialno(next, this.offset, end, page.serialno(), m + 1);
            if (ret == -128) {
                return -128;
            }
        }
        this.offsets[m] = begin;
        return 0;
    }

    int clear() {
        this.vb.clear();
        this.vd.clear();
        this.os.clear();
        if (this.vi != null && this.links != 0) {
            for (int i = 0; i < this.links; ++i) {
                this.vi[i].clear();
                this.vc[i].clear();
            }
            this.vi = null;
            this.vc = null;
        }
        if (this.dataoffsets != null) {
            this.dataoffsets = null;
        }
        if (this.pcmlengths != null) {
            this.pcmlengths = null;
        }
        if (this.serialnos != null) {
            this.serialnos = null;
        }
        if (this.offsets != null) {
            this.offsets = null;
        }
        this.oy.clear();
        return 0;
    }

    void decode_clear() {
        this.os.clear();
        this.vd.clear();
        this.vb.clear();
        this.decodeReady = false;
        this.bittrack = 0.0f;
        this.samptrack = 0.0f;
    }

    int fetch_headers(Info vi, Comment vc, int[] serialno, Page og_ptr) {
        Page og = new Page();
        Packet op = new Packet();
        if (og_ptr == null) {
            int ret = this.get_next_page(og, 8500L);
            if (ret == -128) {
                return -128;
            }
            if (ret < 0) {
                return -132;
            }
            og_ptr = og;
        }
        if (serialno != null) {
            serialno[0] = og_ptr.serialno();
        }
        this.os.init(og_ptr.serialno());
        vi.init();
        vc.init();
        int i = 0;
        while (i < 3) {
            int result;
            this.os.pagein(og_ptr);
            while (i < 3 && (result = this.os.packetout(op)) != 0) {
                if (result == -1) {
                    vi.clear();
                    vc.clear();
                    this.os.clear();
                    return -1;
                }
                if (vi.synthesis_headerin(vc, op) != 0) {
                    vi.clear();
                    vc.clear();
                    this.os.clear();
                    return -1;
                }
                ++i;
            }
            if (i >= 3 || this.get_next_page(og_ptr, 1L) >= 0) continue;
            vi.clear();
            vc.clear();
            this.os.clear();
            return -1;
        }
        return 0;
    }

    int host_is_big_endian() {
        return 1;
    }

    int open(InputStream is, byte[] initial, int ibytes) throws JOrbisException {
        return this.open_callbacks(is, initial, ibytes);
    }

    int open_callbacks(InputStream is, byte[] initial, int ibytes) throws JOrbisException {
        int ret;
        this.datasource = is;
        this.oy.init();
        if (initial != null) {
            int index = this.oy.buffer(ibytes);
            System.arraycopy(initial, 0, this.oy.data, index, ibytes);
            this.oy.wrote(ibytes);
        }
        if ((ret = is instanceof SeekableInputStream ? this.open_seekable() : this.open_nonseekable()) != 0) {
            this.datasource = null;
            this.clear();
        }
        return ret;
    }

    int open_nonseekable() {
        this.links = 1;
        this.vi = new Info[this.links];
        this.vi[0] = new Info();
        this.vc = new Comment[this.links];
        this.vc[0] = new Comment();
        int[] foo = new int[1];
        if (this.fetch_headers(this.vi[0], this.vc[0], foo, null) == -1) {
            return -1;
        }
        this.currentSerialno = foo[0];
        this.make_decode_ready();
        return 0;
    }

    int open_seekable() throws JOrbisException {
        Info initial_i = new Info();
        Comment initial_c = new Comment();
        Page og = new Page();
        int[] foo = new int[1];
        int ret = this.fetch_headers(initial_i, initial_c, foo, null);
        int serialno = foo[0];
        int dataoffset = (int)this.offset;
        this.os.clear();
        if (ret == -1) {
            return -1;
        }
        if (ret < 0) {
            return ret;
        }
        this.seekable = true;
        VorbisFile.fseek(this.datasource, 0L, 2);
        long end = this.offset = VorbisFile.ftell(this.datasource);
        end = this.get_prev_page(og);
        if (og.serialno() != serialno) {
            if (this.bisect_forward_serialno(0L, 0L, end + 1L, serialno, 0) < 0) {
                this.clear();
                return -128;
            }
        } else if (this.bisect_forward_serialno(0L, end, end + 1L, serialno, 0) < 0) {
            this.clear();
            return -128;
        }
        this.prefetch_all_headers(initial_i, initial_c, dataoffset);
        return 0;
    }

    void prefetch_all_headers(Info first_i, Comment first_c, int dataoffset) throws JOrbisException {
        Page og = new Page();
        this.vi = new Info[this.links];
        this.vc = new Comment[this.links];
        this.dataoffsets = new long[this.links];
        this.pcmlengths = new long[this.links];
        this.serialnos = new int[this.links];
        block0: for (int i = 0; i < this.links; ++i) {
            if (first_i != null && first_c != null && i == 0) {
                this.vi[i] = first_i;
                this.vc[i] = first_c;
                this.dataoffsets[i] = dataoffset;
            } else {
                this.seek_helper(this.offsets[i]);
                this.vi[i] = new Info();
                this.vc[i] = new Comment();
                if (this.fetch_headers(this.vi[i], this.vc[i], null, null) == -1) {
                    this.dataoffsets[i] = -1L;
                } else {
                    this.dataoffsets[i] = this.offset;
                    this.os.clear();
                }
            }
            long end = this.offsets[i + 1];
            this.seek_helper(end);
            do {
                int ret;
                if ((ret = this.get_prev_page(og)) != -1) continue;
                this.vi[i].clear();
                this.vc[i].clear();
                continue block0;
            } while (og.granulepos() == -1L);
            this.serialnos[i] = og.serialno();
            this.pcmlengths[i] = og.granulepos();
        }
    }

    int process_packet(int readp) {
        Page og = new Page();
        while (true) {
            Packet op;
            int result;
            if (this.decodeReady && (result = this.os.packetout(op = new Packet())) > 0) {
                long granulepos = op.granulepos;
                if (this.vb.synthesis(op) == 0) {
                    int oldsamples = this.vd.synthesis_pcmout(null, null);
                    this.vd.synthesis_blockin(this.vb);
                    this.samptrack += (float)(this.vd.synthesis_pcmout(null, null) - oldsamples);
                    this.bittrack += (float)(op.bytes * 8);
                    if (granulepos != -1L && op.endOfStream == 0) {
                        int link = this.seekable ? this.currentLink : 0;
                        int samples = this.vd.synthesis_pcmout(null, null);
                        granulepos -= (long)samples;
                        for (int i = 0; i < link; ++i) {
                            granulepos += this.pcmlengths[i];
                        }
                        this.pcmOffset = granulepos;
                    }
                    return 1;
                }
            }
            if (readp == 0) {
                return 0;
            }
            if (this.get_next_page(og, -1L) < 0) {
                return 0;
            }
            this.bittrack += (float)(og.headerLen * 8);
            if (this.decodeReady && this.currentSerialno != og.serialno()) {
                this.decode_clear();
            }
            if (!this.decodeReady) {
                if (this.seekable) {
                    this.currentSerialno = og.serialno();
                    for (i = 0; i < this.links && this.serialnos[i] != this.currentSerialno; ++i) {
                    }
                    if (i == this.links) {
                        return -1;
                    }
                    this.currentLink = i;
                    this.os.init(this.currentSerialno);
                    this.os.reset();
                } else {
                    int[] foo = new int[1];
                    int ret = this.fetch_headers(this.vi[0], this.vc[0], foo, og);
                    this.currentSerialno = foo[0];
                    if (ret != 0) {
                        return ret;
                    }
                    ++this.currentLink;
                    i = 0;
                }
                this.make_decode_ready();
            }
            this.os.pagein(og);
        }
    }

    int read(byte[] buffer, int length, int bigendianp, int word, int sgned, int[] bitstream) {
        int host_endian = this.host_is_big_endian();
        int index = 0;
        while (true) {
            if (this.decodeReady) {
                float[][][] _pcm = new float[1][][];
                int[] _index = new int[this.getInfo((int)-1).channels];
                int samples = this.vd.synthesis_pcmout(_pcm, _index);
                float[][] pcm = _pcm[0];
                if (samples != 0) {
                    int channels = this.getInfo((int)-1).channels;
                    int bytespersample = word * channels;
                    if (samples > length / bytespersample) {
                        samples = length / bytespersample;
                    }
                    if (word == 1) {
                        int off = sgned != 0 ? 0 : 128;
                        for (int j = 0; j < samples; ++j) {
                            for (int i = 0; i < channels; ++i) {
                                int val = (int)((double)pcm[i][_index[i] + j] * 128.0 + 0.5);
                                if (val > 127) {
                                    val = 127;
                                } else if (val < -128) {
                                    val = -128;
                                }
                                buffer[index++] = (byte)(val + off);
                            }
                        }
                    } else {
                        int off;
                        int n = off = sgned != 0 ? 0 : 32768;
                        if (host_endian == bigendianp) {
                            if (sgned != 0) {
                                for (int i = 0; i < channels; ++i) {
                                    int src = _index[i];
                                    int dest = i;
                                    for (int j = 0; j < samples; ++j) {
                                        int val = (int)((double)pcm[i][src + j] * 32768.0 + 0.5);
                                        if (val > Short.MAX_VALUE) {
                                            val = Short.MAX_VALUE;
                                        } else if (val < Short.MIN_VALUE) {
                                            val = Short.MIN_VALUE;
                                        }
                                        buffer[dest] = (byte)(val >>> 8);
                                        buffer[dest + 1] = (byte)val;
                                        dest += channels * 2;
                                    }
                                }
                            } else {
                                for (int i = 0; i < channels; ++i) {
                                    float[] src = pcm[i];
                                    int dest = i;
                                    for (int j = 0; j < samples; ++j) {
                                        int val = (int)((double)src[j] * 32768.0 + 0.5);
                                        if (val > Short.MAX_VALUE) {
                                            val = Short.MAX_VALUE;
                                        } else if (val < Short.MIN_VALUE) {
                                            val = Short.MIN_VALUE;
                                        }
                                        buffer[dest] = (byte)(val + off >>> 8);
                                        buffer[dest + 1] = (byte)(val + off);
                                        dest += channels * 2;
                                    }
                                }
                            }
                        } else if (bigendianp != 0) {
                            for (int j = 0; j < samples; ++j) {
                                for (int i = 0; i < channels; ++i) {
                                    int val = (int)((double)pcm[i][j] * 32768.0 + 0.5);
                                    if (val > Short.MAX_VALUE) {
                                        val = Short.MAX_VALUE;
                                    } else if (val < Short.MIN_VALUE) {
                                        val = Short.MIN_VALUE;
                                    }
                                    buffer[index++] = (byte)((val += off) >>> 8);
                                    buffer[index++] = (byte)val;
                                }
                            }
                        } else {
                            for (int j = 0; j < samples; ++j) {
                                for (int i = 0; i < channels; ++i) {
                                    int val = (int)((double)pcm[i][j] * 32768.0 + 0.5);
                                    if (val > Short.MAX_VALUE) {
                                        val = Short.MAX_VALUE;
                                    } else if (val < Short.MIN_VALUE) {
                                        val = Short.MIN_VALUE;
                                    }
                                    buffer[index++] = (byte)(val += off);
                                    buffer[index++] = (byte)(val >>> 8);
                                }
                            }
                        }
                    }
                    this.vd.synthesis_read(samples);
                    this.pcmOffset += (long)samples;
                    if (bitstream != null) {
                        bitstream[0] = this.currentLink;
                    }
                    return samples * bytespersample;
                }
            }
            switch (this.process_packet(1)) {
                case 0: {
                    return 0;
                }
                case -1: {
                    return -1;
                }
            }
        }
    }

    int time_seek(float seconds) {
        int link = -1;
        long pcm_total = this.pcm_total(-1);
        float time_total = this.time_total(-1);
        if (!this.seekable) {
            return -1;
        }
        if (seconds < 0.0f || seconds > time_total) {
            this.pcmOffset = -1L;
            this.decode_clear();
            return -1;
        }
        for (link = this.links - 1; link >= 0; --link) {
            pcm_total -= this.pcmlengths[link];
            if (seconds >= (time_total -= this.time_total(link))) break;
        }
        long target = (long)((float)pcm_total + (seconds - time_total) * (float)this.vi[link].rate);
        return this.pcm_seek(target);
    }

    private int get_data() {
        int index = this.oy.buffer(8500);
        byte[] buffer = this.oy.data;
        int bytes = 0;
        try {
            bytes = this.datasource.read(buffer, index, 8500);
        }
        catch (Exception e) {
            return -128;
        }
        this.oy.wrote(bytes);
        if (bytes == -1) {
            bytes = 0;
        }
        return bytes;
    }

    private int get_next_page(Page page, long boundary) {
        int ret;
        int more;
        block6: {
            if (boundary > 0L) {
                boundary += this.offset;
            }
            while (true) {
                if (boundary > 0L && this.offset >= boundary) {
                    return -1;
                }
                more = this.oy.pageseek(page);
                if (more < 0) {
                    this.offset -= (long)more;
                    continue;
                }
                if (more != 0) break block6;
                if (boundary == 0L) {
                    return -1;
                }
                ret = this.get_data();
                if (ret == 0) {
                    return -2;
                }
                if (ret < 0) break;
            }
            return -128;
        }
        ret = (int)this.offset;
        this.offset += (long)more;
        return ret;
    }

    private int get_prev_page(Page page) throws JOrbisException {
        int ret;
        long begin = this.offset;
        int offst = -1;
        block0: while (offst == -1) {
            if ((begin -= 8500L) < 0L) {
                begin = 0L;
            }
            this.seek_helper(begin);
            while (this.offset < begin + 8500L) {
                ret = this.get_next_page(page, begin + 8500L - this.offset);
                if (ret == -128) {
                    return -128;
                }
                if (ret < 0) {
                    if (offst != -1) continue block0;
                    throw new JOrbisException();
                }
                offst = ret;
            }
        }
        this.seek_helper(offst);
        ret = this.get_next_page(page, 8500L);
        if (ret < 0) {
            return -129;
        }
        return offst;
    }

    private int make_decode_ready() {
        if (this.decodeReady) {
            System.exit(1);
        }
        this.vd.synthesis_init(this.vi[0]);
        this.vb.init(this.vd);
        this.decodeReady = true;
        return 0;
    }

    private void seek_helper(long offst) {
        VorbisFile.fseek(this.datasource, offst, 0);
        this.offset = offst;
        this.oy.reset();
    }

    class SeekableInputStream
    extends InputStream {
        final String mode = "r";
        RandomAccessFile raf;

        SeekableInputStream(VorbisFile this$0, String file) throws IOException {
            Objects.requireNonNull(this$0);
            this.mode = "r";
            this.raf = new RandomAccessFile(file, "r");
        }

        @Override
        public int available() throws IOException {
            return this.raf.length() == this.raf.getFilePointer() ? 0 : 1;
        }

        @Override
        public void close() throws IOException {
            this.raf.close();
        }

        public long getLength() throws IOException {
            return this.raf.length();
        }

        @Override
        public synchronized void mark(int m) {
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int read() throws IOException {
            return this.raf.read();
        }

        @Override
        public int read(byte[] buf) throws IOException {
            return this.raf.read(buf);
        }

        @Override
        public int read(byte[] buf, int s, int len) throws IOException {
            return this.raf.read(buf, s, len);
        }

        @Override
        public synchronized void reset() throws IOException {
        }

        public void seek(long pos) throws IOException {
            this.raf.seek(pos);
        }

        @Override
        public long skip(long n) throws IOException {
            return this.raf.skipBytes((int)n);
        }

        public long tell() throws IOException {
            return this.raf.getFilePointer();
        }
    }
}

