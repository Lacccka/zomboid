/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.changes;

import java.io.InputStream;
import java.util.Objects;
import org.apache.commons.compress.archivers.ArchiveEntry;

final class Change<E extends ArchiveEntry> {
    private final String targetFileName;
    private final E entry;
    private final InputStream inputStream;
    private final boolean replaceMode;
    private final ChangeType type;

    Change(E archiveEntry, InputStream inputStream2, boolean replace) {
        this.entry = (ArchiveEntry)Objects.requireNonNull(archiveEntry, "archiveEntry");
        this.inputStream = Objects.requireNonNull(inputStream2, "inputStream");
        this.type = ChangeType.ADD;
        this.targetFileName = null;
        this.replaceMode = replace;
    }

    Change(String fileName, ChangeType type) {
        this.targetFileName = Objects.requireNonNull(fileName, "fileName");
        this.type = type;
        this.inputStream = null;
        this.entry = null;
        this.replaceMode = true;
    }

    E getEntry() {
        return this.entry;
    }

    InputStream getInputStream() {
        return this.inputStream;
    }

    String getTargetFileName() {
        return this.targetFileName;
    }

    ChangeType getType() {
        return this.type;
    }

    boolean isReplaceMode() {
        return this.replaceMode;
    }

    static enum ChangeType {
        DELETE,
        ADD,
        MOVE,
        DELETE_DIR;

    }
}

