/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.marshaller;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import java.io.IOException;
import java.io.Writer;

public class MinimumEscapeHandler
implements CharacterEscapeHandler {
    public static final CharacterEscapeHandler theInstance = new MinimumEscapeHandler();

    private MinimumEscapeHandler() {
    }

    @Override
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        int limit = start + length;
        block7: for (int i = start; i < limit; ++i) {
            char c = ch[i];
            if (c != '&' && c != '<' && c != '>' && c != '\r' && (c != '\n' || !isAttVal) && (c != '\"' || !isAttVal)) continue;
            if (i != start) {
                out.write(ch, start, i - start);
            }
            start = i + 1;
            switch (ch[i]) {
                case '&': {
                    out.write("&amp;");
                    continue block7;
                }
                case '<': {
                    out.write("&lt;");
                    continue block7;
                }
                case '>': {
                    out.write("&gt;");
                    continue block7;
                }
                case '\"': {
                    out.write("&quot;");
                    continue block7;
                }
                case '\n': 
                case '\r': {
                    out.write("&#");
                    out.write(Integer.toString(c));
                    out.write(59);
                    continue block7;
                }
                default: {
                    throw new IllegalArgumentException("Cannot escape: '" + c + "'");
                }
            }
        }
        if (start != limit) {
            out.write(ch, start, limit - start);
        }
    }
}

