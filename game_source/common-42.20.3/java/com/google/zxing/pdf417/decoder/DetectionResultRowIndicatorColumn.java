/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.pdf417.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.ResultPoint;
import com.google.zxing.pdf417.decoder.BarcodeMetadata;
import com.google.zxing.pdf417.decoder.BarcodeValue;
import com.google.zxing.pdf417.decoder.BoundingBox;
import com.google.zxing.pdf417.decoder.Codeword;
import com.google.zxing.pdf417.decoder.DetectionResultColumn;

final class DetectionResultRowIndicatorColumn
extends DetectionResultColumn {
    private final boolean isLeft;

    DetectionResultRowIndicatorColumn(BoundingBox boundingBox, boolean isLeft) {
        super(boundingBox);
        this.isLeft = isLeft;
    }

    void setRowNumbers() {
        for (Codeword codeword : this.getCodewords()) {
            if (codeword == null) continue;
            codeword.setRowNumberAsRowIndicatorColumn();
        }
    }

    int adjustCompleteIndicatorColumnRowNumbers(BarcodeMetadata barcodeMetadata) {
        Codeword[] codewords = this.getCodewords();
        this.setRowNumbers();
        this.removeIncorrectCodewords(codewords, barcodeMetadata);
        BoundingBox boundingBox = this.getBoundingBox();
        ResultPoint top = this.isLeft ? boundingBox.getTopLeft() : boundingBox.getTopRight();
        ResultPoint bottom = this.isLeft ? boundingBox.getBottomLeft() : boundingBox.getBottomRight();
        int firstRow = this.imageRowToCodewordIndex((int)top.getY());
        int lastRow = this.imageRowToCodewordIndex((int)bottom.getY());
        float averageRowHeight = (float)(lastRow - firstRow) / (float)barcodeMetadata.getRowCount();
        int barcodeRow = -1;
        int maxRowHeight = 1;
        int currentRowHeight = 0;
        for (int codewordsRow = firstRow; codewordsRow < lastRow; ++codewordsRow) {
            if (codewords[codewordsRow] == null) continue;
            Codeword codeword = codewords[codewordsRow];
            int rowDifference = codeword.getRowNumber() - barcodeRow;
            if (rowDifference == 0) {
                ++currentRowHeight;
                continue;
            }
            if (rowDifference == 1) {
                maxRowHeight = Math.max(maxRowHeight, currentRowHeight);
                currentRowHeight = 1;
                barcodeRow = codeword.getRowNumber();
                continue;
            }
            if (rowDifference < 0 || codeword.getRowNumber() >= barcodeMetadata.getRowCount() || rowDifference > codewordsRow) {
                codewords[codewordsRow] = null;
                continue;
            }
            int checkedRows = maxRowHeight > 2 ? (maxRowHeight - 2) * rowDifference : rowDifference;
            boolean closePreviousCodewordFound = checkedRows >= codewordsRow;
            for (int i = 1; i <= checkedRows && !closePreviousCodewordFound; ++i) {
                closePreviousCodewordFound = codewords[codewordsRow - i] != null;
            }
            if (closePreviousCodewordFound) {
                codewords[codewordsRow] = null;
                continue;
            }
            barcodeRow = codeword.getRowNumber();
            currentRowHeight = 1;
        }
        return (int)((double)averageRowHeight + 0.5);
    }

    int[] getRowHeights() throws FormatException {
        BarcodeMetadata barcodeMetadata = this.getBarcodeMetadata();
        if (barcodeMetadata == null) {
            return null;
        }
        this.adjustIncompleteIndicatorColumnRowNumbers(barcodeMetadata);
        int[] result = new int[barcodeMetadata.getRowCount()];
        for (Codeword codeword : this.getCodewords()) {
            if (codeword == null) continue;
            int rowNumber = codeword.getRowNumber();
            if (rowNumber >= result.length) {
                throw FormatException.getFormatInstance();
            }
            int n = rowNumber;
            result[n] = result[n] + 1;
        }
        return result;
    }

    int adjustIncompleteIndicatorColumnRowNumbers(BarcodeMetadata barcodeMetadata) {
        BoundingBox boundingBox = this.getBoundingBox();
        ResultPoint top = this.isLeft ? boundingBox.getTopLeft() : boundingBox.getTopRight();
        ResultPoint bottom = this.isLeft ? boundingBox.getBottomLeft() : boundingBox.getBottomRight();
        int firstRow = this.imageRowToCodewordIndex((int)top.getY());
        int lastRow = this.imageRowToCodewordIndex((int)bottom.getY());
        float averageRowHeight = (float)(lastRow - firstRow) / (float)barcodeMetadata.getRowCount();
        Codeword[] codewords = this.getCodewords();
        int barcodeRow = -1;
        int maxRowHeight = 1;
        int currentRowHeight = 0;
        for (int codewordsRow = firstRow; codewordsRow < lastRow; ++codewordsRow) {
            if (codewords[codewordsRow] == null) continue;
            Codeword codeword = codewords[codewordsRow];
            codeword.setRowNumberAsRowIndicatorColumn();
            int rowDifference = codeword.getRowNumber() - barcodeRow;
            if (rowDifference == 0) {
                ++currentRowHeight;
                continue;
            }
            if (rowDifference == 1) {
                maxRowHeight = Math.max(maxRowHeight, currentRowHeight);
                currentRowHeight = 1;
                barcodeRow = codeword.getRowNumber();
                continue;
            }
            if (codeword.getRowNumber() >= barcodeMetadata.getRowCount()) {
                codewords[codewordsRow] = null;
                continue;
            }
            barcodeRow = codeword.getRowNumber();
            currentRowHeight = 1;
        }
        return (int)((double)averageRowHeight + 0.5);
    }

    BarcodeMetadata getBarcodeMetadata() {
        Codeword[] codewords = this.getCodewords();
        BarcodeValue barcodeColumnCount = new BarcodeValue();
        BarcodeValue barcodeRowCountUpperPart = new BarcodeValue();
        BarcodeValue barcodeRowCountLowerPart = new BarcodeValue();
        BarcodeValue barcodeECLevel = new BarcodeValue();
        block5: for (Codeword codeword : codewords) {
            if (codeword == null) continue;
            codeword.setRowNumberAsRowIndicatorColumn();
            int rowIndicatorValue = codeword.getValue() % 30;
            int codewordRowNumber = codeword.getRowNumber();
            if (!this.isLeft) {
                codewordRowNumber += 2;
            }
            switch (codewordRowNumber % 3) {
                case 0: {
                    barcodeRowCountUpperPart.setValue(rowIndicatorValue * 3 + 1);
                    continue block5;
                }
                case 1: {
                    barcodeECLevel.setValue(rowIndicatorValue / 3);
                    barcodeRowCountLowerPart.setValue(rowIndicatorValue % 3);
                    continue block5;
                }
                case 2: {
                    barcodeColumnCount.setValue(rowIndicatorValue + 1);
                }
            }
        }
        if (barcodeColumnCount.getValue().length == 0 || barcodeRowCountUpperPart.getValue().length == 0 || barcodeRowCountLowerPart.getValue().length == 0 || barcodeECLevel.getValue().length == 0 || barcodeColumnCount.getValue()[0] < 1 || barcodeRowCountUpperPart.getValue()[0] + barcodeRowCountLowerPart.getValue()[0] < 3 || barcodeRowCountUpperPart.getValue()[0] + barcodeRowCountLowerPart.getValue()[0] > 90) {
            return null;
        }
        BarcodeMetadata barcodeMetadata = new BarcodeMetadata(barcodeColumnCount.getValue()[0], barcodeRowCountUpperPart.getValue()[0], barcodeRowCountLowerPart.getValue()[0], barcodeECLevel.getValue()[0]);
        this.removeIncorrectCodewords(codewords, barcodeMetadata);
        return barcodeMetadata;
    }

    private void removeIncorrectCodewords(Codeword[] codewords, BarcodeMetadata barcodeMetadata) {
        block5: for (int codewordRow = 0; codewordRow < codewords.length; ++codewordRow) {
            Codeword codeword = codewords[codewordRow];
            if (codewords[codewordRow] == null) continue;
            int rowIndicatorValue = codeword.getValue() % 30;
            int codewordRowNumber = codeword.getRowNumber();
            if (codewordRowNumber > barcodeMetadata.getRowCount()) {
                codewords[codewordRow] = null;
                continue;
            }
            if (!this.isLeft) {
                codewordRowNumber += 2;
            }
            switch (codewordRowNumber % 3) {
                case 0: {
                    if (rowIndicatorValue * 3 + 1 == barcodeMetadata.getRowCountUpperPart()) continue block5;
                    codewords[codewordRow] = null;
                    continue block5;
                }
                case 1: {
                    if (rowIndicatorValue / 3 == barcodeMetadata.getErrorCorrectionLevel() && rowIndicatorValue % 3 == barcodeMetadata.getRowCountLowerPart()) continue block5;
                    codewords[codewordRow] = null;
                    continue block5;
                }
                case 2: {
                    if (rowIndicatorValue + 1 == barcodeMetadata.getColumnCount()) continue block5;
                    codewords[codewordRow] = null;
                }
            }
        }
    }

    boolean isLeft() {
        return this.isLeft;
    }

    @Override
    public String toString() {
        return "IsLeft: " + this.isLeft + '\n' + super.toString();
    }
}

