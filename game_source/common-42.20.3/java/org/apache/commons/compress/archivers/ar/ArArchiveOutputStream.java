/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.ar;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.utils.ArchiveUtils;

public class ArArchiveOutputStream
extends ArchiveOutputStream<ArArchiveEntry> {
    private static final char PAD = '\n';
    private static final char SPACE = ' ';
    public static final int LONGFILE_ERROR = 0;
    public static final int LONGFILE_BSD = 1;
    private long entryOffset;
    private int headerPlus;
    private ArArchiveEntry prevEntry;
    private boolean prevEntryOpen;
    private int longFileMode = 0;

    public ArArchiveOutputStream(OutputStream out) {
        super(out);
    }

    private String checkLength(String value, int max, String name) throws IOException {
        if (value.length() > max) {
            throw new IOException(name + " too long");
        }
        return value;
    }

    @Override
    public void close() throws IOException {
        try {
            if (!this.isFinished()) {
                this.finish();
            }
        }
        finally {
            this.prevEntry = null;
            super.close();
        }
    }

    @Override
    public void closeArchiveEntry() throws IOException {
        this.checkFinished();
        if (this.prevEntry == null || !this.prevEntryOpen) {
            throw new IOException("No current entry to close");
        }
        if (((long)this.headerPlus + this.entryOffset) % 2L != 0L) {
            this.out.write(10);
        }
        this.prevEntryOpen = false;
    }

    @Override
    public ArArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
        this.checkFinished();
        return new ArArchiveEntry(inputFile, entryName);
    }

    @Override
    public ArArchiveEntry createArchiveEntry(Path inputPath, String entryName, LinkOption ... options) throws IOException {
        this.checkFinished();
        return new ArArchiveEntry(inputPath, entryName, options);
    }

    @Override
    public void finish() throws IOException {
        if (this.prevEntryOpen) {
            throw new IOException("This archive contains unclosed entries.");
        }
        this.checkFinished();
        super.finish();
    }

    private int pad(int offset, int newOffset, char fill) throws IOException {
        int diff = newOffset - offset;
        if (diff > 0) {
            for (int i = 0; i < diff; ++i) {
                this.write(fill);
            }
        }
        return newOffset;
    }

    @Override
    public void putArchiveEntry(ArArchiveEntry entry) throws IOException {
        this.checkFinished();
        if (this.prevEntry == null) {
            this.writeArchiveHeader();
        } else {
            if (this.prevEntry.getLength() != this.entryOffset) {
                throw new IOException("Length does not match entry (" + this.prevEntry.getLength() + " != " + this.entryOffset);
            }
            if (this.prevEntryOpen) {
                this.closeArchiveEntry();
            }
        }
        this.prevEntry = entry;
        this.headerPlus = this.writeEntryHeader(entry);
        this.entryOffset = 0L;
        this.prevEntryOpen = true;
    }

    public void setLongFileMode(int longFileMode) {
        this.longFileMode = longFileMode;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        this.count(len);
        this.entryOffset += (long)len;
    }

    private int write(String data) throws IOException {
        byte[] bytes = data.getBytes(StandardCharsets.US_ASCII);
        this.write(bytes);
        return bytes.length;
    }

    private void writeArchiveHeader() throws IOException {
        this.out.write(ArchiveUtils.toAsciiBytes("!<arch>\n"));
    }

    private int writeEntryHeader(ArArchiveEntry entry) throws IOException {
        int offset = 0;
        boolean appendName = false;
        String eName = entry.getName();
        int nLength = eName.length();
        if (0 == this.longFileMode && nLength > 16) {
            throw new IOException("File name too long, > 16 chars: " + eName);
        }
        if (1 == this.longFileMode && (nLength > 16 || eName.indexOf(32) > -1)) {
            appendName = true;
            String fileNameLen = "#1/" + nLength;
            if (fileNameLen.length() > 16) {
                throw new IOException("File length too long, > 16 chars: " + eName);
            }
            offset += this.write(fileNameLen);
        } else {
            offset += this.write(eName);
        }
        offset = this.pad(offset, 16, ' ');
        offset += this.write(this.checkLength(String.valueOf(entry.getLastModified()), 12, "Last modified"));
        offset = this.pad(offset, 28, ' ');
        offset += this.write(this.checkLength(String.valueOf(entry.getUserId()), 6, "User ID"));
        offset = this.pad(offset, 34, ' ');
        offset += this.write(this.checkLength(String.valueOf(entry.getGroupId()), 6, "Group ID"));
        offset = this.pad(offset, 40, ' ');
        offset += this.write(this.checkLength(String.valueOf(Integer.toString(entry.getMode(), 8)), 8, "File mode"));
        offset = this.pad(offset, 48, ' ');
        offset += this.write(this.checkLength(String.valueOf(entry.getLength() + (long)(appendName ? nLength : 0)), 10, "Size"));
        offset = this.pad(offset, 58, ' ');
        offset += this.write("`\n");
        if (appendName) {
            offset += this.write(eName);
        }
        return offset;
    }
}

