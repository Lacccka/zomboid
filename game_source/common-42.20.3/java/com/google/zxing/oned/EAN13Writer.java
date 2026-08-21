/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Reader;
import com.google.zxing.oned.UPCEANReader;
import com.google.zxing.oned.UPCEANWriter;
import java.util.Map;

public final class EAN13Writer
extends UPCEANWriter {
    private static final int CODE_WIDTH = 95;

    @Override
    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        if (format != BarcodeFormat.EAN_13) {
            throw new IllegalArgumentException("Can only encode EAN_13, but got " + (Object)((Object)format));
        }
        return super.encode(contents, format, width, height, hints);
    }

    @Override
    public boolean[] encode(String contents) {
        int digit;
        int i;
        if (contents.length() != 13) {
            throw new IllegalArgumentException("Requested contents should be 13 digits long, but got " + contents.length());
        }
        try {
            if (!UPCEANReader.checkStandardUPCEANChecksum(contents)) {
                throw new IllegalArgumentException("Contents do not pass checksum");
            }
        }
        catch (FormatException ignored) {
            throw new IllegalArgumentException("Illegal contents");
        }
        int firstDigit = Integer.parseInt(contents.substring(0, 1));
        int parities = EAN13Reader.FIRST_DIGIT_ENCODINGS[firstDigit];
        boolean[] result = new boolean[95];
        int pos = 0;
        pos += EAN13Writer.appendPattern(result, pos, UPCEANReader.START_END_PATTERN, true);
        for (i = 1; i <= 6; ++i) {
            digit = Integer.parseInt(contents.substring(i, i + 1));
            if ((parities >> 6 - i & 1) == 1) {
                digit += 10;
            }
            pos += EAN13Writer.appendPattern(result, pos, UPCEANReader.L_AND_G_PATTERNS[digit], false);
        }
        pos += EAN13Writer.appendPattern(result, pos, UPCEANReader.MIDDLE_PATTERN, false);
        for (i = 7; i <= 12; ++i) {
            digit = Integer.parseInt(contents.substring(i, i + 1));
            pos += EAN13Writer.appendPattern(result, pos, UPCEANReader.L_PATTERNS[digit], true);
        }
        EAN13Writer.appendPattern(result, pos, UPCEANReader.START_END_PATTERN, true);
        return result;
    }
}

