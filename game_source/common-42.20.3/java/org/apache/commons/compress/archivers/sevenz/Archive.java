/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.sevenz;

import java.util.BitSet;
import org.apache.commons.compress.archivers.sevenz.Folder;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.StreamMap;
import org.apache.commons.compress.archivers.sevenz.SubStreamsInfo;

final class Archive {
    long packPos;
    long[] packSizes = new long[0];
    BitSet packCrcsDefined;
    long[] packCrcs;
    Folder[] folders = Folder.EMPTY_FOLDER_ARRAY;
    SubStreamsInfo subStreamsInfo;
    SevenZArchiveEntry[] files = SevenZArchiveEntry.EMPTY_SEVEN_Z_ARCHIVE_ENTRY_ARRAY;
    StreamMap streamMap;

    Archive() {
    }

    private static String lengthOf(long[] a) {
        return a == null ? "(null)" : Integer.toString(a.length);
    }

    private static String lengthOf(Object[] a) {
        return a == null ? "(null)" : Integer.toString(a.length);
    }

    public String toString() {
        return "Archive with packed streams starting at offset " + this.packPos + ", " + Archive.lengthOf(this.packSizes) + " pack sizes, " + Archive.lengthOf(this.packCrcs) + " CRCs, " + Archive.lengthOf(this.folders) + " folders, " + Archive.lengthOf(this.files) + " files and " + this.streamMap;
    }
}

