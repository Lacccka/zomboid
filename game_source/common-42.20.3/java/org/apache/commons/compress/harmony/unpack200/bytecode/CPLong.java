/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPConstantNumber;

public class CPLong
extends CPConstantNumber {
    public CPLong(Long value, int globalIndex) {
        super((byte)5, value, globalIndex);
    }

    @Override
    public String toString() {
        return "Long: " + this.getValue();
    }

    @Override
    protected void writeBody(DataOutputStream dos) throws IOException {
        dos.writeLong(this.getNumber().longValue());
    }
}

