/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.EvolvedRecipeKey;
import zombie.scripting.objects.EvolvedRecipeTemplateKey;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.SoundKey;

public class EvolvedrecipeBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<Boolean> addIngredientIfCooked = this.property("AddIngredientIfCooked");
    private final Writeable.Property<SoundKey> addIngredientSound = this.property("AddIngredientSound");
    private final Writeable.Property<Boolean> allowFrozenItem = this.property("AllowFrozenItem");
    private final Writeable.Property<ItemKey> baseItem = this.property("BaseItem");
    private final Writeable.Property<Boolean> canAddSpicesEmpty = this.property("CanAddSpicesEmpty");
    private final Writeable.Property<Boolean> cookable = this.property("Cookable");
    private final Writeable.Property<Boolean> isHidden = this.property("IsHidden");
    private final Writeable.Property<Integer> maxItems = this.property("MaxItems");
    private final Writeable.Property<Float> minimumWater = this.property("MinimumWater");
    private final Writeable.Property<String> name = this.property("Name");
    private final Writeable.Property<ItemKey> resultItem = this.property("ResultItem");
    private final Writeable.Property<EvolvedRecipeTemplateKey> template = this.property("Template");

    public static EvolvedrecipeBuilder withId(EvolvedRecipeKey id) {
        return new EvolvedrecipeBuilder(id.toString());
    }

    public EvolvedrecipeBuilder(String name) {
        super(ScriptType.EvolvedRecipe, name);
    }

    public EvolvedrecipeBuilder addIngredientIfCooked(boolean addIngredientIfCooked) {
        this.addIngredientIfCooked.setValue(addIngredientIfCooked);
        return this;
    }

    public EvolvedrecipeBuilder addIngredientSound(SoundKey addIngredientSound) {
        this.addIngredientSound.setValue(addIngredientSound);
        return this;
    }

    public EvolvedrecipeBuilder allowFrozenItem(boolean allowFrozenItem) {
        this.allowFrozenItem.setValue(allowFrozenItem);
        return this;
    }

    public EvolvedrecipeBuilder baseItem(ItemKey baseItem) {
        this.baseItem.setValue(baseItem);
        return this;
    }

    public EvolvedrecipeBuilder canAddSpicesEmpty(boolean canAddSpicesEmpty) {
        this.canAddSpicesEmpty.setValue(canAddSpicesEmpty);
        return this;
    }

    public EvolvedrecipeBuilder cookable(boolean cookable) {
        this.cookable.setValue(cookable);
        return this;
    }

    public EvolvedrecipeBuilder isHidden(boolean isHidden) {
        this.isHidden.setValue(isHidden);
        return this;
    }

    public EvolvedrecipeBuilder maxItems(int maxItems) {
        this.maxItems.setValue(maxItems);
        return this;
    }

    public EvolvedrecipeBuilder minimumWater(float minimumWater) {
        this.minimumWater.setValue(Float.valueOf(minimumWater));
        return this;
    }

    public EvolvedrecipeBuilder name(String name) {
        this.name.setValue(name);
        return this;
    }

    public EvolvedrecipeBuilder resultItem(ItemKey resultItem) {
        this.resultItem.setValue(resultItem);
        return this;
    }

    public EvolvedrecipeBuilder template(EvolvedRecipeTemplateKey template) {
        this.template.setValue(template);
        return this;
    }
}

