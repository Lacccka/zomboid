/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.CraftRecipeTag;

public class ComponentCraftBenchBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.ListProperty<CraftRecipeTag> recipes = this.listProperty("Recipes", ";", new Writeable.ListProperty.Flags[0]);

    public ComponentCraftBenchBuilder() {
        super("CraftBench");
    }

    public ComponentCraftBenchBuilder recipes(CraftRecipeTag ... recipes) {
        this.recipes.addValues((CraftRecipeTag[])recipes);
        return this;
    }
}

