/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

public interface ArchiveEntry {
    public static final long SIZE_UNKNOWN = -1L;

    public Date getLastModifiedDate();

    public String getName();

    public long getSize();

    public boolean isDirectory();

    default public Path resolveIn(Path parentPath) throws IOException {
        String name = this.getName();
        Path outputFile = parentPath.resolve(name).normalize();
        if (!outputFile.startsWith(parentPath)) {
            throw new IOException(String.format("Zip slip '%s' + '%s' -> '%s'", parentPath, name, outputFile));
        }
        return outputFile;
    }
}

