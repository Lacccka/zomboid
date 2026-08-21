/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.AnnotationsAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class RuntimeVisibleorInvisibleParameterAnnotationsAttribute
extends AnnotationsAttribute {
    private final int numParameters;
    private final ParameterAnnotation[] parameterAnnotations;

    public RuntimeVisibleorInvisibleParameterAnnotationsAttribute(CPUTF8 name, ParameterAnnotation[] parameterAnnotations) {
        super(name);
        this.numParameters = parameterAnnotations.length;
        this.parameterAnnotations = parameterAnnotations;
    }

    @Override
    protected int getLength() {
        int length = 1;
        for (int i = 0; i < this.numParameters; ++i) {
            length += this.parameterAnnotations[i].getLength();
        }
        return length;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        ArrayList<Object> nested = new ArrayList<Object>();
        nested.add(this.attributeName);
        for (ParameterAnnotation parameterAnnotation : this.parameterAnnotations) {
            nested.addAll(parameterAnnotation.getClassFileEntries());
        }
        return nested.toArray(NONE);
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        for (ParameterAnnotation parameterAnnotation : this.parameterAnnotations) {
            parameterAnnotation.resolve(pool);
        }
    }

    @Override
    public String toString() {
        return this.attributeName.underlyingString() + ": " + this.numParameters + " parameter annotations";
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeByte(this.numParameters);
        for (int i = 0; i < this.numParameters; ++i) {
            this.parameterAnnotations[i].writeBody(dos);
        }
    }

    public static class ParameterAnnotation {
        private final AnnotationsAttribute.Annotation[] annotations;
        private final int numAnnotations;

        public ParameterAnnotation(AnnotationsAttribute.Annotation[] annotations) {
            this.numAnnotations = annotations.length;
            this.annotations = annotations;
        }

        public List<Object> getClassFileEntries() {
            ArrayList<Object> nested = new ArrayList<Object>();
            for (AnnotationsAttribute.Annotation annotation : this.annotations) {
                nested.addAll(annotation.getClassFileEntries());
            }
            return nested;
        }

        public int getLength() {
            int length = 2;
            for (AnnotationsAttribute.Annotation annotation : this.annotations) {
                length += annotation.getLength();
            }
            return length;
        }

        public void resolve(ClassConstantPool pool) {
            for (AnnotationsAttribute.Annotation annotation : this.annotations) {
                annotation.resolve(pool);
            }
        }

        public void writeBody(DataOutputStream dos) throws IOException {
            dos.writeShort(this.numAnnotations);
            for (AnnotationsAttribute.Annotation annotation : this.annotations) {
                annotation.writeBody(dos);
            }
        }
    }
}

