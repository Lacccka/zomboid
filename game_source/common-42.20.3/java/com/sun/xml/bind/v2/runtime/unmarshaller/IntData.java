/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import java.io.IOException;

public class IntData
extends Pcdata {
    private int data;
    private int length;
    private static final int[] sizeTable = new int[]{9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    public void reset(int i) {
        this.data = i;
        this.length = i == Integer.MIN_VALUE ? 11 : (i < 0 ? IntData.stringSizeOfInt(-i) + 1 : IntData.stringSizeOfInt(i));
    }

    private static int stringSizeOfInt(int x) {
        int i = 0;
        while (x > sizeTable[i]) {
            ++i;
        }
        return i + 1;
    }

    @Override
    public String toString() {
        return String.valueOf(this.data);
    }

    @Override
    public int length() {
        return this.length;
    }

    @Override
    public char charAt(int index) {
        return this.toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return this.toString().substring(start, end);
    }

    @Override
    public void writeTo(UTF8XmlOutput output) throws IOException {
        output.text(this.data);
    }
}

