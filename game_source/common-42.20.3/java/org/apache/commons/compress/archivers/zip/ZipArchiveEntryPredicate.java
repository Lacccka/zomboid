/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

public interface ZipArchiveEntryPredicate {
    public boolean test(ZipArchiveEntry var1);
}

