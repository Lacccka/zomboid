/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstantNumber;

public class CPDouble
extends CPConstantNumber {
    public CPDouble(Double value, int globalIndex) {
        super((byte)6, value, globalIndex);
    }

    @Override
    public String toString() {
        return "Double: " + this.getValue();
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeDouble(this.getNumber().doubleValue());
    }
}

