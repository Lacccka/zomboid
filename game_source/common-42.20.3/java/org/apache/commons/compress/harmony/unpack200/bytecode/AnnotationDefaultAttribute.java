/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.compress.harmony.unpack200.bytecode.AnnotationsAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class AnnotationDefaultAttribute
extends AnnotationsAttribute {
    private static CPUTF8 attributeName;
    private final AnnotationsAttribute.ElementValue elementValue;

    public static void setAttributeName(CPUTF8 cpUTF8Value) {
        attributeName = cpUTF8Value;
    }

    public AnnotationDefaultAttribute(AnnotationsAttribute.ElementValue elementValue) {
        super(attributeName);
        this.elementValue = elementValue;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    protected int getLength() {
        return this.elementValue.getLength();
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ArrayList<Object> nested = new ArrayList<Object>();
        nested.add(attributeName);
        nested.addAll(this.elementValue.getClassFileEntries());
        ClassFileEntry[] nestedEntries = new ClassFileEntry[nested.size()];
        for (int i = 0; i < nestedEntries.length; ++i) {
            nestedEntries[i] = (ClassFileEntry)nested.get(i);
        }
        return nestedEntries;
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        this.elementValue.resolve(pool);
    }

    @Override
    public String toString() {
        return "AnnotationDefault: " + this.elementValue;
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        this.elementValue.writeBody(dos);
    }
}

