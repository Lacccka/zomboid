/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Reader;
import com.google.zxing.oned.OneDimensionalCodeWriter;
import java.util.ArrayList;
import java.util.Map;

public final class Code128Writer
extends OneDimensionalCodeWriter {
    private static final int CODE_START_B = 104;
    private static final int CODE_START_C = 105;
    private static final int CODE_CODE_B = 100;
    private static final int CODE_CODE_C = 99;
    private static final int CODE_STOP = 106;
    private static final char ESCAPE_FNC_1 = '\u00f1';
    private static final char ESCAPE_FNC_2 = '\u00f2';
    private static final char ESCAPE_FNC_3 = '\u00f3';
    private static final char ESCAPE_FNC_4 = '\u00f4';
    private static final int CODE_FNC_1 = 102;
    private static final int CODE_FNC_2 = 97;
    private static final int CODE_FNC_3 = 96;
    private static final int CODE_FNC_4_B = 100;

    @Override
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format != BarcodeFormat.CODE_128) {
            throw new IllegalArgumentException("Can only encode CODE_128, but got " + (Object)((Object)format));
        }
        return super.encode(contents, format, width, height, hints);
    }

    @Override
    public boolean[] encode(String contents) {
        int length = contents.length();
        if (length < 1 || length > 80) {
            throw new IllegalArgumentException("Contents length should be between 1 and 80 characters, but got " + length);
        }
        block9: for (int i = 0; i < length; ++i) {
            char c = contents.charAt(i);
            if (c >= ' ' && c <= '~') continue;
            switch (c) {
                case '\u00f1': 
                case '\u00f2': 
                case '\u00f3': 
                case '\u00f4': {
                    continue block9;
                }
                default: {
                    throw new IllegalArgumentException("Bad character in input: " + c);
                }
            }
        }
        ArrayList<int[]> patterns = new ArrayList<int[]>();
        int checkSum = 0;
        int checkWeight = 1;
        int codeSet = 0;
        int position = 0;
        while (position < length) {
            int patternIndex;
            int requiredDigitCount = codeSet == 99 ? 2 : 4;
            int newCodeSet = Code128Writer.isDigits(contents, position, requiredDigitCount) ? 99 : 100;
            if (newCodeSet == codeSet) {
                switch (contents.charAt(position)) {
                    case '\u00f1': {
                        patternIndex = 102;
                        break;
                    }
                    case '\u00f2': {
                        patternIndex = 97;
                        break;
                    }
                    case '\u00f3': {
                        patternIndex = 96;
                        break;
                    }
                    case '\u00f4': {
                        patternIndex = 100;
                        break;
                    }
                    default: {
                        if (codeSet == 100) {
                            patternIndex = contents.charAt(position) - 32;
                            break;
                        }
                        patternIndex = Integer.parseInt(contents.substring(position, position + 2));
                        ++position;
                    }
                }
                ++position;
            } else {
                patternIndex = codeSet == 0 ? (newCodeSet == 100 ? 104 : 105) : newCodeSet;
                codeSet = newCodeSet;
            }
            patterns.add(Code128Reader.CODE_PATTERNS[patternIndex]);
            checkSum += patternIndex * checkWeight;
            if (position == 0) continue;
            ++checkWeight;
        }
        patterns.add(Code128Reader.CODE_PATTERNS[checkSum %= 103]);
        patterns.add(Code128Reader.CODE_PATTERNS[106]);
        int codeWidth = 0;
        for (int[] pattern : patterns) {
            for (int width : pattern) {
                codeWidth += width;
            }
        }
        boolean[] result = new boolean[codeWidth];
        int pos = 0;
        Object object = patterns.iterator();
        while (object.hasNext()) {
            int[] pattern = (int[])object.next();
            pos += Code128Writer.appendPattern(result, pos, pattern, true);
        }
        return result;
    }

    private static boolean isDigits(CharSequence value, int start, int length) {
        int end = start + length;
        int last = value.length();
        for (int i = start; i < end && i < last; ++i) {
            char c = value.charAt(i);
            if (c >= '0' && c <= '9') continue;
            if (c != '\u00f1') {
                return false;
            }
            ++end;
        }
        return end <= last;
    }
}

