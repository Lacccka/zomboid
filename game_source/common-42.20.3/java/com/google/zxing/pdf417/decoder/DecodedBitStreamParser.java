/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.pdf417.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.pdf417.PDF417ResultMetadata;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

final class DecodedBitStreamParser {
    private static final int TEXT_COMPACTION_MODE_LATCH = 900;
    private static final int BYTE_COMPACTION_MODE_LATCH = 901;
    private static final int NUMERIC_COMPACTION_MODE_LATCH = 902;
    private static final int BYTE_COMPACTION_MODE_LATCH_6 = 924;
    private static final int ECI_USER_DEFINED = 925;
    private static final int ECI_GENERAL_PURPOSE = 926;
    private static final int ECI_CHARSET = 927;
    private static final int BEGIN_MACRO_PDF417_CONTROL_BLOCK = 928;
    private static final int BEGIN_MACRO_PDF417_OPTIONAL_FIELD = 923;
    private static final int MACRO_PDF417_TERMINATOR = 922;
    private static final int MODE_SHIFT_TO_BYTE_COMPACTION_MODE = 913;
    private static final int MAX_NUMERIC_CODEWORDS = 15;
    private static final int PL = 25;
    private static final int LL = 27;
    private static final int AS = 27;
    private static final int ML = 28;
    private static final int AL = 28;
    private static final int PS = 29;
    private static final int PAL = 29;
    private static final char[] PUNCT_CHARS;
    private static final char[] MIXED_CHARS;
    private static final Charset DEFAULT_ENCODING;
    private static final BigInteger[] EXP900;
    private static final int NUMBER_OF_SEQUENCE_CODEWORDS = 2;

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(int[] codewords, String ecLevel) throws FormatException {
        StringBuilder result = new StringBuilder(codewords.length * 2);
        Charset encoding = DEFAULT_ENCODING;
        int codeIndex = 1;
        int code = codewords[codeIndex++];
        PDF417ResultMetadata resultMetadata = new PDF417ResultMetadata();
        while (codeIndex < codewords[0]) {
            switch (code) {
                case 900: {
                    codeIndex = DecodedBitStreamParser.textCompaction(codewords, codeIndex, result);
                    break;
                }
                case 901: 
                case 924: {
                    codeIndex = DecodedBitStreamParser.byteCompaction(code, codewords, encoding, codeIndex, result);
                    break;
                }
                case 913: {
                    result.append((char)codewords[codeIndex++]);
                    break;
                }
                case 902: {
                    codeIndex = DecodedBitStreamParser.numericCompaction(codewords, codeIndex, result);
                    break;
                }
                case 927: {
                    CharacterSetECI charsetECI = CharacterSetECI.getCharacterSetECIByValue(codewords[codeIndex++]);
                    encoding = Charset.forName(charsetECI.name());
                    break;
                }
                case 926: {
                    codeIndex += 2;
                    break;
                }
                case 925: {
                    ++codeIndex;
                    break;
                }
                case 928: {
                    codeIndex = DecodedBitStreamParser.decodeMacroBlock(codewords, codeIndex, resultMetadata);
                    break;
                }
                case 922: 
                case 923: {
                    throw FormatException.getFormatInstance();
                }
                default: {
                    --codeIndex;
                    codeIndex = DecodedBitStreamParser.textCompaction(codewords, codeIndex, result);
                }
            }
            if (codeIndex < codewords.length) {
                code = codewords[codeIndex++];
                continue;
            }
            throw FormatException.getFormatInstance();
        }
        if (result.length() == 0) {
            throw FormatException.getFormatInstance();
        }
        DecoderResult decoderResult = new DecoderResult(null, result.toString(), null, ecLevel);
        decoderResult.setOther(resultMetadata);
        return decoderResult;
    }

    private static int decodeMacroBlock(int[] codewords, int codeIndex, PDF417ResultMetadata resultMetadata) throws FormatException {
        if (codeIndex + 2 > codewords[0]) {
            throw FormatException.getFormatInstance();
        }
        int[] segmentIndexArray = new int[2];
        int i = 0;
        while (i < 2) {
            segmentIndexArray[i] = codewords[codeIndex];
            ++i;
            ++codeIndex;
        }
        resultMetadata.setSegmentIndex(Integer.parseInt(DecodedBitStreamParser.decodeBase900toBase10(segmentIndexArray, 2)));
        StringBuilder fileId = new StringBuilder();
        codeIndex = DecodedBitStreamParser.textCompaction(codewords, codeIndex, fileId);
        resultMetadata.setFileId(fileId.toString());
        if (codewords[codeIndex] == 923) {
            int[] additionalOptionCodeWords = new int[codewords[0] - ++codeIndex];
            int additionalOptionCodeWordsIndex = 0;
            boolean end = false;
            block4: while (codeIndex < codewords[0] && !end) {
                int code;
                if ((code = codewords[codeIndex++]) < 900) {
                    additionalOptionCodeWords[additionalOptionCodeWordsIndex++] = code;
                    continue;
                }
                switch (code) {
                    case 922: {
                        resultMetadata.setLastSegment(true);
                        ++codeIndex;
                        end = true;
                        continue block4;
                    }
                }
                throw FormatException.getFormatInstance();
            }
            resultMetadata.setOptionalData(Arrays.copyOf(additionalOptionCodeWords, additionalOptionCodeWordsIndex));
        } else if (codewords[codeIndex] == 922) {
            resultMetadata.setLastSegment(true);
            ++codeIndex;
        }
        return codeIndex;
    }

    private static int textCompaction(int[] codewords, int codeIndex, StringBuilder result) {
        int[] textCompactionData = new int[(codewords[0] - codeIndex) * 2];
        int[] byteCompactionData = new int[(codewords[0] - codeIndex) * 2];
        int index = 0;
        boolean end = false;
        while (codeIndex < codewords[0] && !end) {
            int code;
            if ((code = codewords[codeIndex++]) < 900) {
                textCompactionData[index] = code / 30;
                textCompactionData[index + 1] = code % 30;
                index += 2;
                continue;
            }
            switch (code) {
                case 900: {
                    textCompactionData[index++] = 900;
                    break;
                }
                case 901: 
                case 902: 
                case 922: 
                case 923: 
                case 924: 
                case 928: {
                    --codeIndex;
                    end = true;
                    break;
                }
                case 913: {
                    textCompactionData[index] = 913;
                    byteCompactionData[index] = code = codewords[codeIndex++];
                    ++index;
                }
            }
        }
        DecodedBitStreamParser.decodeTextCompaction(textCompactionData, byteCompactionData, index, result);
        return codeIndex;
    }

    private static void decodeTextCompaction(int[] textCompactionData, int[] byteCompactionData, int length, StringBuilder result) {
        Mode subMode = Mode.ALPHA;
        Mode priorToShiftMode = Mode.ALPHA;
        for (int i = 0; i < length; ++i) {
            int subModeCh = textCompactionData[i];
            int ch = 0;
            switch (subMode) {
                case ALPHA: {
                    if (subModeCh < 26) {
                        ch = (char)(65 + subModeCh);
                        break;
                    }
                    if (subModeCh == 26) {
                        ch = 32;
                        break;
                    }
                    if (subModeCh == 27) {
                        subMode = Mode.LOWER;
                        break;
                    }
                    if (subModeCh == 28) {
                        subMode = Mode.MIXED;
                        break;
                    }
                    if (subModeCh == 29) {
                        priorToShiftMode = subMode;
                        subMode = Mode.PUNCT_SHIFT;
                        break;
                    }
                    if (subModeCh == 913) {
                        result.append((char)byteCompactionData[i]);
                        break;
                    }
                    if (subModeCh != 900) break;
                    subMode = Mode.ALPHA;
                    break;
                }
                case LOWER: {
                    if (subModeCh < 26) {
                        ch = (char)(97 + subModeCh);
                        break;
                    }
                    if (subModeCh == 26) {
                        ch = 32;
                        break;
                    }
                    if (subModeCh == 27) {
                        priorToShiftMode = subMode;
                        subMode = Mode.ALPHA_SHIFT;
                        break;
                    }
                    if (subModeCh == 28) {
                        subMode = Mode.MIXED;
                        break;
                    }
                    if (subModeCh == 29) {
                        priorToShiftMode = subMode;
                        subMode = Mode.PUNCT_SHIFT;
                        break;
                    }
                    if (subModeCh == 913) {
                        result.append((char)byteCompactionData[i]);
                        break;
                    }
                    if (subModeCh != 900) break;
                    subMode = Mode.ALPHA;
                    break;
                }
                case MIXED: {
                    if (subModeCh < 25) {
                        ch = MIXED_CHARS[subModeCh];
                        break;
                    }
                    if (subModeCh == 25) {
                        subMode = Mode.PUNCT;
                        break;
                    }
                    if (subModeCh == 26) {
                        ch = 32;
                        break;
                    }
                    if (subModeCh == 27) {
                        subMode = Mode.LOWER;
                        break;
                    }
                    if (subModeCh == 28) {
                        subMode = Mode.ALPHA;
                        break;
                    }
                    if (subModeCh == 29) {
                        priorToShiftMode = subMode;
                        subMode = Mode.PUNCT_SHIFT;
                        break;
                    }
                    if (subModeCh == 913) {
                        result.append((char)byteCompactionData[i]);
                        break;
                    }
                    if (subModeCh != 900) break;
                    subMode = Mode.ALPHA;
                    break;
                }
                case PUNCT: {
                    if (subModeCh < 29) {
                        ch = PUNCT_CHARS[subModeCh];
                        break;
                    }
                    if (subModeCh == 29) {
                        subMode = Mode.ALPHA;
                        break;
                    }
                    if (subModeCh == 913) {
                        result.append((char)byteCompactionData[i]);
                        break;
                    }
                    if (subModeCh != 900) break;
                    subMode = Mode.ALPHA;
                    break;
                }
                case ALPHA_SHIFT: {
                    subMode = priorToShiftMode;
                    if (subModeCh < 26) {
                        ch = (char)(65 + subModeCh);
                        break;
                    }
                    if (subModeCh == 26) {
                        ch = 32;
                        break;
                    }
                    if (subModeCh != 900) break;
                    subMode = Mode.ALPHA;
                    break;
                }
                case PUNCT_SHIFT: {
                    subMode = priorToShiftMode;
                    if (subModeCh < 29) {
                        ch = PUNCT_CHARS[subModeCh];
                        break;
                    }
                    if (subModeCh == 29) {
                        subMode = Mode.ALPHA;
                        break;
                    }
                    if (subModeCh == 913) {
                        result.append((char)byteCompactionData[i]);
                        break;
                    }
                    if (subModeCh != 900) break;
                    subMode = Mode.ALPHA;
                }
            }
            if (ch == 0) continue;
            result.append((char)ch);
        }
    }

    private static int byteCompaction(int mode, int[] codewords, Charset encoding, int codeIndex, StringBuilder result) {
        ByteArrayOutputStream decodedBytes = new ByteArrayOutputStream();
        if (mode == 901) {
            int count = 0;
            long value = 0L;
            int[] byteCompactedCodewords = new int[6];
            boolean end = false;
            int nextCode = codewords[codeIndex++];
            while (codeIndex < codewords[0] && !end) {
                byteCompactedCodewords[count++] = nextCode;
                value = 900L * value + (long)nextCode;
                if ((nextCode = codewords[codeIndex++]) == 900 || nextCode == 901 || nextCode == 902 || nextCode == 924 || nextCode == 928 || nextCode == 923 || nextCode == 922) {
                    --codeIndex;
                    end = true;
                    continue;
                }
                if (count % 5 != 0 || count <= 0) continue;
                for (int j = 0; j < 6; ++j) {
                    decodedBytes.write((byte)(value >> 8 * (5 - j)));
                }
                value = 0L;
                count = 0;
            }
            if (codeIndex == codewords[0] && nextCode < 900) {
                byteCompactedCodewords[count++] = nextCode;
            }
            for (int i = 0; i < count; ++i) {
                decodedBytes.write((byte)byteCompactedCodewords[i]);
            }
        } else if (mode == 924) {
            int count = 0;
            long value = 0L;
            boolean end = false;
            while (codeIndex < codewords[0] && !end) {
                int code;
                if ((code = codewords[codeIndex++]) < 900) {
                    ++count;
                    value = 900L * value + (long)code;
                } else if (code == 900 || code == 901 || code == 902 || code == 924 || code == 928 || code == 923 || code == 922) {
                    --codeIndex;
                    end = true;
                }
                if (count % 5 != 0 || count <= 0) continue;
                for (int j = 0; j < 6; ++j) {
                    decodedBytes.write((byte)(value >> 8 * (5 - j)));
                }
                value = 0L;
                count = 0;
            }
        }
        result.append(new String(decodedBytes.toByteArray(), encoding));
        return codeIndex;
    }

    private static int numericCompaction(int[] codewords, int codeIndex, StringBuilder result) throws FormatException {
        int count = 0;
        boolean end = false;
        int[] numericCodewords = new int[15];
        while (codeIndex < codewords[0] && !end) {
            int code = codewords[codeIndex++];
            if (codeIndex == codewords[0]) {
                end = true;
            }
            if (code < 900) {
                numericCodewords[count] = code;
                ++count;
            } else if (code == 900 || code == 901 || code == 924 || code == 928 || code == 923 || code == 922) {
                --codeIndex;
                end = true;
            }
            if (count % 15 != 0 && code != 902 && !end || count <= 0) continue;
            String s = DecodedBitStreamParser.decodeBase900toBase10(numericCodewords, count);
            result.append(s);
            count = 0;
        }
        return codeIndex;
    }

    private static String decodeBase900toBase10(int[] codewords, int count) throws FormatException {
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < count; ++i) {
            result = result.add(EXP900[count - i - 1].multiply(BigInteger.valueOf(codewords[i])));
        }
        String resultString = result.toString();
        if (resultString.charAt(0) != '1') {
            throw FormatException.getFormatInstance();
        }
        return resultString.substring(1);
    }

    static {
        BigInteger nineHundred;
        PUNCT_CHARS = new char[]{';', '<', '>', '@', '[', '\\', ']', '_', '`', '~', '!', '\r', '\t', ',', ':', '\n', '-', '.', '$', '/', '\"', '|', '*', '(', ')', '?', '{', '}', '\''};
        MIXED_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '&', '\r', '\t', ',', ':', '#', '-', '.', '$', '/', '+', '%', '*', '=', '^'};
        DEFAULT_ENCODING = Charset.forName("ISO-8859-1");
        EXP900 = new BigInteger[16];
        DecodedBitStreamParser.EXP900[0] = BigInteger.ONE;
        DecodedBitStreamParser.EXP900[1] = nineHundred = BigInteger.valueOf(900L);
        for (int i = 2; i < EXP900.length; ++i) {
            DecodedBitStreamParser.EXP900[i] = EXP900[i - 1].multiply(nineHundred);
        }
    }

    private static enum Mode {
        ALPHA,
        LOWER,
        MIXED,
        PUNCT,
        ALPHA_SHIFT,
        PUNCT_SHIFT;

    }
}

