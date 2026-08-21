/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class SignatureAttribute
extends Attribute {
    private static CPUTF8 attributeName;
    private int signatureIndex;
    private final CPUTF8 signature;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public SignatureAttribute(CPUTF8 value) {
        super(attributeName);
        this.signature = value;
    }

    @Override
    protected int getLength() {
        return 2;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        return new ClassFileEntry[]{this.getAttributeName(), this.signature};
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.signature.resolve(pool);
        this.signatureIndex = pool.indexOf(this.signature);
    }

    @Override
    public String toString() {
        return "Signature: " + this.signature;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.signatureIndex);
    }
}

