/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import com.google.zxing.datamatrix.encoder.Encoder;
import com.google.zxing.datamatrix.encoder.EncoderContext;
import com.google.zxing.datamatrix.encoder.HighLevelEncoder;

final class Base256Encoder
implements Encoder {
    Base256Encoder() {
    }

    @Override
    public int getEncodingMode() {
        return 5;
    }

    @Override
    public void encode(EncoderContext context) {
        boolean mustPad;
        StringBuilder buffer = new StringBuilder();
        buffer.append('\u0000');
        while (context.hasMoreCharacters()) {
            char c = context.getCurrentChar();
            buffer.append(c);
            ++context.pos;
            int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, this.getEncodingMode());
            if (newMode == this.getEncodingMode()) continue;
            context.signalEncoderChange(newMode);
            break;
        }
        int dataCount = buffer.length() - 1;
        int lengthFieldSize = 1;
        int currentSize = context.getCodewordCount() + dataCount + lengthFieldSize;
        context.updateSymbolInfo(currentSize);
        boolean bl = mustPad = context.getSymbolInfo().getDataCapacity() - currentSize > 0;
        if (context.hasMoreCharacters() || mustPad) {
            if (dataCount <= 249) {
                buffer.setCharAt(0, (char)dataCount);
            } else if (dataCount > 249 && dataCount <= 1555) {
                buffer.setCharAt(0, (char)(dataCount / 250 + 249));
                buffer.insert(1, (char)(dataCount % 250));
            } else {
                throw new IllegalStateException("Message length not in valid ranges: " + dataCount);
            }
        }
        int c = buffer.length();
        for (int i = 0; i < c; ++i) {
            context.writeCodeword(Base256Encoder.randomize255State(buffer.charAt(i), context.getCodewordCount() + 1));
        }
    }

    private static char randomize255State(char ch, int codewordPosition) {
        int pseudoRandom = 149 * codewordPosition % 255 + 1;
        int tempVariable = ch + pseudoRandom;
        if (tempVariable <= 255) {
            return (char)tempVariable;
        }
        return (char)(tempVariable - 256);
    }
}

