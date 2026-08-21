/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.marshaller;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import java.io.IOException;
import java.io.Writer;

public class NoEscapeHandler
implements CharacterEscapeHandler {
    public static final NoEscapeHandler theInstance = new NoEscapeHandler();

    @Override
    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        out.write(ch, start, length);
    }
}

