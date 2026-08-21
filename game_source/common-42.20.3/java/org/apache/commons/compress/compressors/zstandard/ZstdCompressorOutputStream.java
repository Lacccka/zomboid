/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.github.luben.zstd.ZstdOutputStream
 */
package org.apache.commons.compress.compressors.zstandard;

import com.github.luben.zstd.ZstdOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;

public class ZstdCompressorOutputStream
extends CompressorOutputStream<ZstdOutputStream> {
    public ZstdCompressorOutputStream(OutputStream outStream) throws IOException {
        super(new ZstdOutputStream(outStream));
    }

    public ZstdCompressorOutputStream(OutputStream outStream, int level) throws IOException {
        super(new ZstdOutputStream(outStream, level));
    }

    public ZstdCompressorOutputStream(OutputStream outStream, int level, boolean closeFrameOnFlush) throws IOException {
        super(new ZstdOutputStream(outStream, level));
        ((ZstdOutputStream)this.out()).setCloseFrameOnFlush(closeFrameOnFlush);
    }

    public ZstdCompressorOutputStream(OutputStream outStream, int level, boolean closeFrameOnFlush, boolean useChecksum) throws IOException {
        super(new ZstdOutputStream(outStream, level));
        ((ZstdOutputStream)this.out()).setCloseFrameOnFlush(closeFrameOnFlush).setChecksum(useChecksum);
    }

    public String toString() {
        return this.out.toString();
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        this.out.write(buf, off, len);
    }
}

