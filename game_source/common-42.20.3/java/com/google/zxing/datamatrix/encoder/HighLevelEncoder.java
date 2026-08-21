/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import com.google.zxing.Dimension;
import com.google.zxing.datamatrix.encoder.ASCIIEncoder;
import com.google.zxing.datamatrix.encoder.Base256Encoder;
import com.google.zxing.datamatrix.encoder.C40Encoder;
import com.google.zxing.datamatrix.encoder.EdifactEncoder;
import com.google.zxing.datamatrix.encoder.Encoder;
import com.google.zxing.datamatrix.encoder.EncoderContext;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.datamatrix.encoder.TextEncoder;
import com.google.zxing.datamatrix.encoder.X12Encoder;
import java.util.Arrays;

public final class HighLevelEncoder {
    private static final char PAD = '\u0081';
    static final char LATCH_TO_C40 = '\u00e6';
    static final char LATCH_TO_BASE256 = '\u00e7';
    static final char UPPER_SHIFT = '\u00eb';
    private static final char MACRO_05 = '\u00ec';
    private static final char MACRO_06 = '\u00ed';
    static final char LATCH_TO_ANSIX12 = '\u00ee';
    static final char LATCH_TO_TEXT = '\u00ef';
    static final char LATCH_TO_EDIFACT = '\u00f0';
    static final char C40_UNLATCH = '\u00fe';
    static final char X12_UNLATCH = '\u00fe';
    private static final String MACRO_05_HEADER = "[)>\u001e05\u001d";
    private static final String MACRO_06_HEADER = "[)>\u001e06\u001d";
    private static final String MACRO_TRAILER = "\u001e\u0004";
    static final int ASCII_ENCODATION = 0;
    static final int C40_ENCODATION = 1;
    static final int TEXT_ENCODATION = 2;
    static final int X12_ENCODATION = 3;
    static final int EDIFACT_ENCODATION = 4;
    static final int BASE256_ENCODATION = 5;

    private HighLevelEncoder() {
    }

    private static char randomize253State(char ch, int codewordPosition) {
        int pseudoRandom = 149 * codewordPosition % 253 + 1;
        int tempVariable = ch + pseudoRandom;
        return tempVariable <= 254 ? (char)tempVariable : (char)(tempVariable - 254);
    }

    public static String encodeHighLevel(String msg) {
        return HighLevelEncoder.encodeHighLevel(msg, SymbolShapeHint.FORCE_NONE, null, null);
    }

    public static String encodeHighLevel(String msg, SymbolShapeHint shape, Dimension minSize, Dimension maxSize) {
        StringBuilder codewords;
        Encoder[] encoders = new Encoder[]{new ASCIIEncoder(), new C40Encoder(), new TextEncoder(), new X12Encoder(), new EdifactEncoder(), new Base256Encoder()};
        EncoderContext context = new EncoderContext(msg);
        context.setSymbolShape(shape);
        context.setSizeConstraints(minSize, maxSize);
        if (msg.startsWith(MACRO_05_HEADER) && msg.endsWith(MACRO_TRAILER)) {
            context.writeCodeword('\u00ec');
            context.setSkipAtEnd(2);
            context.pos += MACRO_05_HEADER.length();
        } else if (msg.startsWith(MACRO_06_HEADER) && msg.endsWith(MACRO_TRAILER)) {
            context.writeCodeword('\u00ed');
            context.setSkipAtEnd(2);
            context.pos += MACRO_06_HEADER.length();
        }
        int encodingMode = 0;
        while (context.hasMoreCharacters()) {
            encoders[encodingMode].encode(context);
            if (context.getNewEncoding() < 0) continue;
            encodingMode = context.getNewEncoding();
            context.resetEncoderSignal();
        }
        int len = context.getCodewordCount();
        context.updateSymbolInfo();
        int capacity = context.getSymbolInfo().getDataCapacity();
        if (len < capacity && encodingMode != 0 && encodingMode != 5) {
            context.writeCodeword('\u00fe');
        }
        if ((codewords = context.getCodewords()).length() < capacity) {
            codewords.append('\u0081');
        }
        while (codewords.length() < capacity) {
            codewords.append(HighLevelEncoder.randomize253State('\u0081', codewords.length() + 1));
        }
        return context.getCodewords().toString();
    }

    static int lookAheadTest(CharSequence msg, int startpos, int currentMode) {
        float[] charCounts;
        if (startpos >= msg.length()) {
            return currentMode;
        }
        if (currentMode == 0) {
            charCounts = new float[]{0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.25f};
        } else {
            charCounts = new float[]{1.0f, 2.0f, 2.0f, 2.0f, 2.0f, 2.25f};
            charCounts[currentMode] = 0.0f;
        }
        int charsProcessed = 0;
        while (true) {
            int minCount;
            if (startpos + charsProcessed == msg.length()) {
                int min = Integer.MAX_VALUE;
                byte[] mins = new byte[6];
                int[] intCharCounts = new int[6];
                min = HighLevelEncoder.findMinimums(charCounts, intCharCounts, min, mins);
                minCount = HighLevelEncoder.getMinimumCount(mins);
                if (intCharCounts[0] == min) {
                    return 0;
                }
                if (minCount == 1 && mins[5] > 0) {
                    return 5;
                }
                if (minCount == 1 && mins[4] > 0) {
                    return 4;
                }
                if (minCount == 1 && mins[2] > 0) {
                    return 2;
                }
                if (minCount == 1 && mins[3] > 0) {
                    return 3;
                }
                return 1;
            }
            char c = msg.charAt(startpos + charsProcessed);
            ++charsProcessed;
            if (HighLevelEncoder.isDigit(c)) {
                charCounts[0] = (float)((double)charCounts[0] + 0.5);
            } else if (HighLevelEncoder.isExtendedASCII(c)) {
                charCounts[0] = (int)Math.ceil(charCounts[0]);
                charCounts[0] = charCounts[0] + 2.0f;
            } else {
                charCounts[0] = (int)Math.ceil(charCounts[0]);
                charCounts[0] = charCounts[0] + 1.0f;
            }
            charCounts[1] = HighLevelEncoder.isNativeC40(c) ? charCounts[1] + 0.6666667f : (HighLevelEncoder.isExtendedASCII(c) ? charCounts[1] + 2.6666667f : charCounts[1] + 1.3333334f);
            charCounts[2] = HighLevelEncoder.isNativeText(c) ? charCounts[2] + 0.6666667f : (HighLevelEncoder.isExtendedASCII(c) ? charCounts[2] + 2.6666667f : charCounts[2] + 1.3333334f);
            charCounts[3] = HighLevelEncoder.isNativeX12(c) ? charCounts[3] + 0.6666667f : (HighLevelEncoder.isExtendedASCII(c) ? charCounts[3] + 4.3333335f : charCounts[3] + 3.3333333f);
            charCounts[4] = HighLevelEncoder.isNativeEDIFACT(c) ? charCounts[4] + 0.75f : (HighLevelEncoder.isExtendedASCII(c) ? charCounts[4] + 4.25f : charCounts[4] + 3.25f);
            charCounts[5] = HighLevelEncoder.isSpecialB256(c) ? charCounts[5] + 4.0f : charCounts[5] + 1.0f;
            if (charsProcessed < 4) continue;
            int[] intCharCounts = new int[6];
            byte[] mins = new byte[6];
            HighLevelEncoder.findMinimums(charCounts, intCharCounts, Integer.MAX_VALUE, mins);
            minCount = HighLevelEncoder.getMinimumCount(mins);
            if (intCharCounts[0] < intCharCounts[5] && intCharCounts[0] < intCharCounts[1] && intCharCounts[0] < intCharCounts[2] && intCharCounts[0] < intCharCounts[3] && intCharCounts[0] < intCharCounts[4]) {
                return 0;
            }
            if (intCharCounts[5] < intCharCounts[0] || mins[1] + mins[2] + mins[3] + mins[4] == 0) {
                return 5;
            }
            if (minCount == 1 && mins[4] > 0) {
                return 4;
            }
            if (minCount == 1 && mins[2] > 0) {
                return 2;
            }
            if (minCount == 1 && mins[3] > 0) {
                return 3;
            }
            if (intCharCounts[1] + 1 >= intCharCounts[0] || intCharCounts[1] + 1 >= intCharCounts[5] || intCharCounts[1] + 1 >= intCharCounts[4] || intCharCounts[1] + 1 >= intCharCounts[2]) continue;
            if (intCharCounts[1] < intCharCounts[3]) {
                return 1;
            }
            if (intCharCounts[1] == intCharCounts[3]) break;
        }
        for (int p = startpos + charsProcessed + 1; p < msg.length(); ++p) {
            char tc = msg.charAt(p);
            if (HighLevelEncoder.isX12TermSep(tc)) {
                return 3;
            }
            if (!HighLevelEncoder.isNativeX12(tc)) break;
        }
        return 1;
    }

    private static int findMinimums(float[] charCounts, int[] intCharCounts, int min, byte[] mins) {
        Arrays.fill(mins, (byte)0);
        for (int i = 0; i < 6; ++i) {
            intCharCounts[i] = (int)Math.ceil(charCounts[i]);
            int current = intCharCounts[i];
            if (min > current) {
                min = current;
                Arrays.fill(mins, (byte)0);
            }
            if (min != current) continue;
            int n = i;
            mins[n] = (byte)(mins[n] + 1);
        }
        return min;
    }

    private static int getMinimumCount(byte[] mins) {
        int minCount = 0;
        for (int i = 0; i < 6; ++i) {
            minCount += mins[i];
        }
        return minCount;
    }

    static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    static boolean isExtendedASCII(char ch) {
        return ch >= '\u0080' && ch <= '\u00ff';
    }

    private static boolean isNativeC40(char ch) {
        return ch == ' ' || ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'Z';
    }

    private static boolean isNativeText(char ch) {
        return ch == ' ' || ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z';
    }

    private static boolean isNativeX12(char ch) {
        return HighLevelEncoder.isX12TermSep(ch) || ch == ' ' || ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'Z';
    }

    private static boolean isX12TermSep(char ch) {
        return ch == '\r' || ch == '*' || ch == '>';
    }

    private static boolean isNativeEDIFACT(char ch) {
        return ch >= ' ' && ch <= '^';
    }

    private static boolean isSpecialB256(char ch) {
        return false;
    }

    public static int determineConsecutiveDigitCount(CharSequence msg, int startpos) {
        int count = 0;
        int idx = startpos;
        int len = msg.length();
        if (idx < len) {
            char ch = msg.charAt(idx);
            while (HighLevelEncoder.isDigit(ch) && idx < len) {
                ++count;
                if (++idx >= len) continue;
                ch = msg.charAt(idx);
            }
        }
        return count;
    }

    static void illegalCharacter(char c) {
        String hex = Integer.toHexString(c);
        hex = "0000".substring(0, 4 - hex.length()) + hex;
        throw new IllegalArgumentException("Illegal character: " + c + " (0x" + hex + ')');
    }
}

