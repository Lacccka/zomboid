/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.pdf417.decoder;

import com.google.zxing.pdf417.decoder.BoundingBox;
import com.google.zxing.pdf417.decoder.Codeword;
import java.util.Formatter;

class DetectionResultColumn {
    private static final int MAX_NEARBY_DISTANCE = 5;
    private final BoundingBox boundingBox;
    private final Codeword[] codewords;

    DetectionResultColumn(BoundingBox boundingBox) {
        this.boundingBox = new BoundingBox(boundingBox);
        this.codewords = new Codeword[boundingBox.getMaxY() - boundingBox.getMinY() + 1];
    }

    final Codeword getCodewordNearby(int imageRow) {
        Codeword codeword = this.getCodeword(imageRow);
        if (codeword != null) {
            return codeword;
        }
        for (int i = 1; i < 5; ++i) {
            int nearImageRow = this.imageRowToCodewordIndex(imageRow) - i;
            if (nearImageRow >= 0 && (codeword = this.codewords[nearImageRow]) != null) {
                return codeword;
            }
            nearImageRow = this.imageRowToCodewordIndex(imageRow) + i;
            if (nearImageRow >= this.codewords.length || (codeword = this.codewords[nearImageRow]) == null) continue;
            return codeword;
        }
        return null;
    }

    final int imageRowToCodewordIndex(int imageRow) {
        return imageRow - this.boundingBox.getMinY();
    }

    final void setCodeword(int imageRow, Codeword codeword) {
        this.codewords[this.imageRowToCodewordIndex((int)imageRow)] = codeword;
    }

    final Codeword getCodeword(int imageRow) {
        return this.codewords[this.imageRowToCodewordIndex(imageRow)];
    }

    final BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    final Codeword[] getCodewords() {
        return this.codewords;
    }

    public String toString() {
        Formatter formatter = new Formatter();
        int row = 0;
        for (Codeword codeword : this.codewords) {
            if (codeword == null) {
                formatter.format("%3d:    |   %n", row++);
                continue;
            }
            formatter.format("%3d: %3d|%3d%n", row++, codeword.getRowNumber(), codeword.getValue());
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}

