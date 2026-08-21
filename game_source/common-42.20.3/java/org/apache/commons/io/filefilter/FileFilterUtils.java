/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DelegateFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.MagicNumberFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SizeFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class FileFilterUtils {
    private static final IOFileFilter CVS_FILTER = FileFilterUtils.notFileFilter(FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), FileFilterUtils.nameFileFilter("CVS")));
    private static final IOFileFilter SVN_FILTER = FileFilterUtils.notFileFilter(FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), FileFilterUtils.nameFileFilter(".svn")));

    public static IOFileFilter ageFileFilter(Date cutoffDate) {
        return new AgeFileFilter(cutoffDate);
    }

    public static IOFileFilter ageFileFilter(Date cutoffDate, boolean acceptOlder) {
        return new AgeFileFilter(cutoffDate, acceptOlder);
    }

    public static IOFileFilter ageFileFilter(File cutoffReference) {
        return new AgeFileFilter(cutoffReference);
    }

    public static IOFileFilter ageFileFilter(File cutoffReference, boolean acceptOlder) {
        return new AgeFileFilter(cutoffReference, acceptOlder);
    }

    public static IOFileFilter ageFileFilter(long cutoffMillis) {
        return new AgeFileFilter(cutoffMillis);
    }

    public static IOFileFilter ageFileFilter(long cutoffMillis, boolean acceptOlder) {
        return new AgeFileFilter(cutoffMillis, acceptOlder);
    }

    public static IOFileFilter and(IOFileFilter ... filters) {
        return new AndFileFilter(FileFilterUtils.toList(filters));
    }

    @Deprecated
    public static IOFileFilter andFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        return new AndFileFilter(filter1, filter2);
    }

    public static IOFileFilter asFileFilter(FileFilter filter) {
        return new DelegateFileFilter(filter);
    }

    public static IOFileFilter asFileFilter(FilenameFilter filter) {
        return new DelegateFileFilter(filter);
    }

    public static IOFileFilter directoryFileFilter() {
        return DirectoryFileFilter.DIRECTORY;
    }

    public static IOFileFilter falseFileFilter() {
        return FalseFileFilter.FALSE;
    }

    public static IOFileFilter fileFileFilter() {
        return FileFileFilter.INSTANCE;
    }

    public static File[] filter(IOFileFilter filter, File ... files) {
        Objects.requireNonNull(filter, "filter");
        if (files == null) {
            return FileUtils.EMPTY_FILE_ARRAY;
        }
        return FileFilterUtils.filterFiles(filter, Stream.of(files), Collectors.toList()).toArray(FileUtils.EMPTY_FILE_ARRAY);
    }

    public static File[] filter(IOFileFilter filter, Iterable<File> files) {
        return FileFilterUtils.filterList(filter, files).toArray(FileUtils.EMPTY_FILE_ARRAY);
    }

    private static <R, A> R filterFiles(IOFileFilter filter, Stream<File> stream, Collector<? super File, A, R> collector) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(collector, "collector");
        if (stream == null) {
            return Stream.empty().collect(collector);
        }
        return stream.filter(filter::accept).collect(collector);
    }

    public static List<File> filterList(IOFileFilter filter, File ... files) {
        return Arrays.asList(FileFilterUtils.filter(filter, files));
    }

    public static List<File> filterList(IOFileFilter filter, Iterable<File> files) {
        if (files == null) {
            return Collections.emptyList();
        }
        return FileFilterUtils.filterFiles(filter, StreamSupport.stream(files.spliterator(), false), Collectors.toList());
    }

    public static Set<File> filterSet(IOFileFilter filter, File ... files) {
        return new HashSet<File>(Arrays.asList(FileFilterUtils.filter(filter, files)));
    }

    public static Set<File> filterSet(IOFileFilter filter, Iterable<File> files) {
        if (files == null) {
            return Collections.emptySet();
        }
        return FileFilterUtils.filterFiles(filter, StreamSupport.stream(files.spliterator(), false), Collectors.toSet());
    }

    public static IOFileFilter magicNumberFileFilter(byte[] magicNumber) {
        return new MagicNumberFileFilter(magicNumber);
    }

    public static IOFileFilter magicNumberFileFilter(byte[] magicNumber, long offset) {
        return new MagicNumberFileFilter(magicNumber, offset);
    }

    public static IOFileFilter magicNumberFileFilter(String magicNumber) {
        return new MagicNumberFileFilter(magicNumber);
    }

    public static IOFileFilter magicNumberFileFilter(String magicNumber, long offset) {
        return new MagicNumberFileFilter(magicNumber, offset);
    }

    public static IOFileFilter makeCVSAware(IOFileFilter filter) {
        return filter == null ? CVS_FILTER : FileFilterUtils.and(filter, CVS_FILTER);
    }

    public static IOFileFilter makeDirectoryOnly(IOFileFilter filter) {
        if (filter == null) {
            return DirectoryFileFilter.DIRECTORY;
        }
        return DirectoryFileFilter.DIRECTORY.and(filter);
    }

    public static IOFileFilter makeFileOnly(IOFileFilter filter) {
        if (filter == null) {
            return FileFileFilter.INSTANCE;
        }
        return FileFileFilter.INSTANCE.and(filter);
    }

    public static IOFileFilter makeSVNAware(IOFileFilter filter) {
        return filter == null ? SVN_FILTER : FileFilterUtils.and(filter, SVN_FILTER);
    }

    public static IOFileFilter nameFileFilter(String name) {
        return new NameFileFilter(name);
    }

    public static IOFileFilter nameFileFilter(String name, IOCase ioCase) {
        return new NameFileFilter(name, ioCase);
    }

    public static IOFileFilter notFileFilter(IOFileFilter filter) {
        return filter.negate();
    }

    public static IOFileFilter or(IOFileFilter ... filters) {
        return new OrFileFilter(FileFilterUtils.toList(filters));
    }

    @Deprecated
    public static IOFileFilter orFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
        return new OrFileFilter(filter1, filter2);
    }

    public static IOFileFilter prefixFileFilter(String prefix) {
        return new PrefixFileFilter(prefix);
    }

    public static IOFileFilter prefixFileFilter(String prefix, IOCase ioCase) {
        return new PrefixFileFilter(prefix, ioCase);
    }

    public static IOFileFilter sizeFileFilter(long threshold) {
        return new SizeFileFilter(threshold);
    }

    public static IOFileFilter sizeFileFilter(long threshold, boolean acceptLarger) {
        return new SizeFileFilter(threshold, acceptLarger);
    }

    public static IOFileFilter sizeRangeFileFilter(long minSizeInclusive, long maxSizeInclusive) {
        SizeFileFilter minimumFilter = new SizeFileFilter(minSizeInclusive, true);
        SizeFileFilter maximumFilter = new SizeFileFilter(maxSizeInclusive + 1L, false);
        return minimumFilter.and(maximumFilter);
    }

    public static IOFileFilter suffixFileFilter(String suffix) {
        return new SuffixFileFilter(suffix);
    }

    public static IOFileFilter suffixFileFilter(String suffix, IOCase ioCase) {
        return new SuffixFileFilter(suffix, ioCase);
    }

    public static List<IOFileFilter> toList(IOFileFilter ... filters) {
        return Stream.of(Objects.requireNonNull(filters, "filters")).map(Objects::requireNonNull).collect(Collectors.toList());
    }

    public static IOFileFilter trueFileFilter() {
        return TrueFileFilter.TRUE;
    }
}

