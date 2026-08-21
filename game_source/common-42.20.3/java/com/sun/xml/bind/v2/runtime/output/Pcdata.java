/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import java.io.IOException;

public abstract class Pcdata
implements CharSequence {
    public abstract void writeTo(UTF8XmlOutput var1) throws IOException;

    public void writeTo(char[] buf, int start) {
        this.toString().getChars(0, this.length(), buf, start);
    }

    @Override
    public abstract String toString();
}

