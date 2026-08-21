/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;
import zombie.entity.components.crafting.StartMode;
import zombie.scripting.objects.CraftRecipeTag;
import zombie.scripting.objects.TimedActionKey;

public class ComponentDryingCraftLogicBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.ListProperty<CraftRecipeTag> recipes = this.listProperty("Recipes", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<StartMode> startMode = this.property("StartMode");
    private final Writeable.Property<String> inputGroup = this.property("inputGroup");
    private final Writeable.Property<String> outputGroup = this.property("outputGroup");
    private final Writeable.Property<TimedActionKey> actionAnim = this.property("actionAnim");

    public ComponentDryingCraftLogicBuilder() {
        super("DryingCraftLogic");
    }

    public ComponentDryingCraftLogicBuilder recipes(CraftRecipeTag ... recipes) {
        this.recipes.addValues((CraftRecipeTag[])recipes);
        return this;
    }

    public ComponentDryingCraftLogicBuilder startMode(StartMode startMode) {
        this.startMode.setValue(startMode);
        return this;
    }

    public ComponentDryingCraftLogicBuilder inputGroup(String inputGroup) {
        this.inputGroup.setValue(inputGroup);
        return this;
    }

    public ComponentDryingCraftLogicBuilder outputGroup(String outputGroup) {
        this.outputGroup.setValue(outputGroup);
        return this;
    }

    public ComponentDryingCraftLogicBuilder actionAnim(TimedActionKey actionKey) {
        this.actionAnim.setValue(actionKey);
        return this;
    }
}

