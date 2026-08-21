/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.qrcode.encoder;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.CharacterSetECI;
import com.google.zxing.common.reedsolomon.GenericGF;
import com.google.zxing.common.reedsolomon.ReedSolomonEncoder;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.decoder.Mode;
import com.google.zxing.qrcode.decoder.Version;
import com.google.zxing.qrcode.encoder.BlockPair;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.MaskUtil;
import com.google.zxing.qrcode.encoder.MatrixUtil;
import com.google.zxing.qrcode.encoder.QRCode;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

public final class Encoder {
    private static final int[] ALPHANUMERIC_TABLE = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 36, -1, -1, -1, 37, 38, -1, -1, -1, -1, 39, 40, -1, 41, 42, 43, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 44, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1};
    static final String DEFAULT_BYTE_MODE_ENCODING = "ISO-8859-1";

    private Encoder() {
    }

    private static int calculateMaskPenalty(ByteMatrix matrix) {
        return MaskUtil.applyMaskPenaltyRule1(matrix) + MaskUtil.applyMaskPenaltyRule2(matrix) + MaskUtil.applyMaskPenaltyRule3(matrix) + MaskUtil.applyMaskPenaltyRule4(matrix);
    }

    public static QRCode encode(String content, ErrorCorrectionLevel ecLevel) throws WriterException {
        return Encoder.encode(content, ecLevel, null);
    }

    public static QRCode encode(String content, ErrorCorrectionLevel ecLevel, Map<EncodeHintType, ?> hints) throws WriterException {
        CharacterSetECI eci;
        String encoding;
        String string = encoding = hints == null ? null : (String)hints.get((Object)EncodeHintType.CHARACTER_SET);
        if (encoding == null) {
            encoding = DEFAULT_BYTE_MODE_ENCODING;
        }
        Mode mode = Encoder.chooseMode(content, encoding);
        BitArray headerBits = new BitArray();
        if (mode == Mode.BYTE && !DEFAULT_BYTE_MODE_ENCODING.equals(encoding) && (eci = CharacterSetECI.getCharacterSetECIByName(encoding)) != null) {
            Encoder.appendECI(eci, headerBits);
        }
        Encoder.appendModeInfo(mode, headerBits);
        BitArray dataBits = new BitArray();
        Encoder.appendBytes(content, mode, dataBits, encoding);
        int provisionalBitsNeeded = headerBits.getSize() + mode.getCharacterCountBits(Version.getVersionForNumber(1)) + dataBits.getSize();
        Version provisionalVersion = Encoder.chooseVersion(provisionalBitsNeeded, ecLevel);
        int bitsNeeded = headerBits.getSize() + mode.getCharacterCountBits(provisionalVersion) + dataBits.getSize();
        Version version = Encoder.chooseVersion(bitsNeeded, ecLevel);
        BitArray headerAndDataBits = new BitArray();
        headerAndDataBits.appendBitArray(headerBits);
        int numLetters = mode == Mode.BYTE ? dataBits.getSizeInBytes() : content.length();
        Encoder.appendLengthInfo(numLetters, version, mode, headerAndDataBits);
        headerAndDataBits.appendBitArray(dataBits);
        Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevel);
        int numDataBytes = version.getTotalCodewords() - ecBlocks.getTotalECCodewords();
        Encoder.terminateBits(numDataBytes, headerAndDataBits);
        BitArray finalBits = Encoder.interleaveWithECBytes(headerAndDataBits, version.getTotalCodewords(), numDataBytes, ecBlocks.getNumBlocks());
        QRCode qrCode = new QRCode();
        qrCode.setECLevel(ecLevel);
        qrCode.setMode(mode);
        qrCode.setVersion(version);
        int dimension = version.getDimensionForVersion();
        ByteMatrix matrix = new ByteMatrix(dimension, dimension);
        int maskPattern = Encoder.chooseMaskPattern(finalBits, ecLevel, version, matrix);
        qrCode.setMaskPattern(maskPattern);
        MatrixUtil.buildMatrix(finalBits, ecLevel, version, maskPattern, matrix);
        qrCode.setMatrix(matrix);
        return qrCode;
    }

    static int getAlphanumericCode(int code) {
        if (code < ALPHANUMERIC_TABLE.length) {
            return ALPHANUMERIC_TABLE[code];
        }
        return -1;
    }

    public static Mode chooseMode(String content) {
        return Encoder.chooseMode(content, null);
    }

    private static Mode chooseMode(String content, String encoding) {
        if ("Shift_JIS".equals(encoding)) {
            return Encoder.isOnlyDoubleByteKanji(content) ? Mode.KANJI : Mode.BYTE;
        }
        boolean hasNumeric = false;
        boolean hasAlphanumeric = false;
        for (int i = 0; i < content.length(); ++i) {
            char c = content.charAt(i);
            if (c >= '0' && c <= '9') {
                hasNumeric = true;
                continue;
            }
            if (Encoder.getAlphanumericCode(c) != -1) {
                hasAlphanumeric = true;
                continue;
            }
            return Mode.BYTE;
        }
        if (hasAlphanumeric) {
            return Mode.ALPHANUMERIC;
        }
        if (hasNumeric) {
            return Mode.NUMERIC;
        }
        return Mode.BYTE;
    }

    private static boolean isOnlyDoubleByteKanji(String content) {
        byte[] bytes;
        try {
            bytes = content.getBytes("Shift_JIS");
        }
        catch (UnsupportedEncodingException ignored) {
            return false;
        }
        int length = bytes.length;
        if (length % 2 != 0) {
            return false;
        }
        for (int i = 0; i < length; i += 2) {
            int byte1 = bytes[i] & 0xFF;
            if (byte1 >= 129 && byte1 <= 159 || byte1 >= 224 && byte1 <= 235) continue;
            return false;
        }
        return true;
    }

    private static int chooseMaskPattern(BitArray bits, ErrorCorrectionLevel ecLevel, Version version, ByteMatrix matrix) throws WriterException {
        int minPenalty = Integer.MAX_VALUE;
        int bestMaskPattern = -1;
        for (int maskPattern = 0; maskPattern < 8; ++maskPattern) {
            MatrixUtil.buildMatrix(bits, ecLevel, version, maskPattern, matrix);
            int penalty = Encoder.calculateMaskPenalty(matrix);
            if (penalty >= minPenalty) continue;
            minPenalty = penalty;
            bestMaskPattern = maskPattern;
        }
        return bestMaskPattern;
    }

    private static Version chooseVersion(int numInputBits, ErrorCorrectionLevel ecLevel) throws WriterException {
        for (int versionNum = 1; versionNum <= 40; ++versionNum) {
            int totalInputBytes;
            Version.ECBlocks ecBlocks;
            int numEcBytes;
            Version version = Version.getVersionForNumber(versionNum);
            int numBytes = version.getTotalCodewords();
            int numDataBytes = numBytes - (numEcBytes = (ecBlocks = version.getECBlocksForLevel(ecLevel)).getTotalECCodewords());
            if (numDataBytes < (totalInputBytes = (numInputBits + 7) / 8)) continue;
            return version;
        }
        throw new WriterException("Data too big");
    }

    static void terminateBits(int numDataBytes, BitArray bits) throws WriterException {
        int capacity = numDataBytes * 8;
        if (bits.getSize() > capacity) {
            throw new WriterException("data bits cannot fit in the QR Code" + bits.getSize() + " > " + capacity);
        }
        for (int i = 0; i < 4 && bits.getSize() < capacity; ++i) {
            bits.appendBit(false);
        }
        int numBitsInLastByte = bits.getSize() & 7;
        if (numBitsInLastByte > 0) {
            for (int i = numBitsInLastByte; i < 8; ++i) {
                bits.appendBit(false);
            }
        }
        int numPaddingBytes = numDataBytes - bits.getSizeInBytes();
        for (int i = 0; i < numPaddingBytes; ++i) {
            bits.appendBits((i & 1) == 0 ? 236 : 17, 8);
        }
        if (bits.getSize() != capacity) {
            throw new WriterException("Bits size does not equal capacity");
        }
    }

    static void getNumDataBytesAndNumECBytesForBlockID(int numTotalBytes, int numDataBytes, int numRSBlocks, int blockID, int[] numDataBytesInBlock, int[] numECBytesInBlock) throws WriterException {
        if (blockID >= numRSBlocks) {
            throw new WriterException("Block ID too large");
        }
        int numRsBlocksInGroup2 = numTotalBytes % numRSBlocks;
        int numRsBlocksInGroup1 = numRSBlocks - numRsBlocksInGroup2;
        int numTotalBytesInGroup1 = numTotalBytes / numRSBlocks;
        int numDataBytesInGroup1 = numDataBytes / numRSBlocks;
        int numEcBytesInGroup1 = numTotalBytesInGroup1 - numDataBytesInGroup1;
        int numTotalBytesInGroup2 = numTotalBytesInGroup1 + 1;
        int numDataBytesInGroup2 = numDataBytesInGroup1 + 1;
        int numEcBytesInGroup2 = numTotalBytesInGroup2 - numDataBytesInGroup2;
        if (numEcBytesInGroup1 != numEcBytesInGroup2) {
            throw new WriterException("EC bytes mismatch");
        }
        if (numRSBlocks != numRsBlocksInGroup1 + numRsBlocksInGroup2) {
            throw new WriterException("RS blocks mismatch");
        }
        if (numTotalBytes != (numDataBytesInGroup1 + numEcBytesInGroup1) * numRsBlocksInGroup1 + (numDataBytesInGroup2 + numEcBytesInGroup2) * numRsBlocksInGroup2) {
            throw new WriterException("Total bytes mismatch");
        }
        if (blockID < numRsBlocksInGroup1) {
            numDataBytesInBlock[0] = numDataBytesInGroup1;
            numECBytesInBlock[0] = numEcBytesInGroup1;
        } else {
            numDataBytesInBlock[0] = numDataBytesInGroup2;
            numECBytesInBlock[0] = numEcBytesInGroup2;
        }
    }

    static BitArray interleaveWithECBytes(BitArray bits, int numTotalBytes, int numDataBytes, int numRSBlocks) throws WriterException {
        int i;
        byte[] dataBytes;
        if (bits.getSizeInBytes() != numDataBytes) {
            throw new WriterException("Number of bits and data bytes does not match");
        }
        int dataBytesOffset = 0;
        int maxNumDataBytes = 0;
        int maxNumEcBytes = 0;
        ArrayList<BlockPair> blocks = new ArrayList<BlockPair>(numRSBlocks);
        for (int i2 = 0; i2 < numRSBlocks; ++i2) {
            int[] numDataBytesInBlock = new int[1];
            int[] numEcBytesInBlock = new int[1];
            Encoder.getNumDataBytesAndNumECBytesForBlockID(numTotalBytes, numDataBytes, numRSBlocks, i2, numDataBytesInBlock, numEcBytesInBlock);
            int size = numDataBytesInBlock[0];
            dataBytes = new byte[size];
            bits.toBytes(8 * dataBytesOffset, dataBytes, 0, size);
            byte[] ecBytes = Encoder.generateECBytes(dataBytes, numEcBytesInBlock[0]);
            blocks.add(new BlockPair(dataBytes, ecBytes));
            maxNumDataBytes = Math.max(maxNumDataBytes, size);
            maxNumEcBytes = Math.max(maxNumEcBytes, ecBytes.length);
            dataBytesOffset += numDataBytesInBlock[0];
        }
        if (numDataBytes != dataBytesOffset) {
            throw new WriterException("Data bytes does not match offset");
        }
        BitArray result = new BitArray();
        for (i = 0; i < maxNumDataBytes; ++i) {
            for (BlockPair block : blocks) {
                dataBytes = block.getDataBytes();
                if (i >= dataBytes.length) continue;
                result.appendBits(dataBytes[i], 8);
            }
        }
        for (i = 0; i < maxNumEcBytes; ++i) {
            for (BlockPair block : blocks) {
                byte[] ecBytes = block.getErrorCorrectionBytes();
                if (i >= ecBytes.length) continue;
                result.appendBits(ecBytes[i], 8);
            }
        }
        if (numTotalBytes != result.getSizeInBytes()) {
            throw new WriterException("Interleaving error: " + numTotalBytes + " and " + result.getSizeInBytes() + " differ.");
        }
        return result;
    }

    static byte[] generateECBytes(byte[] dataBytes, int numEcBytesInBlock) {
        int numDataBytes = dataBytes.length;
        int[] toEncode = new int[numDataBytes + numEcBytesInBlock];
        for (int i = 0; i < numDataBytes; ++i) {
            toEncode[i] = dataBytes[i] & 0xFF;
        }
        new ReedSolomonEncoder(GenericGF.QR_CODE_FIELD_256).encode(toEncode, numEcBytesInBlock);
        byte[] ecBytes = new byte[numEcBytesInBlock];
        for (int i = 0; i < numEcBytesInBlock; ++i) {
            ecBytes[i] = (byte)toEncode[numDataBytes + i];
        }
        return ecBytes;
    }

    static void appendModeInfo(Mode mode, BitArray bits) {
        bits.appendBits(mode.getBits(), 4);
    }

    static void appendLengthInfo(int numLetters, Version version, Mode mode, BitArray bits) throws WriterException {
        int numBits = mode.getCharacterCountBits(version);
        if (numLetters >= 1 << numBits) {
            throw new WriterException(numLetters + " is bigger than " + ((1 << numBits) - 1));
        }
        bits.appendBits(numLetters, numBits);
    }

    static void appendBytes(String content, Mode mode, BitArray bits, String encoding) throws WriterException {
        switch (mode) {
            case NUMERIC: {
                Encoder.appendNumericBytes(content, bits);
                break;
            }
            case ALPHANUMERIC: {
                Encoder.appendAlphanumericBytes(content, bits);
                break;
            }
            case BYTE: {
                Encoder.append8BitBytes(content, bits, encoding);
                break;
            }
            case KANJI: {
                Encoder.appendKanjiBytes(content, bits);
                break;
            }
            default: {
                throw new WriterException("Invalid mode: " + (Object)((Object)mode));
            }
        }
    }

    static void appendNumericBytes(CharSequence content, BitArray bits) {
        int length = content.length();
        int i = 0;
        while (i < length) {
            int num2;
            int num1 = content.charAt(i) - 48;
            if (i + 2 < length) {
                num2 = content.charAt(i + 1) - 48;
                int num3 = content.charAt(i + 2) - 48;
                bits.appendBits(num1 * 100 + num2 * 10 + num3, 10);
                i += 3;
                continue;
            }
            if (i + 1 < length) {
                num2 = content.charAt(i + 1) - 48;
                bits.appendBits(num1 * 10 + num2, 7);
                i += 2;
                continue;
            }
            bits.appendBits(num1, 4);
            ++i;
        }
    }

    static void appendAlphanumericBytes(CharSequence content, BitArray bits) throws WriterException {
        int length = content.length();
        int i = 0;
        while (i < length) {
            int code1 = Encoder.getAlphanumericCode(content.charAt(i));
            if (code1 == -1) {
                throw new WriterException();
            }
            if (i + 1 < length) {
                int code2 = Encoder.getAlphanumericCode(content.charAt(i + 1));
                if (code2 == -1) {
                    throw new WriterException();
                }
                bits.appendBits(code1 * 45 + code2, 11);
                i += 2;
                continue;
            }
            bits.appendBits(code1, 6);
            ++i;
        }
    }

    static void append8BitBytes(String content, BitArray bits, String encoding) throws WriterException {
        byte[] bytes;
        try {
            bytes = content.getBytes(encoding);
        }
        catch (UnsupportedEncodingException uee) {
            throw new WriterException(uee);
        }
        for (byte b : bytes) {
            bits.appendBits(b, 8);
        }
    }

    static void appendKanjiBytes(String content, BitArray bits) throws WriterException {
        byte[] bytes;
        try {
            bytes = content.getBytes("Shift_JIS");
        }
        catch (UnsupportedEncodingException uee) {
            throw new WriterException(uee);
        }
        int length = bytes.length;
        for (int i = 0; i < length; i += 2) {
            int byte1 = bytes[i] & 0xFF;
            int byte2 = bytes[i + 1] & 0xFF;
            int code = byte1 << 8 | byte2;
            int subtracted = -1;
            if (code >= 33088 && code <= 40956) {
                subtracted = code - 33088;
            } else if (code >= 57408 && code <= 60351) {
                subtracted = code - 49472;
            }
            if (subtracted == -1) {
                throw new WriterException("Invalid byte sequence");
            }
            int encoded = (subtracted >> 8) * 192 + (subtracted & 0xFF);
            bits.appendBits(encoded, 13);
        }
    }

    private static void appendECI(CharacterSetECI eci, BitArray bits) {
        bits.appendBits(Mode.ECI.getBits(), 4);
        bits.appendBits(eci.getValue(), 8);
    }
}

