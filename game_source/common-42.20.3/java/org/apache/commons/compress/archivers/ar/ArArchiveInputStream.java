/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.ar;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.utils.ArchiveUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.ParsingUtils;

public class ArArchiveInputStream
extends ArchiveInputStream<ArArchiveEntry> {
    private static final int NAME_OFFSET = 0;
    private static final int NAME_LEN = 16;
    private static final int LAST_MODIFIED_OFFSET = 16;
    private static final int LAST_MODIFIED_LEN = 12;
    private static final int USER_ID_OFFSET = 28;
    private static final int USER_ID_LEN = 6;
    private static final int GROUP_ID_OFFSET = 34;
    private static final int GROUP_ID_LEN = 6;
    private static final int FILE_MODE_OFFSET = 40;
    private static final int FILE_MODE_LEN = 8;
    private static final int LENGTH_OFFSET = 48;
    private static final int LENGTH_LEN = 10;
    static final String BSD_LONGNAME_PREFIX = "#1/";
    private static final int BSD_LONGNAME_PREFIX_LEN = "#1/".length();
    private static final Pattern BSD_LONGNAME_PATTERN = Pattern.compile("^#1/\\d+");
    private static final String GNU_STRING_TABLE_NAME = "//";
    private static final Pattern GNU_LONGNAME_PATTERN = Pattern.compile("^/\\d+");
    private long offset;
    private boolean closed;
    private ArArchiveEntry currentEntry;
    private byte[] namebuffer;
    private long entryOffset = -1L;
    private final byte[] metaData = new byte[58];

    private static boolean isBSDLongName(String name) {
        return name != null && BSD_LONGNAME_PATTERN.matcher(name).matches();
    }

    private static boolean isGNUStringTable(String name) {
        return GNU_STRING_TABLE_NAME.equals(name);
    }

    public static boolean matches(byte[] signature, int length) {
        return length >= 8 && signature[0] == 33 && signature[1] == 60 && signature[2] == 97 && signature[3] == 114 && signature[4] == 99 && signature[5] == 104 && signature[6] == 62 && signature[7] == 10;
    }

    public ArArchiveInputStream(InputStream inputStream2) {
        super(inputStream2, StandardCharsets.US_ASCII.name());
    }

    private int asInt(byte[] byteArray, int offset, int len) throws IOException {
        return this.asInt(byteArray, offset, len, 10, false);
    }

    private int asInt(byte[] byteArray, int offset, int len, boolean treatBlankAsZero) throws IOException {
        return this.asInt(byteArray, offset, len, 10, treatBlankAsZero);
    }

    private int asInt(byte[] byteArray, int offset, int len, int base) throws IOException {
        return this.asInt(byteArray, offset, len, base, false);
    }

    private int asInt(byte[] byteArray, int offset, int len, int base, boolean treatBlankAsZero) throws IOException {
        String string = ArchiveUtils.toAsciiString(byteArray, offset, len).trim();
        if (string.isEmpty() && treatBlankAsZero) {
            return 0;
        }
        return ParsingUtils.parseIntValue(string, base);
    }

    private long asLong(byte[] byteArray, int offset, int len) throws IOException {
        return ParsingUtils.parseLongValue(ArchiveUtils.toAsciiString(byteArray, offset, len).trim());
    }

    @Override
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.in.close();
        }
        this.currentEntry = null;
    }

    private String getBSDLongName(String bsdLongName) throws IOException {
        int nameLen = ParsingUtils.parseIntValue(bsdLongName.substring(BSD_LONGNAME_PREFIX_LEN));
        byte[] name = IOUtils.readRange(this.in, nameLen);
        int read = name.length;
        this.trackReadBytes(read);
        if (read != nameLen) {
            throw new EOFException();
        }
        return ArchiveUtils.toAsciiString(name);
    }

    private String getExtendedName(int offset) throws IOException {
        if (this.namebuffer == null) {
            throw new IOException("Cannot process GNU long file name as no // record was found");
        }
        for (int i = offset; i < this.namebuffer.length; ++i) {
            if (this.namebuffer[i] != 10 && this.namebuffer[i] != 0) continue;
            if (i == 0) break;
            if (this.namebuffer[i - 1] == 47) {
                --i;
            }
            if (i - offset <= 0) break;
            return ArchiveUtils.toAsciiString(this.namebuffer, offset, i - offset);
        }
        throw new IOException("Failed to read entry: " + offset);
    }

    @Deprecated
    public ArArchiveEntry getNextArEntry() throws IOException {
        long len;
        byte[] realized;
        if (this.currentEntry != null) {
            long entryEnd = this.entryOffset + this.currentEntry.getLength();
            long skipped = org.apache.commons.io.IOUtils.skip(this.in, entryEnd - this.offset);
            this.trackReadBytes(skipped);
            this.currentEntry = null;
        }
        if (this.offset == 0L) {
            byte[] expected = ArchiveUtils.toAsciiBytes("!<arch>\n");
            realized = IOUtils.readRange(this.in, expected.length);
            int read = realized.length;
            this.trackReadBytes(read);
            if (read != expected.length) {
                throw new IOException("Failed to read header. Occurred at byte: " + this.getBytesRead());
            }
            if (!Arrays.equals(expected, realized)) {
                throw new IOException("Invalid header " + ArchiveUtils.toAsciiString(realized));
            }
        }
        if (this.offset % 2L != 0L) {
            if (this.in.read() < 0) {
                return null;
            }
            this.trackReadBytes(1L);
        }
        int read = IOUtils.readFully(this.in, this.metaData);
        this.trackReadBytes(read);
        if (read == 0) {
            return null;
        }
        if (read < this.metaData.length) {
            throw new IOException("Truncated ar archive");
        }
        byte[] expected = ArchiveUtils.toAsciiBytes("`\n");
        realized = IOUtils.readRange(this.in, expected.length);
        int read2 = realized.length;
        this.trackReadBytes(read2);
        if (read2 != expected.length) {
            throw new IOException("Failed to read entry trailer. Occurred at byte: " + this.getBytesRead());
        }
        if (!Arrays.equals(expected, realized)) {
            throw new IOException("Invalid entry trailer. not read the content? Occurred at byte: " + this.getBytesRead());
        }
        this.entryOffset = this.offset;
        String temp = ArchiveUtils.toAsciiString(this.metaData, 0, 16).trim();
        if (ArArchiveInputStream.isGNUStringTable(temp)) {
            this.currentEntry = this.readGNUStringTable(this.metaData, 48, 10);
            return this.getNextArEntry();
        }
        try {
            len = this.asLong(this.metaData, 48, 10);
        }
        catch (NumberFormatException ex) {
            throw new IOException("Broken archive, unable to parse ar_size field as a number", ex);
        }
        if (temp.endsWith("/")) {
            temp = temp.substring(0, temp.length() - 1);
        } else if (this.isGNULongName(temp)) {
            int off = ParsingUtils.parseIntValue(temp.substring(1));
            temp = this.getExtendedName(off);
        } else if (ArArchiveInputStream.isBSDLongName(temp)) {
            temp = this.getBSDLongName(temp);
            int nameLen = temp.length();
            len -= (long)nameLen;
            this.entryOffset += (long)nameLen;
        }
        if (len < 0L) {
            throw new IOException("broken archive, entry with negative size");
        }
        try {
            this.currentEntry = new ArArchiveEntry(temp, len, this.asInt(this.metaData, 28, 6, true), this.asInt(this.metaData, 34, 6, true), this.asInt(this.metaData, 40, 8, 8), this.asLong(this.metaData, 16, 12));
            return this.currentEntry;
        }
        catch (NumberFormatException ex) {
            throw new IOException("Broken archive, unable to parse entry metadata fields as numbers", ex);
        }
    }

    @Override
    public ArArchiveEntry getNextEntry() throws IOException {
        return this.getNextArEntry();
    }

    private boolean isGNULongName(String name) {
        return name != null && GNU_LONGNAME_PATTERN.matcher(name).matches();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        if (this.currentEntry == null) {
            throw new IllegalStateException("No current ar entry");
        }
        long entryEnd = this.entryOffset + this.currentEntry.getLength();
        if (len < 0 || this.offset >= entryEnd) {
            return -1;
        }
        int toRead = (int)Math.min((long)len, entryEnd - this.offset);
        int ret = this.in.read(b, off, toRead);
        this.trackReadBytes(ret);
        return ret;
    }

    private ArArchiveEntry readGNUStringTable(byte[] length, int offset, int len) throws IOException {
        int bufflen;
        try {
            bufflen = this.asInt(length, offset, len);
        }
        catch (NumberFormatException ex) {
            throw new IOException("Broken archive, unable to parse GNU string table length field as a number", ex);
        }
        this.namebuffer = IOUtils.readRange(this.in, bufflen);
        int read = this.namebuffer.length;
        this.trackReadBytes(read);
        if (read != bufflen) {
            throw new IOException("Failed to read complete // record: expected=" + bufflen + " read=" + read);
        }
        return new ArArchiveEntry(GNU_STRING_TABLE_NAME, bufflen);
    }

    private void trackReadBytes(long read) {
        this.count(read);
        if (read > 0L) {
            this.offset += read;
        }
    }
}

