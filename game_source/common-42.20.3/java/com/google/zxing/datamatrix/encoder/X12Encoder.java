/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import com.google.zxing.datamatrix.encoder.C40Encoder;
import com.google.zxing.datamatrix.encoder.EncoderContext;
import com.google.zxing.datamatrix.encoder.HighLevelEncoder;

final class X12Encoder
extends C40Encoder {
    X12Encoder() {
    }

    @Override
    public int getEncodingMode() {
        return 3;
    }

    @Override
    public void encode(EncoderContext context) {
        StringBuilder buffer = new StringBuilder();
        while (context.hasMoreCharacters()) {
            char c = context.getCurrentChar();
            ++context.pos;
            this.encodeChar(c, buffer);
            int count = buffer.length();
            if (count % 3 != 0) continue;
            X12Encoder.writeNextTriplet(context, buffer);
            int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, this.getEncodingMode());
            if (newMode == this.getEncodingMode()) continue;
            context.signalEncoderChange(newMode);
            break;
        }
        this.handleEOD(context, buffer);
    }

    @Override
    int encodeChar(char c, StringBuilder sb) {
        if (c == '\r') {
            sb.append('\u0000');
        } else if (c == '*') {
            sb.append('\u0001');
        } else if (c == '>') {
            sb.append('\u0002');
        } else if (c == ' ') {
            sb.append('\u0003');
        } else if (c >= '0' && c <= '9') {
            sb.append((char)(c - 48 + 4));
        } else if (c >= 'A' && c <= 'Z') {
            sb.append((char)(c - 65 + 14));
        } else {
            HighLevelEncoder.illegalCharacter(c);
        }
        return 1;
    }

    @Override
    void handleEOD(EncoderContext context, StringBuilder buffer) {
        context.updateSymbolInfo();
        int available = context.getSymbolInfo().getDataCapacity() - context.getCodewordCount();
        int count = buffer.length();
        context.pos -= count;
        if (context.getRemainingCharacters() > 1 || available > 1 || context.getRemainingCharacters() != available) {
            context.writeCodeword('\u00fe');
        }
        if (context.getNewEncoding() < 0) {
            context.signalEncoderChange(0);
        }
    }
}

