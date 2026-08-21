/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.BitSource;
import com.google.zxing.common.DecoderResult;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;

final class DecodedBitStreamParser {
    private static final char[] C40_BASIC_SET_CHARS = new char[]{'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static final char[] C40_SHIFT2_SET_CHARS = new char[]{'!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_'};
    private static final char[] TEXT_BASIC_SET_CHARS = new char[]{'*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] TEXT_SHIFT2_SET_CHARS = C40_SHIFT2_SET_CHARS;
    private static final char[] TEXT_SHIFT3_SET_CHARS = new char[]{'`', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '{', '|', '}', '~', '\u007f'};

    private DecodedBitStreamParser() {
    }

    static DecoderResult decode(byte[] bytes) throws FormatException {
        BitSource bits = new BitSource(bytes);
        StringBuilder result = new StringBuilder(100);
        StringBuilder resultTrailer = new StringBuilder(0);
        ArrayList<byte[]> byteSegments = new ArrayList<byte[]>(1);
        Mode mode = Mode.ASCII_ENCODE;
        do {
            if (mode == Mode.ASCII_ENCODE) {
                mode = DecodedBitStreamParser.decodeAsciiSegment(bits, result, resultTrailer);
                continue;
            }
            switch (mode) {
                case C40_ENCODE: {
                    DecodedBitStreamParser.decodeC40Segment(bits, result);
                    break;
                }
                case TEXT_ENCODE: {
                    DecodedBitStreamParser.decodeTextSegment(bits, result);
                    break;
                }
                case ANSIX12_ENCODE: {
                    DecodedBitStreamParser.decodeAnsiX12Segment(bits, result);
                    break;
                }
                case EDIFACT_ENCODE: {
                    DecodedBitStreamParser.decodeEdifactSegment(bits, result);
                    break;
                }
                case BASE256_ENCODE: {
                    DecodedBitStreamParser.decodeBase256Segment(bits, result, byteSegments);
                    break;
                }
                default: {
                    throw FormatException.getFormatInstance();
                }
            }
            mode = Mode.ASCII_ENCODE;
        } while (mode != Mode.PAD_ENCODE && bits.available() > 0);
        if (resultTrailer.length() > 0) {
            result.append((CharSequence)resultTrailer);
        }
        return new DecoderResult(bytes, result.toString(), byteSegments.isEmpty() ? null : byteSegments, null);
    }

    private static Mode decodeAsciiSegment(BitSource bits, StringBuilder result, StringBuilder resultTrailer) throws FormatException {
        boolean upperShift = false;
        do {
            int oneByte;
            if ((oneByte = bits.readBits(8)) == 0) {
                throw FormatException.getFormatInstance();
            }
            if (oneByte <= 128) {
                if (upperShift) {
                    oneByte += 128;
                }
                result.append((char)(oneByte - 1));
                return Mode.ASCII_ENCODE;
            }
            if (oneByte == 129) {
                return Mode.PAD_ENCODE;
            }
            if (oneByte <= 229) {
                int value = oneByte - 130;
                if (value < 10) {
                    result.append('0');
                }
                result.append(value);
                continue;
            }
            if (oneByte == 230) {
                return Mode.C40_ENCODE;
            }
            if (oneByte == 231) {
                return Mode.BASE256_ENCODE;
            }
            if (oneByte == 232) {
                result.append('\u001d');
                continue;
            }
            if (oneByte == 233 || oneByte == 234) continue;
            if (oneByte == 235) {
                upperShift = true;
                continue;
            }
            if (oneByte == 236) {
                result.append("[)>\u001e05\u001d");
                resultTrailer.insert(0, "\u001e\u0004");
                continue;
            }
            if (oneByte == 237) {
                result.append("[)>\u001e06\u001d");
                resultTrailer.insert(0, "\u001e\u0004");
                continue;
            }
            if (oneByte == 238) {
                return Mode.ANSIX12_ENCODE;
            }
            if (oneByte == 239) {
                return Mode.TEXT_ENCODE;
            }
            if (oneByte == 240) {
                return Mode.EDIFACT_ENCODE;
            }
            if (oneByte == 241 || oneByte < 242 || oneByte == 254 && bits.available() == 0) continue;
            throw FormatException.getFormatInstance();
        } while (bits.available() > 0);
        return Mode.ASCII_ENCODE;
    }

    private static void decodeC40Segment(BitSource bits, StringBuilder result) throws FormatException {
        boolean upperShift = false;
        int[] cValues = new int[3];
        int shift = 0;
        do {
            if (bits.available() == 8) {
                return;
            }
            int firstByte = bits.readBits(8);
            if (firstByte == 254) {
                return;
            }
            DecodedBitStreamParser.parseTwoBytes(firstByte, bits.readBits(8), cValues);
            block7: for (int i = 0; i < 3; ++i) {
                int cValue = cValues[i];
                switch (shift) {
                    case 0: {
                        char c40char;
                        if (cValue < 3) {
                            shift = cValue + 1;
                            continue block7;
                        }
                        if (cValue < C40_BASIC_SET_CHARS.length) {
                            c40char = C40_BASIC_SET_CHARS[cValue];
                            if (upperShift) {
                                result.append((char)(c40char + 128));
                                upperShift = false;
                                continue block7;
                            }
                            result.append(c40char);
                            continue block7;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    case 1: {
                        if (upperShift) {
                            result.append((char)(cValue + 128));
                            upperShift = false;
                        } else {
                            result.append((char)cValue);
                        }
                        shift = 0;
                        continue block7;
                    }
                    case 2: {
                        char c40char;
                        if (cValue < C40_SHIFT2_SET_CHARS.length) {
                            c40char = C40_SHIFT2_SET_CHARS[cValue];
                            if (upperShift) {
                                result.append((char)(c40char + 128));
                                upperShift = false;
                            } else {
                                result.append(c40char);
                            }
                        } else if (cValue == 27) {
                            result.append('\u001d');
                        } else if (cValue == 30) {
                            upperShift = true;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                        shift = 0;
                        continue block7;
                    }
                    case 3: {
                        if (upperShift) {
                            result.append((char)(cValue + 224));
                            upperShift = false;
                        } else {
                            result.append((char)(cValue + 96));
                        }
                        shift = 0;
                        continue block7;
                    }
                    default: {
                        throw FormatException.getFormatInstance();
                    }
                }
            }
        } while (bits.available() > 0);
    }

    private static void decodeTextSegment(BitSource bits, StringBuilder result) throws FormatException {
        boolean upperShift = false;
        int[] cValues = new int[3];
        int shift = 0;
        do {
            if (bits.available() == 8) {
                return;
            }
            int firstByte = bits.readBits(8);
            if (firstByte == 254) {
                return;
            }
            DecodedBitStreamParser.parseTwoBytes(firstByte, bits.readBits(8), cValues);
            block7: for (int i = 0; i < 3; ++i) {
                int cValue = cValues[i];
                switch (shift) {
                    case 0: {
                        char textChar;
                        if (cValue < 3) {
                            shift = cValue + 1;
                            continue block7;
                        }
                        if (cValue < TEXT_BASIC_SET_CHARS.length) {
                            textChar = TEXT_BASIC_SET_CHARS[cValue];
                            if (upperShift) {
                                result.append((char)(textChar + 128));
                                upperShift = false;
                                continue block7;
                            }
                            result.append(textChar);
                            continue block7;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    case 1: {
                        if (upperShift) {
                            result.append((char)(cValue + 128));
                            upperShift = false;
                        } else {
                            result.append((char)cValue);
                        }
                        shift = 0;
                        continue block7;
                    }
                    case 2: {
                        char textChar;
                        if (cValue < TEXT_SHIFT2_SET_CHARS.length) {
                            textChar = TEXT_SHIFT2_SET_CHARS[cValue];
                            if (upperShift) {
                                result.append((char)(textChar + 128));
                                upperShift = false;
                            } else {
                                result.append(textChar);
                            }
                        } else if (cValue == 27) {
                            result.append('\u001d');
                        } else if (cValue == 30) {
                            upperShift = true;
                        } else {
                            throw FormatException.getFormatInstance();
                        }
                        shift = 0;
                        continue block7;
                    }
                    case 3: {
                        char textChar;
                        if (cValue < TEXT_SHIFT3_SET_CHARS.length) {
                            textChar = TEXT_SHIFT3_SET_CHARS[cValue];
                            if (upperShift) {
                                result.append((char)(textChar + 128));
                                upperShift = false;
                            } else {
                                result.append(textChar);
                            }
                            shift = 0;
                            continue block7;
                        }
                        throw FormatException.getFormatInstance();
                    }
                    default: {
                        throw FormatException.getFormatInstance();
                    }
                }
            }
        } while (bits.available() > 0);
    }

    private static void decodeAnsiX12Segment(BitSource bits, StringBuilder result) throws FormatException {
        int[] cValues = new int[3];
        do {
            if (bits.available() == 8) {
                return;
            }
            int firstByte = bits.readBits(8);
            if (firstByte == 254) {
                return;
            }
            DecodedBitStreamParser.parseTwoBytes(firstByte, bits.readBits(8), cValues);
            for (int i = 0; i < 3; ++i) {
                int cValue = cValues[i];
                if (cValue == 0) {
                    result.append('\r');
                    continue;
                }
                if (cValue == 1) {
                    result.append('*');
                    continue;
                }
                if (cValue == 2) {
                    result.append('>');
                    continue;
                }
                if (cValue == 3) {
                    result.append(' ');
                    continue;
                }
                if (cValue < 14) {
                    result.append((char)(cValue + 44));
                    continue;
                }
                if (cValue < 40) {
                    result.append((char)(cValue + 51));
                    continue;
                }
                throw FormatException.getFormatInstance();
            }
        } while (bits.available() > 0);
    }

    private static void parseTwoBytes(int firstByte, int secondByte, int[] result) {
        int temp;
        int fullBitValue = (firstByte << 8) + secondByte - 1;
        result[0] = temp = fullBitValue / 1600;
        fullBitValue -= temp * 1600;
        result[1] = temp = fullBitValue / 40;
        result[2] = fullBitValue - temp * 40;
    }

    private static void decodeEdifactSegment(BitSource bits, StringBuilder result) {
        do {
            if (bits.available() <= 16) {
                return;
            }
            for (int i = 0; i < 4; ++i) {
                int edifactValue = bits.readBits(6);
                if (edifactValue == 31) {
                    int bitsLeft = 8 - bits.getBitOffset();
                    if (bitsLeft != 8) {
                        bits.readBits(bitsLeft);
                    }
                    return;
                }
                if ((edifactValue & 0x20) == 0) {
                    edifactValue |= 0x40;
                }
                result.append((char)edifactValue);
            }
        } while (bits.available() > 0);
    }

    private static void decodeBase256Segment(BitSource bits, StringBuilder result, Collection<byte[]> byteSegments) throws FormatException {
        int codewordPosition = 1 + bits.getByteOffset();
        int d1 = DecodedBitStreamParser.unrandomize255State(bits.readBits(8), codewordPosition++);
        int count = d1 == 0 ? bits.available() / 8 : (d1 < 250 ? d1 : 250 * (d1 - 249) + DecodedBitStreamParser.unrandomize255State(bits.readBits(8), codewordPosition++));
        if (count < 0) {
            throw FormatException.getFormatInstance();
        }
        byte[] bytes = new byte[count];
        for (int i = 0; i < count; ++i) {
            if (bits.available() < 8) {
                throw FormatException.getFormatInstance();
            }
            bytes[i] = (byte)DecodedBitStreamParser.unrandomize255State(bits.readBits(8), codewordPosition++);
        }
        byteSegments.add(bytes);
        try {
            result.append(new String(bytes, "ISO8859_1"));
        }
        catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Platform does not support required encoding: " + uee);
        }
    }

    private static int unrandomize255State(int randomizedBase256Codeword, int base256CodewordPosition) {
        int pseudoRandomNumber = 149 * base256CodewordPosition % 255 + 1;
        int tempVariable = randomizedBase256Codeword - pseudoRandomNumber;
        return tempVariable >= 0 ? tempVariable : tempVariable + 256;
    }

    private static enum Mode {
        PAD_ENCODE,
        ASCII_ENCODE,
        C40_ENCODE,
        TEXT_ENCODE,
        ANSIX12_ENCODE,
        EDIFACT_ENCODE,
        BASE256_ENCODE;

    }
}

