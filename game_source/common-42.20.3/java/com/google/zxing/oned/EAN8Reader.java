/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.UPCEANReader;

public final class EAN8Reader
extends UPCEANReader {
    private final int[] decodeMiddleCounters = new int[4];

    @Override
    protected int decodeMiddle(BitArray row, int[] startRange, StringBuilder result) throws NotFoundException {
        int[] counters = this.decodeMiddleCounters;
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int end = row.getSize();
        int rowOffset = startRange[1];
        for (int x = 0; x < 4 && rowOffset < end; ++x) {
            int bestMatch = EAN8Reader.decodeDigit(row, counters, rowOffset, L_PATTERNS);
            result.append((char)(48 + bestMatch));
            for (int counter : counters) {
                rowOffset += counter;
            }
        }
        int[] middleRange = EAN8Reader.findGuardPattern(row, rowOffset, true, MIDDLE_PATTERN);
        rowOffset = middleRange[1];
        for (int x = 0; x < 4 && rowOffset < end; ++x) {
            int bestMatch = EAN8Reader.decodeDigit(row, counters, rowOffset, L_PATTERNS);
            result.append((char)(48 + bestMatch));
            for (int counter : counters) {
                rowOffset += counter;
            }
        }
        return rowOffset;
    }

    @Override
    BarcodeFormat getBarcodeFormat() {
        return BarcodeFormat.EAN_8;
    }
}

