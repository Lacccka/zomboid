/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemTag;
import zombie.scripting.objects.VehicleItemEquip;

public class VehicleTemplateItemBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<ItemKey> type = this.property("type");
    private final Writeable.ListProperty<ItemTag> tags = this.listProperty("tags", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Integer> count = this.property("count");
    private final Writeable.Property<Boolean> keep = this.property("keep");
    private final Writeable.Property<VehicleItemEquip> equip = this.property("equip");

    public VehicleTemplateItemBuilder(int id) {
        super(Integer.toString(id), (a, b) -> b);
    }

    public VehicleTemplateItemBuilder type(ItemKey type) {
        this.type.setValue(type);
        return this;
    }

    public VehicleTemplateItemBuilder tags(ItemTag ... tags) {
        this.tags.addValues((ItemTag[])tags);
        return this;
    }

    public VehicleTemplateItemBuilder count(int count) {
        this.count.setValue(count);
        return this;
    }

    public VehicleTemplateItemBuilder keep(boolean keep) {
        this.keep.setValue(keep);
        return this;
    }

    public VehicleTemplateItemBuilder equip(VehicleItemEquip equip) {
        this.equip.setValue(equip);
        return this;
    }
}

