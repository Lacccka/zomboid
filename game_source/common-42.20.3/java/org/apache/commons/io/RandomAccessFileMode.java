/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Objects;

public enum RandomAccessFileMode {
    READ_ONLY("r"),
    READ_WRITE("rw"),
    READ_WRITE_SYNC_ALL("rws"),
    READ_WRITE_SYNC_CONTENT("rwd");

    private final String mode;

    private RandomAccessFileMode(String mode) {
        this.mode = mode;
    }

    public RandomAccessFile create(File file) throws FileNotFoundException {
        return new RandomAccessFile(file, this.mode);
    }

    public RandomAccessFile create(Path file) throws FileNotFoundException {
        return this.create(Objects.requireNonNull(file.toFile(), "file"));
    }

    public RandomAccessFile create(String file) throws FileNotFoundException {
        return new RandomAccessFile(file, this.mode);
    }

    public String toString() {
        return this.mode;
    }
}

