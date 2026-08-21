/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.sevenz;

import java.util.BitSet;

final class SubStreamsInfo {
    final long[] unpackSizes;
    final BitSet hasCrc;
    final long[] crcs;

    SubStreamsInfo(int totalUnpackStreams) {
        this.unpackSizes = new long[totalUnpackStreams];
        this.hasCrc = new BitSet(totalUnpackStreams);
        this.crcs = new long[totalUnpackStreams];
    }
}

