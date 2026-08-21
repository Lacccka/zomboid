/*
 * Decompiled with CFR 0.152.
 */
package zombie.scripting.objects;

import zombie.UsedFromLua;

@UsedFromLua
public enum WeaponReloadType {
    NONE(""),
    BOLT_ACTION("boltaction"),
    BOLT_ACTION_NO_MAG("boltactionnomag"),
    DOUBLE_BARREL_SHOTGUN("doublebarrelshotgun"),
    DOUBLE_BARREL_SHOTGUN_SAWN("doublebarrelshotgunsawn"),
    HANDGUN("handgun"),
    LEVER_ACTION("leveraction"),
    REVOLVER("revolver"),
    SHOTGUN("shotgun");

    private final String id;

    private WeaponReloadType(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }

    public static WeaponReloadType fromValue(String value) {
        for (WeaponReloadType weaponReloadType : WeaponReloadType.values()) {
            if (!weaponReloadType.id.equals(value)) continue;
            return weaponReloadType;
        }
        return NONE;
    }
}

