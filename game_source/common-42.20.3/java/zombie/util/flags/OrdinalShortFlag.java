/*
 * Decompiled with CFR 0.152.
 */
package zombie.util.flags;

import zombie.util.flags.ShortFlag;

public interface OrdinalShortFlag
extends ShortFlag {
    @Override
    default public short flag() {
        return (short)(1 << this.ordinal());
    }

    public int ordinal();
}

