/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm.mapping;

import java.lang.foreign.ValueLayout;
import org.lwjgl.system.ffm.mapping.DataMapping;
import org.lwjgl.system.ffm.mapping.IntegerMapping;
import org.lwjgl.system.ffm.mapping.Mapping;

public sealed interface PrimitiveMapping<L extends ValueLayout>
extends DataMapping<L>
permits Mapping.Boolean, IntegerMapping, Mapping.Float, Mapping.Double {
    @Override
    public PrimitiveMapping<L> withByteAlignment(long var1);

    @Override
    public PrimitiveMapping<L> typedef(String var1);
}

