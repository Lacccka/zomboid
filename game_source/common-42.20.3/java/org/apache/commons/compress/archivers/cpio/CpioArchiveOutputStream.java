/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.cpio;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.commons.compress.archivers.cpio.CpioUtil;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.utils.ArchiveUtils;

public class CpioArchiveOutputStream
extends ArchiveOutputStream<CpioArchiveEntry>
implements CpioConstants {
    private CpioArchiveEntry entry;
    private final short entryFormat;
    private final HashMap<String, CpioArchiveEntry> names = new HashMap();
    private long crc;
    private long written;
    private final int blockSize;
    private long nextArtificalDeviceAndInode = 1L;
    private final ZipEncoding zipEncoding;
    final String charsetName;

    public CpioArchiveOutputStream(OutputStream out) {
        this(out, 1);
    }

    public CpioArchiveOutputStream(OutputStream out, short format) {
        this(out, format, 512, CpioUtil.DEFAULT_CHARSET_NAME);
    }

    public CpioArchiveOutputStream(OutputStream out, short format, int blockSize) {
        this(out, format, blockSize, CpioUtil.DEFAULT_CHARSET_NAME);
    }

    public CpioArchiveOutputStream(OutputStream out, short format, int blockSize, String encoding) {
        super(out);
        switch (format) {
            case 1: 
            case 2: 
            case 4: 
            case 8: {
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown format: " + format);
            }
        }
        this.entryFormat = format;
        this.blockSize = blockSize;
        this.charsetName = encoding;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
    }

    public CpioArchiveOutputStream(OutputStream out, String encoding) {
        this(out, 1, 512, encoding);
    }

    @Override
    public void close() throws IOException {
        try {
            if (!this.isFinished()) {
                this.finish();
            }
        }
        finally {
            if (!this.isClosed()) {
                super.close();
            }
        }
    }

    @Override
    public void closeArchiveEntry() throws IOException {
        this.checkFinished();
        this.checkOpen();
        if (this.entry == null) {
            throw new IOException("Trying to close non-existent entry");
        }
        if (this.entry.getSize() != this.written) {
            throw new IOException("Invalid entry size (expected " + this.entry.getSize() + " but got " + this.written + " bytes)");
        }
        this.pad(this.entry.getDataPadCount());
        if (this.entry.getFormat() == 2 && this.crc != this.entry.getChksum()) {
            throw new IOException("CRC Error");
        }
        this.entry = null;
        this.crc = 0L;
        this.written = 0L;
    }

    @Override
    public CpioArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
        this.checkFinished();
        return new CpioArchiveEntry(inputFile, entryName);
    }

    @Override
    public CpioArchiveEntry createArchiveEntry(Path inputPath, String entryName, LinkOption ... options) throws IOException {
        this.checkFinished();
        return new CpioArchiveEntry(inputPath, entryName, options);
    }

    private byte[] encode(String str) throws IOException {
        ByteBuffer buf = this.zipEncoding.encode(str);
        int len = buf.limit() - buf.position();
        return Arrays.copyOfRange(buf.array(), buf.arrayOffset(), buf.arrayOffset() + len);
    }

    @Override
    public void finish() throws IOException {
        this.checkOpen();
        this.checkFinished();
        if (this.entry != null) {
            throw new IOException("This archive contains unclosed entries.");
        }
        this.entry = new CpioArchiveEntry(this.entryFormat);
        this.entry.setName("TRAILER!!!");
        this.entry.setNumberOfLinks(1L);
        this.writeHeader(this.entry);
        this.closeArchiveEntry();
        int lengthOfLastBlock = (int)(this.getBytesWritten() % (long)this.blockSize);
        if (lengthOfLastBlock != 0) {
            this.pad(this.blockSize - lengthOfLastBlock);
        }
        super.finish();
    }

    private void pad(int count) throws IOException {
        if (count > 0) {
            byte[] buff = new byte[count];
            this.out.write(buff);
            this.count(count);
        }
    }

    @Override
    public void putArchiveEntry(CpioArchiveEntry entry) throws IOException {
        short format;
        this.checkFinished();
        this.checkOpen();
        if (this.entry != null) {
            this.closeArchiveEntry();
        }
        if (entry.getTime() == -1L) {
            entry.setTime(System.currentTimeMillis() / 1000L);
        }
        if ((format = entry.getFormat()) != this.entryFormat) {
            throw new IOException("Header format: " + format + " does not match existing format: " + this.entryFormat);
        }
        if (this.names.put(entry.getName(), entry) != null) {
            throw new IOException("Duplicate entry: " + entry.getName());
        }
        this.writeHeader(entry);
        this.entry = entry;
        this.written = 0L;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.checkOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        if (this.entry == null) {
            throw new IOException("No current CPIO entry");
        }
        if (this.written + (long)len > this.entry.getSize()) {
            throw new IOException("Attempt to write past end of STORED entry");
        }
        this.out.write(b, off, len);
        this.written += (long)len;
        if (this.entry.getFormat() == 2) {
            for (int pos = 0; pos < len; ++pos) {
                this.crc += (long)(b[pos] & 0xFF);
                this.crc &= 0xFFFFFFFFL;
            }
        }
        this.count(len);
    }

    private void writeAsciiLong(long number, int length, int radix) throws IOException {
        String tmpStr;
        StringBuilder tmp = new StringBuilder();
        if (radix == 16) {
            tmp.append(Long.toHexString(number));
        } else if (radix == 8) {
            tmp.append(Long.toOctalString(number));
        } else {
            tmp.append(number);
        }
        if (tmp.length() <= length) {
            int insertLength = length - tmp.length();
            for (int pos = 0; pos < insertLength; ++pos) {
                tmp.insert(0, "0");
            }
            tmpStr = tmp.toString();
        } else {
            tmpStr = tmp.substring(tmp.length() - length);
        }
        byte[] b = ArchiveUtils.toAsciiBytes(tmpStr);
        this.out.write(b);
        this.count(b.length);
    }

    private void writeBinaryLong(long number, int length, boolean swapHalfWord) throws IOException {
        byte[] tmp = CpioUtil.long2byteArray(number, length, swapHalfWord);
        this.out.write(tmp);
        this.count(tmp.length);
    }

    private void writeCString(byte[] str) throws IOException {
        this.out.write(str);
        this.out.write(0);
        this.count(str.length + 1);
    }

    private void writeHeader(CpioArchiveEntry e) throws IOException {
        switch (e.getFormat()) {
            case 1: {
                this.out.write(ArchiveUtils.toAsciiBytes("070701"));
                this.count(6);
                this.writeNewEntry(e);
                break;
            }
            case 2: {
                this.out.write(ArchiveUtils.toAsciiBytes("070702"));
                this.count(6);
                this.writeNewEntry(e);
                break;
            }
            case 4: {
                this.out.write(ArchiveUtils.toAsciiBytes("070707"));
                this.count(6);
                this.writeOldAsciiEntry(e);
                break;
            }
            case 8: {
                boolean swapHalfWord = true;
                this.writeBinaryLong(29127L, 2, true);
                this.writeOldBinaryEntry(e, true);
                break;
            }
            default: {
                throw new IOException("Unknown format " + e.getFormat());
            }
        }
    }

    private void writeNewEntry(CpioArchiveEntry entry) throws IOException {
        long inode = entry.getInode();
        long devMin = entry.getDeviceMin();
        if ("TRAILER!!!".equals(entry.getName())) {
            devMin = 0L;
            inode = 0L;
        } else if (inode == 0L && devMin == 0L) {
            inode = this.nextArtificalDeviceAndInode & 0xFFFFFFFFFFFFFFFFL;
            devMin = this.nextArtificalDeviceAndInode++ >> 32 & 0xFFFFFFFFFFFFFFFFL;
        } else {
            this.nextArtificalDeviceAndInode = Math.max(this.nextArtificalDeviceAndInode, inode + 0x100000000L * devMin) + 1L;
        }
        this.writeAsciiLong(inode, 8, 16);
        this.writeAsciiLong(entry.getMode(), 8, 16);
        this.writeAsciiLong(entry.getUID(), 8, 16);
        this.writeAsciiLong(entry.getGID(), 8, 16);
        this.writeAsciiLong(entry.getNumberOfLinks(), 8, 16);
        this.writeAsciiLong(entry.getTime(), 8, 16);
        this.writeAsciiLong(entry.getSize(), 8, 16);
        this.writeAsciiLong(entry.getDeviceMaj(), 8, 16);
        this.writeAsciiLong(devMin, 8, 16);
        this.writeAsciiLong(entry.getRemoteDeviceMaj(), 8, 16);
        this.writeAsciiLong(entry.getRemoteDeviceMin(), 8, 16);
        byte[] name = this.encode(entry.getName());
        this.writeAsciiLong((long)name.length + 1L, 8, 16);
        this.writeAsciiLong(entry.getChksum(), 8, 16);
        this.writeCString(name);
        this.pad(entry.getHeaderPadCount(name.length));
    }

    private void writeOldAsciiEntry(CpioArchiveEntry entry) throws IOException {
        long inode = entry.getInode();
        long device = entry.getDevice();
        if ("TRAILER!!!".equals(entry.getName())) {
            device = 0L;
            inode = 0L;
        } else if (inode == 0L && device == 0L) {
            inode = this.nextArtificalDeviceAndInode & 0x3FFFFL;
            device = this.nextArtificalDeviceAndInode++ >> 18 & 0x3FFFFL;
        } else {
            this.nextArtificalDeviceAndInode = Math.max(this.nextArtificalDeviceAndInode, inode + 262144L * device) + 1L;
        }
        this.writeAsciiLong(device, 6, 8);
        this.writeAsciiLong(inode, 6, 8);
        this.writeAsciiLong(entry.getMode(), 6, 8);
        this.writeAsciiLong(entry.getUID(), 6, 8);
        this.writeAsciiLong(entry.getGID(), 6, 8);
        this.writeAsciiLong(entry.getNumberOfLinks(), 6, 8);
        this.writeAsciiLong(entry.getRemoteDevice(), 6, 8);
        this.writeAsciiLong(entry.getTime(), 11, 8);
        byte[] name = this.encode(entry.getName());
        this.writeAsciiLong((long)name.length + 1L, 6, 8);
        this.writeAsciiLong(entry.getSize(), 11, 8);
        this.writeCString(name);
    }

    private void writeOldBinaryEntry(CpioArchiveEntry entry, boolean swapHalfWord) throws IOException {
        long inode = entry.getInode();
        long device = entry.getDevice();
        if ("TRAILER!!!".equals(entry.getName())) {
            device = 0L;
            inode = 0L;
        } else if (inode == 0L && device == 0L) {
            inode = this.nextArtificalDeviceAndInode & 0xFFFFL;
            device = this.nextArtificalDeviceAndInode++ >> 16 & 0xFFFFL;
        } else {
            this.nextArtificalDeviceAndInode = Math.max(this.nextArtificalDeviceAndInode, inode + 65536L * device) + 1L;
        }
        this.writeBinaryLong(device, 2, swapHalfWord);
        this.writeBinaryLong(inode, 2, swapHalfWord);
        this.writeBinaryLong(entry.getMode(), 2, swapHalfWord);
        this.writeBinaryLong(entry.getUID(), 2, swapHalfWord);
        this.writeBinaryLong(entry.getGID(), 2, swapHalfWord);
        this.writeBinaryLong(entry.getNumberOfLinks(), 2, swapHalfWord);
        this.writeBinaryLong(entry.getRemoteDevice(), 2, swapHalfWord);
        this.writeBinaryLong(entry.getTime(), 4, swapHalfWord);
        byte[] name = this.encode(entry.getName());
        this.writeBinaryLong((long)name.length + 1L, 2, swapHalfWord);
        this.writeBinaryLong(entry.getSize(), 4, swapHalfWord);
        this.writeCString(name);
        this.pad(entry.getHeaderPadCount(name.length));
    }
}

