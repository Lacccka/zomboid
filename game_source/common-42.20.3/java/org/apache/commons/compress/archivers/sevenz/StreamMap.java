/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.sevenz;

final class StreamMap {
    final int[] folderFirstPackStreamIndex;
    final long[] packStreamOffsets;
    final int[] folderFirstFileIndex;
    final int[] fileFolderIndex;

    StreamMap(int[] folderFirstPackStreamIndex, long[] packStreamOffsets, int[] folderFirstFileIndex, int[] fileFolderIndex) {
        this.folderFirstPackStreamIndex = folderFirstPackStreamIndex;
        this.packStreamOffsets = packStreamOffsets;
        this.folderFirstFileIndex = folderFirstFileIndex;
        this.fileFolderIndex = fileFolderIndex;
    }

    public String toString() {
        return "StreamMap with indices of " + this.folderFirstPackStreamIndex.length + " folders, offsets of " + this.packStreamOffsets.length + " packed streams, first files of " + this.folderFirstFileIndex.length + " folders and folder indices for " + this.fileFolderIndex.length + " files";
    }
}

