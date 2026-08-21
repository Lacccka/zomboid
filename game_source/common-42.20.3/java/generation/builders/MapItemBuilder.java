/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemKey;

public class MapItemBuilder
extends ItemBuilder<MapItemBuilder> {
    private final Writeable.Property<String> map = this.property("Map");

    public MapItemBuilder(ItemKey item) {
        super(item);
    }

    public MapItemBuilder map(String map) {
        this.map.setValue(map);
        return this;
    }
}

