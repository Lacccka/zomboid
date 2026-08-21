/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemBodyLocation;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.SoundKey;

public class ContainerItemBuilder
extends ItemBuilder<ContainerItemBuilder> {
    private final Writeable.Property<String> acceptItemFunction = this.property("AcceptItemFunction");
    private final Writeable.Property<ItemBodyLocation> canBeEquipped = this.property("CanBeEquipped");
    private final Writeable.Property<SoundKey> closeSound = this.property("CloseSound");
    private final Writeable.Property<String> onlyAcceptCategory = this.property("OnlyAcceptCategory");
    private final Writeable.Property<SoundKey> openSound = this.property("OpenSound");
    private final Writeable.Property<SoundKey> putInSound = this.property("PutInSound");
    private final Writeable.Property<Integer> weightReduction = this.property("WeightReduction");

    public ContainerItemBuilder(ItemKey item) {
        super(item);
    }

    public ContainerItemBuilder acceptItemFunction(String acceptItemFunction) {
        this.acceptItemFunction.setValue(acceptItemFunction);
        return this;
    }

    @Override
    public ContainerItemBuilder canBeEquipped(ItemBodyLocation canBeEquipped) {
        this.canBeEquipped.setValue(canBeEquipped);
        return this;
    }

    public ContainerItemBuilder closeSound(SoundKey closeSound) {
        this.closeSound.setValue(closeSound);
        return this;
    }

    public ContainerItemBuilder onlyAcceptCategory(String onlyAcceptCategory) {
        this.onlyAcceptCategory.setValue(onlyAcceptCategory);
        return this;
    }

    public ContainerItemBuilder openSound(SoundKey openSound) {
        this.openSound.setValue(openSound);
        return this;
    }

    public ContainerItemBuilder putInSound(SoundKey putInSound) {
        this.putInSound.setValue(putInSound);
        return this;
    }

    @Override
    public ContainerItemBuilder weightReduction(int weightReduction) {
        this.weightReduction.setValue(weightReduction);
        return this;
    }
}

