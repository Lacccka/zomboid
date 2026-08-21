/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.util.Objects;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public abstract class CPConstant
extends ConstantPoolEntry {
    private final Object value;

    public CPConstant(byte tag, Object value, int globalIndex) {
        super(tag, globalIndex);
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        CPConstant other = (CPConstant)obj;
        return Objects.equals(this.value, other.value);
    }

    protected Object getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}

