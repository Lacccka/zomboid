/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.utils;

import java.io.InputStream;
import java.util.Objects;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

@Deprecated
public class ChecksumCalculatingInputStream
extends CheckedInputStream {
    @Deprecated
    public ChecksumCalculatingInputStream(Checksum checksum, InputStream inputStream2) {
        super(Objects.requireNonNull(inputStream2, "inputStream"), Objects.requireNonNull(checksum, "checksum"));
    }

    @Deprecated
    public long getValue() {
        return this.getChecksum().getValue();
    }
}

