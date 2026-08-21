/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.oned.rss.expanded.decoders;

import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.BitArray;
import com.google.zxing.oned.rss.expanded.decoders.BlockParsedResult;
import com.google.zxing.oned.rss.expanded.decoders.CurrentParsingState;
import com.google.zxing.oned.rss.expanded.decoders.DecodedChar;
import com.google.zxing.oned.rss.expanded.decoders.DecodedInformation;
import com.google.zxing.oned.rss.expanded.decoders.DecodedNumeric;
import com.google.zxing.oned.rss.expanded.decoders.FieldParser;

final class GeneralAppIdDecoder {
    private final BitArray information;
    private final CurrentParsingState current = new CurrentParsingState();
    private final StringBuilder buffer = new StringBuilder();

    GeneralAppIdDecoder(BitArray information) {
        this.information = information;
    }

    String decodeAllCodes(StringBuilder buff, int initialPosition) throws NotFoundException, FormatException {
        int currentPosition = initialPosition;
        String remaining = null;
        while (true) {
            DecodedInformation info;
            String parsedFields;
            if ((parsedFields = FieldParser.parseFieldsInGeneralPurpose((info = this.decodeGeneralPurposeField(currentPosition, remaining)).getNewString())) != null) {
                buff.append(parsedFields);
            }
            remaining = info.isRemaining() ? String.valueOf(info.getRemainingValue()) : null;
            if (currentPosition == info.getNewPosition()) break;
            currentPosition = info.getNewPosition();
        }
        return buff.toString();
    }

    private boolean isStillNumeric(int pos) {
        if (pos + 7 > this.information.getSize()) {
            return pos + 4 <= this.information.getSize();
        }
        for (int i = pos; i < pos + 3; ++i) {
            if (!this.information.get(i)) continue;
            return true;
        }
        return this.information.get(pos + 3);
    }

    private DecodedNumeric decodeNumeric(int pos) throws FormatException {
        if (pos + 7 > this.information.getSize()) {
            int numeric = this.extractNumericValueFromBitArray(pos, 4);
            if (numeric == 0) {
                return new DecodedNumeric(this.information.getSize(), 10, 10);
            }
            return new DecodedNumeric(this.information.getSize(), numeric - 1, 10);
        }
        int numeric = this.extractNumericValueFromBitArray(pos, 7);
        int digit1 = (numeric - 8) / 11;
        int digit2 = (numeric - 8) % 11;
        return new DecodedNumeric(pos + 7, digit1, digit2);
    }

    int extractNumericValueFromBitArray(int pos, int bits) {
        return GeneralAppIdDecoder.extractNumericValueFromBitArray(this.information, pos, bits);
    }

    static int extractNumericValueFromBitArray(BitArray information, int pos, int bits) {
        int value = 0;
        for (int i = 0; i < bits; ++i) {
            if (!information.get(pos + i)) continue;
            value |= 1 << bits - i - 1;
        }
        return value;
    }

    DecodedInformation decodeGeneralPurposeField(int pos, String remaining) throws FormatException {
        this.buffer.setLength(0);
        if (remaining != null) {
            this.buffer.append(remaining);
        }
        this.current.setPosition(pos);
        DecodedInformation lastDecoded = this.parseBlocks();
        if (lastDecoded != null && lastDecoded.isRemaining()) {
            return new DecodedInformation(this.current.getPosition(), this.buffer.toString(), lastDecoded.getRemainingValue());
        }
        return new DecodedInformation(this.current.getPosition(), this.buffer.toString());
    }

    private DecodedInformation parseBlocks() throws FormatException {
        BlockParsedResult result;
        boolean isFinished;
        boolean positionChanged;
        do {
            int initialPosition = this.current.getPosition();
            if (this.current.isAlpha()) {
                result = this.parseAlphaBlock();
                isFinished = result.isFinished();
            } else if (this.current.isIsoIec646()) {
                result = this.parseIsoIec646Block();
                isFinished = result.isFinished();
            } else {
                result = this.parseNumericBlock();
                isFinished = result.isFinished();
            }
            boolean bl = positionChanged = initialPosition != this.current.getPosition();
        } while ((positionChanged || isFinished) && !isFinished);
        return result.getDecodedInformation();
    }

    private BlockParsedResult parseNumericBlock() throws FormatException {
        while (this.isStillNumeric(this.current.getPosition())) {
            DecodedNumeric numeric = this.decodeNumeric(this.current.getPosition());
            this.current.setPosition(numeric.getNewPosition());
            if (numeric.isFirstDigitFNC1()) {
                DecodedInformation information = numeric.isSecondDigitFNC1() ? new DecodedInformation(this.current.getPosition(), this.buffer.toString()) : new DecodedInformation(this.current.getPosition(), this.buffer.toString(), numeric.getSecondDigit());
                return new BlockParsedResult(information, true);
            }
            this.buffer.append(numeric.getFirstDigit());
            if (numeric.isSecondDigitFNC1()) {
                DecodedInformation information = new DecodedInformation(this.current.getPosition(), this.buffer.toString());
                return new BlockParsedResult(information, true);
            }
            this.buffer.append(numeric.getSecondDigit());
        }
        if (this.isNumericToAlphaNumericLatch(this.current.getPosition())) {
            this.current.setAlpha();
            this.current.incrementPosition(4);
        }
        return new BlockParsedResult(false);
    }

    private BlockParsedResult parseIsoIec646Block() throws FormatException {
        while (this.isStillIsoIec646(this.current.getPosition())) {
            DecodedChar iso = this.decodeIsoIec646(this.current.getPosition());
            this.current.setPosition(iso.getNewPosition());
            if (iso.isFNC1()) {
                DecodedInformation information = new DecodedInformation(this.current.getPosition(), this.buffer.toString());
                return new BlockParsedResult(information, true);
            }
            this.buffer.append(iso.getValue());
        }
        if (this.isAlphaOr646ToNumericLatch(this.current.getPosition())) {
            this.current.incrementPosition(3);
            this.current.setNumeric();
        } else if (this.isAlphaTo646ToAlphaLatch(this.current.getPosition())) {
            if (this.current.getPosition() + 5 < this.information.getSize()) {
                this.current.incrementPosition(5);
            } else {
                this.current.setPosition(this.information.getSize());
            }
            this.current.setAlpha();
        }
        return new BlockParsedResult(false);
    }

    private BlockParsedResult parseAlphaBlock() {
        while (this.isStillAlpha(this.current.getPosition())) {
            DecodedChar alpha = this.decodeAlphanumeric(this.current.getPosition());
            this.current.setPosition(alpha.getNewPosition());
            if (alpha.isFNC1()) {
                DecodedInformation information = new DecodedInformation(this.current.getPosition(), this.buffer.toString());
                return new BlockParsedResult(information, true);
            }
            this.buffer.append(alpha.getValue());
        }
        if (this.isAlphaOr646ToNumericLatch(this.current.getPosition())) {
            this.current.incrementPosition(3);
            this.current.setNumeric();
        } else if (this.isAlphaTo646ToAlphaLatch(this.current.getPosition())) {
            if (this.current.getPosition() + 5 < this.information.getSize()) {
                this.current.incrementPosition(5);
            } else {
                this.current.setPosition(this.information.getSize());
            }
            this.current.setIsoIec646();
        }
        return new BlockParsedResult(false);
    }

    private boolean isStillIsoIec646(int pos) {
        if (pos + 5 > this.information.getSize()) {
            return false;
        }
        int fiveBitValue = this.extractNumericValueFromBitArray(pos, 5);
        if (fiveBitValue >= 5 && fiveBitValue < 16) {
            return true;
        }
        if (pos + 7 > this.information.getSize()) {
            return false;
        }
        int sevenBitValue = this.extractNumericValueFromBitArray(pos, 7);
        if (sevenBitValue >= 64 && sevenBitValue < 116) {
            return true;
        }
        if (pos + 8 > this.information.getSize()) {
            return false;
        }
        int eightBitValue = this.extractNumericValueFromBitArray(pos, 8);
        return eightBitValue >= 232 && eightBitValue < 253;
    }

    private DecodedChar decodeIsoIec646(int pos) throws FormatException {
        char c;
        int fiveBitValue = this.extractNumericValueFromBitArray(pos, 5);
        if (fiveBitValue == 15) {
            return new DecodedChar(pos + 5, '$');
        }
        if (fiveBitValue >= 5 && fiveBitValue < 15) {
            return new DecodedChar(pos + 5, (char)(48 + fiveBitValue - 5));
        }
        int sevenBitValue = this.extractNumericValueFromBitArray(pos, 7);
        if (sevenBitValue >= 64 && sevenBitValue < 90) {
            return new DecodedChar(pos + 7, (char)(sevenBitValue + 1));
        }
        if (sevenBitValue >= 90 && sevenBitValue < 116) {
            return new DecodedChar(pos + 7, (char)(sevenBitValue + 7));
        }
        int eightBitValue = this.extractNumericValueFromBitArray(pos, 8);
        switch (eightBitValue) {
            case 232: {
                c = '!';
                break;
            }
            case 233: {
                c = '\"';
                break;
            }
            case 234: {
                c = '%';
                break;
            }
            case 235: {
                c = '&';
                break;
            }
            case 236: {
                c = '\'';
                break;
            }
            case 237: {
                c = '(';
                break;
            }
            case 238: {
                c = ')';
                break;
            }
            case 239: {
                c = '*';
                break;
            }
            case 240: {
                c = '+';
                break;
            }
            case 241: {
                c = ',';
                break;
            }
            case 242: {
                c = '-';
                break;
            }
            case 243: {
                c = '.';
                break;
            }
            case 244: {
                c = '/';
                break;
            }
            case 245: {
                c = ':';
                break;
            }
            case 246: {
                c = ';';
                break;
            }
            case 247: {
                c = '<';
                break;
            }
            case 248: {
                c = '=';
                break;
            }
            case 249: {
                c = '>';
                break;
            }
            case 250: {
                c = '?';
                break;
            }
            case 251: {
                c = '_';
                break;
            }
            case 252: {
                c = ' ';
                break;
            }
            default: {
                throw FormatException.getFormatInstance();
            }
        }
        return new DecodedChar(pos + 8, c);
    }

    private boolean isStillAlpha(int pos) {
        if (pos + 5 > this.information.getSize()) {
            return false;
        }
        int fiveBitValue = this.extractNumericValueFromBitArray(pos, 5);
        if (fiveBitValue >= 5 && fiveBitValue < 16) {
            return true;
        }
        if (pos + 6 > this.information.getSize()) {
            return false;
        }
        int sixBitValue = this.extractNumericValueFromBitArray(pos, 6);
        return sixBitValue >= 16 && sixBitValue < 63;
    }

    private DecodedChar decodeAlphanumeric(int pos) {
        char c;
        int fiveBitValue = this.extractNumericValueFromBitArray(pos, 5);
        if (fiveBitValue == 15) {
            return new DecodedChar(pos + 5, '$');
        }
        if (fiveBitValue >= 5 && fiveBitValue < 15) {
            return new DecodedChar(pos + 5, (char)(48 + fiveBitValue - 5));
        }
        int sixBitValue = this.extractNumericValueFromBitArray(pos, 6);
        if (sixBitValue >= 32 && sixBitValue < 58) {
            return new DecodedChar(pos + 6, (char)(sixBitValue + 33));
        }
        switch (sixBitValue) {
            case 58: {
                c = '*';
                break;
            }
            case 59: {
                c = ',';
                break;
            }
            case 60: {
                c = '-';
                break;
            }
            case 61: {
                c = '.';
                break;
            }
            case 62: {
                c = '/';
                break;
            }
            default: {
                throw new IllegalStateException("Decoding invalid alphanumeric value: " + sixBitValue);
            }
        }
        return new DecodedChar(pos + 6, c);
    }

    private boolean isAlphaTo646ToAlphaLatch(int pos) {
        if (pos + 1 > this.information.getSize()) {
            return false;
        }
        for (int i = 0; i < 5 && i + pos < this.information.getSize(); ++i) {
            if (!(i == 2 ? !this.information.get(pos + 2) : this.information.get(pos + i))) continue;
            return false;
        }
        return true;
    }

    private boolean isAlphaOr646ToNumericLatch(int pos) {
        if (pos + 3 > this.information.getSize()) {
            return false;
        }
        for (int i = pos; i < pos + 3; ++i) {
            if (!this.information.get(i)) continue;
            return false;
        }
        return true;
    }

    private boolean isNumericToAlphaNumericLatch(int pos) {
        if (pos + 1 > this.information.getSize()) {
            return false;
        }
        for (int i = 0; i < 4 && i + pos < this.information.getSize(); ++i) {
            if (!this.information.get(pos + i)) continue;
            return false;
        }
        return true;
    }
}

