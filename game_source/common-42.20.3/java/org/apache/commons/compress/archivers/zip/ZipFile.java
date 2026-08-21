/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.Inflater;
import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.zip.ExplodingInputStream;
import org.apache.commons.compress.archivers.zip.GeneralPurposeBit;
import org.apache.commons.compress.archivers.zip.InflaterInputStreamWithStatistics;
import org.apache.commons.compress.archivers.zip.UnshrinkingInputStream;
import org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException;
import org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipEightByteInteger;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipLong;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.commons.compress.archivers.zip.ZipShort;
import org.apache.commons.compress.archivers.zip.ZipSplitReadOnlySeekableByteChannel;
import org.apache.commons.compress.archivers.zip.ZipUtil;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
import org.apache.commons.compress.utils.BoundedArchiveInputStream;
import org.apache.commons.compress.utils.BoundedSeekableByteChannelInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.build.AbstractOrigin;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.input.BoundedInputStream;

public class ZipFile
implements Closeable {
    private static final String DEFAULT_CHARSET_NAME = StandardCharsets.UTF_8.name();
    private static final EnumSet<StandardOpenOption> READ = EnumSet.of(StandardOpenOption.READ);
    private static final int HASH_SIZE = 509;
    static final int NIBLET_MASK = 15;
    static final int BYTE_SHIFT = 8;
    private static final int POS_0 = 0;
    private static final int POS_1 = 1;
    private static final int POS_2 = 2;
    private static final int POS_3 = 3;
    private static final byte[] ONE_ZERO_BYTE = new byte[1];
    private static final int CFH_LEN = 42;
    private static final long CFH_SIG = ZipLong.getValue(ZipArchiveOutputStream.CFH_SIG);
    static final int MIN_EOCD_SIZE = 22;
    private static final int MAX_EOCD_SIZE = 65557;
    private static final int CFD_LENGTH_OFFSET = 12;
    private static final int CFD_DISK_OFFSET = 6;
    private static final int CFD_LOCATOR_RELATIVE_OFFSET = 8;
    private static final int ZIP64_EOCDL_LENGTH = 20;
    private static final int ZIP64_EOCDL_LOCATOR_OFFSET = 8;
    private static final int ZIP64_EOCD_CFD_LOCATOR_OFFSET = 48;
    private static final int ZIP64_EOCD_CFD_DISK_OFFSET = 20;
    private static final int ZIP64_EOCD_CFD_LOCATOR_RELATIVE_OFFSET = 24;
    private static final long LFH_OFFSET_FOR_FILENAME_LENGTH = 26L;
    private static final Comparator<ZipArchiveEntry> offsetComparator = Comparator.comparingLong(ZipArchiveEntry::getDiskNumberStart).thenComparingLong(ZipArchiveEntry::getLocalHeaderOffset);
    private final List<ZipArchiveEntry> entries = new LinkedList<ZipArchiveEntry>();
    private final Map<String, LinkedList<ZipArchiveEntry>> nameMap = new HashMap<String, LinkedList<ZipArchiveEntry>>(509);
    private final Charset encoding;
    private final ZipEncoding zipEncoding;
    private final SeekableByteChannel archive;
    private final boolean useUnicodeExtraFields;
    private volatile boolean closed = true;
    private final boolean isSplitZipArchive;
    private final byte[] dwordBuf = new byte[8];
    private final byte[] wordBuf = new byte[4];
    private final byte[] cfhBuf = new byte[42];
    private final byte[] shortBuf = new byte[2];
    private final ByteBuffer dwordBbuf = ByteBuffer.wrap(this.dwordBuf);
    private final ByteBuffer wordBbuf = ByteBuffer.wrap(this.wordBuf);
    private final ByteBuffer cfhBbuf = ByteBuffer.wrap(this.cfhBuf);
    private final ByteBuffer shortBbuf = ByteBuffer.wrap(this.shortBuf);
    private long centralDirectoryStartDiskNumber;
    private long centralDirectoryStartRelativeOffset;
    private long centralDirectoryStartOffset;
    private long firstLocalFileHeaderOffset;

    public static Builder builder() {
        return new Builder();
    }

    public static void closeQuietly(ZipFile zipFile) {
        org.apache.commons.io.IOUtils.closeQuietly((Closeable)zipFile);
    }

    private static SeekableByteChannel newReadByteChannel(Path path) throws IOException {
        return Files.newByteChannel(path, READ, new FileAttribute[0]);
    }

    private static SeekableByteChannel openZipChannel(Path path, long maxNumberOfDisks, OpenOption[] openOptions) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        ArrayList channels = new ArrayList();
        try {
            long numberOfDisks;
            ByteBuffer buf;
            boolean is64 = ZipFile.positionAtEndOfCentralDirectoryRecord(channel);
            if (is64) {
                channel.position(channel.position() + 4L + 4L + 8L);
                buf = ByteBuffer.allocate(4);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                IOUtils.readFully(channel, buf);
                buf.flip();
                numberOfDisks = (long)buf.getInt() & 0xFFFFFFFFL;
            } else {
                channel.position(channel.position() + 4L);
                buf = ByteBuffer.allocate(2);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                IOUtils.readFully(channel, buf);
                buf.flip();
                numberOfDisks = (buf.getShort() & 0xFFFF) + 1;
            }
            if (numberOfDisks > Math.min(maxNumberOfDisks, Integer.MAX_VALUE)) {
                throw new IOException("Too many disks for zip archive, max=" + Math.min(maxNumberOfDisks, Integer.MAX_VALUE) + " actual=" + numberOfDisks);
            }
            if (numberOfDisks <= 1L) {
                return channel;
            }
            channel.close();
            Path parent = path.getParent();
            String basename = FilenameUtils.removeExtension(Objects.toString(path.getFileName(), null));
            return ZipSplitReadOnlySeekableByteChannel.forPaths(IntStream.range(0, (int)numberOfDisks).mapToObj(i -> {
                if ((long)i == numberOfDisks - 1L) {
                    return path;
                }
                Path lowercase = parent.resolve(String.format("%s.z%02d", basename, i + 1));
                if (Files.exists(lowercase, new LinkOption[0])) {
                    return lowercase;
                }
                Path uppercase = parent.resolve(String.format("%s.Z%02d", basename, i + 1));
                if (Files.exists(uppercase, new LinkOption[0])) {
                    return uppercase;
                }
                return lowercase;
            }).collect(Collectors.toList()), openOptions);
        }
        catch (Throwable ex) {
            org.apache.commons.io.IOUtils.closeQuietly((Closeable)channel);
            channels.forEach(org.apache.commons.io.IOUtils::closeQuietly);
            throw ex;
        }
    }

    private static boolean positionAtEndOfCentralDirectoryRecord(SeekableByteChannel channel) throws IOException {
        boolean found = ZipFile.tryToLocateSignature(channel, 22L, 65557L, ZipArchiveOutputStream.EOCD_SIG);
        if (!found) {
            throw new ZipException("Archive is not a ZIP archive");
        }
        boolean found64 = false;
        long position = channel.position();
        if (position > 20L) {
            ByteBuffer wordBuf = ByteBuffer.allocate(4);
            channel.position(channel.position() - 20L);
            wordBuf.rewind();
            IOUtils.readFully(channel, wordBuf);
            wordBuf.flip();
            found64 = wordBuf.equals(ByteBuffer.wrap(ZipArchiveOutputStream.ZIP64_EOCD_LOC_SIG));
            if (!found64) {
                channel.position(position);
            } else {
                channel.position(channel.position() - 4L);
            }
        }
        return found64;
    }

    private static boolean tryToLocateSignature(SeekableByteChannel channel, long minDistanceFromEnd, long maxDistanceFromEnd, byte[] sig) throws IOException {
        long off;
        ByteBuffer wordBuf = ByteBuffer.allocate(4);
        boolean found = false;
        long stopSearching = Math.max(0L, channel.size() - maxDistanceFromEnd);
        if (off >= 0L) {
            for (off = channel.size() - minDistanceFromEnd; off >= stopSearching; --off) {
                channel.position(off);
                try {
                    wordBuf.rewind();
                    IOUtils.readFully(channel, wordBuf);
                    wordBuf.flip();
                }
                catch (EOFException ex) {
                    break;
                }
                byte curr = wordBuf.get();
                if (curr != sig[0] || (curr = wordBuf.get()) != sig[1] || (curr = wordBuf.get()) != sig[2] || (curr = wordBuf.get()) != sig[3]) continue;
                found = true;
                break;
            }
        }
        if (found) {
            channel.position(off);
        }
        return found;
    }

    @Deprecated
    public ZipFile(File file) throws IOException {
        this(file, DEFAULT_CHARSET_NAME);
    }

    @Deprecated
    public ZipFile(File file, String encoding) throws IOException {
        this(file.toPath(), encoding, true);
    }

    @Deprecated
    public ZipFile(File file, String encoding, boolean useUnicodeExtraFields) throws IOException {
        this(file.toPath(), encoding, useUnicodeExtraFields, false);
    }

    @Deprecated
    public ZipFile(File file, String encoding, boolean useUnicodeExtraFields, boolean ignoreLocalFileHeader) throws IOException {
        this(ZipFile.newReadByteChannel(file.toPath()), file.getAbsolutePath(), encoding, useUnicodeExtraFields, true, ignoreLocalFileHeader);
    }

    @Deprecated
    public ZipFile(Path path) throws IOException {
        this(path, DEFAULT_CHARSET_NAME);
    }

    @Deprecated
    public ZipFile(Path path, String encoding) throws IOException {
        this(path, encoding, true);
    }

    @Deprecated
    public ZipFile(Path path, String encoding, boolean useUnicodeExtraFields) throws IOException {
        this(path, encoding, useUnicodeExtraFields, false);
    }

    @Deprecated
    public ZipFile(Path path, String encoding, boolean useUnicodeExtraFields, boolean ignoreLocalFileHeader) throws IOException {
        this(ZipFile.newReadByteChannel(path), path.toAbsolutePath().toString(), encoding, useUnicodeExtraFields, true, ignoreLocalFileHeader);
    }

    @Deprecated
    public ZipFile(SeekableByteChannel channel) throws IOException {
        this(channel, "a SeekableByteChannel", DEFAULT_CHARSET_NAME, true);
    }

    @Deprecated
    public ZipFile(SeekableByteChannel channel, String encoding) throws IOException {
        this(channel, "a SeekableByteChannel", encoding, true);
    }

    private ZipFile(SeekableByteChannel channel, String channelDescription, Charset encoding, boolean useUnicodeExtraFields, boolean closeOnError, boolean ignoreLocalFileHeader) throws IOException {
        this.isSplitZipArchive = channel instanceof ZipSplitReadOnlySeekableByteChannel;
        this.encoding = Charsets.toCharset(encoding, Builder.DEFAULT_CHARSET);
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        this.archive = channel;
        boolean success = false;
        try {
            Map<ZipArchiveEntry, NameAndComment> entriesWithoutUTF8Flag = this.populateFromCentralDirectory();
            if (!ignoreLocalFileHeader) {
                this.resolveLocalFileHeaderData(entriesWithoutUTF8Flag);
            }
            this.fillNameMap();
            success = true;
            boolean bl = this.closed = !success;
        }
        catch (IOException e) {
            try {
                throw new IOException("Error reading Zip content from " + channelDescription, e);
            }
            catch (Throwable throwable) {
                boolean bl = this.closed = !success;
                if (!success && closeOnError) {
                    org.apache.commons.io.IOUtils.closeQuietly((Closeable)this.archive);
                }
                throw throwable;
            }
        }
        if (!success && closeOnError) {
            org.apache.commons.io.IOUtils.closeQuietly((Closeable)this.archive);
        }
    }

    @Deprecated
    public ZipFile(SeekableByteChannel channel, String channelDescription, String encoding, boolean useUnicodeExtraFields) throws IOException {
        this(channel, channelDescription, encoding, useUnicodeExtraFields, false, false);
    }

    @Deprecated
    public ZipFile(SeekableByteChannel channel, String channelDescription, String encoding, boolean useUnicodeExtraFields, boolean ignoreLocalFileHeader) throws IOException {
        this(channel, channelDescription, encoding, useUnicodeExtraFields, false, ignoreLocalFileHeader);
    }

    private ZipFile(SeekableByteChannel channel, String channelDescription, String encoding, boolean useUnicodeExtraFields, boolean closeOnError, boolean ignoreLocalFileHeader) throws IOException {
        this(channel, channelDescription, Charsets.toCharset(encoding), useUnicodeExtraFields, closeOnError, ignoreLocalFileHeader);
    }

    @Deprecated
    public ZipFile(String name) throws IOException {
        this(new File(name).toPath(), DEFAULT_CHARSET_NAME);
    }

    @Deprecated
    public ZipFile(String name, String encoding) throws IOException {
        this(new File(name).toPath(), encoding, true);
    }

    public boolean canReadEntryData(ZipArchiveEntry entry) {
        return ZipUtil.canHandleEntryData(entry);
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        this.archive.close();
    }

    public void copyRawEntries(ZipArchiveOutputStream target, ZipArchiveEntryPredicate predicate) throws IOException {
        Enumeration<ZipArchiveEntry> src = this.getEntriesInPhysicalOrder();
        while (src.hasMoreElements()) {
            ZipArchiveEntry entry = src.nextElement();
            if (!predicate.test(entry)) continue;
            target.addRawArchiveEntry(entry, this.getRawInputStream(entry));
        }
    }

    private BoundedArchiveInputStream createBoundedInputStream(long start, long remaining) {
        if (start < 0L || remaining < 0L || start + remaining < start) {
            throw new IllegalArgumentException("Corrupted archive, stream boundaries are out of range");
        }
        return this.archive instanceof FileChannel ? new BoundedFileChannelInputStream(start, remaining, (FileChannel)this.archive) : new BoundedSeekableByteChannelInputStream(start, remaining, this.archive);
    }

    private void fillNameMap() {
        this.entries.forEach(ze -> {
            String name = ze.getName();
            LinkedList entriesOfThatName = this.nameMap.computeIfAbsent(name, k -> new LinkedList());
            entriesOfThatName.addLast(ze);
        });
    }

    protected void finalize() throws Throwable {
        try {
            if (!this.closed) {
                this.close();
            }
        }
        finally {
            super.finalize();
        }
    }

    public InputStream getContentBeforeFirstLocalFileHeader() {
        return this.firstLocalFileHeaderOffset == 0L ? null : this.createBoundedInputStream(0L, this.firstLocalFileHeaderOffset);
    }

    private long getDataOffset(ZipArchiveEntry ze) throws IOException {
        long s = ze.getDataOffset();
        if (s == -1L) {
            this.setDataOffset(ze);
            return ze.getDataOffset();
        }
        return s;
    }

    public String getEncoding() {
        return this.encoding.name();
    }

    public Enumeration<ZipArchiveEntry> getEntries() {
        return Collections.enumeration(this.entries);
    }

    public Iterable<ZipArchiveEntry> getEntries(String name) {
        return this.nameMap.getOrDefault(name, ZipArchiveEntry.EMPTY_LINKED_LIST);
    }

    public Enumeration<ZipArchiveEntry> getEntriesInPhysicalOrder() {
        ZipArchiveEntry[] allEntries = this.entries.toArray(ZipArchiveEntry.EMPTY_ARRAY);
        return Collections.enumeration(Arrays.asList(this.sortByOffset(allEntries)));
    }

    public Iterable<ZipArchiveEntry> getEntriesInPhysicalOrder(String name) {
        LinkedList<ZipArchiveEntry> linkedList = this.nameMap.getOrDefault(name, ZipArchiveEntry.EMPTY_LINKED_LIST);
        return Arrays.asList(this.sortByOffset(linkedList.toArray(ZipArchiveEntry.EMPTY_ARRAY)));
    }

    public ZipArchiveEntry getEntry(String name) {
        LinkedList<ZipArchiveEntry> entries = this.nameMap.get(name);
        return entries != null ? entries.getFirst() : null;
    }

    public long getFirstLocalFileHeaderOffset() {
        return this.firstLocalFileHeaderOffset;
    }

    public InputStream getInputStream(ZipArchiveEntry entry) throws IOException {
        if (!(entry instanceof Entry)) {
            return null;
        }
        ZipUtil.checkRequestedFeatures(entry);
        BufferedInputStream is = new BufferedInputStream(this.getRawInputStream(entry));
        switch (ZipMethod.getMethodByCode(entry.getMethod())) {
            case STORED: {
                return new StoredStatisticsStream(is);
            }
            case UNSHRINKING: {
                return new UnshrinkingInputStream(is);
            }
            case IMPLODING: {
                try {
                    return new ExplodingInputStream(entry.getGeneralPurposeBit().getSlidingDictionarySize(), entry.getGeneralPurposeBit().getNumberOfShannonFanoTrees(), is);
                }
                catch (IllegalArgumentException ex) {
                    throw new IOException("bad IMPLODE data", ex);
                }
            }
            case DEFLATED: {
                final Inflater inflater = new Inflater(true);
                return new InflaterInputStreamWithStatistics(new SequenceInputStream(is, new ByteArrayInputStream(ONE_ZERO_BYTE)), inflater){

                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        }
                        finally {
                            inflater.end();
                        }
                    }
                };
            }
            case BZIP2: {
                return new BZip2CompressorInputStream(is);
            }
            case ENHANCED_DEFLATED: {
                return new Deflate64CompressorInputStream(is);
            }
        }
        throw new UnsupportedZipFeatureException(ZipMethod.getMethodByCode(entry.getMethod()), entry);
    }

    public InputStream getRawInputStream(ZipArchiveEntry entry) throws IOException {
        if (!(entry instanceof Entry)) {
            return null;
        }
        long start = this.getDataOffset(entry);
        if (start == -1L) {
            return null;
        }
        return this.createBoundedInputStream(start, entry.getCompressedSize());
    }

    public String getUnixSymlink(ZipArchiveEntry entry) throws IOException {
        if (entry != null && entry.isUnixSymlink()) {
            try (InputStream in = this.getInputStream(entry);){
                String string = this.zipEncoding.decode(org.apache.commons.io.IOUtils.toByteArray(in));
                return string;
            }
        }
        return null;
    }

    private Map<ZipArchiveEntry, NameAndComment> populateFromCentralDirectory() throws IOException {
        HashMap<ZipArchiveEntry, NameAndComment> noUTF8Flag = new HashMap<ZipArchiveEntry, NameAndComment>();
        this.positionAtCentralDirectory();
        this.centralDirectoryStartOffset = this.archive.position();
        this.wordBbuf.rewind();
        IOUtils.readFully(this.archive, this.wordBbuf);
        long sig = ZipLong.getValue(this.wordBuf);
        if (sig != CFH_SIG && this.startsWithLocalFileHeader()) {
            throw new IOException("Central directory is empty, can't expand corrupt archive.");
        }
        while (sig == CFH_SIG) {
            this.readCentralDirectoryEntry(noUTF8Flag);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            sig = ZipLong.getValue(this.wordBuf);
        }
        return noUTF8Flag;
    }

    private void positionAtCentralDirectory() throws IOException {
        boolean is64 = ZipFile.positionAtEndOfCentralDirectoryRecord(this.archive);
        if (!is64) {
            this.positionAtCentralDirectory32();
        } else {
            this.positionAtCentralDirectory64();
        }
    }

    private void positionAtCentralDirectory32() throws IOException {
        long endOfCentralDirectoryRecordOffset = this.archive.position();
        if (this.isSplitZipArchive) {
            this.skipBytes(6);
            this.shortBbuf.rewind();
            IOUtils.readFully(this.archive, this.shortBbuf);
            this.centralDirectoryStartDiskNumber = ZipShort.getValue(this.shortBuf);
            this.skipBytes(8);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            this.centralDirectoryStartRelativeOffset = ZipLong.getValue(this.wordBuf);
            ((ZipSplitReadOnlySeekableByteChannel)this.archive).position(this.centralDirectoryStartDiskNumber, this.centralDirectoryStartRelativeOffset);
        } else {
            this.skipBytes(12);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            long centralDirectoryLength = ZipLong.getValue(this.wordBuf);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            this.centralDirectoryStartDiskNumber = 0L;
            this.centralDirectoryStartRelativeOffset = ZipLong.getValue(this.wordBuf);
            this.firstLocalFileHeaderOffset = Long.max(endOfCentralDirectoryRecordOffset - centralDirectoryLength - this.centralDirectoryStartRelativeOffset, 0L);
            this.archive.position(this.centralDirectoryStartRelativeOffset + this.firstLocalFileHeaderOffset);
        }
    }

    private void positionAtCentralDirectory64() throws IOException {
        this.skipBytes(4);
        if (this.isSplitZipArchive) {
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            long diskNumberOfEOCD = ZipLong.getValue(this.wordBuf);
            this.dwordBbuf.rewind();
            IOUtils.readFully(this.archive, this.dwordBbuf);
            long relativeOffsetOfEOCD = ZipEightByteInteger.getLongValue(this.dwordBuf);
            ((ZipSplitReadOnlySeekableByteChannel)this.archive).position(diskNumberOfEOCD, relativeOffsetOfEOCD);
        } else {
            this.skipBytes(4);
            this.dwordBbuf.rewind();
            IOUtils.readFully(this.archive, this.dwordBbuf);
            this.archive.position(ZipEightByteInteger.getLongValue(this.dwordBuf));
        }
        this.wordBbuf.rewind();
        IOUtils.readFully(this.archive, this.wordBbuf);
        if (!Arrays.equals(this.wordBuf, ZipArchiveOutputStream.ZIP64_EOCD_SIG)) {
            throw new ZipException("Archive's ZIP64 end of central directory locator is corrupt.");
        }
        if (this.isSplitZipArchive) {
            this.skipBytes(16);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            this.centralDirectoryStartDiskNumber = ZipLong.getValue(this.wordBuf);
            this.skipBytes(24);
            this.dwordBbuf.rewind();
            IOUtils.readFully(this.archive, this.dwordBbuf);
            this.centralDirectoryStartRelativeOffset = ZipEightByteInteger.getLongValue(this.dwordBuf);
            ((ZipSplitReadOnlySeekableByteChannel)this.archive).position(this.centralDirectoryStartDiskNumber, this.centralDirectoryStartRelativeOffset);
        } else {
            this.skipBytes(44);
            this.dwordBbuf.rewind();
            IOUtils.readFully(this.archive, this.dwordBbuf);
            this.centralDirectoryStartDiskNumber = 0L;
            this.centralDirectoryStartRelativeOffset = ZipEightByteInteger.getLongValue(this.dwordBuf);
            this.archive.position(this.centralDirectoryStartRelativeOffset);
        }
    }

    private void readCentralDirectoryEntry(Map<ZipArchiveEntry, NameAndComment> noUTF8Flag) throws IOException {
        ZipEncoding entryEncoding;
        this.cfhBbuf.rewind();
        IOUtils.readFully(this.archive, this.cfhBbuf);
        int off = 0;
        Entry ze = new Entry();
        int versionMadeBy = ZipShort.getValue(this.cfhBuf, off);
        ze.setVersionMadeBy(versionMadeBy);
        ze.setPlatform(versionMadeBy >> 8 & 0xF);
        ze.setVersionRequired(ZipShort.getValue(this.cfhBuf, off += 2));
        GeneralPurposeBit gpFlag = GeneralPurposeBit.parse(this.cfhBuf, off += 2);
        boolean hasUTF8Flag = gpFlag.usesUTF8ForNames();
        ZipEncoding zipEncoding = entryEncoding = hasUTF8Flag ? ZipEncodingHelper.ZIP_ENCODING_UTF_8 : this.zipEncoding;
        if (hasUTF8Flag) {
            ze.setNameSource(ZipArchiveEntry.NameSource.NAME_WITH_EFS_FLAG);
        }
        ze.setGeneralPurposeBit(gpFlag);
        ze.setRawFlag(ZipShort.getValue(this.cfhBuf, off));
        ze.setMethod(ZipShort.getValue(this.cfhBuf, off += 2));
        long time = ZipUtil.dosToJavaTime(ZipLong.getValue(this.cfhBuf, off += 2));
        ze.setTime(time);
        ze.setCrc(ZipLong.getValue(this.cfhBuf, off += 4));
        long size = ZipLong.getValue(this.cfhBuf, off += 4);
        if (size < 0L) {
            throw new IOException("broken archive, entry with negative compressed size");
        }
        ze.setCompressedSize(size);
        size = ZipLong.getValue(this.cfhBuf, off += 4);
        if (size < 0L) {
            throw new IOException("broken archive, entry with negative size");
        }
        ze.setSize(size);
        int fileNameLen = ZipShort.getValue(this.cfhBuf, off += 4);
        off += 2;
        if (fileNameLen < 0) {
            throw new IOException("broken archive, entry with negative fileNameLen");
        }
        int extraLen = ZipShort.getValue(this.cfhBuf, off);
        off += 2;
        if (extraLen < 0) {
            throw new IOException("broken archive, entry with negative extraLen");
        }
        int commentLen = ZipShort.getValue(this.cfhBuf, off);
        off += 2;
        if (commentLen < 0) {
            throw new IOException("broken archive, entry with negative commentLen");
        }
        ze.setDiskNumberStart(ZipShort.getValue(this.cfhBuf, off));
        ze.setInternalAttributes(ZipShort.getValue(this.cfhBuf, off += 2));
        ze.setExternalAttributes(ZipLong.getValue(this.cfhBuf, off += 2));
        off += 4;
        byte[] fileName = IOUtils.readRange(this.archive, fileNameLen);
        if (fileName.length < fileNameLen) {
            throw new EOFException();
        }
        ze.setName(entryEncoding.decode(fileName), fileName);
        ze.setLocalHeaderOffset(ZipLong.getValue(this.cfhBuf, off) + this.firstLocalFileHeaderOffset);
        this.entries.add(ze);
        byte[] cdExtraData = IOUtils.readRange(this.archive, extraLen);
        if (cdExtraData.length < extraLen) {
            throw new EOFException();
        }
        try {
            ze.setCentralDirectoryExtra(cdExtraData);
        }
        catch (RuntimeException e) {
            ZipException z = new ZipException("Invalid extra data in entry " + ze.getName());
            z.initCause(e);
            throw z;
        }
        this.setSizesAndOffsetFromZip64Extra(ze);
        this.sanityCheckLFHOffset(ze);
        byte[] comment = IOUtils.readRange(this.archive, commentLen);
        if (comment.length < commentLen) {
            throw new EOFException();
        }
        ze.setComment(entryEncoding.decode(comment));
        if (!hasUTF8Flag && this.useUnicodeExtraFields) {
            noUTF8Flag.put(ze, new NameAndComment(fileName, comment));
        }
        ze.setStreamContiguous(true);
    }

    private void resolveLocalFileHeaderData(Map<ZipArchiveEntry, NameAndComment> entriesWithoutUTF8Flag) throws IOException {
        for (ZipArchiveEntry zipArchiveEntry : this.entries) {
            Entry ze = (Entry)zipArchiveEntry;
            int[] lens = this.setDataOffset(ze);
            int fileNameLen = lens[0];
            int extraFieldLen = lens[1];
            this.skipBytes(fileNameLen);
            byte[] localExtraData = IOUtils.readRange(this.archive, extraFieldLen);
            if (localExtraData.length < extraFieldLen) {
                throw new EOFException();
            }
            try {
                ze.setExtra(localExtraData);
            }
            catch (RuntimeException e) {
                ZipException z = new ZipException("Invalid extra data in entry " + ze.getName());
                z.initCause(e);
                throw z;
            }
            if (!entriesWithoutUTF8Flag.containsKey(ze)) continue;
            NameAndComment nc = entriesWithoutUTF8Flag.get(ze);
            ZipUtil.setNameAndCommentFromExtraFields(ze, nc.name, nc.comment);
        }
    }

    private void sanityCheckLFHOffset(ZipArchiveEntry entry) throws IOException {
        if (entry.getDiskNumberStart() < 0L) {
            throw new IOException("broken archive, entry with negative disk number");
        }
        if (entry.getLocalHeaderOffset() < 0L) {
            throw new IOException("broken archive, entry with negative local file header offset");
        }
        if (this.isSplitZipArchive) {
            if (entry.getDiskNumberStart() > this.centralDirectoryStartDiskNumber) {
                throw new IOException("local file header for " + entry.getName() + " starts on a later disk than central directory");
            }
            if (entry.getDiskNumberStart() == this.centralDirectoryStartDiskNumber && entry.getLocalHeaderOffset() > this.centralDirectoryStartRelativeOffset) {
                throw new IOException("local file header for " + entry.getName() + " starts after central directory");
            }
        } else if (entry.getLocalHeaderOffset() > this.centralDirectoryStartOffset) {
            throw new IOException("local file header for " + entry.getName() + " starts after central directory");
        }
    }

    private int[] setDataOffset(ZipArchiveEntry entry) throws IOException {
        long offset = entry.getLocalHeaderOffset();
        if (this.isSplitZipArchive) {
            ((ZipSplitReadOnlySeekableByteChannel)this.archive).position(entry.getDiskNumberStart(), offset + 26L);
            offset = this.archive.position() - 26L;
        } else {
            this.archive.position(offset + 26L);
        }
        this.wordBbuf.rewind();
        IOUtils.readFully(this.archive, this.wordBbuf);
        this.wordBbuf.flip();
        this.wordBbuf.get(this.shortBuf);
        int fileNameLen = ZipShort.getValue(this.shortBuf);
        this.wordBbuf.get(this.shortBuf);
        int extraFieldLen = ZipShort.getValue(this.shortBuf);
        entry.setDataOffset(offset + 26L + 2L + 2L + (long)fileNameLen + (long)extraFieldLen);
        if (entry.getDataOffset() + entry.getCompressedSize() > this.centralDirectoryStartOffset) {
            throw new IOException("data for " + entry.getName() + " overlaps with central directory.");
        }
        return new int[]{fileNameLen, extraFieldLen};
    }

    private void setSizesAndOffsetFromZip64Extra(ZipArchiveEntry entry) throws IOException {
        ZipExtraField extra = entry.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
        if (extra != null && !(extra instanceof Zip64ExtendedInformationExtraField)) {
            throw new ZipException("archive contains unparseable zip64 extra field");
        }
        Zip64ExtendedInformationExtraField z64 = (Zip64ExtendedInformationExtraField)extra;
        if (z64 != null) {
            long size;
            boolean hasUncompressedSize = entry.getSize() == 0xFFFFFFFFL;
            boolean hasCompressedSize = entry.getCompressedSize() == 0xFFFFFFFFL;
            boolean hasRelativeHeaderOffset = entry.getLocalHeaderOffset() == 0xFFFFFFFFL;
            boolean hasDiskStart = entry.getDiskNumberStart() == 65535L;
            z64.reparseCentralDirectoryData(hasUncompressedSize, hasCompressedSize, hasRelativeHeaderOffset, hasDiskStart);
            if (hasUncompressedSize) {
                size = z64.getSize().getLongValue();
                if (size < 0L) {
                    throw new IOException("broken archive, entry with negative size");
                }
                entry.setSize(size);
            } else if (hasCompressedSize) {
                z64.setSize(new ZipEightByteInteger(entry.getSize()));
            }
            if (hasCompressedSize) {
                size = z64.getCompressedSize().getLongValue();
                if (size < 0L) {
                    throw new IOException("broken archive, entry with negative compressed size");
                }
                entry.setCompressedSize(size);
            } else if (hasUncompressedSize) {
                z64.setCompressedSize(new ZipEightByteInteger(entry.getCompressedSize()));
            }
            if (hasRelativeHeaderOffset) {
                entry.setLocalHeaderOffset(z64.getRelativeHeaderOffset().getLongValue());
            }
            if (hasDiskStart) {
                entry.setDiskNumberStart(z64.getDiskStartNumber().getValue());
            }
        }
    }

    private void skipBytes(int count) throws IOException {
        long currentPosition = this.archive.position();
        long newPosition = currentPosition + (long)count;
        if (newPosition > this.archive.size()) {
            throw new EOFException();
        }
        this.archive.position(newPosition);
    }

    private ZipArchiveEntry[] sortByOffset(ZipArchiveEntry[] allEntries) {
        Arrays.sort(allEntries, offsetComparator);
        return allEntries;
    }

    private boolean startsWithLocalFileHeader() throws IOException {
        this.archive.position(this.firstLocalFileHeaderOffset);
        this.wordBbuf.rewind();
        IOUtils.readFully(this.archive, this.wordBbuf);
        return Arrays.equals(this.wordBuf, ZipArchiveOutputStream.LFH_SIG);
    }

    public static class Builder
    extends AbstractStreamBuilder<ZipFile, Builder> {
        static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
        private SeekableByteChannel seekableByteChannel;
        private boolean useUnicodeExtraFields = true;
        private boolean ignoreLocalFileHeader;
        private long maxNumberOfDisks = 1L;

        public Builder() {
            this.setCharset(DEFAULT_CHARSET);
            this.setCharsetDefault(DEFAULT_CHARSET);
        }

        @Override
        public ZipFile get() throws IOException {
            String actualDescription;
            SeekableByteChannel actualChannel;
            if (this.seekableByteChannel != null) {
                actualChannel = this.seekableByteChannel;
                actualDescription = actualChannel.getClass().getSimpleName();
            } else if (this.checkOrigin() instanceof AbstractOrigin.ByteArrayOrigin) {
                actualChannel = new SeekableInMemoryByteChannel(this.checkOrigin().getByteArray());
                actualDescription = actualChannel.getClass().getSimpleName();
            } else {
                OpenOption[] openOptions = this.getOpenOptions();
                if (openOptions.length == 0) {
                    openOptions = new OpenOption[]{StandardOpenOption.READ};
                }
                Path path = this.getPath();
                actualChannel = ZipFile.openZipChannel(path, this.maxNumberOfDisks, openOptions);
                actualDescription = path.toString();
            }
            boolean closeOnError = this.seekableByteChannel != null;
            return new ZipFile(actualChannel, actualDescription, this.getCharset(), this.useUnicodeExtraFields, closeOnError, this.ignoreLocalFileHeader);
        }

        public Builder setIgnoreLocalFileHeader(boolean ignoreLocalFileHeader) {
            this.ignoreLocalFileHeader = ignoreLocalFileHeader;
            return this;
        }

        public Builder setMaxNumberOfDisks(long maxNumberOfDisks) {
            this.maxNumberOfDisks = maxNumberOfDisks;
            return this;
        }

        public Builder setSeekableByteChannel(SeekableByteChannel seekableByteChannel) {
            this.seekableByteChannel = seekableByteChannel;
            return this;
        }

        public Builder setUseUnicodeExtraFields(boolean useUnicodeExtraFields) {
            this.useUnicodeExtraFields = useUnicodeExtraFields;
            return this;
        }
    }

    private static class BoundedFileChannelInputStream
    extends BoundedArchiveInputStream {
        private final FileChannel archive;

        BoundedFileChannelInputStream(long start, long remaining, FileChannel archive) {
            super(start, remaining);
            this.archive = archive;
        }

        @Override
        protected int read(long pos, ByteBuffer buf) throws IOException {
            int read = this.archive.read(buf, pos);
            buf.flip();
            return read;
        }
    }

    private static final class Entry
    extends ZipArchiveEntry {
        private Entry() {
        }

        @Override
        public boolean equals(Object other) {
            if (super.equals(other)) {
                Entry otherEntry = (Entry)other;
                return this.getLocalHeaderOffset() == otherEntry.getLocalHeaderOffset() && super.getDataOffset() == otherEntry.getDataOffset() && super.getDiskNumberStart() == otherEntry.getDiskNumberStart();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 3 * super.hashCode() + (int)this.getLocalHeaderOffset() + (int)(this.getLocalHeaderOffset() >> 32);
        }
    }

    private static final class StoredStatisticsStream
    extends BoundedInputStream
    implements InputStreamStatistics {
        StoredStatisticsStream(InputStream in) {
            super(in);
        }

        @Override
        public long getCompressedCount() {
            return super.getCount();
        }

        @Override
        public long getUncompressedCount() {
            return this.getCompressedCount();
        }
    }

    private static final class NameAndComment {
        private final byte[] name;
        private final byte[] comment;

        private NameAndComment(byte[] name, byte[] comment) {
            this.name = name;
            this.comment = comment;
        }
    }
}

