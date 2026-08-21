/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.tukaani.xz.FinishableOutputStream
 *  org.tukaani.xz.FinishableWrapperOutputStream
 *  org.tukaani.xz.LZMA2InputStream
 *  org.tukaani.xz.LZMA2Options
 */
package org.apache.commons.compress.archivers.sevenz;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.compress.MemoryLimitException;
import org.apache.commons.compress.archivers.sevenz.AbstractCoder;
import org.apache.commons.compress.archivers.sevenz.Coder;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.FinishableWrapperOutputStream;
import org.tukaani.xz.LZMA2InputStream;
import org.tukaani.xz.LZMA2Options;

final class LZMA2Decoder
extends AbstractCoder {
    LZMA2Decoder() {
        super(LZMA2Options.class, Number.class);
    }

    @Override
    InputStream decode(String archiveName, InputStream in, long uncompressedLength, Coder coder, byte[] password, int maxMemoryLimitInKb) throws IOException {
        try {
            int dictionarySize = this.getDictionarySize(coder);
            int memoryUsageInKb = LZMA2InputStream.getMemoryUsage((int)dictionarySize);
            if (memoryUsageInKb > maxMemoryLimitInKb) {
                throw new MemoryLimitException(memoryUsageInKb, maxMemoryLimitInKb);
            }
            return new LZMA2InputStream(in, dictionarySize);
        }
        catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    OutputStream encode(OutputStream out, Object opts) throws IOException {
        return this.getOptions(opts).getOutputStream((FinishableOutputStream)new FinishableWrapperOutputStream(out));
    }

    private int getDictionarySize(Coder coder) throws IOException {
        if (coder.properties == null) {
            throw new IOException("Missing LZMA2 properties");
        }
        if (coder.properties.length < 1) {
            throw new IOException("LZMA2 properties too short");
        }
        int dictionarySizeBits = 0xFF & coder.properties[0];
        if ((dictionarySizeBits & 0xFFFFFFC0) != 0) {
            throw new IOException("Unsupported LZMA2 property bits");
        }
        if (dictionarySizeBits > 40) {
            throw new IOException("Dictionary larger than 4GiB maximum size");
        }
        if (dictionarySizeBits == 40) {
            return -1;
        }
        return (2 | dictionarySizeBits & 1) << dictionarySizeBits / 2 + 11;
    }

    private int getDictSize(Object opts) {
        if (opts instanceof LZMA2Options) {
            return ((LZMA2Options)opts).getDictSize();
        }
        return this.numberOptionOrDefault(opts);
    }

    private LZMA2Options getOptions(Object opts) throws IOException {
        if (opts instanceof LZMA2Options) {
            return (LZMA2Options)opts;
        }
        LZMA2Options options = new LZMA2Options();
        options.setDictSize(this.numberOptionOrDefault(opts));
        return options;
    }

    @Override
    byte[] getOptionsAsProperties(Object opts) {
        int dictSize = this.getDictSize(opts);
        int lead = Integer.numberOfLeadingZeros(dictSize);
        int secondBit = (dictSize >>> 30 - lead) - 2;
        return new byte[]{(byte)((19 - lead) * 2 + secondBit)};
    }

    @Override
    Object getOptionsFromCoder(Coder coder, InputStream in) throws IOException {
        return this.getDictionarySize(coder);
    }

    private int numberOptionOrDefault(Object opts) {
        return LZMA2Decoder.toInt(opts, 0x800000);
    }
}

