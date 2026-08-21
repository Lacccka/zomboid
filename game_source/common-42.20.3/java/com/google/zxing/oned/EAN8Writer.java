/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.UPCEANReader;
import com.google.zxing.oned.UPCEANWriter;
import java.util.Map;

public final class EAN8Writer
extends UPCEANWriter {
    private static final int CODE_WIDTH = 67;

    @Override
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format != BarcodeFormat.EAN_8) {
            throw new IllegalArgumentException("Can only encode EAN_8, but got " + (Object)((Object)format));
        }
        return super.encode(contents, format, width, height, hints);
    }

    @Override
    public boolean[] encode(String contents) {
        int digit;
        int i;
        if (contents.length() != 8) {
            throw new IllegalArgumentException("Requested contents should be 8 digits long, but got " + contents.length());
        }
        boolean[] result = new boolean[67];
        int pos = 0;
        pos += EAN8Writer.appendPattern(result, pos, UPCEANReader.START_END_PATTERN, true);
        for (i = 0; i <= 3; ++i) {
            digit = Integer.parseInt(contents.substring(i, i + 1));
            pos += EAN8Writer.appendPattern(result, pos, UPCEANReader.L_PATTERNS[digit], false);
        }
        pos += EAN8Writer.appendPattern(result, pos, UPCEANReader.MIDDLE_PATTERN, false);
        for (i = 4; i <= 7; ++i) {
            digit = Integer.parseInt(contents.substring(i, i + 1));
            pos += EAN8Writer.appendPattern(result, pos, UPCEANReader.L_PATTERNS[digit], true);
        }
        EAN8Writer.appendPattern(result, pos, UPCEANReader.START_END_PATTERN, true);
        return result;
    }
}

