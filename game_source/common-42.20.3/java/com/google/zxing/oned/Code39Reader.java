/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.OneDReader;
import java.util.Arrays;
import java.util.Map;

public final class Code39Reader
extends OneDReader {
    static final String ALPHABET_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%";
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".toCharArray();
    static final int[] CHARACTER_ENCODINGS = new int[]{52, 289, 97, 352, 49, 304, 112, 37, 292, 100, 265, 73, 328, 25, 280, 88, 13, 268, 76, 28, 259, 67, 322, 19, 274, 82, 7, 262, 70, 22, 385, 193, 448, 145, 400, 208, 133, 388, 196, 148, 168, 162, 138, 42};
    private static final int ASTERISK_ENCODING = CHARACTER_ENCODINGS[39];
    private final boolean usingCheckDigit;
    private final boolean extendedMode;
    private final StringBuilder decodeRowResult;
    private final int[] counters;

    public Code39Reader() {
        this(false);
    }

    public Code39Reader(boolean usingCheckDigit) {
        this(usingCheckDigit, false);
    }

    public Code39Reader(boolean usingCheckDigit, boolean extendedMode) {
        this.usingCheckDigit = usingCheckDigit;
        this.extendedMode = extendedMode;
        this.decodeRowResult = new StringBuilder(20);
        this.counters = new int[9];
    }

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        int lastStart;
        char decodedChar;
        int[] theCounters = this.counters;
        Arrays.fill(theCounters, 0);
        StringBuilder result = this.decodeRowResult;
        result.setLength(0);
        int[] start = Code39Reader.findAsteriskPattern(row, theCounters);
        int nextStart = row.getNextSet(start[1]);
        int end = row.getSize();
        do {
            Code39Reader.recordPattern(row, nextStart, theCounters);
            int pattern = Code39Reader.toNarrowWidePattern(theCounters);
            if (pattern < 0) {
                throw NotFoundException.getNotFoundInstance();
            }
            decodedChar = Code39Reader.patternToChar(pattern);
            result.append(decodedChar);
            lastStart = nextStart;
            for (int counter : theCounters) {
                nextStart += counter;
            }
            nextStart = row.getNextSet(nextStart);
        } while (decodedChar != '*');
        result.setLength(result.length() - 1);
        int lastPatternSize = 0;
        for (int counter : theCounters) {
            lastPatternSize += counter;
        }
        int whiteSpaceAfterEnd = nextStart - lastStart - lastPatternSize;
        if (nextStart != end && whiteSpaceAfterEnd * 2 < lastPatternSize) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (this.usingCheckDigit) {
            int max = result.length() - 1;
            int total = 0;
            for (int i = 0; i < max; ++i) {
                total += ALPHABET_STRING.indexOf(this.decodeRowResult.charAt(i));
            }
            if (result.charAt(max) != ALPHABET[total % 43]) {
                throw ChecksumException.getChecksumInstance();
            }
            result.setLength(max);
        }
        if (result.length() == 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        String resultString = this.extendedMode ? Code39Reader.decodeExtended(result) : result.toString();
        float left = (float)(start[1] + start[0]) / 2.0f;
        float right = (float)lastStart + (float)lastPatternSize / 2.0f;
        return new Result(resultString, null, new ResultPoint[]{new ResultPoint(left, rowNumber), new ResultPoint(right, rowNumber)}, BarcodeFormat.CODE_39);
    }

    private static int[] findAsteriskPattern(BitArray row, int[] counters) throws NotFoundException {
        int width = row.getSize();
        int rowOffset = row.getNextSet(0);
        int counterPosition = 0;
        int patternStart = rowOffset;
        boolean isWhite = false;
        int patternLength = counters.length;
        for (int i = rowOffset; i < width; ++i) {
            if (row.get(i) ^ isWhite) {
                int n = counterPosition;
                counters[n] = counters[n] + 1;
                continue;
            }
            if (counterPosition == patternLength - 1) {
                if (Code39Reader.toNarrowWidePattern(counters) == ASTERISK_ENCODING && row.isRange(Math.max(0, patternStart - (i - patternStart) / 2), patternStart, false)) {
                    return new int[]{patternStart, i};
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

    private static int toNarrowWidePattern(int[] counters) {
        int wideCounters;
        int numCounters = counters.length;
        int maxNarrowCounter = 0;
        do {
            int i;
            int counter2;
            int minCounter = Integer.MAX_VALUE;
            for (int counter2 : counters) {
                if (counter2 >= minCounter || counter2 <= maxNarrowCounter) continue;
                minCounter = counter2;
            }
            maxNarrowCounter = minCounter;
            wideCounters = 0;
            int totalWideCountersWidth = 0;
            int pattern = 0;
            for (i = 0; i < numCounters; ++i) {
                counter2 = counters[i];
                if (counter2 <= maxNarrowCounter) continue;
                pattern |= 1 << numCounters - 1 - i;
                ++wideCounters;
                totalWideCountersWidth += counter2;
            }
            if (wideCounters != 3) continue;
            for (i = 0; i < numCounters && wideCounters > 0; ++i) {
                counter2 = counters[i];
                if (counter2 <= maxNarrowCounter) continue;
                --wideCounters;
                if (counter2 * 2 < totalWideCountersWidth) continue;
                return -1;
            }
            return pattern;
        } while (wideCounters > 3);
        return -1;
    }

    private static char patternToChar(int pattern) throws NotFoundException {
        for (int i = 0; i < CHARACTER_ENCODINGS.length; ++i) {
            if (CHARACTER_ENCODINGS[i] != pattern) continue;
            return ALPHABET[i];
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static String decodeExtended(CharSequence encoded) throws FormatException {
        int length = encoded.length();
        StringBuilder decoded = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            char c = encoded.charAt(i);
            if (c == '+' || c == '$' || c == '%' || c == '/') {
                char next = encoded.charAt(i + 1);
                int decodedChar = 0;
                switch (c) {
                    case '+': {
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char)(next + 32);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    case '$': {
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char)(next - 64);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    case '%': {
                        if (next >= 'A' && next <= 'E') {
                            decodedChar = (char)(next - 38);
                            break;
                        }
                        if (next >= 'F' && next <= 'W') {
                            decodedChar = (char)(next - 11);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    case '/': {
                        if (next >= 'A' && next <= 'O') {
                            decodedChar = (char)(next - 32);
                            break;
                        }
                        if (next == 'Z') {
                            decodedChar = 58;
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    }
                }
                decoded.append((char)decodedChar);
                ++i;
                continue;
            }
            decoded.append(c);
        }
        return decoded.toString();
    }
}

