/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.commons.io.FileUtils;

@Deprecated
public class FileSystemUtils {
    @Deprecated
    public static long freeSpace(String path) throws IOException {
        return FileSystemUtils.getFreeSpace(path);
    }

    @Deprecated
    public static long freeSpaceKb() throws IOException {
        return FileSystemUtils.freeSpaceKb(-1L);
    }

    @Deprecated
    public static long freeSpaceKb(long timeout2) throws IOException {
        return FileSystemUtils.freeSpaceKb(FileUtils.current().getAbsolutePath(), timeout2);
    }

    @Deprecated
    public static long freeSpaceKb(String path) throws IOException {
        return FileSystemUtils.freeSpaceKb(path, -1L);
    }

    @Deprecated
    public static long freeSpaceKb(String path, long timeout2) throws IOException {
        return FileSystemUtils.getFreeSpace(path) / 1024L;
    }

    static long getFreeSpace(String pathStr) throws IOException {
        Path path = Paths.get(Objects.requireNonNull(pathStr, "pathStr"), new String[0]);
        if (Files.exists(path, new LinkOption[0])) {
            return Files.getFileStore(path.toAbsolutePath()).getUsableSpace();
        }
        throw new IllegalArgumentException(path.toString());
    }

    @Deprecated
    public FileSystemUtils() {
    }
}

