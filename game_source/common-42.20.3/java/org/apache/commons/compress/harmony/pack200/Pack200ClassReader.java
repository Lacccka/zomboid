/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.ClassReader
 */
package org.apache.commons.compress.harmony.pack200;

import org.objectweb.asm.ClassReader;

public class Pack200ClassReader
extends ClassReader {
    private boolean lastConstantHadWideIndex;
    private int lastUnsignedShort;
    private boolean anySyntheticAttributes;
    private String fileName;

    public Pack200ClassReader(byte[] b) {
        super(b);
    }

    public String getFileName() {
        return this.fileName;
    }

    public boolean hasSyntheticAttributes() {
        return this.anySyntheticAttributes;
    }

    public boolean lastConstantHadWideIndex() {
        return this.lastConstantHadWideIndex;
    }

    public Object readConst(int item, char[] buf) {
        this.lastConstantHadWideIndex = item == this.lastUnsignedShort;
        return super.readConst(item, buf);
    }

    public int readUnsignedShort(int index) {
        int unsignedShort = super.readUnsignedShort(index);
        this.lastUnsignedShort = index > 0 && this.b[index - 1] == 19 ? unsignedShort : Short.MIN_VALUE;
        return unsignedShort;
    }

    public String readUTF8(int arg0, char[] arg1) {
        String utf8 = super.readUTF8(arg0, arg1);
        if (!this.anySyntheticAttributes && "Synthetic".equals(utf8)) {
            this.anySyntheticAttributes = true;
        }
        return utf8;
    }

    public void setFileName(String name) {
        this.fileName = name;
    }
}

