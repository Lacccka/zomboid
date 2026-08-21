/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.harmony.unpack200.bytecode;

import java.util.List;
import org.apache.commons.compress.harmony.unpack200.bytecode.Attribute;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPMember;
import org.apache.commons.compress.harmony.unpack200.bytecode.CPUTF8;

public class CPField
extends CPMember {
    public CPField(CPUTF8 name, CPUTF8 descriptor, long flags, List<Attribute> attributes) {
        super(name, descriptor, flags, attributes);
    }

    @Override
    public String toString() {
        return "Field: " + this.name + "(" + this.descriptor + ")";
    }
}

