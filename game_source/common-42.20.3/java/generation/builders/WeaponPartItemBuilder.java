/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import generation.builders.validation.SerializableMethod;
import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.HandWeapon;
import zombie.inventory.types.WeaponPart;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemType;
import zombie.scripting.objects.WeaponPartType;

public class WeaponPartItemBuilder
extends ItemBuilder<WeaponPartItemBuilder> {
    private final Writeable.Property<Integer> aimingTimeModifier = this.property("AimingTimeModifier");
    private final Writeable.Property<String> canAttach = this.property("CanAttach");
    private final Writeable.Property<String> canDetach = this.property("CanDetach");
    private final Writeable.Property<Integer> clipSizeModifier = this.property("ClipSizeModifier");
    private final Writeable.Property<Float> damageModifier = this.property("DamageModifier");
    private final Writeable.Property<Integer> hitChanceModifier = this.property("HitChanceModifier");
    private final Writeable.Property<Integer> maxRangeModifier = this.property("MaxRangeModifier");
    private final Writeable.Property<Float> maxSightRange = this.property("MaxSightRange");
    private final Writeable.Property<Integer> minSightRange = this.property("MinSightRange");
    private final Writeable.ListProperty<ItemKey> mountOn = this.listProperty("MountOn", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<String> onAttach = this.property("OnAttach");
    private final Writeable.Property<String> onDetach = this.property("OnDetach");
    private final Writeable.Property<WeaponPartType> partType = this.property("PartType");
    private final Writeable.Property<Float> projectileSpreadModifier = this.property("ProjectileSpreadModifier");
    private final Writeable.Property<Float> recoilDelayModifier = this.property("RecoilDelayModifier");
    private final Writeable.Property<Integer> reloadTimeModifier = this.property("ReloadTimeModifier");
    private final Writeable.Property<Float> useDelta = this.property("UseDelta", this::formatFloat);
    private final Writeable.Property<Float> weightModifier = this.property("WeightModifier");

    public WeaponPartItemBuilder(ItemKey item) {
        super(item);
    }

    public WeaponPartItemBuilder aimingTimeModifier(int aimingTimeModifier) {
        this.aimingTimeModifier.setValue(aimingTimeModifier);
        return this;
    }

    public WeaponPartItemBuilder canAttach(SerializableMethod.Function3<IsoGameCharacter, HandWeapon, WeaponPart, Boolean> canAttach) {
        this.canAttach.setValue(SerializableMethod.asLuaString(canAttach));
        return this;
    }

    public WeaponPartItemBuilder canDetach(SerializableMethod.Function3<IsoGameCharacter, HandWeapon, WeaponPart, Boolean> canDetach) {
        this.canDetach.setValue(SerializableMethod.asLuaString(canDetach));
        return this;
    }

    public WeaponPartItemBuilder clipSizeModifier(int clipSizeModifier) {
        this.clipSizeModifier.setValue(clipSizeModifier);
        return this;
    }

    public WeaponPartItemBuilder damageModifier(float damageModifier) {
        this.damageModifier.setValue(Float.valueOf(damageModifier));
        return this;
    }

    public WeaponPartItemBuilder hitChanceModifier(int hitChanceModifier) {
        this.hitChanceModifier.setValue(hitChanceModifier);
        return this;
    }

    public WeaponPartItemBuilder maxRangeModifier(int maxRangeModifier) {
        this.maxRangeModifier.setValue(maxRangeModifier);
        return this;
    }

    public WeaponPartItemBuilder maxSightRange(float maxSightRange) {
        this.maxSightRange.setValue(Float.valueOf(maxSightRange));
        return this;
    }

    public WeaponPartItemBuilder minSightRange(int minSightRange) {
        this.minSightRange.setValue(minSightRange);
        return this;
    }

    public WeaponPartItemBuilder mountOn(ItemKey ... mountOn) {
        for (ItemKey item : mountOn) {
            if (item.itemType() == ItemType.WEAPON) continue;
            throw new RuntimeException("Invalid item type: " + String.valueOf(item.itemType()));
        }
        this.mountOn.addValues((ItemKey[])mountOn);
        return this;
    }

    public WeaponPartItemBuilder onAttach(String onAttach) {
        this.onAttach.setValue(onAttach);
        return this;
    }

    public WeaponPartItemBuilder onDetach(String onDetach) {
        this.onDetach.setValue(onDetach);
        return this;
    }

    public WeaponPartItemBuilder partType(WeaponPartType partType) {
        this.partType.setValue(partType);
        return this;
    }

    public WeaponPartItemBuilder projectileSpreadModifier(float projectileSpreadModifier) {
        this.projectileSpreadModifier.setValue(Float.valueOf(projectileSpreadModifier));
        return this;
    }

    public WeaponPartItemBuilder recoilDelayModifier(float recoilDelayModifier) {
        this.recoilDelayModifier.setValue(Float.valueOf(recoilDelayModifier));
        return this;
    }

    public WeaponPartItemBuilder reloadTimeModifier(int reloadTimeModifier) {
        this.reloadTimeModifier.setValue(reloadTimeModifier);
        return this;
    }

    public WeaponPartItemBuilder useDelta(float useDelta) {
        this.useDelta.setValue(Float.valueOf(useDelta));
        return this;
    }

    @Override
    public WeaponPartItemBuilder weightModifier(float weightModifier) {
        this.weightModifier.setValue(Float.valueOf(weightModifier));
        return this;
    }
}

