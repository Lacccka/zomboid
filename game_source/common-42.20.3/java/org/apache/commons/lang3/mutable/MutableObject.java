/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.mutable;

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.mutable.Mutable;

public class MutableObject<T>
implements Mutable<T>,
Serializable {
    private static final long serialVersionUID = 86241875189L;
    private T value;

    public MutableObject() {
    }

    public MutableObject(T value) {
        this.value = value;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() == obj.getClass()) {
            MutableObject that = (MutableObject)obj;
            return Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    public String toString() {
        return Objects.toString(this.value);
    }
}

