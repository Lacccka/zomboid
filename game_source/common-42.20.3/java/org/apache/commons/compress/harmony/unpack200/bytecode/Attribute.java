/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public abstract class Attribute
extends ClassFileEntry {
    protected final CPUTF8 attributeName;
    private int attributeNameIndex;

    public Attribute(CPUTF8 attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    protected void doWrite(DataOutputStream dos) throws IOException {
        dos.writeShort(this.attributeNameIndex);
        dos.writeInt(this.getLength());
        this.writeBody(dos);
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
        Attribute other = (Attribute)obj;
        return Objects.equals(this.attributeName, other.attributeName);
    }

    protected CPUTF8 getAttributeName() {
        return this.attributeName;
    }

    protected abstract int getLength();

    protected int getLengthIncludingHeader() {
        return this.getLength() + 2 + 4;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.getAttributeName()};
    }

    public boolean hasBCIRenumbering() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.attributeName);
    }

    public boolean isSourceFileAttribute() {
        return false;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.attributeNameIndex = pool.indexOf(this.attributeName);
    }

    protected abstract void writeBody(DataOutputStream var1) throws IOException;
}

