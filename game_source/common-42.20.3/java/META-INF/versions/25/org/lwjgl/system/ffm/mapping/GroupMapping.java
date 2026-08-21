/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm.mapping;

import java.lang.foreign.GroupLayout;
import org.lwjgl.system.ffm.mapping.DataMapping;

public interface GroupMapping<L extends GroupLayout>
extends DataMapping<L> {
    @Override
    public GroupMapping<L> withByteAlignment(long var1);

    @Override
    public GroupMapping<L> typedef(String var1);
}

