/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.qrcode.decoder.Decoder;
import com.google.zxing.qrcode.decoder.QRCodeDecoderMetaData;
import com.google.zxing.qrcode.detector.Detector;
import java.util.List;
import java.util.Map;

public class QRCodeReader
implements Reader {
    private static final ResultPoint[] NO_POINTS = new ResultPoint[0];
    private final Decoder decoder = new Decoder();

    protected final Decoder getDecoder() {
        return this.decoder;
    }

    @Override
    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return this.decode(image, null);
    }

    @Override
    public final Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        String ecLevel;
        ResultPoint[] points;
        DecoderResult decoderResult;
        if (hints != null && hints.containsKey((Object)DecodeHintType.PURE_BARCODE)) {
            BitMatrix bits = QRCodeReader.extractPureBits(image.getBlackMatrix());
            decoderResult = this.decoder.decode(bits, hints);
            points = NO_POINTS;
        } else {
            DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect(hints);
            decoderResult = this.decoder.decode(detectorResult.getBits(), hints);
            points = detectorResult.getPoints();
        }
        if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
            ((QRCodeDecoderMetaData)decoderResult.getOther()).applyMirroredCorrection(points);
        }
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE);
        List<byte[]> byteSegments = decoderResult.getByteSegments();
        if (byteSegments != null) {
            result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
        }
        if ((ecLevel = decoderResult.getECLevel()) != null) {
            result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
        }
        if (decoderResult.hasStructuredAppend()) {
            result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_SEQUENCE, decoderResult.getStructuredAppendSequenceNumber());
            result.putMetadata(ResultMetadataType.STRUCTURED_APPEND_PARITY, decoderResult.getStructuredAppendParity());
        }
        return result;
    }

    @Override
    public void reset() {
    }

    private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {
        int nudgedTooFarDown;
        int[] leftTopBlack = image.getTopLeftOnBit();
        int[] rightBottomBlack = image.getBottomRightOnBit();
        if (leftTopBlack == null || rightBottomBlack == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        float moduleSize = QRCodeReader.moduleSize(leftTopBlack, image);
        int top = leftTopBlack[1];
        int bottom = rightBottomBlack[1];
        int left = leftTopBlack[0];
        int right = rightBottomBlack[0];
        if (left >= right || top >= bottom) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (bottom - top != right - left) {
            right = left + (bottom - top);
        }
        int matrixWidth = Math.round((float)(right - left + 1) / moduleSize);
        int matrixHeight = Math.round((float)(bottom - top + 1) / moduleSize);
        if (matrixWidth <= 0 || matrixHeight <= 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (matrixHeight != matrixWidth) {
            throw NotFoundException.getNotFoundInstance();
        }
        int nudge = (int)(moduleSize / 2.0f);
        top += nudge;
        int nudgedTooFarRight = (left += nudge) + (int)((float)(matrixWidth - 1) * moduleSize) - right;
        if (nudgedTooFarRight > 0) {
            if (nudgedTooFarRight > nudge) {
                throw NotFoundException.getNotFoundInstance();
            }
            left -= nudgedTooFarRight;
        }
        if ((nudgedTooFarDown = top + (int)((float)(matrixHeight - 1) * moduleSize) - bottom) > 0) {
            if (nudgedTooFarDown > nudge) {
                throw NotFoundException.getNotFoundInstance();
            }
            top -= nudgedTooFarDown;
        }
        BitMatrix bits = new BitMatrix(matrixWidth, matrixHeight);
        for (int y = 0; y < matrixHeight; ++y) {
            int iOffset = top + (int)((float)y * moduleSize);
            for (int x = 0; x < matrixWidth; ++x) {
                if (!image.get(left + (int)((float)x * moduleSize), iOffset)) continue;
                bits.set(x, y);
            }
        }
        return bits;
    }

    private static float moduleSize(int[] leftTopBlack, BitMatrix image) throws NotFoundException {
        int y;
        int height = image.getHeight();
        int width = image.getWidth();
        int x = leftTopBlack[0];
        boolean inBlack = true;
        int transitions = 0;
        for (y = leftTopBlack[1]; x < width && y < height; ++x, ++y) {
            if (inBlack == image.get(x, y)) continue;
            if (++transitions == 5) break;
            inBlack = !inBlack;
        }
        if (x == width || y == height) {
            throw NotFoundException.getNotFoundInstance();
        }
        return (float)(x - leftTopBlack[0]) / 7.0f;
    }
}

