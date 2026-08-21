/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.apache.commons.compress.archivers.zip.FileRandomAccessOutputStream;
import org.apache.commons.compress.archivers.zip.RandomAccessOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipIoUtil;
import org.apache.commons.compress.utils.FileNameUtils;

final class ZipSplitOutputStream
extends RandomAccessOutputStream {
    private static final long ZIP_SEGMENT_MIN_SIZE = 65536L;
    private static final long ZIP_SEGMENT_MAX_SIZE = 0xFFFFFFFFL;
    private FileChannel currentChannel;
    private FileRandomAccessOutputStream outputStream;
    private Path zipFile;
    private final long splitSize;
    private long totalPosition;
    private int currentSplitSegmentIndex;
    private long currentSplitSegmentBytesWritten;
    private boolean finished;
    private final byte[] singleByte = new byte[1];
    private final List<Long> diskToPosition = new ArrayList<Long>();
    private final TreeMap<Long, Path> positionToFiles = new TreeMap();

    ZipSplitOutputStream(File zipFile, long splitSize) throws IllegalArgumentException, IOException {
        this(zipFile.toPath(), splitSize);
    }

    ZipSplitOutputStream(Path zipFile, long splitSize) throws IllegalArgumentException, IOException {
        if (splitSize < 65536L || splitSize > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Zip split segment size should between 64K and 4,294,967,295");
        }
        this.zipFile = zipFile;
        this.splitSize = splitSize;
        this.outputStream = new FileRandomAccessOutputStream(zipFile);
        this.currentChannel = this.outputStream.channel();
        this.positionToFiles.put(0L, this.zipFile);
        this.diskToPosition.add(0L);
        this.writeZipSplitSignature();
    }

    public long calculateDiskPosition(long disk, long localOffset) throws IOException {
        if (disk >= Integer.MAX_VALUE) {
            throw new IOException("Disk number exceeded internal limits: limit=2147483647 requested=" + disk);
        }
        return this.diskToPosition.get((int)disk) + localOffset;
    }

    @Override
    public void close() throws IOException {
        if (!this.finished) {
            this.finish();
        }
    }

    private Path createNewSplitSegmentFile(Integer zipSplitSegmentSuffixIndex) throws IOException {
        Path newFile = this.getSplitSegmentFileName(zipSplitSegmentSuffixIndex);
        if (Files.exists(newFile, new LinkOption[0])) {
            throw new IOException("split ZIP segment " + newFile + " already exists");
        }
        return newFile;
    }

    private void finish() throws IOException {
        if (this.finished) {
            throw new IOException("This archive has already been finished");
        }
        String zipFileBaseName = FileNameUtils.getBaseName(this.zipFile);
        this.outputStream.close();
        Files.move(this.zipFile, this.zipFile.resolveSibling(zipFileBaseName + ".zip"), StandardCopyOption.ATOMIC_MOVE);
        this.finished = true;
    }

    public long getCurrentSplitSegmentBytesWritten() {
        return this.currentSplitSegmentBytesWritten;
    }

    public int getCurrentSplitSegmentIndex() {
        return this.currentSplitSegmentIndex;
    }

    private Path getSplitSegmentFileName(Integer zipSplitSegmentSuffixIndex) {
        int newZipSplitSegmentSuffixIndex = zipSplitSegmentSuffixIndex == null ? this.currentSplitSegmentIndex + 2 : zipSplitSegmentSuffixIndex;
        String baseName = FileNameUtils.getBaseName(this.zipFile);
        StringBuilder extension = new StringBuilder(".z");
        if (newZipSplitSegmentSuffixIndex <= 9) {
            extension.append("0").append(newZipSplitSegmentSuffixIndex);
        } else {
            extension.append(newZipSplitSegmentSuffixIndex);
        }
        Path parent = this.zipFile.getParent();
        String dir = Objects.nonNull(parent) ? parent.toAbsolutePath().toString() : ".";
        return this.zipFile.getFileSystem().getPath(dir, baseName + extension.toString());
    }

    private void openNewSplitSegment() throws IOException {
        Path newFile;
        if (this.currentSplitSegmentIndex == 0) {
            this.outputStream.close();
            newFile = this.createNewSplitSegmentFile(1);
            Files.move(this.zipFile, newFile, StandardCopyOption.ATOMIC_MOVE);
            this.positionToFiles.put(0L, newFile);
        }
        newFile = this.createNewSplitSegmentFile(null);
        this.outputStream.close();
        this.outputStream = new FileRandomAccessOutputStream(newFile);
        this.currentChannel = this.outputStream.channel();
        this.currentSplitSegmentBytesWritten = 0L;
        this.zipFile = newFile;
        ++this.currentSplitSegmentIndex;
        this.diskToPosition.add(this.totalPosition);
        this.positionToFiles.put(this.totalPosition, newFile);
    }

    @Override
    public long position() {
        return this.totalPosition;
    }

    public void prepareToWriteUnsplittableContent(long unsplittableContentSize) throws IllegalArgumentException, IOException {
        if (unsplittableContentSize > this.splitSize) {
            throw new IllegalArgumentException("The unsplittable content size is bigger than the split segment size");
        }
        long bytesRemainingInThisSegment = this.splitSize - this.currentSplitSegmentBytesWritten;
        if (bytesRemainingInThisSegment < unsplittableContentSize) {
            this.openNewSplitSegment();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len <= 0) {
            return;
        }
        if (this.currentSplitSegmentBytesWritten >= this.splitSize) {
            this.openNewSplitSegment();
            this.write(b, off, len);
        } else if (this.currentSplitSegmentBytesWritten + (long)len > this.splitSize) {
            int bytesToWriteForThisSegment = (int)this.splitSize - (int)this.currentSplitSegmentBytesWritten;
            this.write(b, off, bytesToWriteForThisSegment);
            this.openNewSplitSegment();
            this.write(b, off + bytesToWriteForThisSegment, len - bytesToWriteForThisSegment);
        } else {
            this.outputStream.write(b, off, len);
            this.currentSplitSegmentBytesWritten += (long)len;
            this.totalPosition += (long)len;
        }
    }

    @Override
    public void write(int i) throws IOException {
        this.singleByte[0] = (byte)(i & 0xFF);
        this.write(this.singleByte);
    }

    @Override
    public void writeFully(byte[] b, int off, int len, long atPosition) throws IOException {
        long remainingPosition = atPosition;
        int remainingOff = off;
        int remainingLen = len;
        while (remainingLen > 0) {
            Map.Entry<Long, Path> segment = this.positionToFiles.floorEntry(remainingPosition);
            Long segmentEnd = this.positionToFiles.higherKey(remainingPosition);
            if (segmentEnd == null) {
                ZipIoUtil.writeFullyAt(this.currentChannel, ByteBuffer.wrap(b, remainingOff, remainingLen), remainingPosition - segment.getKey());
                remainingPosition += (long)remainingLen;
                remainingOff += remainingLen;
                remainingLen = 0;
                continue;
            }
            if (remainingPosition + (long)remainingLen <= segmentEnd) {
                this.writeToSegment(segment.getValue(), remainingPosition - segment.getKey(), b, remainingOff, remainingLen);
                remainingPosition += (long)remainingLen;
                remainingOff += remainingLen;
                remainingLen = 0;
                continue;
            }
            int toWrite = Math.toIntExact(segmentEnd - remainingPosition);
            this.writeToSegment(segment.getValue(), remainingPosition - segment.getKey(), b, remainingOff, toWrite);
            remainingPosition += (long)toWrite;
            remainingOff += toWrite;
            remainingLen -= toWrite;
        }
    }

    private void writeToSegment(Path segment, long position, byte[] b, int off, int len) throws IOException {
        try (FileChannel channel = FileChannel.open(segment, StandardOpenOption.WRITE);){
            ZipIoUtil.writeFullyAt(channel, ByteBuffer.wrap(b, off, len), position);
        }
    }

    private void writeZipSplitSignature() throws IOException {
        this.outputStream.write(ZipArchiveOutputStream.DD_SIG);
        this.currentSplitSegmentBytesWritten += (long)ZipArchiveOutputStream.DD_SIG.length;
        this.totalPosition += (long)ZipArchiveOutputStream.DD_SIG.length;
    }
}

