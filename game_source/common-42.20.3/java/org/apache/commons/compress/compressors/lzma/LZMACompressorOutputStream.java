/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.tukaani.xz.LZMA2Options
 *  org.tukaani.xz.LZMAOutputStream
 */
package org.apache.commons.compress.compressors.lzma;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;

public class LZMACompressorOutputStream
extends CompressorOutputStream<LZMAOutputStream> {
    public LZMACompressorOutputStream(OutputStream outputStream2) throws IOException {
        super(new LZMAOutputStream(outputStream2, new LZMA2Options(), -1L));
    }

    public void finish() throws IOException {
        ((LZMAOutputStream)this.out()).finish();
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        this.out.write(buf, off, len);
    }
}

