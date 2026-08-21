/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.ar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.compress.archivers.ArchiveEntry;

public class ArArchiveEntry
implements ArchiveEntry {
    public static final String HEADER = "!<arch>\n";
    public static final String TRAILER = "`\n";
    private static final int DEFAULT_MODE = 33188;
    private final String name;
    private final int userId;
    private final int groupId;
    private final int mode;
    private final long lastModified;
    private final long length;

    public ArArchiveEntry(File inputFile, String entryName) {
        this(entryName, inputFile.isFile() ? inputFile.length() : 0L, 0, 0, 33188, inputFile.lastModified() / 1000L);
    }

    public ArArchiveEntry(Path inputPath, String entryName, LinkOption ... options) throws IOException {
        this(entryName, Files.isRegularFile(inputPath, options) ? Files.size(inputPath) : 0L, 0, 0, 33188, Files.getLastModifiedTime(inputPath, options).toMillis() / 1000L);
    }

    public ArArchiveEntry(String name, long length) {
        this(name, length, 0, 0, 33188, System.currentTimeMillis() / 1000L);
    }

    public ArArchiveEntry(String name, long length, int userId, int groupId, int mode, long lastModified) {
        this.name = name;
        if (length < 0L) {
            throw new IllegalArgumentException("length must not be negative");
        }
        this.length = length;
        this.userId = userId;
        this.groupId = groupId;
        this.mode = mode;
        this.lastModified = lastModified;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ArArchiveEntry other = (ArArchiveEntry)obj;
        return Objects.equals(this.name, other.name);
    }

    public int getGroupId() {
        return this.groupId;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    @Override
    public Date getLastModifiedDate() {
        return new Date(1000L * this.getLastModified());
    }

    public long getLength() {
        return this.length;
    }

    public int getMode() {
        return this.mode;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public long getSize() {
        return this.getLength();
    }

    public int getUserId() {
        return this.userId;
    }

    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }
}

