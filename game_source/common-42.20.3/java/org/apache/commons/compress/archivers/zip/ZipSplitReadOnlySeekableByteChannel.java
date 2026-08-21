/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.compress.archivers.zip.ZipLong;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.compress.utils.MultiReadOnlySeekableByteChannel;

public class ZipSplitReadOnlySeekableByteChannel
extends MultiReadOnlySeekableByteChannel {
    private static final Path[] EMPTY_PATH_ARRAY = new Path[0];
    private static final int ZIP_SPLIT_SIGNATURE_LENGTH = 4;
    private final ByteBuffer zipSplitSignatureByteBuffer = ByteBuffer.allocate(4);

    public static SeekableByteChannel buildFromLastSplitSegment(File lastSegmentFile) throws IOException {
        return ZipSplitReadOnlySeekableByteChannel.buildFromLastSplitSegment(lastSegmentFile.toPath());
    }

    public static SeekableByteChannel buildFromLastSplitSegment(Path lastSegmentPath) throws IOException {
        ArrayList splitZipSegments;
        String extension = FileNameUtils.getExtension(lastSegmentPath);
        if (!extension.equalsIgnoreCase("zip")) {
            throw new IllegalArgumentException("The extension of last ZIP split segment should be .zip");
        }
        Path parent = Objects.nonNull(lastSegmentPath.getParent()) ? lastSegmentPath.getParent() : lastSegmentPath.getFileSystem().getPath(".", new String[0]);
        String fileBaseName = FileNameUtils.getBaseName(lastSegmentPath);
        Pattern pattern = Pattern.compile(Pattern.quote(fileBaseName) + ".[zZ][0-9]+");
        try (Stream<Path> walk = Files.walk(parent, 1, new FileVisitOption[0]);){
            splitZipSegments = walk.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).filter(path -> pattern.matcher(path.getFileName().toString()).matches()).sorted(new ZipSplitSegmentComparator()).collect(Collectors.toCollection(ArrayList::new));
        }
        return ZipSplitReadOnlySeekableByteChannel.forPaths(lastSegmentPath, splitZipSegments);
    }

    public static SeekableByteChannel forFiles(File ... files) throws IOException {
        ArrayList<Path> paths = new ArrayList<Path>();
        for (File f : Objects.requireNonNull(files, "files")) {
            paths.add(f.toPath());
        }
        return ZipSplitReadOnlySeekableByteChannel.forPaths(paths.toArray(EMPTY_PATH_ARRAY));
    }

    public static SeekableByteChannel forFiles(File lastSegmentFile, Iterable<File> files) throws IOException {
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(lastSegmentFile, "lastSegmentFile");
        ArrayList<Path> filesList = new ArrayList<Path>();
        files.forEach(f -> filesList.add(f.toPath()));
        return ZipSplitReadOnlySeekableByteChannel.forPaths(lastSegmentFile.toPath(), filesList);
    }

    public static SeekableByteChannel forOrderedSeekableByteChannels(SeekableByteChannel ... channels) throws IOException {
        if (Objects.requireNonNull(channels, "channels").length == 1) {
            return channels[0];
        }
        return new ZipSplitReadOnlySeekableByteChannel(Arrays.asList(channels));
    }

    public static SeekableByteChannel forOrderedSeekableByteChannels(SeekableByteChannel lastSegmentChannel, Iterable<SeekableByteChannel> channels) throws IOException {
        Objects.requireNonNull(channels, "channels");
        Objects.requireNonNull(lastSegmentChannel, "lastSegmentChannel");
        ArrayList<SeekableByteChannel> channelsList = new ArrayList<SeekableByteChannel>();
        channels.forEach(channelsList::add);
        channelsList.add(lastSegmentChannel);
        return ZipSplitReadOnlySeekableByteChannel.forOrderedSeekableByteChannels(channelsList.toArray(new SeekableByteChannel[0]));
    }

    public static SeekableByteChannel forPaths(List<Path> paths, OpenOption[] openOptions) throws IOException {
        ArrayList<SeekableByteChannel> channels = new ArrayList<SeekableByteChannel>();
        for (Path path : Objects.requireNonNull(paths, "paths")) {
            channels.add(Files.newByteChannel(path, openOptions));
        }
        if (channels.size() == 1) {
            return (SeekableByteChannel)channels.get(0);
        }
        return new ZipSplitReadOnlySeekableByteChannel(channels);
    }

    public static SeekableByteChannel forPaths(Path ... paths) throws IOException {
        return ZipSplitReadOnlySeekableByteChannel.forPaths(Arrays.asList(paths), new OpenOption[]{StandardOpenOption.READ});
    }

    public static SeekableByteChannel forPaths(Path lastSegmentPath, Iterable<Path> paths) throws IOException {
        Objects.requireNonNull(paths, "paths");
        Objects.requireNonNull(lastSegmentPath, "lastSegmentPath");
        ArrayList<Path> filesList = new ArrayList<Path>();
        paths.forEach(filesList::add);
        filesList.add(lastSegmentPath);
        return ZipSplitReadOnlySeekableByteChannel.forPaths(filesList.toArray(EMPTY_PATH_ARRAY));
    }

    public ZipSplitReadOnlySeekableByteChannel(List<SeekableByteChannel> channels) throws IOException {
        super(channels);
        this.assertSplitSignature(channels);
    }

    private void assertSplitSignature(List<SeekableByteChannel> channels) throws IOException {
        SeekableByteChannel channel = channels.get(0);
        channel.position(0L);
        this.zipSplitSignatureByteBuffer.rewind();
        channel.read(this.zipSplitSignatureByteBuffer);
        ZipLong signature = new ZipLong(this.zipSplitSignatureByteBuffer.array());
        if (!signature.equals(ZipLong.DD_SIG)) {
            channel.position(0L);
            throw new IOException("The first ZIP split segment does not begin with split ZIP file signature");
        }
        channel.position(0L);
    }

    private static final class ZipSplitSegmentComparator
    implements Comparator<Path>,
    Serializable {
        private static final long serialVersionUID = 20200123L;

        private ZipSplitSegmentComparator() {
        }

        @Override
        public int compare(Path file1, Path file2) {
            String extension1 = FileNameUtils.getExtension(file1);
            String extension2 = FileNameUtils.getExtension(file2);
            if (!extension1.startsWith("z")) {
                return -1;
            }
            if (!extension2.startsWith("z")) {
                return 1;
            }
            Integer splitSegmentNumber1 = Integer.parseInt(extension1.substring(1));
            Integer splitSegmentNumber2 = Integer.parseInt(extension2.substring(1));
            return splitSegmentNumber1.compareTo(splitSegmentNumber2);
        }
    }
}

