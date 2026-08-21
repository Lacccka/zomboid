/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.ConstantPoolEntry;

public class ClassFile {
    private static final int MAGIC = -889275714;
    public int major;
    public int minor;
    public ClassConstantPool pool = new ClassConstantPool();
    public int accessFlags;
    public int thisClass;
    public int superClass;
    public int[] interfaces;
    public ClassFileEntry[] fields;
    public ClassFileEntry[] methods;
    public Attribute[] attributes;

    /*
     * WARNING - void declaration
     */
    public void write(DataOutputStream dos) throws IOException {
        void var2_3;
        dos.writeInt(-889275714);
        dos.writeShort(this.minor);
        dos.writeShort(this.major);
        dos.writeShort(this.pool.size() + 1);
        boolean bl = true;
        while (var2_3 <= this.pool.size()) {
            ConstantPoolEntry entry = (ConstantPoolEntry)this.pool.get((int)var2_3);
            entry.doWrite(dos);
            if (entry.getTag() == 6 || entry.getTag() == 5) {
                ++var2_3;
            }
            ++var2_3;
        }
        dos.writeShort(this.accessFlags);
        dos.writeShort(this.thisClass);
        dos.writeShort(this.superClass);
        dos.writeShort(this.interfaces.length);
        for (int element : this.interfaces) {
            dos.writeShort(element);
        }
        dos.writeShort(this.fields.length);
        for (ClassFileEntry field : this.fields) {
            field.write(dos);
        }
        dos.writeShort(this.methods.length);
        for (ClassFileEntry method : this.methods) {
            method.write(dos);
        }
        dos.writeShort(this.attributes.length);
        for (Attribute attribute : this.attributes) {
            attribute.write(dos);
        }
    }
}

