/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public class InnerClassesAttribute
extends Attribute {
    private static CPUTF8 attributeName;
    private final List<InnerClassesEntry> innerClasses = new ArrayList<InnerClassesEntry>();
    private final List<ConstantPoolEntry> nestedClassFileEntries = new ArrayList<ConstantPoolEntry>();

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public InnerClassesAttribute(String name) {
        super(attributeName);
        this.nestedClassFileEntries.add(this.getAttributeName());
    }

    public void addInnerClassesEntry(CPClass innerClass, CPClass outerClass, CPUTF8 innerName, int flags) {
        if (innerClass != null) {
            this.nestedClassFileEntries.add(innerClass);
        }
        if (outerClass != null) {
            this.nestedClassFileEntries.add(outerClass);
        }
        if (innerName != null) {
            this.nestedClassFileEntries.add(innerName);
        }
        this.addInnerClassesEntry(new InnerClassesEntry(innerClass, outerClass, innerName, flags));
    }

    private void addInnerClassesEntry(InnerClassesEntry innerClassesEntry) {
        this.innerClasses.add(innerClassesEntry);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        InnerClassesAttribute other = (InnerClassesAttribute)obj;
        return !(this.getAttributeName() == null ? other.getAttributeName() != null : !this.getAttributeName().equals(other.getAttributeName()));
    }

    @Override
    protected int getLength() {
        return 2 + 8 * this.innerClasses.size();
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return this.nestedClassFileEntries.toArray(NONE);
    }

    @Override
    public int hashCode() {
        int PRIME = 31;
        int result = super.hashCode();
        result = 31 * result + (this.getAttributeName() == null ? 0 : this.getAttributeName().hashCode());
        return result;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        for (InnerClassesEntry entry : this.innerClasses) {
            entry.resolve(pool);
        }
    }

    @Override
    public String toString() {
        return "InnerClasses: " + this.getAttributeName();
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.innerClasses.size());
        for (InnerClassesEntry entry : this.innerClasses) {
            entry.write(dos);
        }
    }

    private static final class InnerClassesEntry {
        CPClass innerClassInfo;
        CPClass outerClassInfo;
        CPUTF8 innerClassName;
        int innerClassInfoIndex = -1;
        int outerClassInfoIndex = -1;
        int innerNameIndex = -1;
        int innerClassAccessFlags = -1;

        InnerClassesEntry(CPClass innerClass, CPClass outerClass, CPUTF8 innerName, int flags) {
            this.innerClassInfo = innerClass;
            this.outerClassInfo = outerClass;
            this.innerClassName = innerName;
            this.innerClassAccessFlags = flags;
        }

        public void resolve(ClassConstantPool pool) {
            if (this.innerClassInfo != null) {
                this.innerClassInfo.resolve(pool);
                this.innerClassInfoIndex = pool.indexOf(this.innerClassInfo);
            } else {
                this.innerClassInfoIndex = 0;
            }
            if (this.innerClassName != null) {
                this.innerClassName.resolve(pool);
                this.innerNameIndex = pool.indexOf(this.innerClassName);
            } else {
                this.innerNameIndex = 0;
            }
            if (this.outerClassInfo != null) {
                this.outerClassInfo.resolve(pool);
                this.outerClassInfoIndex = pool.indexOf(this.outerClassInfo);
            } else {
                this.outerClassInfoIndex = 0;
            }
        }

        public void write(DataOutputStream dos) throws IOException {
            dos.writeShort(this.innerClassInfoIndex);
            dos.writeShort(this.outerClassInfoIndex);
            dos.writeShort(this.innerNameIndex);
            dos.writeShort(this.innerClassAccessFlags);
        }
    }
}

