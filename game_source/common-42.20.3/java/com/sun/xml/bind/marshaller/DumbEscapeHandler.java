/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.marshaller;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import java.io.IOException;
import java.io.Writer;

public class DumbEscapeHandler
implements CharacterEscapeHandler {
    public static final CharacterEscapeHandler theInstance = new DumbEscapeHandler();

    private DumbEscapeHandler() {
    }

    @Override
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        int limit = start + length;
        block6: for (int i = start; i < limit; ++i) {
            switch (ch[i]) {
                case '&': {
                    out.write("&amp;");
                    continue block6;
                }
                case '<': {
                    out.write("&lt;");
                    continue block6;
                }
                case '>': {
                    out.write("&gt;");
                    continue block6;
                }
                case '\"': {
                    if (isAttVal) {
                        out.write("&quot;");
                        continue block6;
                    }
                    out.write(34);
                    continue block6;
                }
                default: {
                    if (ch[i] > '\u007f') {
                        out.write("&#");
                        out.write(Integer.toString(ch[i]));
                        out.write(59);
                        continue block6;
                    }
                    out.write(ch[i]);
                }
            }
        }
    }
}

