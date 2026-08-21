/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemKey;

public class MoveableItemBuilder
extends ItemBuilder<MoveableItemBuilder> {
    private final Writeable.Property<String> worldObjectSprite = this.property("WorldObjectSprite");

    public MoveableItemBuilder(ItemKey item) {
        super(item);
    }

    @Override
    public MoveableItemBuilder worldObjectSprite(String worldObjectSprite) {
        this.worldObjectSprite.setValue(worldObjectSprite);
        return this;
    }
}

