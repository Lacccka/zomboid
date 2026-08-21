/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import generation.builders.validation.SerializableMethod;
import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.DrainableComboItem;
import zombie.scripting.objects.ItemKey;

public class DrainableItemBuilder
extends ItemBuilder<DrainableItemBuilder> {
    private final Writeable.Property<Boolean> cantBeConsolided = this.property("cantBeConsolided");
    private final Writeable.Property<Boolean> isCookable = this.property("IsCookable");
    private final Writeable.Property<Integer> minutesToCook = this.property("MinutesToCook");
    private final Writeable.Property<String> onCooked = this.property("OnCooked");
    private final Writeable.Property<String> onEat = this.property("OnEat");
    private final Writeable.Property<ItemKey> replaceOnCooked = this.property("ReplaceOnCooked");
    private final Writeable.Property<ItemKey> replaceOnDeplete = this.property("ReplaceOnDeplete");
    private final Writeable.Property<Integer> ticksPerEquipUse = this.property("ticksPerEquipUse");
    private final Writeable.Property<Float> useDelta = this.property("UseDelta", this::formatFloat);
    private final Writeable.Property<Boolean> useWhileEquipped = this.property("UseWhileEquipped");
    private final Writeable.Property<Boolean> useWhileUnequipped = this.property("UseWhileUnequipped");
    private final Writeable.Property<Float> weightEmpty = this.property("WeightEmpty");

    public DrainableItemBuilder(ItemKey item) {
        super(item);
    }

    public DrainableItemBuilder cantBeConsolided(boolean cantBeConsolided) {
        this.cantBeConsolided.setValue(cantBeConsolided);
        return this;
    }

    @Override
    public DrainableItemBuilder isCookable(boolean isCookable) {
        this.isCookable.setValue(isCookable);
        return this;
    }

    public DrainableItemBuilder minutesToCook(int minutesToCook) {
        this.minutesToCook.setValue(minutesToCook);
        return this;
    }

    public DrainableItemBuilder onCooked(String onCooked) {
        this.onCooked.setValue(onCooked);
        return this;
    }

    public DrainableItemBuilder onEat(String onEat) {
        this.onEat.setValue(onEat);
        return this;
    }

    public DrainableItemBuilder onEat(SerializableMethod.Consumer2<DrainableComboItem, IsoGameCharacter> onEat) {
        this.onEat.setValue(SerializableMethod.asLuaString(onEat));
        return this;
    }

    public DrainableItemBuilder replaceOnCooked(ItemKey replaceOnCooked) {
        this.replaceOnCooked.setValue(replaceOnCooked);
        return this;
    }

    public DrainableItemBuilder replaceOnDeplete(ItemKey replaceOnDeplete) {
        this.replaceOnDeplete.setValue(replaceOnDeplete);
        return this;
    }

    public DrainableItemBuilder ticksPerEquipUse(int ticksPerEquipUse) {
        this.ticksPerEquipUse.setValue(ticksPerEquipUse);
        return this;
    }

    public DrainableItemBuilder useDelta(float useDelta) {
        this.useDelta.setValue(Float.valueOf(useDelta));
        return this;
    }

    @Override
    public DrainableItemBuilder useWhileEquipped(boolean useWhileEquipped) {
        this.useWhileEquipped.setValue(useWhileEquipped);
        return this;
    }

    public DrainableItemBuilder useWhileUnequipped(boolean useWhileUnequipped) {
        this.useWhileUnequipped.setValue(useWhileUnequipped);
        return this;
    }

    @Override
    public DrainableItemBuilder weightEmpty(float weightEmpty) {
        this.weightEmpty.setValue(Float.valueOf(weightEmpty));
        return this;
    }
}

