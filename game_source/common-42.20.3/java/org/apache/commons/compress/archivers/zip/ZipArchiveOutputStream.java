/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.FileRandomAccessOutputStream;
import org.apache.commons.compress.archivers.zip.GeneralPurposeBit;
import org.apache.commons.compress.archivers.zip.RandomAccessOutputStream;
import org.apache.commons.compress.archivers.zip.ResourceAlignmentExtraField;
import org.apache.commons.compress.archivers.zip.SeekableChannelRandomAccessOutputStream;
import org.apache.commons.compress.archivers.zip.StreamCompressor;
import org.apache.commons.compress.archivers.zip.UnicodeCommentExtraField;
import org.apache.commons.compress.archivers.zip.UnicodePathExtraField;
import org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.Zip64RequiredException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipEightByteInteger;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipLong;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.commons.compress.archivers.zip.ZipShort;
import org.apache.commons.compress.archivers.zip.ZipSplitOutputStream;
import org.apache.commons.compress.archivers.zip.ZipUtil;
import org.apache.commons.compress.utils.ByteUtils;
import org.apache.commons.io.Charsets;

public class ZipArchiveOutputStream
extends ArchiveOutputStream<ZipArchiveEntry> {
    static final int BUFFER_SIZE = 512;
    private static final int LFH_SIG_OFFSET = 0;
    private static final int LFH_VERSION_NEEDED_OFFSET = 4;
    private static final int LFH_GPB_OFFSET = 6;
    private static final int LFH_METHOD_OFFSET = 8;
    private static final int LFH_TIME_OFFSET = 10;
    private static final int LFH_CRC_OFFSET = 14;
    private static final int LFH_COMPRESSED_SIZE_OFFSET = 18;
    private static final int LFH_ORIGINAL_SIZE_OFFSET = 22;
    private static final int LFH_FILENAME_LENGTH_OFFSET = 26;
    private static final int LFH_EXTRA_LENGTH_OFFSET = 28;
    private static final int LFH_FILENAME_OFFSET = 30;
    private static final int CFH_SIG_OFFSET = 0;
    private static final int CFH_VERSION_MADE_BY_OFFSET = 4;
    private static final int CFH_VERSION_NEEDED_OFFSET = 6;
    private static final int CFH_GPB_OFFSET = 8;
    private static final int CFH_METHOD_OFFSET = 10;
    private static final int CFH_TIME_OFFSET = 12;
    private static final int CFH_CRC_OFFSET = 16;
    private static final int CFH_COMPRESSED_SIZE_OFFSET = 20;
    private static final int CFH_ORIGINAL_SIZE_OFFSET = 24;
    private static final int CFH_FILENAME_LENGTH_OFFSET = 28;
    private static final int CFH_EXTRA_LENGTH_OFFSET = 30;
    private static final int CFH_COMMENT_LENGTH_OFFSET = 32;
    private static final int CFH_DISK_NUMBER_OFFSET = 34;
    private static final int CFH_INTERNAL_ATTRIBUTES_OFFSET = 36;
    private static final int CFH_EXTERNAL_ATTRIBUTES_OFFSET = 38;
    private static final int CFH_LFH_OFFSET = 42;
    private static final int CFH_FILENAME_OFFSET = 46;
    public static final int DEFLATED = 8;
    public static final int DEFAULT_COMPRESSION = -1;
    public static final int STORED = 0;
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    @Deprecated
    public static final int EFS_FLAG = 2048;
    private static final byte[] ZERO = new byte[]{0, 0};
    private static final byte[] LZERO = new byte[]{0, 0, 0, 0};
    private static final byte[] ONE = ZipLong.getBytes(1L);
    static final byte[] LFH_SIG = ZipLong.LFH_SIG.getBytes();
    static final byte[] DD_SIG = ZipLong.DD_SIG.getBytes();
    static final byte[] CFH_SIG = ZipLong.CFH_SIG.getBytes();
    static final byte[] EOCD_SIG = ZipLong.getBytes(101010256L);
    static final byte[] ZIP64_EOCD_SIG = ZipLong.getBytes(101075792L);
    static final byte[] ZIP64_EOCD_LOC_SIG = ZipLong.getBytes(117853008L);
    @Deprecated
    protected boolean finished;
    private CurrentEntry entry;
    private String comment = "";
    private int level = -1;
    private boolean hasCompressionLevelChanged;
    private int method = 8;
    private final List<ZipArchiveEntry> entries = new LinkedList<ZipArchiveEntry>();
    private final StreamCompressor streamCompressor;
    private long cdOffset;
    private long cdLength;
    private long cdDiskNumberStart;
    private long eocdLength;
    private final Map<ZipArchiveEntry, EntryMetaData> metaData = new HashMap<ZipArchiveEntry, EntryMetaData>();
    private Charset charset = DEFAULT_CHARSET;
    private ZipEncoding zipEncoding = ZipEncodingHelper.getZipEncoding(DEFAULT_CHARSET);
    protected final Deflater def;
    private boolean useUTF8Flag = true;
    private boolean fallbackToUTF8;
    private UnicodeExtraFieldPolicy createUnicodeExtraFields = UnicodeExtraFieldPolicy.NEVER;
    private boolean hasUsedZip64;
    private Zip64Mode zip64Mode = Zip64Mode.AsNeeded;
    private final byte[] copyBuffer = new byte[32768];
    private final boolean isSplitZip;
    private final Map<Integer, Integer> numberOfCDInDiskData = new HashMap<Integer, Integer>();

    public ZipArchiveOutputStream(File file) throws IOException {
        this(file.toPath(), new OpenOption[0]);
    }

    public ZipArchiveOutputStream(File file, long zipSplitSize) throws IOException {
        this(file.toPath(), zipSplitSize);
    }

    public ZipArchiveOutputStream(OutputStream out) {
        this.out = out;
        this.def = new Deflater(this.level, true);
        this.streamCompressor = StreamCompressor.create(out, this.def);
        this.isSplitZip = false;
    }

    public ZipArchiveOutputStream(Path path, long zipSplitSize) throws IOException {
        this.def = new Deflater(this.level, true);
        this.out = new ZipSplitOutputStream(path, zipSplitSize);
        this.streamCompressor = StreamCompressor.create(this.out, this.def);
        this.isSplitZip = true;
    }

    public ZipArchiveOutputStream(Path file, OpenOption ... options) throws IOException {
        this.def = new Deflater(this.level, true);
        this.out = options.length == 0 ? new FileRandomAccessOutputStream(file) : new FileRandomAccessOutputStream(file, options);
        this.streamCompressor = StreamCompressor.create(this.out, this.def);
        this.isSplitZip = false;
    }

    public ZipArchiveOutputStream(SeekableByteChannel channel) {
        this.out = new SeekableChannelRandomAccessOutputStream(channel);
        this.def = new Deflater(this.level, true);
        this.streamCompressor = StreamCompressor.create(this.out, this.def);
        this.isSplitZip = false;
    }

    public void addRawArchiveEntry(ZipArchiveEntry entry, InputStream rawStream) throws IOException {
        ZipArchiveEntry ae = new ZipArchiveEntry(entry);
        if (this.hasZip64Extra(ae)) {
            ae.removeExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
        }
        boolean is2PhaseSource = ae.getCrc() != -1L && ae.getSize() != -1L && ae.getCompressedSize() != -1L;
        this.putArchiveEntry(ae, is2PhaseSource);
        this.copyFromZipInputStream(rawStream);
        this.closeCopiedEntry(is2PhaseSource);
    }

    private void addUnicodeExtraFields(ZipArchiveEntry ze, boolean encodable, ByteBuffer name) throws IOException {
        String comm;
        if (this.createUnicodeExtraFields == UnicodeExtraFieldPolicy.ALWAYS || !encodable) {
            ze.addExtraField(new UnicodePathExtraField(ze.getName(), name.array(), name.arrayOffset(), name.limit() - name.position()));
        }
        if ((comm = ze.getComment()) != null && !comm.isEmpty()) {
            boolean commentEncodable = this.zipEncoding.canEncode(comm);
            if (this.createUnicodeExtraFields == UnicodeExtraFieldPolicy.ALWAYS || !commentEncodable) {
                ByteBuffer commentB = this.getEntryEncoding(ze).encode(comm);
                ze.addExtraField(new UnicodeCommentExtraField(comm, commentB.array(), commentB.arrayOffset(), commentB.limit() - commentB.position()));
            }
        }
    }

    @Override
    public boolean canWriteEntryData(ArchiveEntry ae) {
        if (ae instanceof ZipArchiveEntry) {
            ZipArchiveEntry zae = (ZipArchiveEntry)ae;
            return zae.getMethod() != ZipMethod.IMPLODING.getCode() && zae.getMethod() != ZipMethod.UNSHRINKING.getCode() && ZipUtil.canHandleEntryData(zae);
        }
        return false;
    }

    private boolean checkIfNeedsZip64(Zip64Mode effectiveMode) throws ZipException {
        boolean actuallyNeedsZip64 = this.isZip64Required(this.entry.entry, effectiveMode);
        if (actuallyNeedsZip64 && effectiveMode == Zip64Mode.Never) {
            throw new Zip64RequiredException(Zip64RequiredException.getEntryTooBigMessage(this.entry.entry));
        }
        return actuallyNeedsZip64;
    }

    @Override
    public void close() throws IOException {
        try {
            if (!this.finished) {
                this.finish();
            }
        }
        finally {
            this.destroy();
        }
    }

    @Override
    public void closeArchiveEntry() throws IOException {
        this.preClose();
        this.flushDeflater();
        long bytesWritten = this.streamCompressor.getTotalBytesWritten() - this.entry.dataStart;
        long realCrc = this.streamCompressor.getCrc32();
        this.entry.bytesRead = this.streamCompressor.getBytesRead();
        Zip64Mode effectiveMode = this.getEffectiveZip64Mode(this.entry.entry);
        boolean actuallyNeedsZip64 = this.handleSizesAndCrc(bytesWritten, realCrc, effectiveMode);
        this.closeEntry(actuallyNeedsZip64, false);
        this.streamCompressor.reset();
    }

    private void closeCopiedEntry(boolean phased) throws IOException {
        this.preClose();
        this.entry.bytesRead = this.entry.entry.getSize();
        Zip64Mode effectiveMode = this.getEffectiveZip64Mode(this.entry.entry);
        boolean actuallyNeedsZip64 = this.checkIfNeedsZip64(effectiveMode);
        this.closeEntry(actuallyNeedsZip64, phased);
    }

    private void closeEntry(boolean actuallyNeedsZip64, boolean phased) throws IOException {
        if (!phased && this.out instanceof RandomAccessOutputStream) {
            this.rewriteSizesAndCrc(actuallyNeedsZip64);
        }
        if (!phased) {
            this.writeDataDescriptor(this.entry.entry);
        }
        this.entry = null;
    }

    private void copyFromZipInputStream(InputStream src) throws IOException {
        int length;
        if (this.entry == null) {
            throw new IllegalStateException("No current entry");
        }
        ZipUtil.checkRequestedFeatures(this.entry.entry);
        this.entry.hasWritten = true;
        while ((length = src.read(this.copyBuffer)) >= 0) {
            this.streamCompressor.writeCounted(this.copyBuffer, 0, length);
            this.count(length);
        }
    }

    @Override
    public ZipArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
        if (this.finished) {
            throw new IOException("Stream has already been finished");
        }
        return new ZipArchiveEntry(inputFile, entryName);
    }

    @Override
    public ZipArchiveEntry createArchiveEntry(Path inputPath, String entryName, LinkOption ... options) throws IOException {
        if (this.finished) {
            throw new IOException("Stream has already been finished");
        }
        return new ZipArchiveEntry(inputPath, entryName, new LinkOption[0]);
    }

    private byte[] createCentralFileHeader(ZipArchiveEntry ze) throws IOException {
        boolean needsZip64Extra;
        EntryMetaData entryMetaData = this.metaData.get(ze);
        boolean bl = needsZip64Extra = this.hasZip64Extra(ze) || ze.getCompressedSize() >= 0xFFFFFFFFL || ze.getSize() >= 0xFFFFFFFFL || entryMetaData.offset >= 0xFFFFFFFFL || ze.getDiskNumberStart() >= 65535L || this.zip64Mode == Zip64Mode.Always || this.zip64Mode == Zip64Mode.AlwaysWithCompatibility;
        if (needsZip64Extra && this.zip64Mode == Zip64Mode.Never) {
            throw new Zip64RequiredException("Archive's size exceeds the limit of 4GByte.");
        }
        this.handleZip64Extra(ze, entryMetaData.offset, needsZip64Extra);
        return this.createCentralFileHeader(ze, this.getName(ze), entryMetaData, needsZip64Extra);
    }

    private byte[] createCentralFileHeader(ZipArchiveEntry ze, ByteBuffer name, EntryMetaData entryMetaData, boolean needsZip64Extra) throws IOException {
        if (this.isSplitZip) {
            int currentSplitSegment = ((ZipSplitOutputStream)this.out).getCurrentSplitSegmentIndex();
            if (this.numberOfCDInDiskData.get(currentSplitSegment) == null) {
                this.numberOfCDInDiskData.put(currentSplitSegment, 1);
            } else {
                int originalNumberOfCD = this.numberOfCDInDiskData.get(currentSplitSegment);
                this.numberOfCDInDiskData.put(currentSplitSegment, originalNumberOfCD + 1);
            }
        }
        byte[] extra = ze.getCentralDirectoryExtra();
        int extraLength = extra.length;
        String comm = ze.getComment();
        if (comm == null) {
            comm = "";
        }
        ByteBuffer commentB = this.getEntryEncoding(ze).encode(comm);
        int nameLen = name.limit() - name.position();
        int commentLen = commentB.limit() - commentB.position();
        int len = 46 + nameLen + extraLength + commentLen;
        byte[] buf = new byte[len];
        System.arraycopy(CFH_SIG, 0, buf, 0, 4);
        ZipShort.putShort(ze.getPlatform() << 8 | (!this.hasUsedZip64 ? 20 : 45), buf, 4);
        int zipMethod = ze.getMethod();
        boolean encodable = this.zipEncoding.canEncode(ze.getName());
        ZipShort.putShort(this.versionNeededToExtract(zipMethod, needsZip64Extra, entryMetaData.usesDataDescriptor), buf, 6);
        this.getGeneralPurposeBits(!encodable && this.fallbackToUTF8, entryMetaData.usesDataDescriptor).encode(buf, 8);
        ZipShort.putShort(zipMethod, buf, 10);
        ZipUtil.toDosTime(ze.getTime(), buf, 12);
        ZipLong.putLong(ze.getCrc(), buf, 16);
        if (ze.getCompressedSize() >= 0xFFFFFFFFL || ze.getSize() >= 0xFFFFFFFFL || this.zip64Mode == Zip64Mode.Always || this.zip64Mode == Zip64Mode.AlwaysWithCompatibility) {
            ZipLong.ZIP64_MAGIC.putLong(buf, 20);
            ZipLong.ZIP64_MAGIC.putLong(buf, 24);
        } else {
            ZipLong.putLong(ze.getCompressedSize(), buf, 20);
            ZipLong.putLong(ze.getSize(), buf, 24);
        }
        ZipShort.putShort(nameLen, buf, 28);
        ZipShort.putShort(extraLength, buf, 30);
        ZipShort.putShort(commentLen, buf, 32);
        if (this.isSplitZip) {
            if (ze.getDiskNumberStart() >= 65535L || this.zip64Mode == Zip64Mode.Always) {
                ZipShort.putShort(65535, buf, 34);
            } else {
                ZipShort.putShort((int)ze.getDiskNumberStart(), buf, 34);
            }
        } else {
            System.arraycopy(ZERO, 0, buf, 34, 2);
        }
        ZipShort.putShort(ze.getInternalAttributes(), buf, 36);
        ZipLong.putLong(ze.getExternalAttributes(), buf, 38);
        if (entryMetaData.offset >= 0xFFFFFFFFL || this.zip64Mode == Zip64Mode.Always) {
            ZipLong.putLong(0xFFFFFFFFL, buf, 42);
        } else {
            ZipLong.putLong(Math.min(entryMetaData.offset, 0xFFFFFFFFL), buf, 42);
        }
        System.arraycopy(name.array(), name.arrayOffset(), buf, 46, nameLen);
        int extraStart = 46 + nameLen;
        System.arraycopy(extra, 0, buf, extraStart, extraLength);
        int commentStart = extraStart + extraLength;
        System.arraycopy(commentB.array(), commentB.arrayOffset(), buf, commentStart, commentLen);
        return buf;
    }

    private byte[] createLocalFileHeader(ZipArchiveEntry ze, ByteBuffer name, boolean encodable, boolean phased, long archiveOffset) {
        ZipExtraField oldEx = ze.getExtraField(ResourceAlignmentExtraField.ID);
        if (oldEx != null) {
            ze.removeExtraField(ResourceAlignmentExtraField.ID);
        }
        ResourceAlignmentExtraField oldAlignmentEx = oldEx instanceof ResourceAlignmentExtraField ? (ResourceAlignmentExtraField)oldEx : null;
        int alignment = ze.getAlignment();
        if (alignment <= 0 && oldAlignmentEx != null) {
            alignment = oldAlignmentEx.getAlignment();
        }
        if (alignment > 1 || oldAlignmentEx != null && !oldAlignmentEx.allowMethodChange()) {
            int oldLength = 30 + name.limit() - name.position() + ze.getLocalFileDataExtra().length;
            int padding = (int)(-archiveOffset - (long)oldLength - 4L - 2L & (long)(alignment - 1));
            ze.addExtraField(new ResourceAlignmentExtraField(alignment, oldAlignmentEx != null && oldAlignmentEx.allowMethodChange(), padding));
        }
        byte[] extra = ze.getLocalFileDataExtra();
        int nameLen = name.limit() - name.position();
        int len = 30 + nameLen + extra.length;
        byte[] buf = new byte[len];
        System.arraycopy(LFH_SIG, 0, buf, 0, 4);
        int zipMethod = ze.getMethod();
        boolean dataDescriptor = this.usesDataDescriptor(zipMethod, phased);
        ZipShort.putShort(this.versionNeededToExtract(zipMethod, this.hasZip64Extra(ze), dataDescriptor), buf, 4);
        GeneralPurposeBit generalPurposeBit = this.getGeneralPurposeBits(!encodable && this.fallbackToUTF8, dataDescriptor);
        generalPurposeBit.encode(buf, 6);
        ZipShort.putShort(zipMethod, buf, 8);
        ZipUtil.toDosTime(ze.getTime(), buf, 10);
        if (phased || zipMethod != 8 && !(this.out instanceof RandomAccessOutputStream)) {
            ZipLong.putLong(ze.getCrc(), buf, 14);
        } else {
            System.arraycopy(LZERO, 0, buf, 14, 4);
        }
        if (this.hasZip64Extra(this.entry.entry)) {
            ZipLong.ZIP64_MAGIC.putLong(buf, 18);
            ZipLong.ZIP64_MAGIC.putLong(buf, 22);
        } else if (phased) {
            ZipLong.putLong(ze.getCompressedSize(), buf, 18);
            ZipLong.putLong(ze.getSize(), buf, 22);
        } else if (zipMethod == 8 || this.out instanceof RandomAccessOutputStream) {
            System.arraycopy(LZERO, 0, buf, 18, 4);
            System.arraycopy(LZERO, 0, buf, 22, 4);
        } else {
            ZipLong.putLong(ze.getSize(), buf, 18);
            ZipLong.putLong(ze.getSize(), buf, 22);
        }
        ZipShort.putShort(nameLen, buf, 26);
        ZipShort.putShort(extra.length, buf, 28);
        System.arraycopy(name.array(), name.arrayOffset(), buf, 30, nameLen);
        System.arraycopy(extra, 0, buf, 30 + nameLen, extra.length);
        return buf;
    }

    protected final void deflate() throws IOException {
        this.streamCompressor.deflate();
    }

    void destroy() throws IOException {
        if (this.out != null) {
            this.out.close();
        }
    }

    @Override
    public void finish() throws IOException {
        long cdOverallOffset;
        if (this.finished) {
            throw new IOException("This archive has already been finished");
        }
        if (this.entry != null) {
            throw new IOException("This archive contains unclosed entries.");
        }
        this.cdOffset = cdOverallOffset = this.streamCompressor.getTotalBytesWritten();
        if (this.isSplitZip) {
            ZipSplitOutputStream zipSplitOutputStream = (ZipSplitOutputStream)this.out;
            this.cdOffset = zipSplitOutputStream.getCurrentSplitSegmentBytesWritten();
            this.cdDiskNumberStart = zipSplitOutputStream.getCurrentSplitSegmentIndex();
        }
        this.writeCentralDirectoryInChunks();
        this.cdLength = this.streamCompressor.getTotalBytesWritten() - cdOverallOffset;
        ByteBuffer commentData = this.zipEncoding.encode(this.comment);
        long commentLength = (long)commentData.limit() - (long)commentData.position();
        this.eocdLength = 22L + commentLength;
        this.writeZip64CentralDirectory();
        this.writeCentralDirectoryEnd();
        this.metaData.clear();
        this.entries.clear();
        this.streamCompressor.close();
        if (this.isSplitZip) {
            this.out.close();
        }
        this.finished = true;
    }

    @Override
    public void flush() throws IOException {
        if (this.out != null) {
            this.out.flush();
        }
    }

    private void flushDeflater() throws IOException {
        if (this.entry.entry.getMethod() == 8) {
            this.streamCompressor.flushDeflater();
        }
    }

    @Override
    public long getBytesWritten() {
        return this.streamCompressor.getTotalBytesWritten();
    }

    private Zip64Mode getEffectiveZip64Mode(ZipArchiveEntry ze) {
        if (this.zip64Mode != Zip64Mode.AsNeeded || this.out instanceof RandomAccessOutputStream || ze.getMethod() != 8 || ze.getSize() != -1L) {
            return this.zip64Mode;
        }
        return Zip64Mode.Never;
    }

    public String getEncoding() {
        return this.charset != null ? this.charset.name() : null;
    }

    private ZipEncoding getEntryEncoding(ZipArchiveEntry ze) {
        boolean encodable = this.zipEncoding.canEncode(ze.getName());
        return !encodable && this.fallbackToUTF8 ? ZipEncodingHelper.ZIP_ENCODING_UTF_8 : this.zipEncoding;
    }

    private GeneralPurposeBit getGeneralPurposeBits(boolean utfFallback, boolean usesDataDescriptor) {
        GeneralPurposeBit b = new GeneralPurposeBit();
        b.useUTF8ForNames(this.useUTF8Flag || utfFallback);
        if (usesDataDescriptor) {
            b.useDataDescriptor(true);
        }
        return b;
    }

    private ByteBuffer getName(ZipArchiveEntry ze) throws IOException {
        return this.getEntryEncoding(ze).encode(ze.getName());
    }

    private Zip64ExtendedInformationExtraField getZip64Extra(ZipArchiveEntry ze) {
        Zip64ExtendedInformationExtraField z64;
        if (this.entry != null) {
            this.entry.causedUseOfZip64 = !this.hasUsedZip64;
        }
        this.hasUsedZip64 = true;
        ZipExtraField extra = ze.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
        Zip64ExtendedInformationExtraField zip64ExtendedInformationExtraField = z64 = extra instanceof Zip64ExtendedInformationExtraField ? (Zip64ExtendedInformationExtraField)extra : null;
        if (z64 == null) {
            z64 = new Zip64ExtendedInformationExtraField();
        }
        ze.addAsFirstExtraField(z64);
        return z64;
    }

    private boolean handleSizesAndCrc(long bytesWritten, long crc, Zip64Mode effectiveMode) throws ZipException {
        if (this.entry.entry.getMethod() == 8) {
            this.entry.entry.setSize(this.entry.bytesRead);
            this.entry.entry.setCompressedSize(bytesWritten);
            this.entry.entry.setCrc(crc);
        } else if (!(this.out instanceof RandomAccessOutputStream)) {
            if (this.entry.entry.getCrc() != crc) {
                throw new ZipException("Bad CRC checksum for entry " + this.entry.entry.getName() + ": " + Long.toHexString(this.entry.entry.getCrc()) + " instead of " + Long.toHexString(crc));
            }
            if (this.entry.entry.getSize() != bytesWritten) {
                throw new ZipException("Bad size for entry " + this.entry.entry.getName() + ": " + this.entry.entry.getSize() + " instead of " + bytesWritten);
            }
        } else {
            this.entry.entry.setSize(bytesWritten);
            this.entry.entry.setCompressedSize(bytesWritten);
            this.entry.entry.setCrc(crc);
        }
        return this.checkIfNeedsZip64(effectiveMode);
    }

    private void handleZip64Extra(ZipArchiveEntry ze, long lfhOffset, boolean needsZip64Extra) {
        if (needsZip64Extra) {
            boolean needsToEncodeDiskNumberStart;
            Zip64ExtendedInformationExtraField z64 = this.getZip64Extra(ze);
            if (ze.getCompressedSize() >= 0xFFFFFFFFL || ze.getSize() >= 0xFFFFFFFFL || this.zip64Mode == Zip64Mode.Always || this.zip64Mode == Zip64Mode.AlwaysWithCompatibility) {
                z64.setCompressedSize(new ZipEightByteInteger(ze.getCompressedSize()));
                z64.setSize(new ZipEightByteInteger(ze.getSize()));
            } else {
                z64.setCompressedSize(null);
                z64.setSize(null);
            }
            boolean needsToEncodeLfhOffset = lfhOffset >= 0xFFFFFFFFL || this.zip64Mode == Zip64Mode.Always;
            boolean bl = needsToEncodeDiskNumberStart = ze.getDiskNumberStart() >= 65535L || this.zip64Mode == Zip64Mode.Always;
            if (needsToEncodeLfhOffset || needsToEncodeDiskNumberStart) {
                z64.setRelativeHeaderOffset(new ZipEightByteInteger(lfhOffset));
            }
            if (needsToEncodeDiskNumberStart) {
                z64.setDiskStartNumber(new ZipLong(ze.getDiskNumberStart()));
            }
            ze.setExtra();
        }
    }

    private boolean hasZip64Extra(ZipArchiveEntry ze) {
        return ze.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID) instanceof Zip64ExtendedInformationExtraField;
    }

    public boolean isSeekable() {
        return this.out instanceof RandomAccessOutputStream;
    }

    private boolean isTooLargeForZip32(ZipArchiveEntry zipArchiveEntry) {
        return zipArchiveEntry.getSize() >= 0xFFFFFFFFL || zipArchiveEntry.getCompressedSize() >= 0xFFFFFFFFL;
    }

    private boolean isZip64Required(ZipArchiveEntry entry1, Zip64Mode requestedMode) {
        return requestedMode == Zip64Mode.Always || requestedMode == Zip64Mode.AlwaysWithCompatibility || this.isTooLargeForZip32(entry1);
    }

    private void preClose() throws IOException {
        if (this.finished) {
            throw new IOException("Stream has already been finished");
        }
        if (this.entry == null) {
            throw new IOException("No current entry to close");
        }
        if (!this.entry.hasWritten) {
            this.write(ByteUtils.EMPTY_BYTE_ARRAY, 0, 0);
        }
    }

    @Override
    public void putArchiveEntry(ZipArchiveEntry archiveEntry) throws IOException {
        this.putArchiveEntry(archiveEntry, false);
    }

    private void putArchiveEntry(ZipArchiveEntry archiveEntry, boolean phased) throws IOException {
        if (this.finished) {
            throw new IOException("Stream has already been finished");
        }
        if (this.entry != null) {
            this.closeArchiveEntry();
        }
        this.entry = new CurrentEntry(archiveEntry);
        this.entries.add(this.entry.entry);
        this.setDefaults(this.entry.entry);
        Zip64Mode effectiveMode = this.getEffectiveZip64Mode(this.entry.entry);
        this.validateSizeInformation(effectiveMode);
        if (this.shouldAddZip64Extra(this.entry.entry, effectiveMode)) {
            ZipEightByteInteger compressedSize;
            ZipEightByteInteger size;
            Zip64ExtendedInformationExtraField z64 = this.getZip64Extra(this.entry.entry);
            if (phased) {
                size = new ZipEightByteInteger(this.entry.entry.getSize());
                compressedSize = new ZipEightByteInteger(this.entry.entry.getCompressedSize());
            } else {
                compressedSize = this.entry.entry.getMethod() == 0 && this.entry.entry.getSize() != -1L ? (size = new ZipEightByteInteger(this.entry.entry.getSize())) : (size = ZipEightByteInteger.ZERO);
            }
            z64.setSize(size);
            z64.setCompressedSize(compressedSize);
            this.entry.entry.setExtra();
        }
        if (this.entry.entry.getMethod() == 8 && this.hasCompressionLevelChanged) {
            this.def.setLevel(this.level);
            this.hasCompressionLevelChanged = false;
        }
        this.writeLocalFileHeader(archiveEntry, phased);
    }

    private void rewriteSizesAndCrc(boolean actuallyNeedsZip64) throws IOException {
        RandomAccessOutputStream randomStream = (RandomAccessOutputStream)this.out;
        long dataStart = this.entry.localDataStart;
        if (randomStream instanceof ZipSplitOutputStream) {
            dataStart = ((ZipSplitOutputStream)randomStream).calculateDiskPosition(this.entry.entry.getDiskNumberStart(), dataStart);
        }
        long position = dataStart;
        randomStream.writeFully(ZipLong.getBytes(this.entry.entry.getCrc()), position);
        position += 4L;
        if (!this.hasZip64Extra(this.entry.entry) || !actuallyNeedsZip64) {
            randomStream.writeFully(ZipLong.getBytes(this.entry.entry.getCompressedSize()), position);
            randomStream.writeFully(ZipLong.getBytes(this.entry.entry.getSize()), position += 4L);
            position += 4L;
        } else {
            randomStream.writeFully(ZipLong.ZIP64_MAGIC.getBytes(), position);
            randomStream.writeFully(ZipLong.ZIP64_MAGIC.getBytes(), position += 4L);
            position += 4L;
        }
        if (this.hasZip64Extra(this.entry.entry)) {
            ByteBuffer name = this.getName(this.entry.entry);
            int nameLen = name.limit() - name.position();
            position = dataStart + 12L + 4L + (long)nameLen + 4L;
            randomStream.writeFully(ZipEightByteInteger.getBytes(this.entry.entry.getSize()), position);
            randomStream.writeFully(ZipEightByteInteger.getBytes(this.entry.entry.getCompressedSize()), position += 8L);
            position += 8L;
            if (!actuallyNeedsZip64) {
                position = dataStart - 10L;
                randomStream.writeFully(ZipShort.getBytes(this.versionNeededToExtract(this.entry.entry.getMethod(), false, false)), position);
                position += 2L;
                this.entry.entry.removeExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
                this.entry.entry.setExtra();
                if (this.entry.causedUseOfZip64) {
                    this.hasUsedZip64 = false;
                }
            }
        }
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy b) {
        this.createUnicodeExtraFields = b;
    }

    private void setDefaults(ZipArchiveEntry entry) {
        if (entry.getMethod() == -1) {
            entry.setMethod(this.method);
        }
        if (entry.getTime() == -1L) {
            entry.setTime(System.currentTimeMillis());
        }
    }

    private void setEncoding(Charset encoding) {
        this.charset = encoding;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        if (this.useUTF8Flag && !ZipEncodingHelper.isUTF8(encoding)) {
            this.useUTF8Flag = false;
        }
    }

    public void setEncoding(String encoding) {
        this.setEncoding(Charsets.toCharset(encoding));
    }

    public void setFallbackToUTF8(boolean b) {
        this.fallbackToUTF8 = b;
    }

    public void setLevel(int level) {
        if (level < -1 || level > 9) {
            throw new IllegalArgumentException("Invalid compression level: " + level);
        }
        if (this.level == level) {
            return;
        }
        this.hasCompressionLevelChanged = true;
        this.level = level;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public void setUseLanguageEncodingFlag(boolean b) {
        this.useUTF8Flag = b && ZipEncodingHelper.isUTF8(this.charset);
    }

    public void setUseZip64(Zip64Mode mode) {
        this.zip64Mode = mode;
    }

    private boolean shouldAddZip64Extra(ZipArchiveEntry entry, Zip64Mode mode) {
        return mode == Zip64Mode.Always || mode == Zip64Mode.AlwaysWithCompatibility || entry.getSize() >= 0xFFFFFFFFL || entry.getCompressedSize() >= 0xFFFFFFFFL || entry.getSize() == -1L && this.out instanceof RandomAccessOutputStream && mode != Zip64Mode.Never;
    }

    private boolean shouldUseZip64EOCD() {
        int numberOfThisDisk = 0;
        if (this.isSplitZip) {
            numberOfThisDisk = ((ZipSplitOutputStream)this.out).getCurrentSplitSegmentIndex();
        }
        int numOfEntriesOnThisDisk = this.numberOfCDInDiskData.getOrDefault(numberOfThisDisk, 0);
        return numberOfThisDisk >= 65535 || this.cdDiskNumberStart >= 65535L || numOfEntriesOnThisDisk >= 65535 || this.entries.size() >= 65535 || this.cdLength >= 0xFFFFFFFFL || this.cdOffset >= 0xFFFFFFFFL;
    }

    private boolean usesDataDescriptor(int zipMethod, boolean phased) {
        return !phased && zipMethod == 8 && !(this.out instanceof RandomAccessOutputStream);
    }

    private void validateIfZip64IsNeededInEOCD() throws Zip64RequiredException {
        if (this.zip64Mode != Zip64Mode.Never) {
            return;
        }
        int numberOfThisDisk = 0;
        if (this.isSplitZip) {
            numberOfThisDisk = ((ZipSplitOutputStream)this.out).getCurrentSplitSegmentIndex();
        }
        if (numberOfThisDisk >= 65535) {
            throw new Zip64RequiredException("Number of the disk of End Of Central Directory exceeds the limit of 65535.");
        }
        if (this.cdDiskNumberStart >= 65535L) {
            throw new Zip64RequiredException("Number of the disk with the start of Central Directory exceeds the limit of 65535.");
        }
        int numOfEntriesOnThisDisk = this.numberOfCDInDiskData.getOrDefault(numberOfThisDisk, 0);
        if (numOfEntriesOnThisDisk >= 65535) {
            throw new Zip64RequiredException("Number of entries on this disk exceeds the limit of 65535.");
        }
        if (this.entries.size() >= 65535) {
            throw new Zip64RequiredException("Archive contains more than 65535 entries.");
        }
        if (this.cdLength >= 0xFFFFFFFFL) {
            throw new Zip64RequiredException("The size of the entire central directory exceeds the limit of 4GByte.");
        }
        if (this.cdOffset >= 0xFFFFFFFFL) {
            throw new Zip64RequiredException("Archive's size exceeds the limit of 4GByte.");
        }
    }

    private void validateSizeInformation(Zip64Mode effectiveMode) throws ZipException {
        if (this.entry.entry.getMethod() == 0 && !(this.out instanceof RandomAccessOutputStream)) {
            if (this.entry.entry.getSize() == -1L) {
                throw new ZipException("Uncompressed size is required for STORED method when not writing to a file");
            }
            if (this.entry.entry.getCrc() == -1L) {
                throw new ZipException("CRC checksum is required for STORED method when not writing to a file");
            }
            this.entry.entry.setCompressedSize(this.entry.entry.getSize());
        }
        if ((this.entry.entry.getSize() >= 0xFFFFFFFFL || this.entry.entry.getCompressedSize() >= 0xFFFFFFFFL) && effectiveMode == Zip64Mode.Never) {
            throw new Zip64RequiredException(Zip64RequiredException.getEntryTooBigMessage(this.entry.entry));
        }
    }

    private int versionNeededToExtract(int zipMethod, boolean zip64, boolean usedDataDescriptor) {
        if (zip64) {
            return 45;
        }
        if (usedDataDescriptor) {
            return 20;
        }
        return this.versionNeededToExtractMethod(zipMethod);
    }

    private int versionNeededToExtractMethod(int zipMethod) {
        return zipMethod == 8 ? 20 : 10;
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        if (this.entry == null) {
            throw new IllegalStateException("No current entry");
        }
        ZipUtil.checkRequestedFeatures(this.entry.entry);
        long writtenThisTime = this.streamCompressor.write(b, offset, length, this.entry.entry.getMethod());
        this.count(writtenThisTime);
    }

    protected void writeCentralDirectoryEnd() throws IOException {
        if (!this.hasUsedZip64 && this.isSplitZip) {
            ((ZipSplitOutputStream)this.out).prepareToWriteUnsplittableContent(this.eocdLength);
        }
        this.validateIfZip64IsNeededInEOCD();
        this.writeCounted(EOCD_SIG);
        int numberOfThisDisk = 0;
        if (this.isSplitZip) {
            numberOfThisDisk = ((ZipSplitOutputStream)this.out).getCurrentSplitSegmentIndex();
        }
        this.writeCounted(ZipShort.getBytes(numberOfThisDisk));
        this.writeCounted(ZipShort.getBytes((int)this.cdDiskNumberStart));
        int numberOfEntries = this.entries.size();
        int numOfEntriesOnThisDisk = this.isSplitZip ? this.numberOfCDInDiskData.getOrDefault(numberOfThisDisk, 0) : numberOfEntries;
        byte[] numOfEntriesOnThisDiskData = ZipShort.getBytes(Math.min(numOfEntriesOnThisDisk, 65535));
        this.writeCounted(numOfEntriesOnThisDiskData);
        byte[] num = ZipShort.getBytes(Math.min(numberOfEntries, 65535));
        this.writeCounted(num);
        this.writeCounted(ZipLong.getBytes(Math.min(this.cdLength, 0xFFFFFFFFL)));
        this.writeCounted(ZipLong.getBytes(Math.min(this.cdOffset, 0xFFFFFFFFL)));
        ByteBuffer data = this.zipEncoding.encode(this.comment);
        int dataLen = data.limit() - data.position();
        this.writeCounted(ZipShort.getBytes(dataLen));
        this.streamCompressor.writeCounted(data.array(), data.arrayOffset(), dataLen);
    }

    private void writeCentralDirectoryInChunks() throws IOException {
        int NUM_PER_WRITE = 1000;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(70000);
        int count = 0;
        for (ZipArchiveEntry ze : this.entries) {
            byteArrayOutputStream.write(this.createCentralFileHeader(ze));
            if (++count <= 1000) continue;
            this.writeCounted(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.reset();
            count = 0;
        }
        this.writeCounted(byteArrayOutputStream.toByteArray());
    }

    protected void writeCentralFileHeader(ZipArchiveEntry ze) throws IOException {
        byte[] centralFileHeader = this.createCentralFileHeader(ze);
        this.writeCounted(centralFileHeader);
    }

    private void writeCounted(byte[] data) throws IOException {
        this.streamCompressor.writeCounted(data);
    }

    protected void writeDataDescriptor(ZipArchiveEntry ze) throws IOException {
        if (!this.usesDataDescriptor(ze.getMethod(), false)) {
            return;
        }
        this.writeCounted(DD_SIG);
        this.writeCounted(ZipLong.getBytes(ze.getCrc()));
        if (!this.hasZip64Extra(ze)) {
            this.writeCounted(ZipLong.getBytes(ze.getCompressedSize()));
            this.writeCounted(ZipLong.getBytes(ze.getSize()));
        } else {
            this.writeCounted(ZipEightByteInteger.getBytes(ze.getCompressedSize()));
            this.writeCounted(ZipEightByteInteger.getBytes(ze.getSize()));
        }
    }

    protected void writeLocalFileHeader(ZipArchiveEntry ze) throws IOException {
        this.writeLocalFileHeader(ze, false);
    }

    private void writeLocalFileHeader(ZipArchiveEntry ze, boolean phased) throws IOException {
        boolean encodable = this.zipEncoding.canEncode(ze.getName());
        ByteBuffer name = this.getName(ze);
        if (this.createUnicodeExtraFields != UnicodeExtraFieldPolicy.NEVER) {
            this.addUnicodeExtraFields(ze, encodable, name);
        }
        long localHeaderStart = this.streamCompressor.getTotalBytesWritten();
        if (this.isSplitZip) {
            ZipSplitOutputStream splitOutputStream = (ZipSplitOutputStream)this.out;
            ze.setDiskNumberStart(splitOutputStream.getCurrentSplitSegmentIndex());
            localHeaderStart = splitOutputStream.getCurrentSplitSegmentBytesWritten();
        }
        byte[] localHeader = this.createLocalFileHeader(ze, name, encodable, phased, localHeaderStart);
        this.metaData.put(ze, new EntryMetaData(localHeaderStart, this.usesDataDescriptor(ze.getMethod(), phased)));
        this.entry.localDataStart = localHeaderStart + 14L;
        this.writeCounted(localHeader);
        this.entry.dataStart = this.streamCompressor.getTotalBytesWritten();
    }

    protected final void writeOut(byte[] data) throws IOException {
        this.streamCompressor.writeOut(data, 0, data.length);
    }

    protected final void writeOut(byte[] data, int offset, int length) throws IOException {
        this.streamCompressor.writeOut(data, offset, length);
    }

    public void writePreamble(byte[] preamble) throws IOException {
        this.writePreamble(preamble, 0, preamble.length);
    }

    public void writePreamble(byte[] preamble, int offset, int length) throws IOException {
        if (this.entry != null) {
            throw new IllegalStateException("Preamble must be written before creating an entry");
        }
        this.streamCompressor.writeCounted(preamble, offset, length);
    }

    protected void writeZip64CentralDirectory() throws IOException {
        if (this.zip64Mode == Zip64Mode.Never) {
            return;
        }
        if (!this.hasUsedZip64 && this.shouldUseZip64EOCD()) {
            this.hasUsedZip64 = true;
        }
        if (!this.hasUsedZip64) {
            return;
        }
        long offset = this.streamCompressor.getTotalBytesWritten();
        long diskNumberStart = 0L;
        if (this.isSplitZip) {
            ZipSplitOutputStream zipSplitOutputStream = (ZipSplitOutputStream)this.out;
            offset = zipSplitOutputStream.getCurrentSplitSegmentBytesWritten();
            diskNumberStart = zipSplitOutputStream.getCurrentSplitSegmentIndex();
        }
        this.writeOut(ZIP64_EOCD_SIG);
        this.writeOut(ZipEightByteInteger.getBytes(44L));
        this.writeOut(ZipShort.getBytes(45));
        this.writeOut(ZipShort.getBytes(45));
        int numberOfThisDisk = 0;
        if (this.isSplitZip) {
            numberOfThisDisk = ((ZipSplitOutputStream)this.out).getCurrentSplitSegmentIndex();
        }
        this.writeOut(ZipLong.getBytes(numberOfThisDisk));
        this.writeOut(ZipLong.getBytes(this.cdDiskNumberStart));
        int numOfEntriesOnThisDisk = this.isSplitZip ? this.numberOfCDInDiskData.getOrDefault(numberOfThisDisk, 0).intValue() : this.entries.size();
        byte[] numOfEntriesOnThisDiskData = ZipEightByteInteger.getBytes(numOfEntriesOnThisDisk);
        this.writeOut(numOfEntriesOnThisDiskData);
        byte[] num = ZipEightByteInteger.getBytes(this.entries.size());
        this.writeOut(num);
        this.writeOut(ZipEightByteInteger.getBytes(this.cdLength));
        this.writeOut(ZipEightByteInteger.getBytes(this.cdOffset));
        if (this.isSplitZip) {
            int zip64EOCDLOCLength = 20;
            long unsplittableContentSize = 20L + this.eocdLength;
            ((ZipSplitOutputStream)this.out).prepareToWriteUnsplittableContent(unsplittableContentSize);
        }
        this.writeOut(ZIP64_EOCD_LOC_SIG);
        this.writeOut(ZipLong.getBytes(diskNumberStart));
        this.writeOut(ZipEightByteInteger.getBytes(offset));
        if (this.isSplitZip) {
            int totalNumberOfDisks = ((ZipSplitOutputStream)this.out).getCurrentSplitSegmentIndex() + 1;
            this.writeOut(ZipLong.getBytes(totalNumberOfDisks));
        } else {
            this.writeOut(ONE);
        }
    }

    public static final class UnicodeExtraFieldPolicy {
        public static final UnicodeExtraFieldPolicy ALWAYS = new UnicodeExtraFieldPolicy("always");
        public static final UnicodeExtraFieldPolicy NEVER = new UnicodeExtraFieldPolicy("never");
        public static final UnicodeExtraFieldPolicy NOT_ENCODEABLE = new UnicodeExtraFieldPolicy("not encodeable");
        private final String name;

        private UnicodeExtraFieldPolicy(String n) {
            this.name = n;
        }

        public String toString() {
            return this.name;
        }
    }

    private static final class CurrentEntry {
        private final ZipArchiveEntry entry;
        private long localDataStart;
        private long dataStart;
        private long bytesRead;
        private boolean causedUseOfZip64;
        private boolean hasWritten;

        private CurrentEntry(ZipArchiveEntry entry) {
            this.entry = entry;
        }
    }

    private static final class EntryMetaData {
        private final long offset;
        private final boolean usesDataDescriptor;

        private EntryMetaData(long offset, boolean usesDataDescriptor) {
            this.offset = offset;
            this.usesDataDescriptor = usesDataDescriptor;
        }
    }
}

