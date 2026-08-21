/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.OneDReader;
import java.util.Map;

public final class ITFReader
extends OneDReader {
    private static final float MAX_AVG_VARIANCE = 0.38f;
    private static final float MAX_INDIVIDUAL_VARIANCE = 0.78f;
    private static final int W = 3;
    private static final int N = 1;
    private static final int[] DEFAULT_ALLOWED_LENGTHS = new int[]{6, 8, 10, 12, 14};
    private int narrowLineWidth = -1;
    private static final int[] START_PATTERN = new int[]{1, 1, 1, 1};
    private static final int[] END_PATTERN_REVERSED = new int[]{1, 1, 3};
    static final int[][] PATTERNS = new int[][]{{1, 1, 3, 3, 1}, {3, 1, 1, 1, 3}, {1, 3, 1, 1, 3}, {3, 3, 1, 1, 1}, {1, 1, 3, 1, 3}, {3, 1, 3, 1, 1}, {1, 3, 3, 1, 1}, {1, 1, 1, 3, 3}, {3, 1, 1, 3, 1}, {1, 3, 1, 3, 1}};

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws FormatException, NotFoundException {
        int[] startRange = this.decodeStart(row);
        int[] endRange = this.decodeEnd(row);
        StringBuilder result = new StringBuilder(20);
        ITFReader.decodeMiddle(row, startRange[1], endRange[0], result);
        String resultString = result.toString();
        int[] allowedLengths = null;
        if (hints != null) {
            allowedLengths = (int[])hints.get((Object)DecodeHintType.ALLOWED_LENGTHS);
        }
        if (allowedLengths == null) {
            allowedLengths = DEFAULT_ALLOWED_LENGTHS;
        }
        int length = resultString.length();
        boolean lengthOK = false;
        int maxAllowedLength = 0;
        for (int allowedLength : allowedLengths) {
            if (length == allowedLength) {
                lengthOK = true;
                break;
            }
            if (allowedLength <= maxAllowedLength) continue;
            maxAllowedLength = allowedLength;
        }
        if (!lengthOK && length > maxAllowedLength) {
            lengthOK = true;
        }
        if (!lengthOK) {
            throw FormatException.getFormatInstance();
        }
        return new Result(resultString, null, new ResultPoint[]{new ResultPoint(startRange[1], rowNumber), new ResultPoint(endRange[0], rowNumber)}, BarcodeFormat.ITF);
    }

    private static void decodeMiddle(BitArray row, int payloadStart, int payloadEnd, StringBuilder resultString) throws NotFoundException {
        int[] counterDigitPair = new int[10];
        int[] counterBlack = new int[5];
        int[] counterWhite = new int[5];
        while (payloadStart < payloadEnd) {
            ITFReader.recordPattern(row, payloadStart, counterDigitPair);
            for (int k = 0; k < 5; ++k) {
                int twoK = 2 * k;
                counterBlack[k] = counterDigitPair[twoK];
                counterWhite[k] = counterDigitPair[twoK + 1];
            }
            int bestMatch = ITFReader.decodeDigit(counterBlack);
            resultString.append((char)(48 + bestMatch));
            bestMatch = ITFReader.decodeDigit(counterWhite);
            resultString.append((char)(48 + bestMatch));
            for (int counterDigit : counterDigitPair) {
                payloadStart += counterDigit;
            }
        }
    }

    int[] decodeStart(BitArray row) throws NotFoundException {
        int endStart = ITFReader.skipWhiteSpace(row);
        int[] startPattern = ITFReader.findGuardPattern(row, endStart, START_PATTERN);
        this.narrowLineWidth = (startPattern[1] - startPattern[0]) / 4;
        this.validateQuietZone(row, startPattern[0]);
        return startPattern;
    }

    private void validateQuietZone(BitArray row, int startPattern) throws NotFoundException {
        int quietCount = this.narrowLineWidth * 10;
        quietCount = quietCount < startPattern ? quietCount : startPattern;
        for (int i = startPattern - 1; quietCount > 0 && i >= 0 && !row.get(i); --quietCount, --i) {
        }
        if (quietCount != 0) {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private static int skipWhiteSpace(BitArray row) throws NotFoundException {
        int width = row.getSize();
        int endStart = row.getNextSet(0);
        if (endStart == width) {
            throw NotFoundException.getNotFoundInstance();
        }
        return endStart;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    int[] decodeEnd(BitArray row) throws NotFoundException {
        row.reverse();
        try {
            int endStart = ITFReader.skipWhiteSpace(row);
            int[] endPattern = ITFReader.findGuardPattern(row, endStart, END_PATTERN_REVERSED);
            this.validateQuietZone(row, endPattern[0]);
            int temp = endPattern[0];
            endPattern[0] = row.getSize() - endPattern[1];
            endPattern[1] = row.getSize() - temp;
            int[] nArray = endPattern;
            return nArray;
        }
        finally {
            row.reverse();
        }
    }

    private static int[] findGuardPattern(BitArray row, int rowOffset, int[] pattern) throws NotFoundException {
        int patternLength = pattern.length;
        int[] counters = new int[patternLength];
        int width = row.getSize();
        boolean isWhite = false;
        int counterPosition = 0;
        int patternStart = rowOffset;
        for (int x = rowOffset; x < width; ++x) {
            if (row.get(x) ^ isWhite) {
                int n = counterPosition;
                counters[n] = counters[n] + 1;
                continue;
            }
            if (counterPosition == patternLength - 1) {
                if (ITFReader.patternMatchVariance(counters, pattern, 0.78f) < 0.38f) {
                    return new int[]{patternStart, x};
                }
                patternStart += counters[0] + counters[1];
                System.arraycopy(counters, 2, counters, 0, patternLength - 2);
                counters[patternLength - 2] = 0;
                counters[patternLength - 1] = 0;
                --counterPosition;
            } else {
                ++counterPosition;
            }
            counters[counterPosition] = 1;
            isWhite = !isWhite;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int decodeDigit(int[] counters) throws NotFoundException {
        float bestVariance = 0.38f;
        int bestMatch = -1;
        int max = PATTERNS.length;
        for (int i = 0; i < max; ++i) {
            int[] pattern = PATTERNS[i];
            float variance = ITFReader.patternMatchVariance(counters, pattern, 0.78f);
            if (!(variance < bestVariance)) continue;
            bestVariance = variance;
            bestMatch = i;
        }
        if (bestMatch >= 0) {
            return bestMatch;
        }
        throw NotFoundException.getNotFoundInstance();
    }
}

