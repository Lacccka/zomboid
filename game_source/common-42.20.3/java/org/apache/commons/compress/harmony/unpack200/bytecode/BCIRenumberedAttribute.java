/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;

public abstract class BCIRenumberedAttribute
extends Attribute {
    protected boolean renumbered;

    public BCIRenumberedAttribute(CPUTF8 attributeName) {
        super(attributeName);
    }

    @Override
    protected abstract int getLength();

    protected abstract int[] getStartPCs();

    @Override
    public boolean hasBCIRenumbering() {
        return true;
    }

    public void renumber(List<Integer> byteCodeOffsets) throws Pack200Exception {
        if (this.renumbered) {
            throw new Error("Trying to renumber a line number table that has already been renumbered");
        }
        this.renumbered = true;
        int[] startPCs = this.getStartPCs();
        Arrays.setAll(startPCs, i -> (Integer)byteCodeOffsets.get(startPCs[i]));
    }

    @Override
    public abstract String toString();

    @Override
    protected abstract void writeBody(DataOutputStream var1) throws IOException;
}

