/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.OneDReader;
import java.util.Arrays;
import java.util.Map;

public final class CodaBarReader
extends OneDReader {
    private static final float MAX_ACCEPTABLE = 2.0f;
    private static final float PADDING = 1.5f;
    private static final String ALPHABET_STRING = "0123456789-$:/.+ABCD";
    static final char[] ALPHABET = "0123456789-$:/.+ABCD".toCharArray();
    static final int[] CHARACTER_ENCODINGS = new int[]{3, 6, 9, 96, 18, 66, 33, 36, 48, 72, 12, 24, 69, 81, 84, 21, 26, 41, 11, 14};
    private static final int MIN_CHARACTER_LENGTH = 3;
    private static final char[] STARTEND_ENCODING = new char[]{'A', 'B', 'C', 'D'};
    private final StringBuilder decodeRowResult = new StringBuilder(20);
    private int[] counters = new int[80];
    private int counterLength = 0;

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException {
        int i;
        int charOffset;
        int startOffset;
        Arrays.fill(this.counters, 0);
        this.setCounters(row);
        int nextStart = startOffset = this.findStartPattern();
        this.decodeRowResult.setLength(0);
        do {
            if ((charOffset = this.toNarrowWidePattern(nextStart)) == -1) {
                throw NotFoundException.getNotFoundInstance();
            }
            this.decodeRowResult.append((char)charOffset);
        } while ((this.decodeRowResult.length() <= 1 || !CodaBarReader.arrayContains(STARTEND_ENCODING, ALPHABET[charOffset])) && (nextStart += 8) < this.counterLength);
        int trailingWhitespace = this.counters[nextStart - 1];
        int lastPatternSize = 0;
        for (i = -8; i < -1; ++i) {
            lastPatternSize += this.counters[nextStart + i];
        }
        if (nextStart < this.counterLength && trailingWhitespace < lastPatternSize / 2) {
            throw NotFoundException.getNotFoundInstance();
        }
        this.validatePattern(startOffset);
        for (i = 0; i < this.decodeRowResult.length(); ++i) {
            this.decodeRowResult.setCharAt(i, ALPHABET[this.decodeRowResult.charAt(i)]);
        }
        char startchar = this.decodeRowResult.charAt(0);
        if (!CodaBarReader.arrayContains(STARTEND_ENCODING, startchar)) {
            throw NotFoundException.getNotFoundInstance();
        }
        char endchar = this.decodeRowResult.charAt(this.decodeRowResult.length() - 1);
        if (!CodaBarReader.arrayContains(STARTEND_ENCODING, endchar)) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (this.decodeRowResult.length() <= 3) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (hints == null || !hints.containsKey((Object)DecodeHintType.RETURN_CODABAR_START_END)) {
            this.decodeRowResult.deleteCharAt(this.decodeRowResult.length() - 1);
            this.decodeRowResult.deleteCharAt(0);
        }
        int runningCount = 0;
        for (int i2 = 0; i2 < startOffset; ++i2) {
            runningCount += this.counters[i2];
        }
        float left = runningCount;
        for (int i3 = startOffset; i3 < nextStart - 1; ++i3) {
            runningCount += this.counters[i3];
        }
        float right = runningCount;
        return new Result(this.decodeRowResult.toString(), null, new ResultPoint[]{new ResultPoint(left, rowNumber), new ResultPoint(right, rowNumber)}, BarcodeFormat.CODABAR);
    }

    void validatePattern(int start) throws NotFoundException {
        int i;
        int[] sizes = new int[]{0, 0, 0, 0};
        int[] counts = new int[]{0, 0, 0, 0};
        int end = this.decodeRowResult.length() - 1;
        int pos = start;
        int i2 = 0;
        while (true) {
            int pattern = CHARACTER_ENCODINGS[this.decodeRowResult.charAt(i2)];
            for (int j = 6; j >= 0; --j) {
                int category;
                int n = category = (j & 1) + (pattern & 1) * 2;
                sizes[n] = sizes[n] + this.counters[pos + j];
                int n2 = category;
                counts[n2] = counts[n2] + 1;
                pattern >>= 1;
            }
            if (i2 >= end) break;
            pos += 8;
            ++i2;
        }
        float[] maxes = new float[4];
        float[] mins = new float[4];
        for (i = 0; i < 2; ++i) {
            mins[i] = 0.0f;
            mins[i + 2] = ((float)sizes[i] / (float)counts[i] + (float)sizes[i + 2] / (float)counts[i + 2]) / 2.0f;
            maxes[i] = mins[i + 2];
            maxes[i + 2] = ((float)sizes[i + 2] * 2.0f + 1.5f) / (float)counts[i + 2];
        }
        pos = start;
        i = 0;
        while (true) {
            int pattern = CHARACTER_ENCODINGS[this.decodeRowResult.charAt(i)];
            for (int j = 6; j >= 0; --j) {
                int size = this.counters[pos + j];
                int category = (j & 1) + (pattern & 1) * 2;
                if ((float)size < mins[category] || (float)size > maxes[category]) {
                    throw NotFoundException.getNotFoundInstance();
                }
                pattern >>= 1;
            }
            if (i >= end) break;
            pos += 8;
            ++i;
        }
    }

    private void setCounters(BitArray row) throws NotFoundException {
        int end;
        this.counterLength = 0;
        int i = row.getNextUnset(0);
        if (i >= (end = row.getSize())) {
            throw NotFoundException.getNotFoundInstance();
        }
        boolean isWhite = true;
        int count = 0;
        while (i < end) {
            if (row.get(i) ^ isWhite) {
                ++count;
            } else {
                this.counterAppend(count);
                count = 1;
                isWhite = !isWhite;
            }
            ++i;
        }
        this.counterAppend(count);
    }

    private void counterAppend(int e) {
        this.counters[this.counterLength] = e;
        ++this.counterLength;
        if (this.counterLength >= this.counters.length) {
            int[] temp = new int[this.counterLength * 2];
            System.arraycopy(this.counters, 0, temp, 0, this.counterLength);
            this.counters = temp;
        }
    }

    private int findStartPattern() throws NotFoundException {
        for (int i = 1; i < this.counterLength; i += 2) {
            int charOffset = this.toNarrowWidePattern(i);
            if (charOffset == -1 || !CodaBarReader.arrayContains(STARTEND_ENCODING, ALPHABET[charOffset])) continue;
            int patternSize = 0;
            for (int j = i; j < i + 7; ++j) {
                patternSize += this.counters[j];
            }
            if (i != 1 && this.counters[i - 1] < patternSize / 2) continue;
            return i;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    static boolean arrayContains(char[] array, char key) {
        if (array != null) {
            for (char c : array) {
                if (c != key) continue;
                return true;
            }
        }
        return false;
    }

    private int toNarrowWidePattern(int position) {
        int i;
        int end = position + 7;
        if (end >= this.counterLength) {
            return -1;
        }
        int[] theCounters = this.counters;
        int maxBar = 0;
        int minBar = Integer.MAX_VALUE;
        for (int j = position; j < end; j += 2) {
            int currentCounter = theCounters[j];
            if (currentCounter < minBar) {
                minBar = currentCounter;
            }
            if (currentCounter <= maxBar) continue;
            maxBar = currentCounter;
        }
        int thresholdBar = (minBar + maxBar) / 2;
        int maxSpace = 0;
        int minSpace = Integer.MAX_VALUE;
        for (int j = position + 1; j < end; j += 2) {
            int currentCounter = theCounters[j];
            if (currentCounter < minSpace) {
                minSpace = currentCounter;
            }
            if (currentCounter <= maxSpace) continue;
            maxSpace = currentCounter;
        }
        int thresholdSpace = (minSpace + maxSpace) / 2;
        int bitmask = 128;
        int pattern = 0;
        for (i = 0; i < 7; ++i) {
            int threshold = (i & 1) == 0 ? thresholdBar : thresholdSpace;
            bitmask >>= 1;
            if (theCounters[position + i] <= threshold) continue;
            pattern |= bitmask;
        }
        for (i = 0; i < CHARACTER_ENCODINGS.length; ++i) {
            if (CHARACTER_ENCODINGS[i] != pattern) continue;
            return i;
        }
        return -1;
    }
}

