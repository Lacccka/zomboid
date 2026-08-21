/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers;

public interface EntryStreamOffsets {
    public static final long OFFSET_UNKNOWN = -1L;

    public long getDataOffset();

    public boolean isStreamContiguous();
}

