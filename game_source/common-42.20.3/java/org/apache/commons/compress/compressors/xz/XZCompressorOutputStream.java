/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.tukaani.xz.FilterOptions
 *  org.tukaani.xz.LZMA2Options
 *  org.tukaani.xz.XZOutputStream
 */
package org.apache.commons.compress.compressors.xz;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class XZCompressorOutputStream
extends CompressorOutputStream<XZOutputStream> {
    public XZCompressorOutputStream(OutputStream outputStream2) throws IOException {
        super(new XZOutputStream(outputStream2, (FilterOptions)new LZMA2Options()));
    }

    public XZCompressorOutputStream(OutputStream outputStream2, int preset) throws IOException {
        super(new XZOutputStream(outputStream2, (FilterOptions)new LZMA2Options(preset)));
    }

    public void finish() throws IOException {
        ((XZOutputStream)this.out()).finish();
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        this.out.write(buf, off, len);
    }
}

