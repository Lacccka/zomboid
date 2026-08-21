/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm.mapping;

import java.lang.foreign.ValueLayout;
import org.lwjgl.system.ffm.mapping.Mapping;
import org.lwjgl.system.ffm.mapping.PrimitiveMapping;

public sealed interface IntegerMapping<L extends ValueLayout>
extends PrimitiveMapping<L>
permits Mapping.Byte, Mapping.Char, Mapping.Short, Mapping.Int, Mapping.Long, Mapping.CLong, Mapping.Size {
    public boolean signed();

    @Override
    public IntegerMapping<L> typedef(String var1);
}

