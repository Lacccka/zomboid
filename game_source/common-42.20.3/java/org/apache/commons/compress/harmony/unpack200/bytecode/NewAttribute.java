/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.BCIRenumberedAttribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;

public class NewAttribute
extends BCIRenumberedAttribute {
    private final List<Integer> lengths = new ArrayList<Integer>();
    private final List<Object> body = new ArrayList<Object>();
    private ClassConstantPool pool;
    private final int layoutIndex;

    public NewAttribute(CPUTF8 attributeName, int layoutIndex) {
        super(attributeName);
        this.layoutIndex = layoutIndex;
    }

    public void addBCIndex(int length, int value) {
        this.lengths.add(length);
        this.body.add(new BCIndex(value));
    }

    public void addBCLength(int length, int value) {
        this.lengths.add(length);
        this.body.add(new BCLength(value));
    }

    public void addBCOffset(int length, int value) {
        this.lengths.add(length);
        this.body.add(new BCOffset(value));
    }

    public void addInteger(int length, long value) {
        this.lengths.add(length);
        this.body.add(value);
    }

    public void addToBody(int length, Object value) {
        this.lengths.add(length);
        this.body.add(value);
    }

    public int getLayoutIndex() {
        return this.layoutIndex;
    }

    @Override
    protected int getLength() {
        int length = 0;
        for (Integer len : this.lengths) {
            length += len.intValue();
        }
        return length;
    }

    @Override
    protected ClassFileEntry[] getNestedClassFileEntries() {
        int total = 1;
        for (Object element : this.body) {
            if (!(element instanceof ClassFileEntry)) continue;
            ++total;
        }
        ClassFileEntry[] nested = new ClassFileEntry[total];
        nested[0] = this.getAttributeName();
        int i = 1;
        for (Object element : this.body) {
            if (!(element instanceof ClassFileEntry)) continue;
            nested[i] = (ClassFileEntry)element;
            ++i;
        }
        return nested;
    }

    @Override
    protected int[] getStartPCs() {
        return null;
    }

    @Override
    public void renumber(List<Integer> byteCodeOffsets) {
        if (!this.renumbered) {
            Object previous = null;
            for (Object obj : this.body) {
                if (obj instanceof BCIndex) {
                    BCIndex bcIndex = (BCIndex)obj;
                    bcIndex.setActualValue(byteCodeOffsets.get(bcIndex.index));
                } else if (obj instanceof BCOffset) {
                    BCOffset bcOffset = (BCOffset)obj;
                    if (previous instanceof BCIndex) {
                        int index = ((BCIndex)previous).index + bcOffset.offset;
                        bcOffset.setIndex(index);
                        bcOffset.setActualValue(byteCodeOffsets.get(index));
                    } else if (previous instanceof BCOffset) {
                        int index = ((BCOffset)previous).index + bcOffset.offset;
                        bcOffset.setIndex(index);
                        bcOffset.setActualValue(byteCodeOffsets.get(index));
                    } else {
                        bcOffset.setActualValue(byteCodeOffsets.get(bcOffset.offset));
                    }
                } else if (obj instanceof BCLength) {
                    BCLength bcLength = (BCLength)obj;
                    BCIndex prevIndex = (BCIndex)previous;
                    int index = prevIndex.index + bcLength.length;
                    int actualLength = byteCodeOffsets.get(index) - prevIndex.actualValue;
                    bcLength.setActualValue(actualLength);
                }
                previous = obj;
            }
            this.renumbered = true;
        }
    }

    @Override
    protected void resolve(ClassConstantPool pool) {
        super.resolve(pool);
        for (Object element : this.body) {
            if (!(element instanceof ClassFileEntry)) continue;
            ((ClassFileEntry)element).resolve(pool);
        }
        this.pool = pool;
    }

    @Override
    public String toString() {
        return this.attributeName.underlyingString();
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        block6: for (int i = 0; i < this.lengths.size(); ++i) {
            int length = this.lengths.get(i);
            Object obj = this.body.get(i);
            long value = 0L;
            if (obj instanceof Long) {
                value = (Long)obj;
            } else if (obj instanceof ClassFileEntry) {
                value = this.pool.indexOf((ClassFileEntry)obj);
            } else if (obj instanceof AbstractBcValue) {
                value = ((AbstractBcValue)obj).actualValue;
            }
            switch (length) {
                case 1: {
                    dos.writeByte((int)value);
                    continue block6;
                }
                case 2: {
                    dos.writeShort((int)value);
                    continue block6;
                }
                case 4: {
                    dos.writeInt((int)value);
                    continue block6;
                }
                case 8: {
                    dos.writeLong(value);
                    continue block6;
                }
            }
        }
    }

    private static final class BCIndex
    extends AbstractBcValue {
        private final int index;

        BCIndex(int index) {
            this.index = index;
        }
    }

    private static final class BCLength
    extends AbstractBcValue {
        private final int length;

        BCLength(int length) {
            this.length = length;
        }
    }

    private static final class BCOffset
    extends AbstractBcValue {
        private final int offset;
        private int index;

        BCOffset(int offset) {
            this.offset = offset;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    private static abstract class AbstractBcValue {
        int actualValue;

        private AbstractBcValue() {
        }

        public void setActualValue(int value) {
            this.actualValue = value;
        }
    }
}

