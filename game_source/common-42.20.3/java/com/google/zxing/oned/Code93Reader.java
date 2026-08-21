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

public final class Code93Reader
extends OneDReader {
    private static final String ALPHABET_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*";
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*".toCharArray();
    private static final int[] CHARACTER_ENCODINGS = new int[]{276, 328, 324, 322, 296, 292, 290, 336, 274, 266, 424, 420, 418, 404, 402, 394, 360, 356, 354, 308, 282, 344, 332, 326, 300, 278, 436, 434, 428, 422, 406, 410, 364, 358, 310, 314, 302, 468, 466, 458, 366, 374, 430, 294, 474, 470, 306, 350};
    private static final int ASTERISK_ENCODING = CHARACTER_ENCODINGS[47];
    private final StringBuilder decodeRowResult = new StringBuilder(20);
    private final int[] counters = new int[6];

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        int lastStart;
        char decodedChar;
        int[] start = this.findAsteriskPattern(row);
        int nextStart = row.getNextSet(start[1]);
        int end = row.getSize();
        int[] theCounters = this.counters;
        Arrays.fill(theCounters, 0);
        StringBuilder result = this.decodeRowResult;
        result.setLength(0);
        do {
            Code93Reader.recordPattern(row, nextStart, theCounters);
            int pattern = Code93Reader.toPattern(theCounters);
            if (pattern < 0) {
                throw NotFoundException.getNotFoundInstance();
            }
            decodedChar = Code93Reader.patternToChar(pattern);
            result.append(decodedChar);
            lastStart = nextStart;
            for (int counter : theCounters) {
                nextStart += counter;
            }
            nextStart = row.getNextSet(nextStart);
        } while (decodedChar != '*');
        result.deleteCharAt(result.length() - 1);
        int lastPatternSize = 0;
        for (int counter : theCounters) {
            lastPatternSize += counter;
        }
        if (nextStart == end || !row.get(nextStart)) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (result.length() < 2) {
            throw NotFoundException.getNotFoundInstance();
        }
        Code93Reader.checkChecksums(result);
        result.setLength(result.length() - 2);
        String resultString = Code93Reader.decodeExtended(result);
        float left = (float)(start[1] + start[0]) / 2.0f;
        float right = (float)lastStart + (float)lastPatternSize / 2.0f;
        return new Result(resultString, null, new ResultPoint[]{new ResultPoint(left, rowNumber), new ResultPoint(right, rowNumber)}, BarcodeFormat.CODE_93);
    }

    private int[] findAsteriskPattern(BitArray row) throws NotFoundException {
        int width = row.getSize();
        int rowOffset = row.getNextSet(0);
        Arrays.fill(this.counters, 0);
        int[] theCounters = this.counters;
        int patternStart = rowOffset;
        boolean isWhite = false;
        int patternLength = theCounters.length;
        int counterPosition = 0;
        for (int i = rowOffset; i < width; ++i) {
            if (row.get(i) ^ isWhite) {
                int n = counterPosition;
                theCounters[n] = theCounters[n] + 1;
                continue;
            }
            if (counterPosition == patternLength - 1) {
                if (Code93Reader.toPattern(theCounters) == ASTERISK_ENCODING) {
                    return new int[]{patternStart, i};
                }
                patternStart += theCounters[0] + theCounters[1];
                System.arraycopy(theCounters, 2, theCounters, 0, patternLength - 2);
                theCounters[patternLength - 2] = 0;
                theCounters[patternLength - 1] = 0;
                --counterPosition;
            } else {
                ++counterPosition;
            }
            theCounters[counterPosition] = 1;
            isWhite = !isWhite;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int toPattern(int[] counters) {
        int max = counters.length;
        int sum = 0;
        for (int counter : counters) {
            sum += counter;
        }
        int pattern = 0;
        for (int i = 0; i < max; ++i) {
            int scaled = Math.round((float)counters[i] * 9.0f / (float)sum);
            if (scaled < 1 || scaled > 4) {
                return -1;
            }
            if ((i & 1) == 0) {
                for (int j = 0; j < scaled; ++j) {
                    pattern = pattern << 1 | 1;
                }
                continue;
            }
            pattern <<= scaled;
        }
        return pattern;
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
            if (c >= 'a' && c <= 'd') {
                if (i >= length - 1) {
                    throw FormatException.getFormatInstance();
                }
                char next = encoded.charAt(i + 1);
                int decodedChar = 0;
                switch (c) {
                    case 'd': {
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char)(next + 32);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    case 'a': {
                        if (next >= 'A' && next <= 'Z') {
                            decodedChar = (char)(next - 64);
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    case 'b': {
                        if (next >= 'A' && next <= 'E') {
                            decodedChar = (char)(next - 38);
                            break;
                        }
                        if (next >= 'F' && next <= 'J') {
                            decodedChar = (char)(next - 11);
                            break;
                        }
                        if (next >= 'K' && next <= 'O') {
                            decodedChar = (char)(next + 16);
                            break;
                        }
                        if (next >= 'P' && next <= 'S') {
                            decodedChar = (char)(next + 43);
                            break;
                        }
                        if (next >= 'T' && next <= 'Z') {
                            decodedChar = 127;
                            break;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    case 'c': {
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

    private static void checkChecksums(CharSequence result) throws ChecksumException {
        int length = result.length();
        Code93Reader.checkOneChecksum(result, length - 2, 20);
        Code93Reader.checkOneChecksum(result, length - 1, 15);
    }

    private static void checkOneChecksum(CharSequence result, int checkPosition, int weightMax) throws ChecksumException {
        int weight = 1;
        int total = 0;
        for (int i = checkPosition - 1; i >= 0; --i) {
            total += weight * ALPHABET_STRING.indexOf(result.charAt(i));
            if (++weight <= weightMax) continue;
            weight = 1;
        }
        if (result.charAt(checkPosition) != ALPHABET[total % 47]) {
            throw ChecksumException.getChecksumInstance();
        }
    }
}

