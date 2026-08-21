/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.deflate;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;

public class DeflateCompressorOutputStream
extends CompressorOutputStream<DeflaterOutputStream> {
    private final Deflater deflater;

    public DeflateCompressorOutputStream(OutputStream outputStream2) {
        this(outputStream2, new DeflateParameters());
    }

    public DeflateCompressorOutputStream(OutputStream outputStream2, DeflateParameters parameters) {
        this.deflater = new Deflater(parameters.getCompressionLevel(), !parameters.withZlibHeader());
        this.out = new DeflaterOutputStream(outputStream2, this.deflater);
    }

    @Override
    public void close() throws IOException {
        try {
            this.out.close();
        }
        finally {
            this.deflater.end();
        }
    }

    public void finish() throws IOException {
        ((DeflaterOutputStream)this.out()).finish();
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        this.out.write(buf, off, len);
    }
}

