/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import java.util.Objects;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.SegmentConstantPool;
import org.apache.commons.compress.harmony.unpack200.bytecode.ByteCode;
import org.apache.commons.compress.harmony.unpack200.bytecode.ClassFileEntry;
import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ByteCodeForm;

public abstract class ReferenceForm
extends ByteCodeForm {
    public ReferenceForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    protected abstract int getOffset(OperandManager var1);

    protected abstract int getPoolID();

    @Override
    public void setByteCodeOperands(ByteCode byteCode, OperandManager operandManager, int codeLength) {
        int offset = this.getOffset(operandManager);
        try {
            this.setNestedEntries(byteCode, operandManager, offset);
        }
        catch (Pack200Exception ex) {
            throw new Error("Got a pack200 exception. What to do?");
        }
    }

    protected void setNestedEntries(ByteCode byteCode, OperandManager operandManager, int offset) throws Pack200Exception {
        SegmentConstantPool globalPool = operandManager.globalConstantPool();
        ClassFileEntry[] nested = new ClassFileEntry[]{globalPool.getConstantPoolEntry(this.getPoolID(), offset)};
        Objects.requireNonNull(nested[0], "Null nested entries are not allowed");
        byteCode.setNested(nested);
        byteCode.setNestedPositions(new int[][]{{0, 2}});
    }
}

