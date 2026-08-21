/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.pdf417;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.Compaction;
import com.google.zxing.pdf417.encoder.Dimensions;
import com.google.zxing.pdf417.encoder.PDF417;
import java.nio.charset.Charset;
import java.util.Map;

public final class PDF417Writer
implements Writer {
    static final int WHITE_SPACE = 30;
    static final int DEFAULT_ERROR_CORRECTION_LEVEL = 2;

    @Override
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format != BarcodeFormat.PDF_417) {
            throw new IllegalArgumentException("Can only encode PDF_417, but got " + (Object)((Object)format));
        }
        PDF417 encoder = new PDF417();
        int margin = 30;
        int errorCorrectionLevel = 2;
        if (hints != null) {
            if (hints.containsKey((Object)EncodeHintType.PDF417_COMPACT)) {
                encoder.setCompact((Boolean)hints.get((Object)EncodeHintType.PDF417_COMPACT));
            }
            if (hints.containsKey((Object)EncodeHintType.PDF417_COMPACTION)) {
                encoder.setCompaction((Compaction)((Object)hints.get((Object)EncodeHintType.PDF417_COMPACTION)));
            }
            if (hints.containsKey((Object)EncodeHintType.PDF417_DIMENSIONS)) {
                Dimensions dimensions = (Dimensions)hints.get((Object)EncodeHintType.PDF417_DIMENSIONS);
                encoder.setDimensions(dimensions.getMaxCols(), dimensions.getMinCols(), dimensions.getMaxRows(), dimensions.getMinRows());
            }
            if (hints.containsKey((Object)EncodeHintType.MARGIN)) {
                margin = ((Number)hints.get((Object)EncodeHintType.MARGIN)).intValue();
            }
            if (hints.containsKey((Object)EncodeHintType.ERROR_CORRECTION)) {
                errorCorrectionLevel = ((Number)hints.get((Object)EncodeHintType.ERROR_CORRECTION)).intValue();
            }
            if (hints.containsKey((Object)EncodeHintType.CHARACTER_SET)) {
                String encoding = (String)hints.get((Object)EncodeHintType.CHARACTER_SET);
                encoder.setEncoding(Charset.forName(encoding));
            }
        }
        return PDF417Writer.bitMatrixFromEncoder(encoder, contents, errorCorrectionLevel, width, height, margin);
    }

    @Override
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
        return this.encode(contents, format, width, height, null);
    }

    private static BitMatrix bitMatrixFromEncoder(PDF417 encoder, String contents, int errorCorrectionLevel, int width, int height, int margin) throws WriterException {
        int scaleY;
        int scaleX;
        int scale;
        encoder.generateBarcodeLogic(contents, errorCorrectionLevel);
        int aspectRatio = 4;
        byte[][] originalScale = encoder.getBarcodeMatrix().getScaledMatrix(1, aspectRatio);
        boolean rotated = false;
        if (height > width ^ originalScale[0].length < originalScale.length) {
            originalScale = PDF417Writer.rotateArray(originalScale);
            rotated = true;
        }
        if ((scale = (scaleX = width / originalScale[0].length) < (scaleY = height / originalScale.length) ? scaleX : scaleY) > 1) {
            byte[][] scaledMatrix = encoder.getBarcodeMatrix().getScaledMatrix(scale, scale * aspectRatio);
            if (rotated) {
                scaledMatrix = PDF417Writer.rotateArray(scaledMatrix);
            }
            return PDF417Writer.bitMatrixFrombitArray(scaledMatrix, margin);
        }
        return PDF417Writer.bitMatrixFrombitArray(originalScale, margin);
    }

    private static BitMatrix bitMatrixFrombitArray(byte[][] input, int margin) {
        BitMatrix output = new BitMatrix(input[0].length + 2 * margin, input.length + 2 * margin);
        output.clear();
        int y = 0;
        int yOutput = output.getHeight() - margin - 1;
        while (y < input.length) {
            for (int x = 0; x < input[0].length; ++x) {
                if (input[y][x] != 1) continue;
                output.set(x + margin, yOutput);
            }
            ++y;
            --yOutput;
        }
        return output;
    }

    private static byte[][] rotateArray(byte[][] bitarray) {
        byte[][] temp = new byte[bitarray[0].length][bitarray.length];
        for (int ii = 0; ii < bitarray.length; ++ii) {
            int inverseii = bitarray.length - ii - 1;
            for (int jj = 0; jj < bitarray[0].length; ++jj) {
                temp[jj][inverseii] = bitarray[ii][jj];
            }
        }
        return temp;
    }
}

