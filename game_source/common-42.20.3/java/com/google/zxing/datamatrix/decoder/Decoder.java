/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.decoder;

import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.google.zxing.common.reedsolomon.ReedSolomonException;
import com.google.zxing.datamatrix.decoder.BitMatrixParser;
import com.google.zxing.datamatrix.decoder.DataBlock;
import com.google.zxing.datamatrix.decoder.DecodedBitStreamParser;
import com.google.zxing.datamatrix.decoder.Version;

public final class Decoder {
    private final ReedSolomonDecoder rsDecoder = new ReedSolomonDecoder(GenericGF.DATA_MATRIX_FIELD_256);

    public DecoderResult decode(boolean[][] image) throws FormatException, ChecksumException {
        int dimension = image.length;
        BitMatrix bits = new BitMatrix(dimension);
        for (int i = 0; i < dimension; ++i) {
            for (int j = 0; j < dimension; ++j) {
                if (!image[i][j]) continue;
                bits.set(j, i);
            }
        }
        return this.decode(bits);
    }

    public DecoderResult decode(BitMatrix bits) throws FormatException, ChecksumException {
        BitMatrixParser parser = new BitMatrixParser(bits);
        Version version = parser.getVersion();
        byte[] codewords = parser.readCodewords();
        DataBlock[] dataBlocks = DataBlock.getDataBlocks(codewords, version);
        int dataBlocksCount = dataBlocks.length;
        int totalBytes = 0;
        for (DataBlock db : dataBlocks) {
            totalBytes += db.getNumDataCodewords();
        }
        byte[] resultBytes = new byte[totalBytes];
        for (int j = 0; j < dataBlocksCount; ++j) {
            DataBlock dataBlock = dataBlocks[j];
            byte[] codewordBytes = dataBlock.getCodewords();
            int numDataCodewords = dataBlock.getNumDataCodewords();
            this.correctErrors(codewordBytes, numDataCodewords);
            for (int i = 0; i < numDataCodewords; ++i) {
                resultBytes[i * dataBlocksCount + j] = codewordBytes[i];
            }
        }
        return DecodedBitStreamParser.decode(resultBytes);
    }

    private void correctErrors(byte[] codewordBytes, int numDataCodewords) throws ChecksumException {
        int numCodewords = codewordBytes.length;
        int[] codewordsInts = new int[numCodewords];
        for (int i = 0; i < numCodewords; ++i) {
            codewordsInts[i] = codewordBytes[i] & 0xFF;
        }
        int numECCodewords = codewordBytes.length - numDataCodewords;
        try {
            this.rsDecoder.decode(codewordsInts, numECCodewords);
        }
        catch (ReedSolomonException ignored) {
            throw ChecksumException.getChecksumInstance();
        }
        for (int i = 0; i < numDataCodewords; ++i) {
            codewordBytes[i] = (byte)codewordsInts[i];
        }
    }
}

