/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.qrcode.decoder;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.Version;

final class DataBlock {
    private final int numDataCodewords;
    private final byte[] codewords;

    private DataBlock(int numDataCodewords, byte[] codewords) {
        this.numDataCodewords = numDataCodewords;
        this.codewords = codewords;
    }

    static DataBlock[] getDataBlocks(byte[] rawCodewords, Version version, ErrorCorrectionLevel ecLevel) {
        int numCodewords;
        int longerBlocksStartAt;
        int i;
        Version.ECB[] ecBlockArray;
        if (rawCodewords.length != version.getTotalCodewords()) {
            throw new IllegalArgumentException();
        }
        Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevel);
        int totalBlocks = 0;
        for (Version.ECB ecBlock : ecBlockArray = ecBlocks.getECBlocks()) {
            totalBlocks += ecBlock.getCount();
        }
        DataBlock[] result = new DataBlock[totalBlocks];
        int numResultBlocks = 0;
        for (Version.ECB ecBlock : ecBlockArray) {
            for (i = 0; i < ecBlock.getCount(); ++i) {
                int numDataCodewords = ecBlock.getDataCodewords();
                int numBlockCodewords = ecBlocks.getECCodewordsPerBlock() + numDataCodewords;
                result[numResultBlocks++] = new DataBlock(numDataCodewords, new byte[numBlockCodewords]);
            }
        }
        int shorterBlocksTotalCodewords = result[0].codewords.length;
        for (longerBlocksStartAt = result.length - 1; longerBlocksStartAt >= 0 && (numCodewords = result[longerBlocksStartAt].codewords.length) != shorterBlocksTotalCodewords; --longerBlocksStartAt) {
        }
        ++longerBlocksStartAt;
        int shorterBlocksNumDataCodewords = shorterBlocksTotalCodewords - ecBlocks.getECCodewordsPerBlock();
        int rawCodewordsOffset = 0;
        for (i = 0; i < shorterBlocksNumDataCodewords; ++i) {
            for (int j = 0; j < numResultBlocks; ++j) {
                result[j].codewords[i] = rawCodewords[rawCodewordsOffset++];
            }
        }
        for (int j = longerBlocksStartAt; j < numResultBlocks; ++j) {
            result[j].codewords[shorterBlocksNumDataCodewords] = rawCodewords[rawCodewordsOffset++];
        }
        int max = result[0].codewords.length;
        for (int i2 = shorterBlocksNumDataCodewords; i2 < max; ++i2) {
            for (int j = 0; j < numResultBlocks; ++j) {
                int iOffset = j < longerBlocksStartAt ? i2 : i2 + 1;
                result[j].codewords[iOffset] = rawCodewords[rawCodewordsOffset++];
            }
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

