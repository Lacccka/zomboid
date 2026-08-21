/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPClass;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class ExceptionsAttribute
extends Attribute {
    private static CPUTF8 attributeName;
    private transient int[] exceptionIndexes;
    private final CPClass[] exceptions;

    private static int hashCode(Object[] array) {
        int prime = 31;
        if (array == null) {
            return 0;
        }
        int result = 1;
        for (Object element : array) {
            result = 31 * result + (element == null ? 0 : element.hashCode());
        }
        return result;
    }

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public ExceptionsAttribute(CPClass[] exceptions) {
        super(attributeName);
        this.exceptions = exceptions;
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
        ExceptionsAttribute other = (ExceptionsAttribute)obj;
        return Arrays.equals(this.exceptions, other.exceptions);
    }

    @Override
    protected int getLength() {
        return 2 + 2 * this.exceptions.length;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ClassFileEntry[] result = new ClassFileEntry[this.exceptions.length + 1];
        System.arraycopy(this.exceptions, 0, result, 0, this.exceptions.length);
        result[this.exceptions.length] = this.getAttributeName();
        return result;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + ExceptionsAttribute.hashCode(this.exceptions);
        return result;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.exceptionIndexes = new int[this.exceptions.length];
        for (int i = 0; i < this.exceptions.length; ++i) {
            this.exceptions[i].resolve(pool);
            this.exceptionIndexes[i] = pool.indexOf(this.exceptions[i]);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Exceptions: ");
        for (CPClass exception : this.exceptions) {
            sb.append(exception);
            sb.append(' ');
        }
        return sb.toString();
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeShort(this.exceptionIndexes.length);
        for (int element : this.exceptionIndexes) {
            dos.writeShort(element);
        }
    }
}

