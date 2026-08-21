/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.RandomAccessFileMode;
import org.apache.commons.io.RandomAccessFiles;
import org.apache.commons.io.ThreadUtils;
import org.apache.commons.io.file.AccumulatorPathVisitor;
import org.apache.commons.io.file.CleaningPathVisitor;
import org.apache.commons.io.file.CopyDirectoryVisitor;
import org.apache.commons.io.file.Counters;
import org.apache.commons.io.file.CountingPathVisitor;
import org.apache.commons.io.file.DeleteOption;
import org.apache.commons.io.file.DeletingPathVisitor;
import org.apache.commons.io.file.DirectoryStreamFilter;
import org.apache.commons.io.file.PathFilter;
import org.apache.commons.io.file.StandardDeleteOption;
import org.apache.commons.io.file.attribute.FileTimes;
import org.apache.commons.io.function.IOFunction;
import org.apache.commons.io.function.IOSupplier;

public final class PathUtils {
    private static final OpenOption[] OPEN_OPTIONS_TRUNCATE = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};
    private static final OpenOption[] OPEN_OPTIONS_APPEND = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};
    public static final CopyOption[] EMPTY_COPY_OPTIONS = new CopyOption[0];
    public static final DeleteOption[] EMPTY_DELETE_OPTION_ARRAY = new DeleteOption[0];
    public static final FileAttribute<?>[] EMPTY_FILE_ATTRIBUTE_ARRAY = new FileAttribute[0];
    public static final FileVisitOption[] EMPTY_FILE_VISIT_OPTION_ARRAY = new FileVisitOption[0];
    public static final LinkOption[] EMPTY_LINK_OPTION_ARRAY = new LinkOption[0];
    @Deprecated
    public static final LinkOption[] NOFOLLOW_LINK_OPTION_ARRAY = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
    static final LinkOption NULL_LINK_OPTION = null;
    public static final OpenOption[] EMPTY_OPEN_OPTION_ARRAY = new OpenOption[0];
    public static final Path[] EMPTY_PATH_ARRAY = new Path[0];

    private static AccumulatorPathVisitor accumulate(Path directory, int maxDepth, FileVisitOption[] fileVisitOptions) throws IOException {
        return PathUtils.visitFileTree(AccumulatorPathVisitor.withLongCounters(), directory, PathUtils.toFileVisitOptionSet(fileVisitOptions), maxDepth);
    }

    public static Counters.PathCounters cleanDirectory(Path directory) throws IOException {
        return PathUtils.cleanDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
    }

    public static Counters.PathCounters cleanDirectory(Path directory, DeleteOption ... deleteOptions) throws IOException {
        return PathUtils.visitFileTree(new CleaningPathVisitor(Counters.longPathCounters(), deleteOptions, new String[0]), directory).getPathCounters();
    }

    private static int compareLastModifiedTimeTo(Path file, FileTime fileTime, LinkOption ... options) throws IOException {
        return PathUtils.getLastModifiedTime(file, options).compareTo(fileTime);
    }

    public static long copy(IOSupplier<InputStream> in, Path target, CopyOption ... copyOptions) throws IOException {
        try (InputStream inputStream2 = in.get();){
            long l = Files.copy(inputStream2, target, copyOptions);
            return l;
        }
    }

    public static Counters.PathCounters copyDirectory(Path sourceDirectory, Path targetDirectory, CopyOption ... copyOptions) throws IOException {
        Path absoluteSource = sourceDirectory.toAbsolutePath();
        return PathUtils.visitFileTree(new CopyDirectoryVisitor(Counters.longPathCounters(), absoluteSource, targetDirectory, copyOptions), absoluteSource).getPathCounters();
    }

    public static Path copyFile(URL sourceFile, Path targetFile, CopyOption ... copyOptions) throws IOException {
        PathUtils.copy(sourceFile::openStream, targetFile, copyOptions);
        return targetFile;
    }

    public static Path copyFileToDirectory(Path sourceFile, Path targetDirectory, CopyOption ... copyOptions) throws IOException {
        return Files.copy(sourceFile, targetDirectory.resolve(sourceFile.getFileName()), copyOptions);
    }

    public static Path copyFileToDirectory(URL sourceFile, Path targetDirectory, CopyOption ... copyOptions) throws IOException {
        Path resolve = targetDirectory.resolve(FilenameUtils.getName(sourceFile.getFile()));
        PathUtils.copy(sourceFile::openStream, resolve, copyOptions);
        return resolve;
    }

    public static Counters.PathCounters countDirectory(Path directory) throws IOException {
        return PathUtils.visitFileTree(CountingPathVisitor.withLongCounters(), directory).getPathCounters();
    }

    public static Counters.PathCounters countDirectoryAsBigInteger(Path directory) throws IOException {
        return PathUtils.visitFileTree(CountingPathVisitor.withBigIntegerCounters(), directory).getPathCounters();
    }

    public static Path createParentDirectories(Path path, FileAttribute<?> ... attrs) throws IOException {
        return PathUtils.createParentDirectories(path, LinkOption.NOFOLLOW_LINKS, attrs);
    }

    public static Path createParentDirectories(Path path, LinkOption linkOption, FileAttribute<?> ... attrs) throws IOException {
        Path parent = PathUtils.getParent(path);
        Path path2 = parent = linkOption == LinkOption.NOFOLLOW_LINKS ? parent : PathUtils.readIfSymbolicLink(parent);
        if (parent == null) {
            return null;
        }
        boolean exists = linkOption == null ? Files.exists(parent, new LinkOption[0]) : Files.exists(parent, linkOption);
        return exists ? parent : Files.createDirectories(parent, attrs);
    }

    public static Path current() {
        return Paths.get(".", new String[0]);
    }

    public static Counters.PathCounters delete(Path path) throws IOException {
        return PathUtils.delete(path, EMPTY_DELETE_OPTION_ARRAY);
    }

    public static Counters.PathCounters delete(Path path, DeleteOption ... deleteOptions) throws IOException {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) ? PathUtils.deleteDirectory(path, deleteOptions) : PathUtils.deleteFile(path, deleteOptions);
    }

    public static Counters.PathCounters delete(Path path, LinkOption[] linkOptions, DeleteOption ... deleteOptions) throws IOException {
        return Files.isDirectory(path, linkOptions) ? PathUtils.deleteDirectory(path, linkOptions, deleteOptions) : PathUtils.deleteFile(path, linkOptions, deleteOptions);
    }

    public static Counters.PathCounters deleteDirectory(Path directory) throws IOException {
        return PathUtils.deleteDirectory(directory, EMPTY_DELETE_OPTION_ARRAY);
    }

    public static Counters.PathCounters deleteDirectory(Path directory, DeleteOption ... deleteOptions) throws IOException {
        LinkOption[] linkOptions = PathUtils.noFollowLinkOptionArray();
        return PathUtils.withPosixFileAttributes(PathUtils.getParent(directory), linkOptions, PathUtils.overrideReadOnly(deleteOptions), pfa -> PathUtils.visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions, new String[0]), directory).getPathCounters());
    }

    public static Counters.PathCounters deleteDirectory(Path directory, LinkOption[] linkOptions, DeleteOption ... deleteOptions) throws IOException {
        return PathUtils.visitFileTree(new DeletingPathVisitor(Counters.longPathCounters(), linkOptions, deleteOptions, new String[0]), directory).getPathCounters();
    }

    public static Counters.PathCounters deleteFile(Path file) throws IOException {
        return PathUtils.deleteFile(file, EMPTY_DELETE_OPTION_ARRAY);
    }

    public static Counters.PathCounters deleteFile(Path file, DeleteOption ... deleteOptions) throws IOException {
        return PathUtils.deleteFile(file, PathUtils.noFollowLinkOptionArray(), deleteOptions);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Counters.PathCounters deleteFile(Path file, LinkOption[] linkOptions, DeleteOption ... deleteOptions) throws NoSuchFileException, IOException {
        if (Files.isDirectory(file, linkOptions)) {
            throw new NoSuchFileException(file.toString());
        }
        Counters.PathCounters pathCounts = Counters.longPathCounters();
        boolean exists = PathUtils.exists(file, linkOptions);
        long size = exists && !Files.isSymbolicLink(file) ? Files.size(file) : 0L;
        try {
            if (Files.deleteIfExists(file)) {
                pathCounts.getFileCounter().increment();
                pathCounts.getByteCounter().add(size);
                return pathCounts;
            }
        }
        catch (AccessDeniedException accessDeniedException) {
            // empty catch block
        }
        Path parent = PathUtils.getParent(file);
        PosixFileAttributes posixFileAttributes = null;
        try {
            if (PathUtils.overrideReadOnly(deleteOptions)) {
                posixFileAttributes = PathUtils.readPosixFileAttributes(parent, linkOptions);
                PathUtils.setReadOnly(file, false, linkOptions);
            }
            long l = size = (exists = PathUtils.exists(file, linkOptions)) && !Files.isSymbolicLink(file) ? Files.size(file) : 0L;
            if (Files.deleteIfExists(file)) {
                pathCounts.getFileCounter().increment();
                pathCounts.getByteCounter().add(size);
            }
        }
        finally {
            if (posixFileAttributes != null) {
                Files.setPosixFilePermissions(parent, posixFileAttributes.permissions());
            }
        }
        return pathCounts;
    }

    public static void deleteOnExit(Path path) {
        Objects.requireNonNull(path.toFile()).deleteOnExit();
    }

    public static boolean directoryAndFileContentEquals(Path path1, Path path2) throws IOException {
        return PathUtils.directoryAndFileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
    }

    public static boolean directoryAndFileContentEquals(Path path1, Path path2, LinkOption[] linkOptions, OpenOption[] openOptions, FileVisitOption[] fileVisitOption) throws IOException {
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null || path2 == null) {
            return false;
        }
        if (PathUtils.notExists(path1, new LinkOption[0]) && PathUtils.notExists(path2, new LinkOption[0])) {
            return true;
        }
        RelativeSortedPaths relativeSortedPaths = new RelativeSortedPaths(path1, path2, Integer.MAX_VALUE, linkOptions, fileVisitOption);
        if (!relativeSortedPaths.equals) {
            return false;
        }
        List<Path> fileList1 = relativeSortedPaths.relativeFileList1;
        List<Path> fileList2 = relativeSortedPaths.relativeFileList2;
        for (Path path : fileList1) {
            int binarySearch = Collections.binarySearch(fileList2, path);
            if (binarySearch <= -1) {
                throw new IllegalStateException("Unexpected mismatch.");
            }
            if (PathUtils.fileContentEquals(path1.resolve(path), path2.resolve(path), linkOptions, openOptions)) continue;
            return false;
        }
        return true;
    }

    public static boolean directoryContentEquals(Path path1, Path path2) throws IOException {
        return PathUtils.directoryContentEquals(path1, path2, Integer.MAX_VALUE, EMPTY_LINK_OPTION_ARRAY, EMPTY_FILE_VISIT_OPTION_ARRAY);
    }

    public static boolean directoryContentEquals(Path path1, Path path2, int maxDepth, LinkOption[] linkOptions, FileVisitOption[] fileVisitOptions) throws IOException {
        return new RelativeSortedPaths((Path)path1, (Path)path2, (int)maxDepth, (LinkOption[])linkOptions, (FileVisitOption[])fileVisitOptions).equals;
    }

    private static boolean exists(Path path, LinkOption ... options) {
        Objects.requireNonNull(path, "path");
        return options != null ? Files.exists(path, options) : Files.exists(path, new LinkOption[0]);
    }

    public static boolean fileContentEquals(Path path1, Path path2) throws IOException {
        return PathUtils.fileContentEquals(path1, path2, EMPTY_LINK_OPTION_ARRAY, EMPTY_OPEN_OPTION_ARRAY);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static boolean fileContentEquals(Path path1, Path path2, LinkOption[] linkOptions, OpenOption[] openOptions) throws IOException {
        if (path1 == null && path2 == null) {
            return true;
        }
        if (path1 == null || path2 == null) {
            return false;
        }
        Path nPath1 = path1.normalize();
        Path nPath2 = path2.normalize();
        boolean path1Exists = PathUtils.exists(nPath1, linkOptions);
        if (path1Exists != PathUtils.exists(nPath2, linkOptions)) {
            return false;
        }
        if (!path1Exists) {
            return true;
        }
        if (Files.isDirectory(nPath1, linkOptions)) {
            throw new IOException("Can't compare directories, only files: " + nPath1);
        }
        if (Files.isDirectory(nPath2, linkOptions)) {
            throw new IOException("Can't compare directories, only files: " + nPath2);
        }
        if (Files.size(nPath1) != Files.size(nPath2)) {
            return false;
        }
        if (path1.equals(path2)) {
            return true;
        }
        try (RandomAccessFile raf1 = RandomAccessFileMode.READ_ONLY.create(path1.toRealPath(linkOptions));){
            boolean bl;
            block34: {
                RandomAccessFile raf2 = RandomAccessFileMode.READ_ONLY.create(path2.toRealPath(linkOptions));
                try {
                    bl = RandomAccessFiles.contentEquals(raf1, raf2);
                    if (raf2 == null) break block34;
                }
                catch (Throwable throwable) {
                    if (raf2 != null) {
                        try {
                            raf2.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                raf2.close();
            }
            return bl;
        }
        catch (UnsupportedOperationException e) {
            try (InputStream inputStream1 = Files.newInputStream(nPath1, openOptions);){
                boolean bl;
                block35: {
                    InputStream inputStream2 = Files.newInputStream(nPath2, openOptions);
                    try {
                        bl = IOUtils.contentEquals(inputStream1, inputStream2);
                        if (inputStream2 == null) break block35;
                    }
                    catch (Throwable throwable) {
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            }
                            catch (Throwable throwable3) {
                                throwable.addSuppressed(throwable3);
                            }
                        }
                        throw throwable;
                    }
                    inputStream2.close();
                }
                return bl;
            }
        }
    }

    public static Path[] filter(PathFilter filter, Path ... paths) {
        Objects.requireNonNull(filter, "filter");
        if (paths == null) {
            return EMPTY_PATH_ARRAY;
        }
        return PathUtils.filterPaths(filter, Stream.of(paths), Collectors.toList()).toArray(EMPTY_PATH_ARRAY);
    }

    private static <R, A> R filterPaths(PathFilter filter, Stream<Path> stream, Collector<? super Path, A, R> collector) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(collector, "collector");
        if (stream == null) {
            return Stream.empty().collect(collector);
        }
        return stream.filter(p -> {
            try {
                return p != null && filter.accept((Path)p, PathUtils.readBasicFileAttributes(p)) == FileVisitResult.CONTINUE;
            }
            catch (IOException e) {
                return false;
            }
        }).collect(collector);
    }

    public static List<AclEntry> getAclEntryList(Path sourcePath) throws IOException {
        AclFileAttributeView fileAttributeView = PathUtils.getAclFileAttributeView(sourcePath, new LinkOption[0]);
        return fileAttributeView == null ? null : fileAttributeView.getAcl();
    }

    public static AclFileAttributeView getAclFileAttributeView(Path path, LinkOption ... options) {
        return Files.getFileAttributeView(path, AclFileAttributeView.class, options);
    }

    public static String getBaseName(Path path) {
        if (path == null) {
            return null;
        }
        Path fileName = path.getFileName();
        return fileName != null ? FilenameUtils.removeExtension(fileName.toString()) : null;
    }

    public static DosFileAttributeView getDosFileAttributeView(Path path, LinkOption ... options) {
        return Files.getFileAttributeView(path, DosFileAttributeView.class, options);
    }

    public static String getExtension(Path path) {
        String fileName = PathUtils.getFileNameString(path);
        return fileName != null ? FilenameUtils.getExtension(fileName) : null;
    }

    public static <R> R getFileName(Path path, Function<Path, R> function) {
        Path fileName = path != null ? path.getFileName() : null;
        return fileName != null ? (R)function.apply(fileName) : null;
    }

    public static String getFileNameString(Path path) {
        return PathUtils.getFileName(path, Path::toString);
    }

    public static FileTime getLastModifiedFileTime(File file) throws IOException {
        return PathUtils.getLastModifiedFileTime(file.toPath(), null, EMPTY_LINK_OPTION_ARRAY);
    }

    public static FileTime getLastModifiedFileTime(Path path, FileTime defaultIfAbsent, LinkOption ... options) throws IOException {
        return Files.exists(path, new LinkOption[0]) ? PathUtils.getLastModifiedTime(path, options) : defaultIfAbsent;
    }

    public static FileTime getLastModifiedFileTime(Path path, LinkOption ... options) throws IOException {
        return PathUtils.getLastModifiedFileTime(path, null, options);
    }

    public static FileTime getLastModifiedFileTime(URI uri) throws IOException {
        return PathUtils.getLastModifiedFileTime(Paths.get(uri), null, EMPTY_LINK_OPTION_ARRAY);
    }

    public static FileTime getLastModifiedFileTime(URL url) throws IOException, URISyntaxException {
        return PathUtils.getLastModifiedFileTime(url.toURI());
    }

    private static FileTime getLastModifiedTime(Path path, LinkOption ... options) throws IOException {
        return Files.getLastModifiedTime(Objects.requireNonNull(path, "path"), options);
    }

    private static Path getParent(Path path) {
        return path == null ? null : path.getParent();
    }

    public static PosixFileAttributeView getPosixFileAttributeView(Path path, LinkOption ... options) {
        return Files.getFileAttributeView(path, PosixFileAttributeView.class, options);
    }

    public static Path getTempDirectory() {
        return Paths.get(FileUtils.getTempDirectoryPath(), new String[0]);
    }

    public static boolean isDirectory(Path path, LinkOption ... options) {
        return path != null && Files.isDirectory(path, options);
    }

    public static boolean isEmpty(Path path) throws IOException {
        return Files.isDirectory(path, new LinkOption[0]) ? PathUtils.isEmptyDirectory(path) : PathUtils.isEmptyFile(path);
    }

    public static boolean isEmptyDirectory(Path directory) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);){
            boolean bl = !directoryStream.iterator().hasNext();
            return bl;
        }
    }

    public static boolean isEmptyFile(Path file) throws IOException {
        return Files.size(file) <= 0L;
    }

    public static boolean isNewer(Path file, ChronoZonedDateTime<?> czdt, LinkOption ... options) throws IOException {
        Objects.requireNonNull(czdt, "czdt");
        return PathUtils.isNewer(file, czdt.toInstant(), options);
    }

    public static boolean isNewer(Path file, FileTime fileTime, LinkOption ... options) throws IOException {
        if (PathUtils.notExists(file, new LinkOption[0])) {
            return false;
        }
        return PathUtils.compareLastModifiedTimeTo(file, fileTime, options) > 0;
    }

    public static boolean isNewer(Path file, Instant instant, LinkOption ... options) throws IOException {
        return PathUtils.isNewer(file, FileTime.from(instant), options);
    }

    public static boolean isNewer(Path file, long timeMillis, LinkOption ... options) throws IOException {
        return PathUtils.isNewer(file, FileTime.fromMillis(timeMillis), options);
    }

    public static boolean isNewer(Path file, Path reference) throws IOException {
        return PathUtils.isNewer(file, PathUtils.getLastModifiedTime(reference, new LinkOption[0]), new LinkOption[0]);
    }

    public static boolean isOlder(Path file, FileTime fileTime, LinkOption ... options) throws IOException {
        if (PathUtils.notExists(file, new LinkOption[0])) {
            return false;
        }
        return PathUtils.compareLastModifiedTimeTo(file, fileTime, options) < 0;
    }

    public static boolean isOlder(Path file, Instant instant, LinkOption ... options) throws IOException {
        return PathUtils.isOlder(file, FileTime.from(instant), options);
    }

    public static boolean isOlder(Path file, long timeMillis, LinkOption ... options) throws IOException {
        return PathUtils.isOlder(file, FileTime.fromMillis(timeMillis), options);
    }

    public static boolean isOlder(Path file, Path reference) throws IOException {
        return PathUtils.isOlder(file, PathUtils.getLastModifiedTime(reference, new LinkOption[0]), new LinkOption[0]);
    }

    public static boolean isPosix(Path test, LinkOption ... options) {
        return PathUtils.exists(test, options) && PathUtils.readPosixFileAttributes(test, options) != null;
    }

    public static boolean isRegularFile(Path path, LinkOption ... options) {
        return path != null && Files.isRegularFile(path, options);
    }

    public static DirectoryStream<Path> newDirectoryStream(Path dir, PathFilter pathFilter) throws IOException {
        return Files.newDirectoryStream(dir, new DirectoryStreamFilter(pathFilter));
    }

    public static OutputStream newOutputStream(Path path, boolean append) throws IOException {
        return PathUtils.newOutputStream(path, EMPTY_LINK_OPTION_ARRAY, append ? OPEN_OPTIONS_APPEND : OPEN_OPTIONS_TRUNCATE);
    }

    static OutputStream newOutputStream(Path path, LinkOption[] linkOptions, OpenOption ... openOptions) throws IOException {
        if (!PathUtils.exists(path, linkOptions)) {
            PathUtils.createParentDirectories(path, linkOptions != null && linkOptions.length > 0 ? linkOptions[0] : NULL_LINK_OPTION, new FileAttribute[0]);
        }
        ArrayList<OpenOption> list = new ArrayList<OpenOption>(Arrays.asList(openOptions != null ? openOptions : EMPTY_OPEN_OPTION_ARRAY));
        list.addAll(Arrays.asList(linkOptions != null ? linkOptions : EMPTY_LINK_OPTION_ARRAY));
        return Files.newOutputStream(path, list.toArray(EMPTY_OPEN_OPTION_ARRAY));
    }

    public static LinkOption[] noFollowLinkOptionArray() {
        return (LinkOption[])NOFOLLOW_LINK_OPTION_ARRAY.clone();
    }

    private static boolean notExists(Path path, LinkOption ... options) {
        return Files.notExists(Objects.requireNonNull(path, "path"), options);
    }

    private static boolean overrideReadOnly(DeleteOption ... deleteOptions) {
        if (deleteOptions == null) {
            return false;
        }
        return Stream.of(deleteOptions).anyMatch(e -> e == StandardDeleteOption.OVERRIDE_READ_ONLY);
    }

    public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption ... options) {
        try {
            return path == null ? null : (A)Files.readAttributes(path, type, options);
        }
        catch (IOException | UnsupportedOperationException e) {
            return null;
        }
    }

    public static BasicFileAttributes readBasicFileAttributes(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class, new LinkOption[0]);
    }

    public static BasicFileAttributes readBasicFileAttributes(Path path, LinkOption ... options) {
        return PathUtils.readAttributes(path, BasicFileAttributes.class, options);
    }

    @Deprecated
    public static BasicFileAttributes readBasicFileAttributesUnchecked(Path path) {
        return PathUtils.readBasicFileAttributes(path, EMPTY_LINK_OPTION_ARRAY);
    }

    public static DosFileAttributes readDosFileAttributes(Path path, LinkOption ... options) {
        return PathUtils.readAttributes(path, DosFileAttributes.class, options);
    }

    private static Path readIfSymbolicLink(Path path) throws IOException {
        return path != null ? (Files.isSymbolicLink(path) ? Files.readSymbolicLink(path) : path) : null;
    }

    public static BasicFileAttributes readOsFileAttributes(Path path, LinkOption ... options) {
        PosixFileAttributes fileAttributes = PathUtils.readPosixFileAttributes(path, options);
        return fileAttributes != null ? fileAttributes : PathUtils.readDosFileAttributes(path, options);
    }

    public static PosixFileAttributes readPosixFileAttributes(Path path, LinkOption ... options) {
        return PathUtils.readAttributes(path, PosixFileAttributes.class, options);
    }

    public static String readString(Path path, Charset charset) throws IOException {
        return new String(Files.readAllBytes(path), Charsets.toCharset(charset));
    }

    static List<Path> relativize(Collection<Path> collection, Path parent, boolean sort, Comparator<? super Path> comparator) {
        Stream<Path> stream = collection.stream().map(parent::relativize);
        if (sort) {
            stream = comparator == null ? stream.sorted() : stream.sorted(comparator);
        }
        return stream.collect(Collectors.toList());
    }

    private static Path requireExists(Path file, String fileParamName, LinkOption ... options) {
        Objects.requireNonNull(file, fileParamName);
        if (!PathUtils.exists(file, options)) {
            throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    private static boolean setDosReadOnly(Path path, boolean readOnly, LinkOption ... linkOptions) throws IOException {
        DosFileAttributeView dosFileAttributeView = PathUtils.getDosFileAttributeView(path, linkOptions);
        if (dosFileAttributeView != null) {
            dosFileAttributeView.setReadOnly(readOnly);
            return true;
        }
        return false;
    }

    public static void setLastModifiedTime(Path sourceFile, Path targetFile) throws IOException {
        Objects.requireNonNull(sourceFile, "sourceFile");
        Files.setLastModifiedTime(targetFile, PathUtils.getLastModifiedTime(sourceFile, new LinkOption[0]));
    }

    private static boolean setPosixDeletePermissions(Path parent, boolean enableDeleteChildren, LinkOption ... linkOptions) throws IOException {
        return PathUtils.setPosixPermissions(parent, enableDeleteChildren, Arrays.asList(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE), linkOptions);
    }

    private static boolean setPosixPermissions(Path path, boolean addPermissions, List<PosixFilePermission> updatePermissions, LinkOption ... linkOptions) throws IOException {
        if (path != null) {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, linkOptions);
            HashSet<PosixFilePermission> newPermissions = new HashSet<PosixFilePermission>(permissions);
            if (addPermissions) {
                newPermissions.addAll(updatePermissions);
            } else {
                newPermissions.removeAll(updatePermissions);
            }
            if (!newPermissions.equals(permissions)) {
                Files.setPosixFilePermissions(path, newPermissions);
            }
            return true;
        }
        return false;
    }

    private static void setPosixReadOnlyFile(Path path, boolean readOnly, LinkOption ... linkOptions) throws IOException {
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, linkOptions);
        List<PosixFilePermission> readPermissions = Arrays.asList(PosixFilePermission.OWNER_READ);
        List<PosixFilePermission> writePermissions = Arrays.asList(PosixFilePermission.OWNER_WRITE);
        if (readOnly) {
            permissions.addAll(readPermissions);
            permissions.removeAll(writePermissions);
        } else {
            permissions.addAll(readPermissions);
            permissions.addAll(writePermissions);
        }
        Files.setPosixFilePermissions(path, permissions);
    }

    public static Path setReadOnly(Path path, boolean readOnly, LinkOption ... linkOptions) throws IOException {
        try {
            if (PathUtils.setDosReadOnly(path, readOnly, linkOptions)) {
                return path;
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        Path parent = PathUtils.getParent(path);
        if (!PathUtils.isPosix(parent, linkOptions)) {
            throw new IOException(String.format("DOS or POSIX file operations not available for '%s' %s", path, Arrays.toString(linkOptions)));
        }
        if (readOnly) {
            PathUtils.setPosixReadOnlyFile(path, readOnly, linkOptions);
            PathUtils.setPosixDeletePermissions(parent, false, linkOptions);
        } else {
            PathUtils.setPosixDeletePermissions(parent, true, linkOptions);
        }
        return path;
    }

    public static long sizeOf(Path path) throws IOException {
        PathUtils.requireExists(path, "path", new LinkOption[0]);
        return Files.isDirectory(path, new LinkOption[0]) ? PathUtils.sizeOfDirectory(path) : Files.size(path);
    }

    public static BigInteger sizeOfAsBigInteger(Path path) throws IOException {
        PathUtils.requireExists(path, "path", new LinkOption[0]);
        return Files.isDirectory(path, new LinkOption[0]) ? PathUtils.sizeOfDirectoryAsBigInteger(path) : BigInteger.valueOf(Files.size(path));
    }

    public static long sizeOfDirectory(Path directory) throws IOException {
        return PathUtils.countDirectory(directory).getByteCounter().getLong();
    }

    public static BigInteger sizeOfDirectoryAsBigInteger(Path directory) throws IOException {
        return PathUtils.countDirectoryAsBigInteger(directory).getByteCounter().getBigInteger();
    }

    static Set<FileVisitOption> toFileVisitOptionSet(FileVisitOption ... fileVisitOptions) {
        return fileVisitOptions == null ? EnumSet.noneOf(FileVisitOption.class) : Stream.of(fileVisitOptions).collect(Collectors.toSet());
    }

    public static Path touch(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        if (!Files.exists(file, new LinkOption[0])) {
            PathUtils.createParentDirectories(file, new FileAttribute[0]);
            Files.createFile(file, new FileAttribute[0]);
        } else {
            FileTimes.setLastModifiedTime(file);
        }
        return file;
    }

    public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, Path directory) throws IOException {
        Files.walkFileTree(directory, visitor);
        return visitor;
    }

    public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, Path start, Set<FileVisitOption> options, int maxDepth) throws IOException {
        Files.walkFileTree(start, options, maxDepth, visitor);
        return visitor;
    }

    public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, String first, String ... more) throws IOException {
        return PathUtils.visitFileTree(visitor, Paths.get(first, more));
    }

    public static <T extends FileVisitor<? super Path>> T visitFileTree(T visitor, URI uri) throws IOException {
        return PathUtils.visitFileTree(visitor, Paths.get(uri));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean waitFor(Path file, Duration timeout2, LinkOption ... options) {
        Objects.requireNonNull(file, "file");
        Instant finishInstant = Instant.now().plus(timeout2);
        boolean interrupted = false;
        long minSleepMillis = 100L;
        try {
            while (!PathUtils.exists(file, options)) {
                Instant now = Instant.now();
                if (now.isAfter(finishInstant)) {
                    boolean bl = false;
                    return bl;
                }
                try {
                    ThreadUtils.sleep(Duration.ofMillis(Math.min(100L, finishInstant.minusMillis(now.toEpochMilli()).toEpochMilli())));
                }
                catch (InterruptedException ignore) {
                    interrupted = true;
                }
                catch (Exception ex) {
                    break;
                }
            }
        }
        finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return PathUtils.exists(file, options);
    }

    public static Stream<Path> walk(Path start, PathFilter pathFilter, int maxDepth, boolean readAttributes, FileVisitOption ... options) throws IOException {
        return Files.walk(start, maxDepth, options).filter(path -> pathFilter.accept((Path)path, readAttributes ? PathUtils.readBasicFileAttributesUnchecked(path) : null) == FileVisitResult.CONTINUE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static <R> R withPosixFileAttributes(Path path, LinkOption[] linkOptions, boolean overrideReadOnly, IOFunction<PosixFileAttributes, R> function) throws IOException {
        PosixFileAttributes posixFileAttributes = overrideReadOnly ? PathUtils.readPosixFileAttributes(path, linkOptions) : null;
        try {
            R r = function.apply(posixFileAttributes);
            return r;
        }
        finally {
            if (posixFileAttributes != null && path != null && Files.exists(path, linkOptions)) {
                Files.setPosixFilePermissions(path, posixFileAttributes.permissions());
            }
        }
    }

    public static Path writeString(Path path, CharSequence charSequence, Charset charset, OpenOption ... openOptions) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(charSequence, "charSequence");
        Files.write(path, String.valueOf(charSequence).getBytes(Charsets.toCharset(charset)), openOptions);
        return path;
    }

    private PathUtils() {
    }

    private static final class RelativeSortedPaths {
        final boolean equals;
        final List<Path> relativeFileList1;
        final List<Path> relativeFileList2;

        private RelativeSortedPaths(Path dir1, Path dir2, int maxDepth, LinkOption[] linkOptions, FileVisitOption[] fileVisitOptions) throws IOException {
            List<Path> tmpRelativeFileList1 = null;
            List<Path> tmpRelativeFileList2 = null;
            if (dir1 == null && dir2 == null) {
                this.equals = true;
            } else if (dir1 == null ^ dir2 == null) {
                this.equals = false;
            } else {
                boolean parentDirNotExists1 = Files.notExists(dir1, linkOptions);
                boolean parentDirNotExists2 = Files.notExists(dir2, linkOptions);
                if (parentDirNotExists1 || parentDirNotExists2) {
                    this.equals = parentDirNotExists1 && parentDirNotExists2;
                } else {
                    AccumulatorPathVisitor visitor1 = PathUtils.accumulate(dir1, maxDepth, fileVisitOptions);
                    AccumulatorPathVisitor visitor2 = PathUtils.accumulate(dir2, maxDepth, fileVisitOptions);
                    if (visitor1.getDirList().size() != visitor2.getDirList().size() || visitor1.getFileList().size() != visitor2.getFileList().size()) {
                        this.equals = false;
                    } else {
                        List<Path> tmpRelativeDirList2;
                        List<Path> tmpRelativeDirList1 = visitor1.relativizeDirectories(dir1, true, null);
                        if (!tmpRelativeDirList1.equals(tmpRelativeDirList2 = visitor2.relativizeDirectories(dir2, true, null))) {
                            this.equals = false;
                        } else {
                            tmpRelativeFileList1 = visitor1.relativizeFiles(dir1, true, null);
                            tmpRelativeFileList2 = visitor2.relativizeFiles(dir2, true, null);
                            this.equals = tmpRelativeFileList1.equals(tmpRelativeFileList2);
                        }
                    }
                }
            }
            this.relativeFileList1 = tmpRelativeFileList1;
            this.relativeFileList2 = tmpRelativeFileList2;
        }
    }
}

