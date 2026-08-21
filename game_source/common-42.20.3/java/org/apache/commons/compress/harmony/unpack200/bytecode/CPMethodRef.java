/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPRef;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class CPMethodRef
extends CPRef {
    private boolean hashCodeComputed;
    private int cachedHashCode;

    public CPMethodRef(CPClass className, CPNameAndType descriptor, int globalIndex) {
        super((byte)10, className, descriptor, globalIndex);
    }

    private void generateHashCode() {
        this.hashCodeComputed = true;
        int PRIME = 31;
        int result = 1;
        result = 31 * result + this.className.hashCode();
        this.cachedHashCode = result = 31 * result + this.nameAndType.hashCode();
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.className, this.nameAndType};
    }

    @Override
    public int hashCode() {
        if (!this.hashCodeComputed) {
            this.generateHashCode();
        }
        return this.cachedHashCode;
    }
}

