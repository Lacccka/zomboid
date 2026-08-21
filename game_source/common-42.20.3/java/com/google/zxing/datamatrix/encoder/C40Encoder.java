/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import com.google.zxing.datamatrix.encoder.Encoder;
import com.google.zxing.datamatrix.encoder.EncoderContext;
import com.google.zxing.datamatrix.encoder.HighLevelEncoder;

class C40Encoder
implements Encoder {
    C40Encoder() {
    }

    @Override
    public int getEncodingMode() {
        return 1;
    }

    @Override
    public void encode(EncoderContext context) {
        StringBuilder buffer = new StringBuilder();
        while (context.hasMoreCharacters()) {
            int newMode;
            char c = context.getCurrentChar();
            ++context.pos;
            int lastCharSize = this.encodeChar(c, buffer);
            int unwritten = buffer.length() / 3 * 2;
            int curCodewordCount = context.getCodewordCount() + unwritten;
            context.updateSymbolInfo(curCodewordCount);
            int available = context.getSymbolInfo().getDataCapacity() - curCodewordCount;
            if (!context.hasMoreCharacters()) {
                StringBuilder removed = new StringBuilder();
                if (buffer.length() % 3 == 2 && (available < 2 || available > 2)) {
                    lastCharSize = this.backtrackOneCharacter(context, buffer, removed, lastCharSize);
                }
                while (buffer.length() % 3 == 1 && (lastCharSize <= 3 && available != 1 || lastCharSize > 3)) {
                    lastCharSize = this.backtrackOneCharacter(context, buffer, removed, lastCharSize);
                }
                break;
            }
            int count = buffer.length();
            if (count % 3 != 0 || (newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, this.getEncodingMode())) == this.getEncodingMode()) continue;
            context.signalEncoderChange(newMode);
            break;
        }
        this.handleEOD(context, buffer);
    }

    private int backtrackOneCharacter(EncoderContext context, StringBuilder buffer, StringBuilder removed, int lastCharSize) {
        int count = buffer.length();
        buffer.delete(count - lastCharSize, count);
        --context.pos;
        char c = context.getCurrentChar();
        lastCharSize = this.encodeChar(c, removed);
        context.resetSymbolInfo();
        return lastCharSize;
    }

    static void writeNextTriplet(EncoderContext context, StringBuilder buffer) {
        context.writeCodewords(C40Encoder.encodeToCodewords(buffer, 0));
        buffer.delete(0, 3);
    }

    void handleEOD(EncoderContext context, StringBuilder buffer) {
        int unwritten = buffer.length() / 3 * 2;
        int rest = buffer.length() % 3;
        int curCodewordCount = context.getCodewordCount() + unwritten;
        context.updateSymbolInfo(curCodewordCount);
        int available = context.getSymbolInfo().getDataCapacity() - curCodewordCount;
        if (rest == 2) {
            buffer.append('\u0000');
            while (buffer.length() >= 3) {
                C40Encoder.writeNextTriplet(context, buffer);
            }
            if (context.hasMoreCharacters()) {
                context.writeCodeword('\u00fe');
            }
        } else if (available == 1 && rest == 1) {
            while (buffer.length() >= 3) {
                C40Encoder.writeNextTriplet(context, buffer);
            }
            if (context.hasMoreCharacters()) {
                context.writeCodeword('\u00fe');
            }
            --context.pos;
        } else if (rest == 0) {
            while (buffer.length() >= 3) {
                C40Encoder.writeNextTriplet(context, buffer);
            }
            if (available > 0 || context.hasMoreCharacters()) {
                context.writeCodeword('\u00fe');
            }
        } else {
            throw new IllegalStateException("Unexpected case. Please report!");
        }
        context.signalEncoderChange(0);
    }

    int encodeChar(char c, StringBuilder sb) {
        if (c == ' ') {
            sb.append('\u0003');
            return 1;
        }
        if (c >= '0' && c <= '9') {
            sb.append((char)(c - 48 + 4));
            return 1;
        }
        if (c >= 'A' && c <= 'Z') {
            sb.append((char)(c - 65 + 14));
            return 1;
        }
        if (c >= '\u0000' && c <= '\u001f') {
            sb.append('\u0000');
            sb.append(c);
            return 2;
        }
        if (c >= '!' && c <= '/') {
            sb.append('\u0001');
            sb.append((char)(c - 33));
            return 2;
        }
        if (c >= ':' && c <= '@') {
            sb.append('\u0001');
            sb.append((char)(c - 58 + 15));
            return 2;
        }
        if (c >= '[' && c <= '_') {
            sb.append('\u0001');
            sb.append((char)(c - 91 + 22));
            return 2;
        }
        if (c >= '`' && c <= '\u007f') {
            sb.append('\u0002');
            sb.append((char)(c - 96));
            return 2;
        }
        if (c >= '\u0080') {
            sb.append("\u0001\u001e");
            int len = 2;
            return len += this.encodeChar((char)(c - 128), sb);
        }
        throw new IllegalArgumentException("Illegal character: " + c);
    }

    private static String encodeToCodewords(CharSequence sb, int startPos) {
        char c1 = sb.charAt(startPos);
        char c2 = sb.charAt(startPos + 1);
        char c3 = sb.charAt(startPos + 2);
        int v = 1600 * c1 + 40 * c2 + c3 + 1;
        char cw1 = (char)(v / 256);
        char cw2 = (char)(v % 256);
        return new String(new char[]{cw1, cw2});
    }
}

