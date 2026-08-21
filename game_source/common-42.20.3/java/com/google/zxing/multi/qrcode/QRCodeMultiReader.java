/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.multi.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.multi.qrcode.detector.MultiDetector;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.QRCodeDecoderMetaData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class QRCodeMultiReader
extends QRCodeReader
implements MultipleBarcodeReader {
    private static final Result[] EMPTY_RESULT_ARRAY = new Result[0];
    private static final ResultPoint[] NO_POINTS = new ResultPoint[0];

    @Override
    public Result[] decodeMultiple(BinaryBitmap image) throws NotFoundException {
        return this.decodeMultiple(image, null);
    }

    @Override
    public Result[] decodeMultiple(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        DetectorResult[] detectorResults;
        List<Result> results = new ArrayList<Result>();
        for (DetectorResult detectorResult : detectorResults = new MultiDetector(image.getBlackMatrix()).detectMulti(hints)) {
            try {
                String ecLevel;
                DecoderResult decoderResult = this.getDecoder().decode(detectorResult.getBits(), hints);
                ResultPoint[] points = detectorResult.getPoints();
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
                results.add(result);
            }
            catch (ReaderException readerException) {
                // empty catch block
            }
        }
        if (results.isEmpty()) {
            return EMPTY_RESULT_ARRAY;
        }
        results = QRCodeMultiReader.processStructuredAppend(results);
        return results.toArray(new Result[results.size()]);
    }

    private static List<Result> processStructuredAppend(List<Result> results) {
        boolean hasSA = false;
        for (Result result : results) {
            if (!result.getResultMetadata().containsKey((Object)ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)) continue;
            hasSA = true;
            break;
        }
        if (!hasSA) {
            return results;
        }
        ArrayList<Result> newResults = new ArrayList<Result>();
        ArrayList<Result> saResults = new ArrayList<Result>();
        for (Result result : results) {
            newResults.add(result);
            if (!result.getResultMetadata().containsKey((Object)ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)) continue;
            saResults.add(result);
        }
        Collections.sort(saResults, new SAComparator());
        StringBuilder concatedText = new StringBuilder();
        int rawBytesLen = 0;
        int byteSegmentLength = 0;
        for (Result saResult : saResults) {
            concatedText.append(saResult.getText());
            rawBytesLen += saResult.getRawBytes().length;
            if (!saResult.getResultMetadata().containsKey((Object)ResultMetadataType.BYTE_SEGMENTS)) continue;
            Iterable byteSegments = (Iterable)saResult.getResultMetadata().get((Object)ResultMetadataType.BYTE_SEGMENTS);
            Iterator iterator2 = byteSegments.iterator();
            while (iterator2.hasNext()) {
                Object segment = (byte[])iterator2.next();
                byteSegmentLength += ((Object)segment).length;
            }
        }
        byte[] newRawBytes = new byte[rawBytesLen];
        byte[] newByteSegment = new byte[byteSegmentLength];
        int newRawBytesIndex = 0;
        int byteSegmentIndex = 0;
        for (Result saResult : saResults) {
            System.arraycopy(saResult.getRawBytes(), 0, newRawBytes, newRawBytesIndex, saResult.getRawBytes().length);
            newRawBytesIndex += saResult.getRawBytes().length;
            if (!saResult.getResultMetadata().containsKey((Object)ResultMetadataType.BYTE_SEGMENTS)) continue;
            Iterable byteSegments = (Iterable)saResult.getResultMetadata().get((Object)ResultMetadataType.BYTE_SEGMENTS);
            for (byte[] segment : byteSegments) {
                System.arraycopy(segment, 0, newByteSegment, byteSegmentIndex, segment.length);
                byteSegmentIndex += segment.length;
            }
        }
        Result newResult = new Result(concatedText.toString(), newRawBytes, NO_POINTS, BarcodeFormat.QR_CODE);
        if (byteSegmentLength > 0) {
            ArrayList<byte[]> byteSegmentList = new ArrayList<byte[]>();
            byteSegmentList.add(newByteSegment);
            newResult.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegmentList);
        }
        newResults.add(newResult);
        return newResults;
    }

    private static final class SAComparator
    implements Comparator<Result>,
    Serializable {
        private SAComparator() {
        }

        @Override
        public int compare(Result a, Result b) {
            int bNumber;
            int aNumber = (Integer)a.getResultMetadata().get((Object)ResultMetadataType.STRUCTURED_APPEND_SEQUENCE);
            if (aNumber < (bNumber = ((Integer)b.getResultMetadata().get((Object)ResultMetadataType.STRUCTURED_APPEND_SEQUENCE)).intValue())) {
                return -1;
            }
            if (aNumber > bNumber) {
                return 1;
            }
            return 0;
        }
    }
}

