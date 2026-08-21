/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemKey;

public class KeyItemBuilder
extends ItemBuilder<KeyItemBuilder> {
    private final Writeable.Property<Boolean> digitalPadlock = this.property("DigitalPadlock");
    private final Writeable.Property<Boolean> padlock = this.property("Padlock");

    public KeyItemBuilder(ItemKey item) {
        super(item);
    }

    public KeyItemBuilder digitalPadlock(boolean digitalPadlock) {
        this.digitalPadlock.setValue(digitalPadlock);
        return this;
    }

    public KeyItemBuilder padlock(boolean padlock) {
        this.padlock.setValue(padlock);
        return this;
    }
}

