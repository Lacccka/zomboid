/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import com.google.zxing.datamatrix.encoder.Encoder;
import com.google.zxing.datamatrix.encoder.EncoderContext;
import com.google.zxing.datamatrix.encoder.HighLevelEncoder;

final class EdifactEncoder
implements Encoder {
    EdifactEncoder() {
    }

    @Override
    public int getEncodingMode() {
        return 4;
    }

    @Override
    public void encode(EncoderContext context) {
        StringBuilder buffer = new StringBuilder();
        while (context.hasMoreCharacters()) {
            char c = context.getCurrentChar();
            EdifactEncoder.encodeChar(c, buffer);
            ++context.pos;
            int count = buffer.length();
            if (count < 4) continue;
            context.writeCodewords(EdifactEncoder.encodeToCodewords(buffer, 0));
            buffer.delete(0, 4);
            int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, this.getEncodingMode());
            if (newMode == this.getEncodingMode()) continue;
            context.signalEncoderChange(0);
            break;
        }
        buffer.append('\u001f');
        EdifactEncoder.handleEOD(context, buffer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void handleEOD(EncoderContext context, CharSequence buffer) {
        try {
            boolean restInAscii;
            int count = buffer.length();
            if (count == 0) {
                return;
            }
            if (count == 1) {
                context.updateSymbolInfo();
                int available = context.getSymbolInfo().getDataCapacity() - context.getCodewordCount();
                int remaining = context.getRemainingCharacters();
                if (remaining == 0 && available <= 2) {
                    return;
                }
            }
            if (count > 4) {
                throw new IllegalStateException("Count must not exceed 4");
            }
            int restChars = count - 1;
            String encoded = EdifactEncoder.encodeToCodewords(buffer, 0);
            boolean endOfSymbolReached = !context.hasMoreCharacters();
            boolean bl = restInAscii = endOfSymbolReached && restChars <= 2;
            if (restChars <= 2) {
                context.updateSymbolInfo(context.getCodewordCount() + restChars);
                int available = context.getSymbolInfo().getDataCapacity() - context.getCodewordCount();
                if (available >= 3) {
                    restInAscii = false;
                    context.updateSymbolInfo(context.getCodewordCount() + encoded.length());
                }
            }
            if (restInAscii) {
                context.resetSymbolInfo();
                context.pos -= restChars;
            } else {
                context.writeCodewords(encoded);
            }
        }
        finally {
            context.signalEncoderChange(0);
        }
    }

    private static void encodeChar(char c, StringBuilder sb) {
        if (c >= ' ' && c <= '?') {
            sb.append(c);
        } else if (c >= '@' && c <= '^') {
            sb.append((char)(c - 64));
        } else {
            HighLevelEncoder.illegalCharacter(c);
        }
    }

    private static String encodeToCodewords(CharSequence sb, int startPos) {
        int len = sb.length() - startPos;
        if (len == 0) {
            throw new IllegalStateException("StringBuilder must not be empty");
        }
        char c1 = sb.charAt(startPos);
        char c2 = len >= 2 ? sb.charAt(startPos + 1) : (char)'\u0000';
        char c3 = len >= 3 ? sb.charAt(startPos + 2) : (char)'\u0000';
        char c4 = len >= 4 ? sb.charAt(startPos + 3) : (char)'\u0000';
        int v = (c1 << 18) + (c2 << 12) + (c3 << 6) + c4;
        char cw1 = (char)(v >> 16 & 0xFF);
        char cw2 = (char)(v >> 8 & 0xFF);
        char cw3 = (char)(v & 0xFF);
        StringBuilder res = new StringBuilder(3);
        res.append(cw1);
        if (len >= 2) {
            res.append(cw2);
        }
        if (len >= 3) {
            res.append(cw3);
        }
        return res.toString();
    }
}

