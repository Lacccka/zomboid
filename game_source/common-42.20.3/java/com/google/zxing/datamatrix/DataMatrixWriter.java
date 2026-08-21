/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Dimension;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.DefaultPlacement;
import com.google.zxing.datamatrix.encoder.ErrorCorrection;
import com.google.zxing.datamatrix.encoder.HighLevelEncoder;
import com.google.zxing.datamatrix.encoder.SymbolInfo;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import java.util.Map;

public final class DataMatrixWriter
implements Writer {
    @Override
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) {
        return this.encode(contents, format, width, height, null);
    }

    @Override
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) {
        if (contents.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        }
        if (format != BarcodeFormat.DATA_MATRIX) {
            throw new IllegalArgumentException("Can only encode DATA_MATRIX, but got " + (Object)((Object)format));
        }
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Requested dimensions are too small: " + width + 'x' + height);
        }
        SymbolShapeHint shape = SymbolShapeHint.FORCE_NONE;
        Dimension minSize = null;
        Dimension maxSize = null;
        if (hints != null) {
            Dimension requestedMaxSize;
            Dimension requestedMinSize;
            SymbolShapeHint requestedShape = (SymbolShapeHint)((Object)hints.get((Object)EncodeHintType.DATA_MATRIX_SHAPE));
            if (requestedShape != null) {
                shape = requestedShape;
            }
            if ((requestedMinSize = (Dimension)hints.get((Object)EncodeHintType.MIN_SIZE)) != null) {
                minSize = requestedMinSize;
            }
            if ((requestedMaxSize = (Dimension)hints.get((Object)EncodeHintType.MAX_SIZE)) != null) {
                maxSize = requestedMaxSize;
            }
        }
        String encoded = HighLevelEncoder.encodeHighLevel(contents, shape, minSize, maxSize);
        SymbolInfo symbolInfo = SymbolInfo.lookup(encoded.length(), shape, minSize, maxSize, true);
        String codewords = ErrorCorrection.encodeECC200(encoded, symbolInfo);
        DefaultPlacement placement = new DefaultPlacement(codewords, symbolInfo.getSymbolDataWidth(), symbolInfo.getSymbolDataHeight());
        placement.place();
        return DataMatrixWriter.encodeLowLevel(placement, symbolInfo);
    }

    private static BitMatrix encodeLowLevel(DefaultPlacement placement, SymbolInfo symbolInfo) {
        int symbolWidth = symbolInfo.getSymbolDataWidth();
        int symbolHeight = symbolInfo.getSymbolDataHeight();
        ByteMatrix matrix = new ByteMatrix(symbolInfo.getSymbolWidth(), symbolInfo.getSymbolHeight());
        int matrixY = 0;
        for (int y = 0; y < symbolHeight; ++y) {
            int x;
            int matrixX;
            if (y % symbolInfo.matrixHeight == 0) {
                matrixX = 0;
                for (x = 0; x < symbolInfo.getSymbolWidth(); ++x) {
                    matrix.set(matrixX, matrixY, x % 2 == 0);
                    ++matrixX;
                }
                ++matrixY;
            }
            matrixX = 0;
            for (x = 0; x < symbolWidth; ++x) {
                if (x % symbolInfo.matrixWidth == 0) {
                    matrix.set(matrixX, matrixY, true);
                    ++matrixX;
                }
                matrix.set(matrixX, matrixY, placement.getBit(x, y));
                ++matrixX;
                if (x % symbolInfo.matrixWidth != symbolInfo.matrixWidth - 1) continue;
                matrix.set(matrixX, matrixY, y % 2 == 0);
                ++matrixX;
            }
            ++matrixY;
            if (y % symbolInfo.matrixHeight != symbolInfo.matrixHeight - 1) continue;
            matrixX = 0;
            for (x = 0; x < symbolInfo.getSymbolWidth(); ++x) {
                matrix.set(matrixX, matrixY, true);
                ++matrixX;
            }
            ++matrixY;
        }
        return DataMatrixWriter.convertByteMatrixToBitMatrix(matrix);
    }

    private static BitMatrix convertByteMatrixToBitMatrix(ByteMatrix matrix) {
        int matrixWidgth = matrix.getWidth();
        int matrixHeight = matrix.getHeight();
        BitMatrix output = new BitMatrix(matrixWidgth, matrixHeight);
        output.clear();
        for (int i = 0; i < matrixWidgth; ++i) {
            for (int j = 0; j < matrixHeight; ++j) {
                if (matrix.get(i, j) != 1) continue;
                output.set(i, j);
            }
        }
        return output;
    }
}

