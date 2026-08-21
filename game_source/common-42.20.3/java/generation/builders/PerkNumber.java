/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import zombie.characters.skills.PerkFactory;

public record PerkNumber(PerkFactory.Perk perk, int number) {
    @Override
    public String toString() {
        return this.perk().name + ":" + this.number();
    }
}

