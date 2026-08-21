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
import java.util.ArrayList;
import java.util.Map;

public final class Code128Reader
extends OneDReader {
    static final int[][] CODE_PATTERNS = new int[][]{{2, 1, 2, 2, 2, 2}, {2, 2, 2, 1, 2, 2}, {2, 2, 2, 2, 2, 1}, {1, 2, 1, 2, 2, 3}, {1, 2, 1, 3, 2, 2}, {1, 3, 1, 2, 2, 2}, {1, 2, 2, 2, 1, 3}, {1, 2, 2, 3, 1, 2}, {1, 3, 2, 2, 1, 2}, {2, 2, 1, 2, 1, 3}, {2, 2, 1, 3, 1, 2}, {2, 3, 1, 2, 1, 2}, {1, 1, 2, 2, 3, 2}, {1, 2, 2, 1, 3, 2}, {1, 2, 2, 2, 3, 1}, {1, 1, 3, 2, 2, 2}, {1, 2, 3, 1, 2, 2}, {1, 2, 3, 2, 2, 1}, {2, 2, 3, 2, 1, 1}, {2, 2, 1, 1, 3, 2}, {2, 2, 1, 2, 3, 1}, {2, 1, 3, 2, 1, 2}, {2, 2, 3, 1, 1, 2}, {3, 1, 2, 1, 3, 1}, {3, 1, 1, 2, 2, 2}, {3, 2, 1, 1, 2, 2}, {3, 2, 1, 2, 2, 1}, {3, 1, 2, 2, 1, 2}, {3, 2, 2, 1, 1, 2}, {3, 2, 2, 2, 1, 1}, {2, 1, 2, 1, 2, 3}, {2, 1, 2, 3, 2, 1}, {2, 3, 2, 1, 2, 1}, {1, 1, 1, 3, 2, 3}, {1, 3, 1, 1, 2, 3}, {1, 3, 1, 3, 2, 1}, {1, 1, 2, 3, 1, 3}, {1, 3, 2, 1, 1, 3}, {1, 3, 2, 3, 1, 1}, {2, 1, 1, 3, 1, 3}, {2, 3, 1, 1, 1, 3}, {2, 3, 1, 3, 1, 1}, {1, 1, 2, 1, 3, 3}, {1, 1, 2, 3, 3, 1}, {1, 3, 2, 1, 3, 1}, {1, 1, 3, 1, 2, 3}, {1, 1, 3, 3, 2, 1}, {1, 3, 3, 1, 2, 1}, {3, 1, 3, 1, 2, 1}, {2, 1, 1, 3, 3, 1}, {2, 3, 1, 1, 3, 1}, {2, 1, 3, 1, 1, 3}, {2, 1, 3, 3, 1, 1}, {2, 1, 3, 1, 3, 1}, {3, 1, 1, 1, 2, 3}, {3, 1, 1, 3, 2, 1}, {3, 3, 1, 1, 2, 1}, {3, 1, 2, 1, 1, 3}, {3, 1, 2, 3, 1, 1}, {3, 3, 2, 1, 1, 1}, {3, 1, 4, 1, 1, 1}, {2, 2, 1, 4, 1, 1}, {4, 3, 1, 1, 1, 1}, {1, 1, 1, 2, 2, 4}, {1, 1, 1, 4, 2, 2}, {1, 2, 1, 1, 2, 4}, {1, 2, 1, 4, 2, 1}, {1, 4, 1, 1, 2, 2}, {1, 4, 1, 2, 2, 1}, {1, 1, 2, 2, 1, 4}, {1, 1, 2, 4, 1, 2}, {1, 2, 2, 1, 1, 4}, {1, 2, 2, 4, 1, 1}, {1, 4, 2, 1, 1, 2}, {1, 4, 2, 2, 1, 1}, {2, 4, 1, 2, 1, 1}, {2, 2, 1, 1, 1, 4}, {4, 1, 3, 1, 1, 1}, {2, 4, 1, 1, 1, 2}, {1, 3, 4, 1, 1, 1}, {1, 1, 1, 2, 4, 2}, {1, 2, 1, 1, 4, 2}, {1, 2, 1, 2, 4, 1}, {1, 1, 4, 2, 1, 2}, {1, 2, 4, 1, 1, 2}, {1, 2, 4, 2, 1, 1}, {4, 1, 1, 2, 1, 2}, {4, 2, 1, 1, 1, 2}, {4, 2, 1, 2, 1, 1}, {2, 1, 2, 1, 4, 1}, {2, 1, 4, 1, 2, 1}, {4, 1, 2, 1, 2, 1}, {1, 1, 1, 1, 4, 3}, {1, 1, 1, 3, 4, 1}, {1, 3, 1, 1, 4, 1}, {1, 1, 4, 1, 1, 3}, {1, 1, 4, 3, 1, 1}, {4, 1, 1, 1, 1, 3}, {4, 1, 1, 3, 1, 1}, {1, 1, 3, 1, 4, 1}, {1, 1, 4, 1, 3, 1}, {3, 1, 1, 1, 4, 1}, {4, 1, 1, 1, 3, 1}, {2, 1, 1, 4, 1, 2}, {2, 1, 1, 2, 1, 4}, {2, 1, 1, 2, 3, 2}, {2, 3, 3, 1, 1, 1, 2}};
    private static final float MAX_AVG_VARIANCE = 0.25f;
    private static final float MAX_INDIVIDUAL_VARIANCE = 0.7f;
    private static final int CODE_SHIFT = 98;
    private static final int CODE_CODE_C = 99;
    private static final int CODE_CODE_B = 100;
    private static final int CODE_CODE_A = 101;
    private static final int CODE_FNC_1 = 102;
    private static final int CODE_FNC_2 = 97;
    private static final int CODE_FNC_3 = 96;
    private static final int CODE_FNC_4_A = 101;
    private static final int CODE_FNC_4_B = 100;
    private static final int CODE_START_A = 103;
    private static final int CODE_START_B = 104;
    private static final int CODE_START_C = 105;
    private static final int CODE_STOP = 106;

    private static int[] findStartPattern(BitArray row) throws NotFoundException {
        int width = row.getSize();
        int rowOffset = row.getNextSet(0);
        int counterPosition = 0;
        int[] counters = new int[6];
        int patternStart = rowOffset;
        boolean isWhite = false;
        int patternLength = counters.length;
        for (int i = rowOffset; i < width; ++i) {
            if (row.get(i) ^ isWhite) {
                int n = counterPosition;
                counters[n] = counters[n] + 1;
                continue;
            }
            if (counterPosition == patternLength - 1) {
                float bestVariance = 0.25f;
                int bestMatch = -1;
                for (int startCode = 103; startCode <= 105; ++startCode) {
                    float variance = Code128Reader.patternMatchVariance(counters, CODE_PATTERNS[startCode], 0.7f);
                    if (!(variance < bestVariance)) continue;
                    bestVariance = variance;
                    bestMatch = startCode;
                }
                if (bestMatch >= 0 && row.isRange(Math.max(0, patternStart - (i - patternStart) / 2), patternStart, false)) {
                    return new int[]{patternStart, i, bestMatch};
                }
                patternStart += counters[0] + counters[1];
                System.arraycopy(counters, 2, counters, 0, patternLength - 2);
                counters[patternLength - 2] = 0;
                counters[patternLength - 1] = 0;
                --counterPosition;
            } else {
                ++counterPosition;
            }
            counters[counterPosition] = 1;
            isWhite = !isWhite;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int decodeCode(BitArray row, int[] counters, int rowOffset) throws NotFoundException {
        Code128Reader.recordPattern(row, rowOffset, counters);
        float bestVariance = 0.25f;
        int bestMatch = -1;
        for (int d = 0; d < CODE_PATTERNS.length; ++d) {
            int[] pattern = CODE_PATTERNS[d];
            float variance = Code128Reader.patternMatchVariance(counters, pattern, 0.7f);
            if (!(variance < bestVariance)) continue;
            bestVariance = variance;
            bestMatch = d;
        }
        if (bestMatch >= 0) {
            return bestMatch;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    @Override
    public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType, ?> hints) throws NotFoundException, FormatException, ChecksumException {
        int codeSet;
        boolean convertFNC1 = hints != null && hints.containsKey((Object)DecodeHintType.ASSUME_GS1);
        int[] startPatternInfo = Code128Reader.findStartPattern(row);
        int startCode = startPatternInfo[2];
        ArrayList<Byte> rawCodes = new ArrayList<Byte>(20);
        rawCodes.add((byte)startCode);
        switch (startCode) {
            case 103: {
                codeSet = 101;
                break;
            }
            case 104: {
                codeSet = 100;
                break;
            }
            case 105: {
                codeSet = 99;
                break;
            }
            default: {
                throw FormatException.getFormatInstance();
            }
        }
        boolean done = false;
        boolean isNextShifted = false;
        StringBuilder result = new StringBuilder(20);
        int lastStart = startPatternInfo[0];
        int nextStart = startPatternInfo[1];
        int[] counters = new int[6];
        int lastCode = 0;
        int code = 0;
        int checksumTotal = startCode;
        int multiplier = 0;
        boolean lastCharacterWasPrintable = true;
        boolean upperMode = false;
        boolean shiftUpperMode = false;
        while (!done) {
            boolean unshift;
            block70: {
                unshift = isNextShifted;
                isNextShifted = false;
                lastCode = code;
                code = Code128Reader.decodeCode(row, counters, nextStart);
                rawCodes.add((byte)code);
                if (code != 106) {
                    lastCharacterWasPrintable = true;
                }
                if (code != 106) {
                    checksumTotal += ++multiplier * code;
                }
                lastStart = nextStart;
                for (int counter : counters) {
                    nextStart += counter;
                }
                switch (code) {
                    case 103: 
                    case 104: 
                    case 105: {
                        throw FormatException.getFormatInstance();
                    }
                }
                block8 : switch (codeSet) {
                    case 101: {
                        if (code < 64) {
                            if (shiftUpperMode == upperMode) {
                                result.append((char)(32 + code));
                            } else {
                                result.append((char)(32 + code + 128));
                            }
                            shiftUpperMode = false;
                            break;
                        }
                        if (code < 96) {
                            if (shiftUpperMode == upperMode) {
                                result.append((char)(code - 64));
                            } else {
                                result.append((char)(code + 64));
                            }
                            shiftUpperMode = false;
                            break;
                        }
                        if (code != 106) {
                            lastCharacterWasPrintable = false;
                        }
                        switch (code) {
                            case 102: {
                                if (!convertFNC1) break;
                                if (result.length() == 0) {
                                    result.append("]C1");
                                    break;
                                }
                                result.append('\u001d');
                                break;
                            }
                            case 96: 
                            case 97: {
                                break;
                            }
                            case 101: {
                                if (!upperMode && shiftUpperMode) {
                                    upperMode = true;
                                    shiftUpperMode = false;
                                    break;
                                }
                                if (upperMode && shiftUpperMode) {
                                    upperMode = false;
                                    shiftUpperMode = false;
                                    break;
                                }
                                shiftUpperMode = true;
                                break;
                            }
                            case 98: {
                                isNextShifted = true;
                                codeSet = 100;
                                break;
                            }
                            case 100: {
                                codeSet = 100;
                                break;
                            }
                            case 99: {
                                codeSet = 99;
                                break;
                            }
                            case 106: {
                                done = true;
                            }
                        }
                        break;
                    }
                    case 100: {
                        if (code < 96) {
                            if (shiftUpperMode == upperMode) {
                                result.append((char)(32 + code));
                            } else {
                                result.append((char)(32 + code + 128));
                            }
                            shiftUpperMode = false;
                            break;
                        }
                        if (code != 106) {
                            lastCharacterWasPrintable = false;
                        }
                        switch (code) {
                            case 102: {
                                if (!convertFNC1) break;
                                if (result.length() == 0) {
                                    result.append("]C1");
                                    break;
                                }
                                result.append('\u001d');
                                break;
                            }
                            case 96: 
                            case 97: {
                                break;
                            }
                            case 100: {
                                if (!upperMode && shiftUpperMode) {
                                    upperMode = true;
                                    shiftUpperMode = false;
                                    break;
                                }
                                if (upperMode && shiftUpperMode) {
                                    upperMode = false;
                                    shiftUpperMode = false;
                                    break;
                                }
                                shiftUpperMode = true;
                                break;
                            }
                            case 98: {
                                isNextShifted = true;
                                codeSet = 101;
                                break;
                            }
                            case 101: {
                                codeSet = 101;
                                break;
                            }
                            case 99: {
                                codeSet = 99;
                                break;
                            }
                            case 106: {
                                done = true;
                            }
                        }
                        break;
                    }
                    case 99: {
                        if (code < 100) {
                            if (code < 10) {
                                result.append('0');
                            }
                            result.append(code);
                            break;
                        }
                        if (code != 106) {
                            lastCharacterWasPrintable = false;
                        }
                        switch (code) {
                            case 102: {
                                if (convertFNC1) {
                                    if (result.length() == 0) {
                                        result.append("]C1");
                                        break block8;
                                    }
                                    result.append('\u001d');
                                    break block8;
                                }
                                break block70;
                            }
                            case 101: {
                                codeSet = 101;
                                break block8;
                            }
                            case 100: {
                                codeSet = 100;
                                break block8;
                            }
                            case 106: {
                                done = true;
                            }
                        }
                    }
                }
            }
            if (!unshift) continue;
            codeSet = codeSet == 101 ? 100 : 101;
        }
        int lastPatternSize = nextStart - lastStart;
        if (!row.isRange(nextStart = row.getNextUnset(nextStart), Math.min(row.getSize(), nextStart + (nextStart - lastStart) / 2), false)) {
            throw NotFoundException.getNotFoundInstance();
        }
        if ((checksumTotal -= multiplier * lastCode) % 103 != lastCode) {
            throw ChecksumException.getChecksumInstance();
        }
        int resultLength = result.length();
        if (resultLength == 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (resultLength > 0 && lastCharacterWasPrintable) {
            if (codeSet == 99) {
                result.delete(resultLength - 2, resultLength);
            } else {
                result.delete(resultLength - 1, resultLength);
            }
        }
        float left = (float)(startPatternInfo[1] + startPatternInfo[0]) / 2.0f;
        float right = (float)lastStart + (float)lastPatternSize / 2.0f;
        int rawCodesSize = rawCodes.size();
        byte[] rawBytes = new byte[rawCodesSize];
        for (int i = 0; i < rawCodesSize; ++i) {
            rawBytes[i] = (Byte)rawCodes.get(i);
        }
        return new Result(result.toString(), rawBytes, new ResultPoint[]{new ResultPoint(left, rowNumber), new ResultPoint(right, rowNumber)}, BarcodeFormat.CODE_128);
    }
}

