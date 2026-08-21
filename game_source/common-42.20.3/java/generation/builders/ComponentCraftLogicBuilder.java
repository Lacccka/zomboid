/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;
import zombie.entity.components.crafting.StartMode;
import zombie.scripting.objects.CraftRecipeTag;

public class ComponentCraftLogicBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.ListProperty<CraftRecipeTag> recipes = this.listProperty("recipes", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<StartMode> startMode = this.property("startMode");
    private final Writeable.Property<String> inputGroup = this.property("inputGroup");
    private final Writeable.Property<String> outputGroup = this.property("outputGroup");

    public ComponentCraftLogicBuilder() {
        super("CraftLogic");
    }

    public ComponentCraftLogicBuilder recipes(CraftRecipeTag ... recipes) {
        this.recipes.addValues((CraftRecipeTag[])recipes);
        return this;
    }

    public ComponentCraftLogicBuilder startMode(StartMode startMode) {
        this.startMode.setValue(startMode);
        return this;
    }

    public ComponentCraftLogicBuilder inputGroup(String inputGroup) {
        this.inputGroup.setValue(inputGroup);
        return this;
    }

    public ComponentCraftLogicBuilder outputGroup(String outputGroup) {
        this.outputGroup.setValue(outputGroup);
        return this;
    }
}

