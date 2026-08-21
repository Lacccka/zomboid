/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstantNumber;

public class CPFloat
extends CPConstantNumber {
    public CPFloat(Float value, int globalIndex) {
        super((byte)4, value, globalIndex);
    }

    @Override
    public String toString() {
        return "Float: " + this.getValue();
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeFloat(this.getNumber().floatValue());
    }
}

