/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPRef;

public class CPInterfaceMethodRef
extends CPRef {
    private boolean hashCodeComputed;
    private int cachedHashCode;

    public CPInterfaceMethodRef(CPClass className, CPNameAndType descriptor, int globalIndex) {
        super((byte)11, className, descriptor, globalIndex);
    }

    private void generateHashCode() {
        this.hashCodeComputed = true;
        int PRIME = 31;
        int result = 1;
        result = 31 * result + this.className.hashCode();
        this.cachedHashCode = result = 31 * result + this.nameAndType.hashCode();
    }

    @Override
    public int hashCode() {
        if (!this.hashCodeComputed) {
            this.generateHashCode();
        }
        return this.cachedHashCode;
    }

    public int invokeInterfaceCount() {
        return this.nameAndType.invokeInterfaceCount();
    }
}

