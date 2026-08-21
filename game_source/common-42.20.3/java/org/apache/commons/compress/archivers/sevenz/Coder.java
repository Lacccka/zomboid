/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.sevenz;

final class Coder {
    final byte[] decompressionMethodId;
    final long numInStreams;
    final long numOutStreams;
    final byte[] properties;

    Coder(byte[] decompressionMethodId, long numInStreams, long numOutStreams, byte[] properties) {
        this.decompressionMethodId = decompressionMethodId;
        this.numInStreams = numInStreams;
        this.numOutStreams = numOutStreams;
        this.properties = properties;
    }
}

