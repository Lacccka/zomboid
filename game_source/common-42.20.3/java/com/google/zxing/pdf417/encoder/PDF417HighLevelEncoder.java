/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.pdf417.encoder;

import com.google.zxing.WriterException;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.pdf417.encoder.Compaction;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

final class PDF417HighLevelEncoder {
    private static final int TEXT_COMPACTION = 0;
    private static final int BYTE_COMPACTION = 1;
    private static final int NUMERIC_COMPACTION = 2;
    private static final int SUBMODE_ALPHA = 0;
    private static final int SUBMODE_LOWER = 1;
    private static final int SUBMODE_MIXED = 2;
    private static final int SUBMODE_PUNCTUATION = 3;
    private static final int LATCH_TO_TEXT = 900;
    private static final int LATCH_TO_BYTE_PADDED = 901;
    private static final int LATCH_TO_NUMERIC = 902;
    private static final int SHIFT_TO_BYTE = 913;
    private static final int LATCH_TO_BYTE = 924;
    private static final int ECI_USER_DEFINED = 925;
    private static final int ECI_GENERAL_PURPOSE = 926;
    private static final int ECI_CHARSET = 927;
    private static final byte[] TEXT_MIXED_RAW;
    private static final byte[] TEXT_PUNCTUATION_RAW;
    private static final byte[] MIXED;
    private static final byte[] PUNCTUATION;
    private static final Charset DEFAULT_ENCODING;

    private PDF417HighLevelEncoder() {
    }

    static String encodeHighLevel(String msg, Compaction compaction, Charset encoding) throws WriterException {
        CharacterSetECI eci;
        StringBuilder sb = new StringBuilder(msg.length());
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        } else if (!DEFAULT_ENCODING.equals(encoding) && (eci = CharacterSetECI.getCharacterSetECIByName(encoding.name())) != null) {
            PDF417HighLevelEncoder.encodingECI(eci.getValue(), sb);
        }
        int len = msg.length();
        int p = 0;
        int textSubMode = 0;
        if (compaction == Compaction.TEXT) {
            PDF417HighLevelEncoder.encodeText(msg, p, len, sb, textSubMode);
        } else if (compaction == Compaction.BYTE) {
            byte[] bytes = msg.getBytes(encoding);
            PDF417HighLevelEncoder.encodeBinary(bytes, p, bytes.length, 1, sb);
        } else if (compaction == Compaction.NUMERIC) {
            sb.append('\u0386');
            PDF417HighLevelEncoder.encodeNumeric(msg, p, len, sb);
        } else {
            int encodingMode = 0;
            while (p < len) {
                byte[] bytes;
                int n = PDF417HighLevelEncoder.determineConsecutiveDigitCount(msg, p);
                if (n >= 13) {
                    sb.append('\u0386');
                    encodingMode = 2;
                    textSubMode = 0;
                    PDF417HighLevelEncoder.encodeNumeric(msg, p, n, sb);
                    p += n;
                    continue;
                }
                int t = PDF417HighLevelEncoder.determineConsecutiveTextCount(msg, p);
                if (t >= 5 || n == len) {
                    if (encodingMode != 0) {
                        sb.append('\u0384');
                        encodingMode = 0;
                        textSubMode = 0;
                    }
                    textSubMode = PDF417HighLevelEncoder.encodeText(msg, p, t, sb, textSubMode);
                    p += t;
                    continue;
                }
                int b = PDF417HighLevelEncoder.determineConsecutiveBinaryCount(msg, p, encoding);
                if (b == 0) {
                    b = 1;
                }
                if ((bytes = msg.substring(p, p + b).getBytes(encoding)).length == 1 && encodingMode == 0) {
                    PDF417HighLevelEncoder.encodeBinary(bytes, 0, 1, 0, sb);
                } else {
                    PDF417HighLevelEncoder.encodeBinary(bytes, 0, bytes.length, encodingMode, sb);
                    encodingMode = 1;
                    textSubMode = 0;
                }
                p += b;
            }
        }
        return sb.toString();
    }

    private static int encodeText(CharSequence msg, int startpos, int count, StringBuilder sb, int initialSubmode) {
        StringBuilder tmp = new StringBuilder(count);
        int submode = initialSubmode;
        int idx = 0;
        block5: while (true) {
            char ch = msg.charAt(startpos + idx);
            switch (submode) {
                case 0: {
                    if (PDF417HighLevelEncoder.isAlphaUpper(ch)) {
                        if (ch == ' ') {
                            tmp.append('\u001a');
                            break;
                        }
                        tmp.append((char)(ch - 65));
                        break;
                    }
                    if (PDF417HighLevelEncoder.isAlphaLower(ch)) {
                        submode = 1;
                        tmp.append('\u001b');
                        continue block5;
                    }
                    if (PDF417HighLevelEncoder.isMixed(ch)) {
                        submode = 2;
                        tmp.append('\u001c');
                        continue block5;
                    }
                    tmp.append('\u001d');
                    tmp.append((char)PUNCTUATION[ch]);
                    break;
                }
                case 1: {
                    if (PDF417HighLevelEncoder.isAlphaLower(ch)) {
                        if (ch == ' ') {
                            tmp.append('\u001a');
                            break;
                        }
                        tmp.append((char)(ch - 97));
                        break;
                    }
                    if (PDF417HighLevelEncoder.isAlphaUpper(ch)) {
                        tmp.append('\u001b');
                        tmp.append((char)(ch - 65));
                        break;
                    }
                    if (PDF417HighLevelEncoder.isMixed(ch)) {
                        submode = 2;
                        tmp.append('\u001c');
                        continue block5;
                    }
                    tmp.append('\u001d');
                    tmp.append((char)PUNCTUATION[ch]);
                    break;
                }
                case 2: {
                    char next;
                    if (PDF417HighLevelEncoder.isMixed(ch)) {
                        tmp.append((char)MIXED[ch]);
                        break;
                    }
                    if (PDF417HighLevelEncoder.isAlphaUpper(ch)) {
                        submode = 0;
                        tmp.append('\u001c');
                        continue block5;
                    }
                    if (PDF417HighLevelEncoder.isAlphaLower(ch)) {
                        submode = 1;
                        tmp.append('\u001b');
                        continue block5;
                    }
                    if (startpos + idx + 1 < count && PDF417HighLevelEncoder.isPunctuation(next = msg.charAt(startpos + idx + 1))) {
                        submode = 3;
                        tmp.append('\u0019');
                        continue block5;
                    }
                    tmp.append('\u001d');
                    tmp.append((char)PUNCTUATION[ch]);
                    break;
                }
                default: {
                    if (PDF417HighLevelEncoder.isPunctuation(ch)) {
                        tmp.append((char)PUNCTUATION[ch]);
                        break;
                    }
                    submode = 0;
                    tmp.append('\u001d');
                    continue block5;
                }
            }
            if (++idx >= count) break;
        }
        char h = '\u0000';
        int len = tmp.length();
        for (int i = 0; i < len; ++i) {
            boolean odd;
            boolean bl = odd = i % 2 != 0;
            if (odd) {
                h = (char)(h * 30 + tmp.charAt(i));
                sb.append(h);
                continue;
            }
            h = tmp.charAt(i);
        }
        if (len % 2 != 0) {
            sb.append((char)(h * 30 + 29));
        }
        return submode;
    }

    private static void encodeBinary(byte[] bytes, int startpos, int count, int startmode, StringBuilder sb) {
        if (count == 1 && startmode == 0) {
            sb.append('\u0391');
        } else {
            boolean sixpack;
            boolean bl = sixpack = count % 6 == 0;
            if (sixpack) {
                sb.append('\u039c');
            } else {
                sb.append('\u0385');
            }
        }
        int idx = startpos;
        if (count >= 6) {
            char[] chars = new char[5];
            while (startpos + count - idx >= 6) {
                int i;
                long t = 0L;
                for (i = 0; i < 6; ++i) {
                    t <<= 8;
                    t += (long)(bytes[idx + i] & 0xFF);
                }
                for (i = 0; i < 5; ++i) {
                    chars[i] = (char)(t % 900L);
                    t /= 900L;
                }
                for (i = chars.length - 1; i >= 0; --i) {
                    sb.append(chars[i]);
                }
                idx += 6;
            }
        }
        for (int i = idx; i < startpos + count; ++i) {
            int ch = bytes[i] & 0xFF;
            sb.append((char)ch);
        }
    }

    private static void encodeNumeric(String msg, int startpos, int count, StringBuilder sb) {
        int len;
        StringBuilder tmp = new StringBuilder(count / 3 + 1);
        BigInteger num900 = BigInteger.valueOf(900L);
        BigInteger num0 = BigInteger.valueOf(0L);
        for (int idx = 0; idx < count; idx += len) {
            tmp.setLength(0);
            len = Math.min(44, count - idx);
            String part = '1' + msg.substring(startpos + idx, startpos + idx + len);
            BigInteger bigint = new BigInteger(part);
            do {
                tmp.append((char)bigint.mod(num900).intValue());
            } while (!(bigint = bigint.divide(num900)).equals(num0));
            for (int i = tmp.length() - 1; i >= 0; --i) {
                sb.append(tmp.charAt(i));
            }
        }
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isAlphaUpper(char ch) {
        return ch == ' ' || ch >= 'A' && ch <= 'Z';
    }

    private static boolean isAlphaLower(char ch) {
        return ch == ' ' || ch >= 'a' && ch <= 'z';
    }

    private static boolean isMixed(char ch) {
        return MIXED[ch] != -1;
    }

    private static boolean isPunctuation(char ch) {
        return PUNCTUATION[ch] != -1;
    }

    private static boolean isText(char ch) {
        return ch == '\t' || ch == '\n' || ch == '\r' || ch >= ' ' && ch <= '~';
    }

    private static int determineConsecutiveDigitCount(CharSequence msg, int startpos) {
        int count = 0;
        int idx = startpos;
        int len = msg.length();
        if (idx < len) {
            char ch = msg.charAt(idx);
            while (PDF417HighLevelEncoder.isDigit(ch) && idx < len) {
                ++count;
                if (++idx >= len) continue;
                ch = msg.charAt(idx);
            }
        }
        return count;
    }

    private static int determineConsecutiveTextCount(CharSequence msg, int startpos) {
        int len = msg.length();
        int idx = startpos;
        while (idx < len) {
            int numericCount;
            char ch = msg.charAt(idx);
            for (numericCount = 0; numericCount < 13 && PDF417HighLevelEncoder.isDigit(ch) && idx < len; ++numericCount) {
                if (++idx >= len) continue;
                ch = msg.charAt(idx);
            }
            if (numericCount >= 13) {
                return idx - startpos - numericCount;
            }
            if (numericCount > 0) continue;
            ch = msg.charAt(idx);
            if (!PDF417HighLevelEncoder.isText(ch)) break;
            ++idx;
        }
        return idx - startpos;
    }

    private static int determineConsecutiveBinaryCount(String msg, int startpos, Charset encoding) throws WriterException {
        int idx;
        CharsetEncoder encoder = encoding.newEncoder();
        int len = msg.length();
        for (idx = startpos; idx < len; ++idx) {
            int i;
            char ch = msg.charAt(idx);
            int numericCount = 0;
            while (numericCount < 13 && PDF417HighLevelEncoder.isDigit(ch) && (i = idx + ++numericCount) < len) {
                ch = msg.charAt(i);
            }
            if (numericCount >= 13) {
                return idx - startpos;
            }
            ch = msg.charAt(idx);
            if (encoder.canEncode(ch)) continue;
            throw new WriterException("Non-encodable character detected: " + ch + " (Unicode: " + ch + ')');
        }
        return idx - startpos;
    }

    private static void encodingECI(int eci, StringBuilder sb) throws WriterException {
        if (eci >= 0 && eci < 900) {
            sb.append('\u039f');
            sb.append((char)eci);
        } else if (eci < 810900) {
            sb.append('\u039e');
            sb.append((char)(eci / 900 - 1));
            sb.append((char)(eci % 900));
        } else if (eci < 811800) {
            sb.append('\u039d');
            sb.append((char)(810900 - eci));
        } else {
            throw new WriterException("ECI number not in valid range from 0..811799, but was " + eci);
        }
    }

    static {
        byte b;
        int i;
        TEXT_MIXED_RAW = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 38, 13, 9, 44, 58, 35, 45, 46, 36, 47, 43, 37, 42, 61, 94, 0, 32, 0, 0, 0};
        TEXT_PUNCTUATION_RAW = new byte[]{59, 60, 62, 64, 91, 92, 93, 95, 96, 126, 33, 13, 9, 44, 58, 10, 45, 46, 36, 47, 34, 124, 42, 40, 41, 63, 123, 125, 39, 0};
        MIXED = new byte[128];
        PUNCTUATION = new byte[128];
        DEFAULT_ENCODING = Charset.forName("ISO-8859-1");
        Arrays.fill(MIXED, (byte)-1);
        for (i = 0; i < TEXT_MIXED_RAW.length; i = (int)((byte)(i + 1))) {
            b = TEXT_MIXED_RAW[i];
            if (b <= 0) continue;
            PDF417HighLevelEncoder.MIXED[b] = i;
        }
        Arrays.fill(PUNCTUATION, (byte)-1);
        for (i = 0; i < TEXT_PUNCTUATION_RAW.length; i = (int)((byte)(i + 1))) {
            b = TEXT_PUNCTUATION_RAW[i];
            if (b <= 0) continue;
            PDF417HighLevelEncoder.PUNCTUATION[b] = i;
        }
    }
}

