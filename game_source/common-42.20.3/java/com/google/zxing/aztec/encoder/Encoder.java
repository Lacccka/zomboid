/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.aztec.encoder;

import com.google.zxing.aztec.encoder.AztecCode;
import com.google.zxing.aztec.encoder.HighLevelEncoder;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;

public final class Encoder {
    public static final int DEFAULT_EC_PERCENT = 33;
    public static final int DEFAULT_AZTEC_LAYERS = 0;
    private static final int MAX_NB_BITS = 32;
    private static final int MAX_NB_BITS_COMPACT = 4;
    private static final int[] WORD_SIZE = new int[]{4, 6, 6, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12};

    private Encoder() {
    }

    public static AztecCode encode(byte[] data) {
        return Encoder.encode(data, 33, 0);
    }

    public static AztecCode encode(byte[] data, int minECCPercent, int userSpecifiedLayers) {
        int i;
        int matrixSize;
        BitArray stuffedBits;
        int wordSize;
        int totalBitsInLayer;
        int layers;
        boolean compact;
        BitArray bits = new HighLevelEncoder(data).encode();
        int eccBits = bits.getSize() * minECCPercent / 100 + 11;
        int totalSizeBits = bits.getSize() + eccBits;
        if (userSpecifiedLayers != 0) {
            compact = userSpecifiedLayers < 0;
            layers = Math.abs(userSpecifiedLayers);
            if (layers > (compact ? 4 : 32)) {
                throw new IllegalArgumentException(String.format("Illegal value %s for layers", userSpecifiedLayers));
            }
            totalBitsInLayer = Encoder.totalBitsInLayer(layers, compact);
            wordSize = WORD_SIZE[layers];
            int usableBitsInLayers = totalBitsInLayer - totalBitsInLayer % wordSize;
            stuffedBits = Encoder.stuffBits(bits, wordSize);
            if (stuffedBits.getSize() + eccBits > usableBitsInLayers) {
                throw new IllegalArgumentException("Data to large for user specified layer");
            }
            if (compact && stuffedBits.getSize() > wordSize * 64) {
                throw new IllegalArgumentException("Data to large for user specified layer");
            }
        } else {
            wordSize = 0;
            stuffedBits = null;
            int i2 = 0;
            while (true) {
                if (i2 > 32) {
                    throw new IllegalArgumentException("Data too large for an Aztec code");
                }
                compact = i2 <= 3;
                layers = compact ? i2 + 1 : i2;
                totalBitsInLayer = Encoder.totalBitsInLayer(layers, compact);
                if (totalSizeBits <= totalBitsInLayer) {
                    if (wordSize != WORD_SIZE[layers]) {
                        wordSize = WORD_SIZE[layers];
                        stuffedBits = Encoder.stuffBits(bits, wordSize);
                    }
                    int usableBitsInLayers = totalBitsInLayer - totalBitsInLayer % wordSize;
                    if ((!compact || stuffedBits.getSize() <= wordSize * 64) && stuffedBits.getSize() + eccBits <= usableBitsInLayers) break;
                }
                ++i2;
            }
        }
        BitArray messageBits = Encoder.generateCheckWords(stuffedBits, totalBitsInLayer, wordSize);
        int messageSizeInWords = stuffedBits.getSize() / wordSize;
        BitArray modeMessage = Encoder.generateModeMessage(compact, layers, messageSizeInWords);
        int baseMatrixSize = compact ? 11 + layers * 4 : 14 + layers * 4;
        int[] alignmentMap = new int[baseMatrixSize];
        if (compact) {
            matrixSize = baseMatrixSize;
            for (int i3 = 0; i3 < alignmentMap.length; ++i3) {
                alignmentMap[i3] = i3;
            }
        } else {
            matrixSize = baseMatrixSize + 1 + 2 * ((baseMatrixSize / 2 - 1) / 15);
            int origCenter = baseMatrixSize / 2;
            int center = matrixSize / 2;
            for (int i4 = 0; i4 < origCenter; ++i4) {
                int newOffset = i4 + i4 / 15;
                alignmentMap[origCenter - i4 - 1] = center - newOffset - 1;
                alignmentMap[origCenter + i4] = center + newOffset + 1;
            }
        }
        BitMatrix matrix = new BitMatrix(matrixSize);
        int rowOffset = 0;
        for (i = 0; i < layers; ++i) {
            int rowSize = compact ? (layers - i) * 4 + 9 : (layers - i) * 4 + 12;
            for (int j = 0; j < rowSize; ++j) {
                int columnOffset = j * 2;
                for (int k = 0; k < 2; ++k) {
                    if (messageBits.get(rowOffset + columnOffset + k)) {
                        matrix.set(alignmentMap[i * 2 + k], alignmentMap[i * 2 + j]);
                    }
                    if (messageBits.get(rowOffset + rowSize * 2 + columnOffset + k)) {
                        matrix.set(alignmentMap[i * 2 + j], alignmentMap[baseMatrixSize - 1 - i * 2 - k]);
                    }
                    if (messageBits.get(rowOffset + rowSize * 4 + columnOffset + k)) {
                        matrix.set(alignmentMap[baseMatrixSize - 1 - i * 2 - k], alignmentMap[baseMatrixSize - 1 - i * 2 - j]);
                    }
                    if (!messageBits.get(rowOffset + rowSize * 6 + columnOffset + k)) continue;
                    matrix.set(alignmentMap[baseMatrixSize - 1 - i * 2 - j], alignmentMap[i * 2 + k]);
                }
            }
            rowOffset += rowSize * 8;
        }
        Encoder.drawModeMessage(matrix, compact, matrixSize, modeMessage);
        if (compact) {
            Encoder.drawBullsEye(matrix, matrixSize / 2, 5);
        } else {
            Encoder.drawBullsEye(matrix, matrixSize / 2, 7);
            i = 0;
            int j = 0;
            while (i < baseMatrixSize / 2 - 1) {
                for (int k = matrixSize / 2 & 1; k < matrixSize; k += 2) {
                    matrix.set(matrixSize / 2 - j, k);
                    matrix.set(matrixSize / 2 + j, k);
                    matrix.set(k, matrixSize / 2 - j);
                    matrix.set(k, matrixSize / 2 + j);
                }
                i += 15;
                j += 16;
            }
        }
        AztecCode aztec = new AztecCode();
        aztec.setCompact(compact);
        aztec.setSize(matrixSize);
        aztec.setLayers(layers);
        aztec.setCodeWords(messageSizeInWords);
        aztec.setMatrix(matrix);
        return aztec;
    }

    private static void drawBullsEye(BitMatrix matrix, int center, int size) {
        for (int i = 0; i < size; i += 2) {
            for (int j = center - i; j <= center + i; ++j) {
                matrix.set(j, center - i);
                matrix.set(j, center + i);
                matrix.set(center - i, j);
                matrix.set(center + i, j);
            }
        }
        matrix.set(center - size, center - size);
        matrix.set(center - size + 1, center - size);
        matrix.set(center - size, center - size + 1);
        matrix.set(center + size, center - size);
        matrix.set(center + size, center - size + 1);
        matrix.set(center + size, center + size - 1);
    }

    static BitArray generateModeMessage(boolean compact, int layers, int messageSizeInWords) {
        BitArray modeMessage = new BitArray();
        if (compact) {
            modeMessage.appendBits(layers - 1, 2);
            modeMessage.appendBits(messageSizeInWords - 1, 6);
            modeMessage = Encoder.generateCheckWords(modeMessage, 28, 4);
        } else {
            modeMessage.appendBits(layers - 1, 5);
            modeMessage.appendBits(messageSizeInWords - 1, 11);
            modeMessage = Encoder.generateCheckWords(modeMessage, 40, 4);
        }
        return modeMessage;
    }

    private static void drawModeMessage(BitMatrix matrix, boolean compact, int matrixSize, BitArray modeMessage) {
        int center = matrixSize / 2;
        if (compact) {
            for (int i = 0; i < 7; ++i) {
                int offset = center - 3 + i;
                if (modeMessage.get(i)) {
                    matrix.set(offset, center - 5);
                }
                if (modeMessage.get(i + 7)) {
                    matrix.set(center + 5, offset);
                }
                if (modeMessage.get(20 - i)) {
                    matrix.set(offset, center + 5);
                }
                if (!modeMessage.get(27 - i)) continue;
                matrix.set(center - 5, offset);
            }
        } else {
            for (int i = 0; i < 10; ++i) {
                int offset = center - 5 + i + i / 5;
                if (modeMessage.get(i)) {
                    matrix.set(offset, center - 7);
                }
                if (modeMessage.get(i + 10)) {
                    matrix.set(center + 7, offset);
                }
                if (modeMessage.get(29 - i)) {
                    matrix.set(offset, center + 7);
                }
                if (!modeMessage.get(39 - i)) continue;
                matrix.set(center - 7, offset);
            }
        }
    }

    private static BitArray generateCheckWords(BitArray bitArray, int totalBits, int wordSize) {
        int messageSizeInWords = bitArray.getSize() / wordSize;
        ReedSolomonEncoder rs = new ReedSolomonEncoder(Encoder.getGF(wordSize));
        int totalWords = totalBits / wordSize;
        int[] messageWords = Encoder.bitsToWords(bitArray, wordSize, totalWords);
        rs.encode(messageWords, totalWords - messageSizeInWords);
        int startPad = totalBits % wordSize;
        BitArray messageBits = new BitArray();
        messageBits.appendBits(0, startPad);
        for (int messageWord : messageWords) {
            messageBits.appendBits(messageWord, wordSize);
        }
        return messageBits;
    }

    private static int[] bitsToWords(BitArray stuffedBits, int wordSize, int totalWords) {
        int[] message = new int[totalWords];
        int n = stuffedBits.getSize() / wordSize;
        for (int i = 0; i < n; ++i) {
            int value = 0;
            for (int j = 0; j < wordSize; ++j) {
                value |= stuffedBits.get(i * wordSize + j) ? 1 << wordSize - j - 1 : 0;
            }
            message[i] = value;
        }
        return message;
    }

    private static GenericGF getGF(int wordSize) {
        switch (wordSize) {
            case 4: {
                return GenericGF.AZTEC_PARAM;
            }
            case 6: {
                return GenericGF.AZTEC_DATA_6;
            }
            case 8: {
                return GenericGF.AZTEC_DATA_8;
            }
            case 10: {
                return GenericGF.AZTEC_DATA_10;
            }
            case 12: {
                return GenericGF.AZTEC_DATA_12;
            }
        }
        throw new IllegalArgumentException("Unsupported word size " + wordSize);
    }

    static BitArray stuffBits(BitArray bits, int wordSize) {
        BitArray out = new BitArray();
        int n = bits.getSize();
        int mask = (1 << wordSize) - 2;
        for (int i = 0; i < n; i += wordSize) {
            int word = 0;
            for (int j = 0; j < wordSize; ++j) {
                if (i + j < n && !bits.get(i + j)) continue;
                word |= 1 << wordSize - 1 - j;
            }
            if ((word & mask) == mask) {
                out.appendBits(word & mask, wordSize);
                --i;
                continue;
            }
            if ((word & mask) == 0) {
                out.appendBits(word | 1, wordSize);
                --i;
                continue;
            }
            out.appendBits(word, wordSize);
        }
        return out;
    }

    private static int totalBitsInLayer(int layers, boolean compact) {
        return ((compact ? 88 : 112) + 16 * layers) * layers;
    }
}

