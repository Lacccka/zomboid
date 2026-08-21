/*
 * Decompiled with CFR 0.152.
 */
package zombie.iso.objects.interfaces;

import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.HandWeapon;
import zombie.iso.IsoMovingObject;

public interface Thumpable {
    public boolean isDestroyed();

    default public void Thump(IsoMovingObject isoMovingObject) {
        this.Thump(isoMovingObject, 1);
    }

    public void Thump(IsoMovingObject var1, int var2);

    public void WeaponHit(IsoGameCharacter var1, HandWeapon var2);

    public Thumpable getThumpableFor(IsoGameCharacter var1);

    public Thumpable getThumpableFor(IsoGameCharacter var1, HandWeapon var2);

    public float getThumpCondition();
}

