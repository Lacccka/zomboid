/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import com.google.zxing.datamatrix.encoder.C40Encoder;
import com.google.zxing.datamatrix.encoder.HighLevelEncoder;

final class TextEncoder
extends C40Encoder {
    TextEncoder() {
    }

    @Override
    public int getEncodingMode() {
        return 2;
    }

    @Override
    int encodeChar(char c, StringBuilder sb) {
        if (c == ' ') {
            sb.append('\u0003');
            return 1;
        }
        if (c >= '0' && c <= '9') {
            sb.append((char)(c - 48 + 4));
            return 1;
        }
        if (c >= 'a' && c <= 'z') {
            sb.append((char)(c - 97 + 14));
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
        if (c == '`') {
            sb.append('\u0002');
            sb.append((char)(c - 96));
            return 2;
        }
        if (c >= 'A' && c <= 'Z') {
            sb.append('\u0002');
            sb.append((char)(c - 65 + 1));
            return 2;
        }
        if (c >= '{' && c <= '\u007f') {
            sb.append('\u0002');
            sb.append((char)(c - 123 + 27));
            return 2;
        }
        if (c >= '\u0080') {
            sb.append("\u0001\u001e");
            int len = 2;
            return len += this.encodeChar((char)(c - 128), sb);
        }
        HighLevelEncoder.illegalCharacter(c);
        return -1;
    }
}

