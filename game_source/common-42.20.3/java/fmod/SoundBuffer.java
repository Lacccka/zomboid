/*
 * Decompiled with CFR 0.152.
 */
package fmod;

import zombie.debug.DebugLog;

public class SoundBuffer {
    public int bufSize;
    public int bufRead;
    public int bufWrite;
    private final short[] intdata;
    private int delay;

    public SoundBuffer(int bufSize) {
        this.bufSize = bufSize;
        this.bufRead = 0;
        this.bufWrite = 0;
        this.delay = 1;
        this.intdata = new short[bufSize];
    }

    public void get(long blocksize, short[] data) {
        int len = this.bufWrite - this.bufRead;
        if (len < 0) {
            len += this.bufSize;
        }
        if ((long)len < blocksize) {
            int i = 0;
            while ((long)i < blocksize - 1L) {
                data[i] = 0;
                ++i;
            }
            return;
        }
        if ((long)len > blocksize * (long)this.delay * 2L) {
            if ((long)this.delay * blocksize * 3L < (long)this.bufSize) {
                ++this.delay;
            }
            DebugLog.log("[SoundBuffer] correct: delay: " + this.delay);
            this.bufRead = (int)((long)this.bufWrite - blocksize * (long)this.delay);
            if (this.bufRead < 0) {
                this.bufRead += this.bufSize;
            }
            int i = 0;
            int k = this.bufRead;
            while ((long)i < blocksize * 2L) {
                this.intdata[k] = 0;
                ++i;
                k = (k + 1) % this.bufSize;
            }
            return;
        }
        int i = 0;
        int k = this.bufRead;
        while ((long)i < blocksize - 1L && k != this.bufWrite) {
            data[i] = this.intdata[k];
            ++i;
            k = (k + 1) % this.bufSize;
        }
        this.bufRead = k;
    }

    public void push(long datasize, short[] data) {
        boolean isZero = false;
        int i = 0;
        int k = this.bufWrite;
        while ((long)i < datasize - 1L) {
            this.intdata[k] = data[i];
            if (data[i] != 0) {
                isZero = true;
            }
            ++i;
            k = (k + 1) % this.bufSize;
        }
        if (isZero) {
            this.bufWrite = k;
        }
    }

    public void push(long datasize, byte[] data) {
        boolean isZero = false;
        int i = 0;
        int k = this.bufWrite;
        while ((long)i < datasize - 1L) {
            this.intdata[k] = (short)(data[i + 1] * 256 + data[i]);
            if (data[i] != 0) {
                isZero = true;
            }
            i += 2;
            k = (k + 1) % this.bufSize;
        }
        if (isZero) {
            this.bufWrite = k;
        }
    }

    public short[] buf() {
        return this.intdata;
    }
}

