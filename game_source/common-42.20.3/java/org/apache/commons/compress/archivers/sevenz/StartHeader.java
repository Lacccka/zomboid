/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.sevenz;

final class StartHeader {
    final long nextHeaderOffset;
    final long nextHeaderSize;
    final long nextHeaderCrc;

    StartHeader(long nextHeaderOffset, long nextHeaderSize, long nextHeaderCrc) {
        this.nextHeaderOffset = nextHeaderOffset;
        this.nextHeaderSize = nextHeaderSize;
        this.nextHeaderCrc = nextHeaderCrc;
    }
}

