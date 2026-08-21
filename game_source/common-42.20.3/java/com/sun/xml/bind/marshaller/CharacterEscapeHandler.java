/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.Writer;

public interface CharacterEscapeHandler {
    public void escape(char[] var1, int var2, int var3, boolean var4, Writer var5) throws IOException;
}

