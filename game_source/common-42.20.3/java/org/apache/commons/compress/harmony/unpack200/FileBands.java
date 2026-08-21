/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.BandSet;
import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.commons.compress.harmony.unpack200.SegmentOptions;
import org.apache.commons.compress.utils.IOUtils;

public class FileBands
extends BandSet {
    private byte[][] fileBits;
    private int[] fileModtime;
    private String[] fileName;
    private int[] fileOptions;
    private long[] fileSize;
    private final String[] cpUTF8;
    private InputStream in;

    public FileBands(Segment segment) {
        super(segment);
        this.cpUTF8 = segment.getCpBands().getCpUTF8();
    }

    public byte[][] getFileBits() {
        return this.fileBits;
    }

    public int[] getFileModtime() {
        return this.fileModtime;
    }

    public String[] getFileName() {
        return this.fileName;
    }

    public int[] getFileOptions() {
        return this.fileOptions;
    }

    public long[] getFileSize() {
        return this.fileSize;
    }

    public void processFileBits() throws IOException {
        int numberOfFiles = this.header.getNumberOfFiles();
        this.fileBits = new byte[numberOfFiles][];
        for (int i = 0; i < numberOfFiles; ++i) {
            int size = (int)this.fileSize[i];
            this.fileBits[i] = IOUtils.readRange(this.in, size);
            int read = this.fileBits[i].length;
            if (size == 0 || read >= size) continue;
            throw new IOException("Expected to read " + size + " bytes but read " + read);
        }
    }

    @Override
    public void read(InputStream in) throws IOException, Pack200Exception {
        int numberOfFiles = this.header.getNumberOfFiles();
        SegmentOptions options = this.header.getOptions();
        this.fileName = this.parseReferences("file_name", in, Codec.UNSIGNED5, numberOfFiles, this.cpUTF8);
        this.fileSize = this.parseFlags("file_size", in, numberOfFiles, Codec.UNSIGNED5, options.hasFileSizeHi());
        this.fileModtime = options.hasFileModtime() ? this.decodeBandInt("file_modtime", in, Codec.DELTA5, numberOfFiles) : new int[numberOfFiles];
        this.fileOptions = options.hasFileOptions() ? this.decodeBandInt("file_options", in, Codec.UNSIGNED5, numberOfFiles) : new int[numberOfFiles];
        this.in = in;
    }

    @Override
    public void unpack() {
    }
}

