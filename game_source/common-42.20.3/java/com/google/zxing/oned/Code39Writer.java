/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code39Reader;
import com.google.zxing.oned.OneDimensionalCodeWriter;
import java.util.Map;

public final class Code39Writer
extends OneDimensionalCodeWriter {
    @Override
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format != BarcodeFormat.CODE_39) {
            throw new IllegalArgumentException("Can only encode CODE_39, but got " + (Object)((Object)format));
        }
        return super.encode(contents, format, width, height, hints);
    }

    @Override
    public boolean[] encode(String contents) {
        int length = contents.length();
        if (length > 80) {
            throw new IllegalArgumentException("Requested contents should be less than 80 digits long, but got " + length);
        }
        int[] widths = new int[9];
        int codeWidth = 25 + length;
        for (int i = 0; i < length; ++i) {
            int indexInString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".indexOf(contents.charAt(i));
            if (indexInString < 0) {
                throw new IllegalArgumentException("Bad contents: " + contents);
            }
            Code39Writer.toIntArray(Code39Reader.CHARACTER_ENCODINGS[indexInString], widths);
            for (int width : widths) {
                codeWidth += width;
            }
        }
        boolean[] result = new boolean[codeWidth];
        Code39Writer.toIntArray(Code39Reader.CHARACTER_ENCODINGS[39], widths);
        int pos = Code39Writer.appendPattern(result, 0, widths, true);
        int[] narrowWhite = new int[]{1};
        pos += Code39Writer.appendPattern(result, pos, narrowWhite, false);
        for (int i = 0; i < length; ++i) {
            int indexInString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".indexOf(contents.charAt(i));
            Code39Writer.toIntArray(Code39Reader.CHARACTER_ENCODINGS[indexInString], widths);
            pos += Code39Writer.appendPattern(result, pos, widths, true);
            pos += Code39Writer.appendPattern(result, pos, narrowWhite, false);
        }
        Code39Writer.toIntArray(Code39Reader.CHARACTER_ENCODINGS[39], widths);
        Code39Writer.appendPattern(result, pos, widths, true);
        return result;
    }

    private static void toIntArray(int a, int[] toReturn) {
        for (int i = 0; i < 9; ++i) {
            int temp = a & 1 << 8 - i;
            toReturn[i] = temp == 0 ? 1 : 2;
        }
    }
}

