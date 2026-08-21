/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.decoder;

import com.google.zxing.datamatrix.decoder.Version;

final class DataBlock {
    private final int numDataCodewords;
    private final byte[] codewords;

    private DataBlock(int numDataCodewords, byte[] codewords) {
        this.numDataCodewords = numDataCodewords;
        this.codewords = codewords;
    }

    static DataBlock[] getDataBlocks(byte[] rawCodewords, Version version) {
        int i;
        Version.ECB[] ecBlockArray;
        Version.ECBlocks ecBlocks = version.getECBlocks();
        int totalBlocks = 0;
        for (Version.ECB ecBlock : ecBlockArray = ecBlocks.getECBlocks()) {
            totalBlocks += ecBlock.getCount();
        }
        DataBlock[] result = new DataBlock[totalBlocks];
        int numResultBlocks = 0;
        for (Version.ECB ecBlock : ecBlockArray) {
            for (i = 0; i < ecBlock.getCount(); ++i) {
                int numDataCodewords = ecBlock.getDataCodewords();
                int numBlockCodewords = ecBlocks.getECCodewords() + numDataCodewords;
                result[numResultBlocks++] = new DataBlock(numDataCodewords, new byte[numBlockCodewords]);
            }
        }
        int longerBlocksTotalCodewords = result[0].codewords.length;
        int longerBlocksNumDataCodewords = longerBlocksTotalCodewords - ecBlocks.getECCodewords();
        int shorterBlocksNumDataCodewords = longerBlocksNumDataCodewords - 1;
        int rawCodewordsOffset = 0;
        for (i = 0; i < shorterBlocksNumDataCodewords; ++i) {
            for (int j = 0; j < numResultBlocks; ++j) {
                result[j].codewords[i] = rawCodewords[rawCodewordsOffset++];
            }
        }
        boolean specialVersion = version.getVersionNumber() == 24;
        int numLongerBlocks = specialVersion ? 8 : numResultBlocks;
        for (int j = 0; j < numLongerBlocks; ++j) {
            result[j].codewords[longerBlocksNumDataCodewords - 1] = rawCodewords[rawCodewordsOffset++];
        }
        int max = result[0].codewords.length;
        for (int i2 = longerBlocksNumDataCodewords; i2 < max; ++i2) {
            for (int j = 0; j < numResultBlocks; ++j) {
                int jOffset = specialVersion ? (j + 8) % numResultBlocks : j;
                int iOffset = specialVersion && jOffset > 7 ? i2 - 1 : i2;
                result[jOffset].codewords[iOffset] = rawCodewords[rawCodewordsOffset++];
            }
        }
        if (rawCodewordsOffset != rawCodewords.length) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    int getNumDataCodewords() {
        return this.numDataCodewords;
    }

    byte[] getCodewords() {
        return this.codewords;
    }
}

