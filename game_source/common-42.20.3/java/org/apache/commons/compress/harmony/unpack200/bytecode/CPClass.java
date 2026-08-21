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
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public class CPClass
extends ConstantPoolEntry {
    private int index;
    public String name;
    private final CPUTF8 utf8;
    private boolean hashCodeComputed;
    private int cachedHashCode;

    public CPClass(CPUTF8 name, int globalIndex) {
        super((byte)7, globalIndex);
        this.name = Objects.requireNonNull(name, "name").underlyingString();
        this.utf8 = name;
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
        CPClass other = (CPClass)obj;
        return this.utf8.equals(other.utf8);
    }

    private void generateHashCode() {
        this.hashCodeComputed = true;
        this.cachedHashCode = this.utf8.hashCode();
    }

    public String getName() {
        return this.name;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.utf8};
    }

    @Override
    public int hashCode() {
        if (!this.hashCodeComputed) {
            this.generateHashCode();
        }
        return this.cachedHashCode;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.index = pool.indexOf(this.utf8);
    }

    @Override
    public String toString() {
        return "Class: " + this.getName();
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.index);
    }
}

