/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.ClassSpecificReferenceForm;

public class SuperFieldRefForm
extends ClassSpecificReferenceForm {
    public SuperFieldRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    @Override
    protected String context(OperandManager operandManager) {
        return operandManager.getSuperClass();
    }

    @Override
    protected int getOffset(OperandManager operandManager) {
        return operandManager.nextSuperFieldRef();
    }

    @Override
    protected int getPoolID() {
        return 10;
    }
}

