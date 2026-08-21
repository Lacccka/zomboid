/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.qrcode.encoder;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitArray;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.Version;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.MaskUtil;
import com.google.zxing.qrcode.encoder.QRCode;

final class MatrixUtil {
    private static final int[][] POSITION_DETECTION_PATTERN = new int[][]{{1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 0, 0, 1}, {1, 0, 1, 1, 1, 0, 1}, {1, 0, 1, 1, 1, 0, 1}, {1, 0, 1, 1, 1, 0, 1}, {1, 0, 0, 0, 0, 0, 1}, {1, 1, 1, 1, 1, 1, 1}};
    private static final int[][] POSITION_ADJUSTMENT_PATTERN = new int[][]{{1, 1, 1, 1, 1}, {1, 0, 0, 0, 1}, {1, 0, 1, 0, 1}, {1, 0, 0, 0, 1}, {1, 1, 1, 1, 1}};
    private static final int[][] POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE = new int[][]{{-1, -1, -1, -1, -1, -1, -1}, {6, 18, -1, -1, -1, -1, -1}, {6, 22, -1, -1, -1, -1, -1}, {6, 26, -1, -1, -1, -1, -1}, {6, 30, -1, -1, -1, -1, -1}, {6, 34, -1, -1, -1, -1, -1}, {6, 22, 38, -1, -1, -1, -1}, {6, 24, 42, -1, -1, -1, -1}, {6, 26, 46, -1, -1, -1, -1}, {6, 28, 50, -1, -1, -1, -1}, {6, 30, 54, -1, -1, -1, -1}, {6, 32, 58, -1, -1, -1, -1}, {6, 34, 62, -1, -1, -1, -1}, {6, 26, 46, 66, -1, -1, -1}, {6, 26, 48, 70, -1, -1, -1}, {6, 26, 50, 74, -1, -1, -1}, {6, 30, 54, 78, -1, -1, -1}, {6, 30, 56, 82, -1, -1, -1}, {6, 30, 58, 86, -1, -1, -1}, {6, 34, 62, 90, -1, -1, -1}, {6, 28, 50, 72, 94, -1, -1}, {6, 26, 50, 74, 98, -1, -1}, {6, 30, 54, 78, 102, -1, -1}, {6, 28, 54, 80, 106, -1, -1}, {6, 32, 58, 84, 110, -1, -1}, {6, 30, 58, 86, 114, -1, -1}, {6, 34, 62, 90, 118, -1, -1}, {6, 26, 50, 74, 98, 122, -1}, {6, 30, 54, 78, 102, 126, -1}, {6, 26, 52, 78, 104, 130, -1}, {6, 30, 56, 82, 108, 134, -1}, {6, 34, 60, 86, 112, 138, -1}, {6, 30, 58, 86, 114, 142, -1}, {6, 34, 62, 90, 118, 146, -1}, {6, 30, 54, 78, 102, 126, 150}, {6, 24, 50, 76, 102, 128, 154}, {6, 28, 54, 80, 106, 132, 158}, {6, 32, 58, 84, 110, 136, 162}, {6, 26, 54, 82, 110, 138, 166}, {6, 30, 58, 86, 114, 142, 170}};
    private static final int[][] TYPE_INFO_COORDINATES = new int[][]{{8, 0}, {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5}, {8, 7}, {8, 8}, {7, 8}, {5, 8}, {4, 8}, {3, 8}, {2, 8}, {1, 8}, {0, 8}};
    private static final int VERSION_INFO_POLY = 7973;
    private static final int TYPE_INFO_POLY = 1335;
    private static final int TYPE_INFO_MASK_PATTERN = 21522;

    private MatrixUtil() {
    }

    static void clearMatrix(ByteMatrix matrix) {
        matrix.clear((byte)-1);
    }

    static void buildMatrix(BitArray dataBits, ErrorCorrectionLevel ecLevel, Version version, int maskPattern, ByteMatrix matrix) throws WriterException {
        MatrixUtil.clearMatrix(matrix);
        MatrixUtil.embedBasicPatterns(version, matrix);
        MatrixUtil.embedTypeInfo(ecLevel, maskPattern, matrix);
        MatrixUtil.maybeEmbedVersionInfo(version, matrix);
        MatrixUtil.embedDataBits(dataBits, maskPattern, matrix);
    }

    static void embedBasicPatterns(Version version, ByteMatrix matrix) throws WriterException {
        MatrixUtil.embedPositionDetectionPatternsAndSeparators(matrix);
        MatrixUtil.embedDarkDotAtLeftBottomCorner(matrix);
        MatrixUtil.maybeEmbedPositionAdjustmentPatterns(version, matrix);
        MatrixUtil.embedTimingPatterns(matrix);
    }

    static void embedTypeInfo(ErrorCorrectionLevel ecLevel, int maskPattern, ByteMatrix matrix) throws WriterException {
        BitArray typeInfoBits = new BitArray();
        MatrixUtil.makeTypeInfoBits(ecLevel, maskPattern, typeInfoBits);
        for (int i = 0; i < typeInfoBits.getSize(); ++i) {
            int y2;
            int x2;
            boolean bit = typeInfoBits.get(typeInfoBits.getSize() - 1 - i);
            int x1 = TYPE_INFO_COORDINATES[i][0];
            int y1 = TYPE_INFO_COORDINATES[i][1];
            matrix.set(x1, y1, bit);
            if (i < 8) {
                x2 = matrix.getWidth() - i - 1;
                y2 = 8;
                matrix.set(x2, y2, bit);
                continue;
            }
            x2 = 8;
            y2 = matrix.getHeight() - 7 + (i - 8);
            matrix.set(x2, y2, bit);
        }
    }

    static void maybeEmbedVersionInfo(Version version, ByteMatrix matrix) throws WriterException {
        if (version.getVersionNumber() < 7) {
            return;
        }
        BitArray versionInfoBits = new BitArray();
        MatrixUtil.makeVersionInfoBits(version, versionInfoBits);
        int bitIndex = 17;
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 3; ++j) {
                boolean bit = versionInfoBits.get(bitIndex);
                --bitIndex;
                matrix.set(i, matrix.getHeight() - 11 + j, bit);
                matrix.set(matrix.getHeight() - 11 + j, i, bit);
            }
        }
    }

    static void embedDataBits(BitArray dataBits, int maskPattern, ByteMatrix matrix) throws WriterException {
        int bitIndex = 0;
        int direction = -1;
        int y = matrix.getHeight() - 1;
        for (int x = matrix.getWidth() - 1; x > 0; x -= 2) {
            if (x == 6) {
                --x;
            }
            while (y >= 0 && y < matrix.getHeight()) {
                for (int i = 0; i < 2; ++i) {
                    boolean bit;
                    int xx = x - i;
                    if (!MatrixUtil.isEmpty(matrix.get(xx, y))) continue;
                    if (bitIndex < dataBits.getSize()) {
                        bit = dataBits.get(bitIndex);
                        ++bitIndex;
                    } else {
                        bit = false;
                    }
                    if (maskPattern != -1 && MaskUtil.getDataMaskBit(maskPattern, xx, y)) {
                        bit = !bit;
                    }
                    matrix.set(xx, y, bit);
                }
                y += direction;
            }
            direction = -direction;
            y += direction;
        }
        if (bitIndex != dataBits.getSize()) {
            throw new WriterException("Not all bits consumed: " + bitIndex + '/' + dataBits.getSize());
        }
    }

    static int findMSBSet(int value) {
        int numDigits = 0;
        while (value != 0) {
            value >>>= 1;
            ++numDigits;
        }
        return numDigits;
    }

    static int calculateBCHCode(int value, int poly) {
        if (poly == 0) {
            throw new IllegalArgumentException("0 polynomial");
        }
        int msbSetInPoly = MatrixUtil.findMSBSet(poly);
        value <<= msbSetInPoly - 1;
        while (MatrixUtil.findMSBSet(value) >= msbSetInPoly) {
            value ^= poly << MatrixUtil.findMSBSet(value) - msbSetInPoly;
        }
        return value;
    }

    static void makeTypeInfoBits(ErrorCorrectionLevel ecLevel, int maskPattern, BitArray bits) throws WriterException {
        if (!QRCode.isValidMaskPattern(maskPattern)) {
            throw new WriterException("Invalid mask pattern");
        }
        int typeInfo = ecLevel.getBits() << 3 | maskPattern;
        bits.appendBits(typeInfo, 5);
        int bchCode = MatrixUtil.calculateBCHCode(typeInfo, 1335);
        bits.appendBits(bchCode, 10);
        BitArray maskBits = new BitArray();
        maskBits.appendBits(21522, 15);
        bits.xor(maskBits);
        if (bits.getSize() != 15) {
            throw new WriterException("should not happen but we got: " + bits.getSize());
        }
    }

    static void makeVersionInfoBits(Version version, BitArray bits) throws WriterException {
        bits.appendBits(version.getVersionNumber(), 6);
        int bchCode = MatrixUtil.calculateBCHCode(version.getVersionNumber(), 7973);
        bits.appendBits(bchCode, 12);
        if (bits.getSize() != 18) {
            throw new WriterException("should not happen but we got: " + bits.getSize());
        }
    }

    private static boolean isEmpty(int value) {
        return value == -1;
    }

    private static void embedTimingPatterns(ByteMatrix matrix) {
        for (int i = 8; i < matrix.getWidth() - 8; ++i) {
            int bit = (i + 1) % 2;
            if (MatrixUtil.isEmpty(matrix.get(i, 6))) {
                matrix.set(i, 6, bit);
            }
            if (!MatrixUtil.isEmpty(matrix.get(6, i))) continue;
            matrix.set(6, i, bit);
        }
    }

    private static void embedDarkDotAtLeftBottomCorner(ByteMatrix matrix) throws WriterException {
        if (matrix.get(8, matrix.getHeight() - 8) == 0) {
            throw new WriterException();
        }
        matrix.set(8, matrix.getHeight() - 8, 1);
    }

    private static void embedHorizontalSeparationPattern(int xStart, int yStart, ByteMatrix matrix) throws WriterException {
        for (int x = 0; x < 8; ++x) {
            if (!MatrixUtil.isEmpty(matrix.get(xStart + x, yStart))) {
                throw new WriterException();
            }
            matrix.set(xStart + x, yStart, 0);
        }
    }

    private static void embedVerticalSeparationPattern(int xStart, int yStart, ByteMatrix matrix) throws WriterException {
        for (int y = 0; y < 7; ++y) {
            if (!MatrixUtil.isEmpty(matrix.get(xStart, yStart + y))) {
                throw new WriterException();
            }
            matrix.set(xStart, yStart + y, 0);
        }
    }

    private static void embedPositionAdjustmentPattern(int xStart, int yStart, ByteMatrix matrix) {
        for (int y = 0; y < 5; ++y) {
            for (int x = 0; x < 5; ++x) {
                matrix.set(xStart + x, yStart + y, POSITION_ADJUSTMENT_PATTERN[y][x]);
            }
        }
    }

    private static void embedPositionDetectionPattern(int xStart, int yStart, ByteMatrix matrix) {
        for (int y = 0; y < 7; ++y) {
            for (int x = 0; x < 7; ++x) {
                matrix.set(xStart + x, yStart + y, POSITION_DETECTION_PATTERN[y][x]);
            }
        }
    }

    private static void embedPositionDetectionPatternsAndSeparators(ByteMatrix matrix) throws WriterException {
        int pdpWidth = POSITION_DETECTION_PATTERN[0].length;
        MatrixUtil.embedPositionDetectionPattern(0, 0, matrix);
        MatrixUtil.embedPositionDetectionPattern(matrix.getWidth() - pdpWidth, 0, matrix);
        MatrixUtil.embedPositionDetectionPattern(0, matrix.getWidth() - pdpWidth, matrix);
        int hspWidth = 8;
        MatrixUtil.embedHorizontalSeparationPattern(0, hspWidth - 1, matrix);
        MatrixUtil.embedHorizontalSeparationPattern(matrix.getWidth() - hspWidth, hspWidth - 1, matrix);
        MatrixUtil.embedHorizontalSeparationPattern(0, matrix.getWidth() - hspWidth, matrix);
        int vspSize = 7;
        MatrixUtil.embedVerticalSeparationPattern(vspSize, 0, matrix);
        MatrixUtil.embedVerticalSeparationPattern(matrix.getHeight() - vspSize - 1, 0, matrix);
        MatrixUtil.embedVerticalSeparationPattern(vspSize, matrix.getHeight() - vspSize, matrix);
    }

    private static void maybeEmbedPositionAdjustmentPatterns(Version version, ByteMatrix matrix) {
        if (version.getVersionNumber() < 2) {
            return;
        }
        int index = version.getVersionNumber() - 1;
        int[] coordinates = POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index];
        int numCoordinates = POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index].length;
        for (int i = 0; i < numCoordinates; ++i) {
            for (int j = 0; j < numCoordinates; ++j) {
                int y = coordinates[i];
                int x = coordinates[j];
                if (x == -1 || y == -1 || !MatrixUtil.isEmpty(matrix.get(x, y))) continue;
                MatrixUtil.embedPositionAdjustmentPattern(x - 2, y - 2, matrix);
            }
        }
    }
}

