/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import zombie.entity.components.fluids.FluidCategory;

public class BlendWhiteListBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Boolean> whitelist = this.property("whitelist");
    private final Writeable.ListProperty<FluidCategory> categories = this.listProperty("categories", Writeable.ListProperty.Flags.HIDE_KEY, Writeable.ListProperty.Flags.SHOW_IF_EMPTY);

    public BlendWhiteListBuilder whitelist(boolean whitelist) {
        this.whitelist.setValue(whitelist);
        return this;
    }

    public BlendWhiteListBuilder addCategories(FluidCategory ... values2) {
        this.categories.addValues((FluidCategory[])values2);
        return this;
    }
}

