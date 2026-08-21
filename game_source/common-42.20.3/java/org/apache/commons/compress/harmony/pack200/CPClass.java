/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.pack200;

import org.apache.commons.compress.harmony.pack200.CPConstant;
import org.apache.commons.compress.harmony.pack200.CPUTF8;

public class CPClass
extends CPConstant<CPClass> {
    private final String className;
    private final CPUTF8 utf8;
    private final boolean isInnerClass;

    public CPClass(CPUTF8 utf8) {
        char[] chars;
        this.utf8 = utf8;
        this.className = utf8.getUnderlyingString();
        for (char element : chars = this.className.toCharArray()) {
            if (element > '-') continue;
            this.isInnerClass = true;
            return;
        }
        this.isInnerClass = false;
    }

    @Override
    public int compareTo(CPClass arg0) {
        return this.className.compareTo(arg0.className);
    }

    public int getIndexInCpUtf8() {
        return this.utf8.getIndex();
    }

    public boolean isInnerClass() {
        return this.isInnerClass;
    }

    public String toString() {
        return this.className;
    }
}

