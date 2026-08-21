/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPNameAndType;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class EnclosingMethodAttribute
extends Attribute {
    private static CPUTF8 attributeName;
    private int classIndex;
    private int methodIndex;
    private final CPClass cpClass;
    private final CPNameAndType method;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public EnclosingMethodAttribute(CPClass cpClass, CPNameAndType method) {
        super(attributeName);
        this.cpClass = cpClass;
        this.method = method;
    }

    @Override
    protected int getLength() {
        return 4;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        if (this.method != null) {
            return new ClassFileEntry[]{attributeName, this.cpClass, this.method};
        }
        return new ClassFileEntry[]{attributeName, this.cpClass};
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.cpClass.resolve(pool);
        this.classIndex = pool.indexOf(this.cpClass);
        if (this.method != null) {
            this.method.resolve(pool);
            this.methodIndex = pool.indexOf(this.method);
        } else {
            this.methodIndex = 0;
        }
    }

    @Override
    public String toString() {
        return "EnclosingMethod";
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.classIndex);
        dos.writeShort(this.methodIndex);
    }
}

