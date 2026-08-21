/*
 * Decompiled with CFR 0.152.
 */
package com.google.zxing.datamatrix.encoder;

import com.google.zxing.datamatrix.encoder.Encoder;
import com.google.zxing.datamatrix.encoder.EncoderContext;
import com.google.zxing.datamatrix.encoder.HighLevelEncoder;

final class ASCIIEncoder
implements Encoder {
    ASCIIEncoder() {
    }

    @Override
    public int getEncodingMode() {
        return 0;
    }

    @Override
    public void encode(EncoderContext context) {
        block10: {
            char c;
            block11: {
                block9: {
                    int n = HighLevelEncoder.determineConsecutiveDigitCount(context.getMessage(), context.pos);
                    if (n < 2) break block9;
                    context.writeCodeword(ASCIIEncoder.encodeASCIIDigits(context.getMessage().charAt(context.pos), context.getMessage().charAt(context.pos + 1)));
                    context.pos += 2;
                    break block10;
                }
                c = context.getCurrentChar();
                int newMode = HighLevelEncoder.lookAheadTest(context.getMessage(), context.pos, this.getEncodingMode());
                if (newMode == this.getEncodingMode()) break block11;
                switch (newMode) {
                    case 5: {
                        context.writeCodeword('\u00e7');
                        context.signalEncoderChange(5);
                        return;
                    }
                    case 1: {
                        context.writeCodeword('\u00e6');
                        context.signalEncoderChange(1);
                        return;
                    }
                    case 3: {
                        context.writeCodeword('\u00ee');
                        context.signalEncoderChange(3);
                        break block10;
                    }
                    case 2: {
                        context.writeCodeword('\u00ef');
                        context.signalEncoderChange(2);
                        break block10;
                    }
                    case 4: {
                        context.writeCodeword('\u00f0');
                        context.signalEncoderChange(4);
                        break block10;
                    }
                    default: {
                        throw new IllegalStateException("Illegal mode: " + newMode);
                    }
                }
            }
            if (HighLevelEncoder.isExtendedASCII(c)) {
                context.writeCodeword('\u00eb');
                context.writeCodeword((char)(c - 128 + 1));
                ++context.pos;
            } else {
                context.writeCodeword((char)(c + '\u0001'));
                ++context.pos;
            }
        }
    }

    private static char encodeASCIIDigits(char digit1, char digit2) {
        if (HighLevelEncoder.isDigit(digit1) && HighLevelEncoder.isDigit(digit2)) {
            int num = (digit1 - 48) * 10 + (digit2 - 48);
            return (char)(num + 130);
        }
        throw new IllegalArgumentException("not digits: " + digit1 + digit2);
    }
}

