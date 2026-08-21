/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.pdf417.decoder;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.pdf417.PDF417Common;
import com.google.zxing.pdf417.decoder.BarcodeMetadata;
import com.google.zxing.pdf417.decoder.BarcodeValue;
import com.google.zxing.pdf417.decoder.BoundingBox;
import com.google.zxing.pdf417.decoder.Codeword;
import com.google.zxing.pdf417.decoder.DecodedBitStreamParser;
import com.google.zxing.pdf417.decoder.DetectionResult;
import com.google.zxing.pdf417.decoder.DetectionResultColumn;
import com.google.zxing.pdf417.decoder.DetectionResultRowIndicatorColumn;
import com.google.zxing.pdf417.decoder.PDF417CodewordDecoder;
import com.google.zxing.pdf417.decoder.ec.ErrorCorrection;
import java.util.ArrayList;
import java.util.Formatter;

public final class PDF417ScanningDecoder {
    private static final int CODEWORD_SKEW_SIZE = 2;
    private static final int MAX_ERRORS = 3;
    private static final int MAX_EC_CODEWORDS = 512;
    private static final ErrorCorrection errorCorrection = new ErrorCorrection();

    private PDF417ScanningDecoder() {
    }

    public static DecoderResult decode(BitMatrix image, ResultPoint imageTopLeft, ResultPoint imageBottomLeft, ResultPoint imageTopRight, ResultPoint imageBottomRight, int minCodewordWidth, int maxCodewordWidth) throws NotFoundException, FormatException, ChecksumException {
        BoundingBox boundingBox = new BoundingBox(image, imageTopLeft, imageBottomLeft, imageTopRight, imageBottomRight);
        DetectionResultRowIndicatorColumn leftRowIndicatorColumn = null;
        DetectionResultRowIndicatorColumn rightRowIndicatorColumn = null;
        DetectionResult detectionResult = null;
        for (int i = 0; i < 2; ++i) {
            if (imageTopLeft != null) {
                leftRowIndicatorColumn = PDF417ScanningDecoder.getRowIndicatorColumn(image, boundingBox, imageTopLeft, true, minCodewordWidth, maxCodewordWidth);
            }
            if (imageTopRight != null) {
                rightRowIndicatorColumn = PDF417ScanningDecoder.getRowIndicatorColumn(image, boundingBox, imageTopRight, false, minCodewordWidth, maxCodewordWidth);
            }
            if ((detectionResult = PDF417ScanningDecoder.merge(leftRowIndicatorColumn, rightRowIndicatorColumn)) == null) {
                throw NotFoundException.getNotFoundInstance();
            }
            if (i != 0 || detectionResult.getBoundingBox() == null || detectionResult.getBoundingBox().getMinY() >= boundingBox.getMinY() && detectionResult.getBoundingBox().getMaxY() <= boundingBox.getMaxY()) {
                detectionResult.setBoundingBox(boundingBox);
                break;
            }
            boundingBox = detectionResult.getBoundingBox();
        }
        int maxBarcodeColumn = detectionResult.getBarcodeColumnCount() + 1;
        detectionResult.setDetectionResultColumn(0, leftRowIndicatorColumn);
        detectionResult.setDetectionResultColumn(maxBarcodeColumn, rightRowIndicatorColumn);
        boolean leftToRight = leftRowIndicatorColumn != null;
        for (int barcodeColumnCount = 1; barcodeColumnCount <= maxBarcodeColumn; ++barcodeColumnCount) {
            int startColumn;
            int barcodeColumn;
            int n = barcodeColumn = leftToRight ? barcodeColumnCount : maxBarcodeColumn - barcodeColumnCount;
            if (detectionResult.getDetectionResultColumn(barcodeColumn) != null) continue;
            DetectionResultColumn detectionResultColumn = barcodeColumn == 0 || barcodeColumn == maxBarcodeColumn ? new DetectionResultRowIndicatorColumn(boundingBox, barcodeColumn == 0) : new DetectionResultColumn(boundingBox);
            detectionResult.setDetectionResultColumn(barcodeColumn, detectionResultColumn);
            int previousStartColumn = startColumn = -1;
            for (int imageRow = boundingBox.getMinY(); imageRow <= boundingBox.getMaxY(); ++imageRow) {
                Codeword codeword;
                startColumn = PDF417ScanningDecoder.getStartColumn(detectionResult, barcodeColumn, imageRow, leftToRight);
                if (startColumn < 0 || startColumn > boundingBox.getMaxX()) {
                    if (previousStartColumn == -1) continue;
                    startColumn = previousStartColumn;
                }
                if ((codeword = PDF417ScanningDecoder.detectCodeword(image, boundingBox.getMinX(), boundingBox.getMaxX(), leftToRight, startColumn, imageRow, minCodewordWidth, maxCodewordWidth)) == null) continue;
                detectionResultColumn.setCodeword(imageRow, codeword);
                previousStartColumn = startColumn;
                minCodewordWidth = Math.min(minCodewordWidth, codeword.getWidth());
                maxCodewordWidth = Math.max(maxCodewordWidth, codeword.getWidth());
            }
        }
        return PDF417ScanningDecoder.createDecoderResult(detectionResult);
    }

    private static DetectionResult merge(DetectionResultRowIndicatorColumn leftRowIndicatorColumn, DetectionResultRowIndicatorColumn rightRowIndicatorColumn) throws NotFoundException, FormatException {
        if (leftRowIndicatorColumn == null && rightRowIndicatorColumn == null) {
            return null;
        }
        BarcodeMetadata barcodeMetadata = PDF417ScanningDecoder.getBarcodeMetadata(leftRowIndicatorColumn, rightRowIndicatorColumn);
        if (barcodeMetadata == null) {
            return null;
        }
        BoundingBox boundingBox = BoundingBox.merge(PDF417ScanningDecoder.adjustBoundingBox(leftRowIndicatorColumn), PDF417ScanningDecoder.adjustBoundingBox(rightRowIndicatorColumn));
        return new DetectionResult(barcodeMetadata, boundingBox);
    }

    private static BoundingBox adjustBoundingBox(DetectionResultRowIndicatorColumn rowIndicatorColumn) throws NotFoundException, FormatException {
        int row;
        if (rowIndicatorColumn == null) {
            return null;
        }
        int[] rowHeights = rowIndicatorColumn.getRowHeights();
        if (rowHeights == null) {
            return null;
        }
        int maxRowHeight = PDF417ScanningDecoder.getMax(rowHeights);
        int missingStartRows = 0;
        for (int rowHeight : rowHeights) {
            missingStartRows += maxRowHeight - rowHeight;
            if (rowHeight > 0) break;
        }
        Codeword[] codewords = rowIndicatorColumn.getCodewords();
        int row2 = 0;
        while (missingStartRows > 0 && codewords[row2] == null) {
            --missingStartRows;
            ++row2;
        }
        int missingEndRows = 0;
        for (row = rowHeights.length - 1; row >= 0; --row) {
            missingEndRows += maxRowHeight - rowHeights[row];
            if (rowHeights[row] > 0) break;
        }
        row = codewords.length - 1;
        while (missingEndRows > 0 && codewords[row] == null) {
            --missingEndRows;
            --row;
        }
        return rowIndicatorColumn.getBoundingBox().addMissingRows(missingStartRows, missingEndRows, rowIndicatorColumn.isLeft());
    }

    private static int getMax(int[] values2) {
        int maxValue = -1;
        for (int value : values2) {
            maxValue = Math.max(maxValue, value);
        }
        return maxValue;
    }

    private static BarcodeMetadata getBarcodeMetadata(DetectionResultRowIndicatorColumn leftRowIndicatorColumn, DetectionResultRowIndicatorColumn rightRowIndicatorColumn) {
        BarcodeMetadata rightBarcodeMetadata;
        BarcodeMetadata leftBarcodeMetadata;
        if (leftRowIndicatorColumn == null || (leftBarcodeMetadata = leftRowIndicatorColumn.getBarcodeMetadata()) == null) {
            return rightRowIndicatorColumn == null ? null : rightRowIndicatorColumn.getBarcodeMetadata();
        }
        if (rightRowIndicatorColumn == null || (rightBarcodeMetadata = rightRowIndicatorColumn.getBarcodeMetadata()) == null) {
            return leftBarcodeMetadata;
        }
        if (leftBarcodeMetadata.getColumnCount() != rightBarcodeMetadata.getColumnCount() && leftBarcodeMetadata.getErrorCorrectionLevel() != rightBarcodeMetadata.getErrorCorrectionLevel() && leftBarcodeMetadata.getRowCount() != rightBarcodeMetadata.getRowCount()) {
            return null;
        }
        return leftBarcodeMetadata;
    }

    private static DetectionResultRowIndicatorColumn getRowIndicatorColumn(BitMatrix image, BoundingBox boundingBox, ResultPoint startPoint, boolean leftToRight, int minCodewordWidth, int maxCodewordWidth) {
        DetectionResultRowIndicatorColumn rowIndicatorColumn = new DetectionResultRowIndicatorColumn(boundingBox, leftToRight);
        for (int i = 0; i < 2; ++i) {
            int increment = i == 0 ? 1 : -1;
            int startColumn = (int)startPoint.getX();
            for (int imageRow = (int)startPoint.getY(); imageRow <= boundingBox.getMaxY() && imageRow >= boundingBox.getMinY(); imageRow += increment) {
                Codeword codeword = PDF417ScanningDecoder.detectCodeword(image, 0, image.getWidth(), leftToRight, startColumn, imageRow, minCodewordWidth, maxCodewordWidth);
                if (codeword == null) continue;
                rowIndicatorColumn.setCodeword(imageRow, codeword);
                startColumn = leftToRight ? codeword.getStartX() : codeword.getEndX();
            }
        }
        return rowIndicatorColumn;
    }

    private static void adjustCodewordCount(DetectionResult detectionResult, BarcodeValue[][] barcodeMatrix) throws NotFoundException {
        int[] numberOfCodewords = barcodeMatrix[0][1].getValue();
        int calculatedNumberOfCodewords = detectionResult.getBarcodeColumnCount() * detectionResult.getBarcodeRowCount() - PDF417ScanningDecoder.getNumberOfECCodeWords(detectionResult.getBarcodeECLevel());
        if (numberOfCodewords.length == 0) {
            if (calculatedNumberOfCodewords < 1 || calculatedNumberOfCodewords > 928) {
                throw NotFoundException.getNotFoundInstance();
            }
            barcodeMatrix[0][1].setValue(calculatedNumberOfCodewords);
        } else if (numberOfCodewords[0] != calculatedNumberOfCodewords) {
            barcodeMatrix[0][1].setValue(calculatedNumberOfCodewords);
        }
    }

    private static DecoderResult createDecoderResult(DetectionResult detectionResult) throws FormatException, ChecksumException, NotFoundException {
        BarcodeValue[][] barcodeMatrix = PDF417ScanningDecoder.createBarcodeMatrix(detectionResult);
        PDF417ScanningDecoder.adjustCodewordCount(detectionResult, barcodeMatrix);
        ArrayList<Integer> erasures = new ArrayList<Integer>();
        int[] codewords = new int[detectionResult.getBarcodeRowCount() * detectionResult.getBarcodeColumnCount()];
        ArrayList<int[]> ambiguousIndexValuesList = new ArrayList<int[]>();
        ArrayList<Integer> ambiguousIndexesList = new ArrayList<Integer>();
        for (int row = 0; row < detectionResult.getBarcodeRowCount(); ++row) {
            for (int column = 0; column < detectionResult.getBarcodeColumnCount(); ++column) {
                int[] values2 = barcodeMatrix[row][column + 1].getValue();
                int codewordIndex = row * detectionResult.getBarcodeColumnCount() + column;
                if (values2.length == 0) {
                    erasures.add(codewordIndex);
                    continue;
                }
                if (values2.length == 1) {
                    codewords[codewordIndex] = values2[0];
                    continue;
                }
                ambiguousIndexesList.add(codewordIndex);
                ambiguousIndexValuesList.add(values2);
            }
        }
        int[][] ambiguousIndexValues = new int[ambiguousIndexValuesList.size()][];
        for (int i = 0; i < ambiguousIndexValues.length; ++i) {
            ambiguousIndexValues[i] = (int[])ambiguousIndexValuesList.get(i);
        }
        return PDF417ScanningDecoder.createDecoderResultFromAmbiguousValues(detectionResult.getBarcodeECLevel(), codewords, PDF417Common.toIntArray(erasures), PDF417Common.toIntArray(ambiguousIndexesList), ambiguousIndexValues);
    }

    private static DecoderResult createDecoderResultFromAmbiguousValues(int ecLevel, int[] codewords, int[] erasureArray, int[] ambiguousIndexes, int[][] ambiguousIndexValues) throws FormatException, ChecksumException {
        int[] ambiguousIndexCount = new int[ambiguousIndexes.length];
        int tries = 100;
        block2: while (tries-- > 0) {
            int i2;
            for (i2 = 0; i2 < ambiguousIndexCount.length; ++i2) {
                codewords[ambiguousIndexes[i2]] = ambiguousIndexValues[i2][ambiguousIndexCount[i2]];
            }
            try {
                return PDF417ScanningDecoder.decodeCodewords(codewords, ecLevel, erasureArray);
            }
            catch (ChecksumException i2) {
                if (ambiguousIndexCount.length == 0) {
                    throw ChecksumException.getChecksumInstance();
                }
                for (i2 = 0; i2 < ambiguousIndexCount.length; ++i2) {
                    if (ambiguousIndexCount[i2] < ambiguousIndexValues[i2].length - 1) {
                        int n = i2;
                        ambiguousIndexCount[n] = ambiguousIndexCount[n] + 1;
                        continue block2;
                    }
                    ambiguousIndexCount[i2] = 0;
                    if (i2 != ambiguousIndexCount.length - 1) continue;
                    throw ChecksumException.getChecksumInstance();
                }
            }
        }
        throw ChecksumException.getChecksumInstance();
    }

    private static BarcodeValue[][] createBarcodeMatrix(DetectionResult detectionResult) throws FormatException {
        BarcodeValue[][] barcodeMatrix = new BarcodeValue[detectionResult.getBarcodeRowCount()][detectionResult.getBarcodeColumnCount() + 2];
        for (int row = 0; row < barcodeMatrix.length; ++row) {
            for (int column = 0; column < barcodeMatrix[row].length; ++column) {
                barcodeMatrix[row][column] = new BarcodeValue();
            }
        }
        int column = 0;
        for (DetectionResultColumn detectionResultColumn : detectionResult.getDetectionResultColumns()) {
            if (detectionResultColumn != null) {
                for (Codeword codeword : detectionResultColumn.getCodewords()) {
                    int rowNumber;
                    if (codeword == null || (rowNumber = codeword.getRowNumber()) < 0) continue;
                    if (rowNumber >= barcodeMatrix.length) {
                        throw FormatException.getFormatInstance();
                    }
                    barcodeMatrix[rowNumber][column].setValue(codeword.getValue());
                }
            }
            ++column;
        }
        return barcodeMatrix;
    }

    private static boolean isValidBarcodeColumn(DetectionResult detectionResult, int barcodeColumn) {
        return barcodeColumn >= 0 && barcodeColumn <= detectionResult.getBarcodeColumnCount() + 1;
    }

    private static int getStartColumn(DetectionResult detectionResult, int barcodeColumn, int imageRow, boolean leftToRight) {
        int offset = leftToRight ? 1 : -1;
        Codeword codeword = null;
        if (PDF417ScanningDecoder.isValidBarcodeColumn(detectionResult, barcodeColumn - offset)) {
            codeword = detectionResult.getDetectionResultColumn(barcodeColumn - offset).getCodeword(imageRow);
        }
        if (codeword != null) {
            return leftToRight ? codeword.getEndX() : codeword.getStartX();
        }
        codeword = detectionResult.getDetectionResultColumn(barcodeColumn).getCodewordNearby(imageRow);
        if (codeword != null) {
            return leftToRight ? codeword.getStartX() : codeword.getEndX();
        }
        if (PDF417ScanningDecoder.isValidBarcodeColumn(detectionResult, barcodeColumn - offset)) {
            codeword = detectionResult.getDetectionResultColumn(barcodeColumn - offset).getCodewordNearby(imageRow);
        }
        if (codeword != null) {
            return leftToRight ? codeword.getEndX() : codeword.getStartX();
        }
        int skippedColumns = 0;
        while (PDF417ScanningDecoder.isValidBarcodeColumn(detectionResult, barcodeColumn - offset)) {
            for (Codeword previousRowCodeword : detectionResult.getDetectionResultColumn(barcodeColumn -= offset).getCodewords()) {
                if (previousRowCodeword == null) continue;
                return (leftToRight ? previousRowCodeword.getEndX() : previousRowCodeword.getStartX()) + offset * skippedColumns * (previousRowCodeword.getEndX() - previousRowCodeword.getStartX());
            }
            ++skippedColumns;
        }
        return leftToRight ? detectionResult.getBoundingBox().getMinX() : detectionResult.getBoundingBox().getMaxX();
    }

    private static Codeword detectCodeword(BitMatrix image, int minColumn, int maxColumn, boolean leftToRight, int startColumn, int imageRow, int minCodewordWidth, int maxCodewordWidth) {
        int endColumn;
        int[] moduleBitCount = PDF417ScanningDecoder.getModuleBitCount(image, minColumn, maxColumn, leftToRight, startColumn = PDF417ScanningDecoder.adjustCodewordStartColumn(image, minColumn, maxColumn, leftToRight, startColumn, imageRow), imageRow);
        if (moduleBitCount == null) {
            return null;
        }
        int codewordBitCount = PDF417Common.getBitCountSum(moduleBitCount);
        if (leftToRight) {
            endColumn = startColumn + codewordBitCount;
        } else {
            for (int i = 0; i < moduleBitCount.length / 2; ++i) {
                int tmpCount = moduleBitCount[i];
                moduleBitCount[i] = moduleBitCount[moduleBitCount.length - 1 - i];
                moduleBitCount[moduleBitCount.length - 1 - i] = tmpCount;
            }
            endColumn = startColumn;
            startColumn = endColumn - codewordBitCount;
        }
        if (!PDF417ScanningDecoder.checkCodewordSkew(codewordBitCount, minCodewordWidth, maxCodewordWidth)) {
            return null;
        }
        int decodedValue = PDF417CodewordDecoder.getDecodedValue(moduleBitCount);
        int codeword = PDF417Common.getCodeword(decodedValue);
        if (codeword == -1) {
            return null;
        }
        return new Codeword(startColumn, endColumn, PDF417ScanningDecoder.getCodewordBucketNumber(decodedValue), codeword);
    }

    private static int[] getModuleBitCount(BitMatrix image, int minColumn, int maxColumn, boolean leftToRight, int startColumn, int imageRow) {
        int imageColumn = startColumn;
        int[] moduleBitCount = new int[8];
        int moduleNumber = 0;
        int increment = leftToRight ? 1 : -1;
        boolean previousPixelValue = leftToRight;
        while ((leftToRight && imageColumn < maxColumn || !leftToRight && imageColumn >= minColumn) && moduleNumber < moduleBitCount.length) {
            if (image.get(imageColumn, imageRow) == previousPixelValue) {
                int n = moduleNumber;
                moduleBitCount[n] = moduleBitCount[n] + 1;
                imageColumn += increment;
                continue;
            }
            ++moduleNumber;
            previousPixelValue = !previousPixelValue;
        }
        if (moduleNumber == moduleBitCount.length || (leftToRight && imageColumn == maxColumn || !leftToRight && imageColumn == minColumn) && moduleNumber == moduleBitCount.length - 1) {
            return moduleBitCount;
        }
        return null;
    }

    private static int getNumberOfECCodeWords(int barcodeECLevel) {
        return 2 << barcodeECLevel;
    }

    private static int adjustCodewordStartColumn(BitMatrix image, int minColumn, int maxColumn, boolean leftToRight, int codewordStartColumn, int imageRow) {
        int correctedStartColumn = codewordStartColumn;
        int increment = leftToRight ? -1 : 1;
        for (int i = 0; i < 2; ++i) {
            while ((leftToRight && correctedStartColumn >= minColumn || !leftToRight && correctedStartColumn < maxColumn) && leftToRight == image.get(correctedStartColumn, imageRow)) {
                if (Math.abs(codewordStartColumn - correctedStartColumn) > 2) {
                    return codewordStartColumn;
                }
                correctedStartColumn += increment;
            }
            increment = -increment;
            leftToRight = !leftToRight;
        }
        return correctedStartColumn;
    }

    private static boolean checkCodewordSkew(int codewordSize, int minCodewordWidth, int maxCodewordWidth) {
        return minCodewordWidth - 2 <= codewordSize && codewordSize <= maxCodewordWidth + 2;
    }

    private static DecoderResult decodeCodewords(int[] codewords, int ecLevel, int[] erasures) throws FormatException, ChecksumException {
        if (codewords.length == 0) {
            throw FormatException.getFormatInstance();
        }
        int numECCodewords = 1 << ecLevel + 1;
        int correctedErrorsCount = PDF417ScanningDecoder.correctErrors(codewords, erasures, numECCodewords);
        PDF417ScanningDecoder.verifyCodewordCount(codewords, numECCodewords);
        DecoderResult decoderResult = DecodedBitStreamParser.decode(codewords, String.valueOf(ecLevel));
        decoderResult.setErrorsCorrected(correctedErrorsCount);
        decoderResult.setErasures(erasures.length);
        return decoderResult;
    }

    private static int correctErrors(int[] codewords, int[] erasures, int numECCodewords) throws ChecksumException {
        if (erasures != null && erasures.length > numECCodewords / 2 + 3 || numECCodewords < 0 || numECCodewords > 512) {
            throw ChecksumException.getChecksumInstance();
        }
        return errorCorrection.decode(codewords, numECCodewords, erasures);
    }

    private static void verifyCodewordCount(int[] codewords, int numECCodewords) throws FormatException {
        if (codewords.length < 4) {
            throw FormatException.getFormatInstance();
        }
        int numberOfCodewords = codewords[0];
        if (numberOfCodewords > codewords.length) {
            throw FormatException.getFormatInstance();
        }
        if (numberOfCodewords == 0) {
            if (numECCodewords < codewords.length) {
                codewords[0] = codewords.length - numECCodewords;
            } else {
                throw FormatException.getFormatInstance();
            }
        }
    }

    private static int[] getBitCountForCodeword(int codeword) {
        int[] result = new int[8];
        int previousValue = 0;
        int i = result.length - 1;
        while (true) {
            if ((codeword & 1) != previousValue) {
                previousValue = codeword & 1;
                if (--i < 0) break;
            }
            int n = i;
            result[n] = result[n] + 1;
            codeword >>= 1;
        }
        return result;
    }

    private static int getCodewordBucketNumber(int codeword) {
        return PDF417ScanningDecoder.getCodewordBucketNumber(PDF417ScanningDecoder.getBitCountForCodeword(codeword));
    }

    private static int getCodewordBucketNumber(int[] moduleBitCount) {
        return (moduleBitCount[0] - moduleBitCount[2] + moduleBitCount[4] - moduleBitCount[6] + 9) % 9;
    }

    public static String toString(BarcodeValue[][] barcodeMatrix) {
        Formatter formatter = new Formatter();
        for (int row = 0; row < barcodeMatrix.length; ++row) {
            formatter.format("Row %2d: ", row);
            for (int column = 0; column < barcodeMatrix[row].length; ++column) {
                BarcodeValue barcodeValue = barcodeMatrix[row][column];
                if (barcodeValue.getValue().length == 0) {
                    formatter.format("        ", (Object[])null);
                    continue;
                }
                formatter.format("%4d(%2d)", barcodeValue.getValue()[0], barcodeValue.getConfidence(barcodeValue.getValue()[0]));
            }
            formatter.format("%n", new Object[0]);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}

