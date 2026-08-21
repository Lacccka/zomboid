/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

public class EmptyFileFilter
extends AbstractFileFilter
implements Serializable {
    public static final IOFileFilter EMPTY = new EmptyFileFilter();
    public static final IOFileFilter NOT_EMPTY = EMPTY.negate();
    private static final long serialVersionUID = 3631422087512832211L;

    protected EmptyFileFilter() {
    }

    @Override
    public boolean accept(File file) {
        if (file == null) {
            return true;
        }
        if (file.isDirectory()) {
            Object[] files = file.listFiles();
            return IOUtils.length(files) == 0;
        }
        return file.length() == 0L;
    }

    @Override
    public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
        if (file == null) {
            return this.toFileVisitResult(true);
        }
        return this.get(() -> {
            if (Files.isDirectory(file, new LinkOption[0])) {
                try (Stream<Path> stream = Files.list(file);){
                    FileVisitResult fileVisitResult = this.toFileVisitResult(!stream.findFirst().isPresent());
                    return fileVisitResult;
                }
            }
            return this.toFileVisitResult(Files.size(file) == 0L);
        });
    }
}

