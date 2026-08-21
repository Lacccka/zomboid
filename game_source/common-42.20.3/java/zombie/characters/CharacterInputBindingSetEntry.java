/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import zombie.UsedFromLua;

@UsedFromLua
public abstract class CharacterInputBindingSetEntry {
    abstract void apply();

    abstract boolean isValid();
}

