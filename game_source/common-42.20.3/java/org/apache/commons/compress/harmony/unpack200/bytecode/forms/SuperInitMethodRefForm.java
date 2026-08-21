/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode.forms;

import org.apache.commons.compress.harmony.unpack200.bytecode.OperandManager;
import org.apache.commons.compress.harmony.unpack200.bytecode.forms.InitMethodReferenceForm;

public class SuperInitMethodRefForm
extends InitMethodReferenceForm {
    public SuperInitMethodRefForm(int opcode, String name, int[] rewrite) {
        super(opcode, name, rewrite);
    }

    @Override
    protected String context(OperandManager operandManager) {
        return operandManager.getSuperClass();
    }
}

