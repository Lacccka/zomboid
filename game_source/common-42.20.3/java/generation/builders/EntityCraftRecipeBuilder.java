/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.CraftRecipeBuilder;
import generation.builders.ItemMapper;
import generation.builders.PerkNumber;
import generation.builders.RecipeElement;
import generation.builders.RecipeMapperBuilder;
import generation.builders.Writeable;
import java.io.IOException;
import java.io.Writer;
import zombie.characters.skills.PerkFactory;
import zombie.scripting.objects.CraftRecipeTag;
import zombie.scripting.objects.EntityCategory;
import zombie.scripting.objects.MetaRecipe;
import zombie.scripting.objects.TimedActionKey;

public class EntityCraftRecipeBuilder
extends AbstractDynamicOrderPropertyBuilder
implements ComponentBuilder {
    protected final Writeable.Property<String> onAddToMenu = this.property("OnAddToMenu");
    protected final Writeable.ListProperty<EntityCategory> category = this.listProperty("category", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.Property<Boolean> allowBatchCraft = this.property("AllowBatchCraft");
    protected final Writeable.ListProperty<PerkNumber> autoLearnAll = this.listProperty("AutoLearnAll", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.ListProperty<PerkNumber> autoLearnAny = this.listProperty("AutoLearnAny", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.Property<String> icon = this.property("Icon");
    protected final Writeable.Property<MetaRecipe> metaRecipe = this.property("MetaRecipe");
    protected final Writeable.Property<Boolean> needToBeLearn = this.property("NeedToBeLearn");
    protected final Writeable.Property<String> onCreate = this.property("OnCreate");
    protected final Writeable.Property<String> onTest = this.property("OnTest");
    protected final Writeable.ListProperty<PerkFactory.Perk> researchAny = this.listProperty("ResearchAny", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.Property<Integer> researchSkillLevel = this.property("ResearchSkillLevel");
    protected final Writeable.ListProperty<PerkNumber> skillRequired = this.listProperty("SkillRequired", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.ListProperty<CraftRecipeTag> tags = this.listProperty("Tags", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.Property<String> tooltip = this.property("Tooltip");
    protected final Writeable.Property<Boolean> needTobeLearn = this.property("needTobeLearn");
    protected final Writeable.Property<Integer> time = this.property("time");
    protected final Writeable.Property<TimedActionKey> timedAction = this.property("timedAction");
    protected final Writeable.ListProperty<PerkNumber> xpAward = this.listProperty("xpAward", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.ListProperty<ItemMapper> itemMappers = this.listProperty("itemMapper", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    protected final Writeable.ListProperty<RecipeElement> inputs = this.listProperty("inputs", Writeable.ListProperty.Flags.SHOW_IF_EMPTY);
    protected final Writeable.ListProperty<RecipeElement> outputs = this.listProperty("outputs", Writeable.ListProperty.Flags.SHOW_IF_EMPTY);

    public EntityCraftRecipeBuilder() {
        super("CraftRecipe");
    }

    @Override
    public String getType() {
        return this.getName();
    }

    public EntityCraftRecipeBuilder onAddToMenu(String onAddToMenu) {
        this.onAddToMenu.setValue(onAddToMenu);
        return this;
    }

    public EntityCraftRecipeBuilder category(EntityCategory ... category) {
        this.category.addValues((EntityCategory[])category);
        return this;
    }

    public EntityCraftRecipeBuilder allowBatchCraft(boolean allowBatchCraft) {
        this.allowBatchCraft.setValue(allowBatchCraft);
        return this;
    }

    public EntityCraftRecipeBuilder autoLearnAll(PerkNumber ... autoLearnAll) {
        this.autoLearnAll.addValues((PerkNumber[])autoLearnAll);
        return this;
    }

    public EntityCraftRecipeBuilder autoLearnAny(PerkNumber ... autoLearnAny) {
        this.autoLearnAny.addValues((PerkNumber[])autoLearnAny);
        return this;
    }

    public EntityCraftRecipeBuilder icon(String icon) {
        this.icon.setValue(icon);
        return this;
    }

    public EntityCraftRecipeBuilder metaRecipe(MetaRecipe metaRecipe) {
        this.metaRecipe.setValue(metaRecipe);
        return this;
    }

    public EntityCraftRecipeBuilder needToBeLearn(boolean needToBeLearn) {
        this.needToBeLearn.setValue(needToBeLearn);
        return this;
    }

    public EntityCraftRecipeBuilder onCreate(String onCreate) {
        this.onCreate.setValue(onCreate);
        return this;
    }

    public EntityCraftRecipeBuilder onTest(String onTest) {
        this.onTest.setValue(onTest);
        return this;
    }

    public EntityCraftRecipeBuilder researchAny(PerkFactory.Perk ... researchAny) {
        this.researchAny.addValues((PerkFactory.Perk[])researchAny);
        return this;
    }

    public EntityCraftRecipeBuilder researchSkillLevel(int researchSkillLevel) {
        this.researchSkillLevel.setValue(researchSkillLevel);
        return this;
    }

    public EntityCraftRecipeBuilder skillRequired(PerkNumber ... skillRequired) {
        this.skillRequired.addValues((PerkNumber[])skillRequired);
        return this;
    }

    public EntityCraftRecipeBuilder tags(CraftRecipeTag ... tags) {
        this.tags.addValues((CraftRecipeTag[])tags);
        return this;
    }

    public EntityCraftRecipeBuilder tooltip(String tooltip) {
        this.tooltip.setValue(tooltip);
        return this;
    }

    public EntityCraftRecipeBuilder needTobeLearn(boolean needTobeLearn) {
        this.needTobeLearn.setValue(needTobeLearn);
        return this;
    }

    public EntityCraftRecipeBuilder time(int time) {
        this.time.setValue(time);
        return this;
    }

    public EntityCraftRecipeBuilder timedAction(TimedActionKey timedAction) {
        this.timedAction.setValue(timedAction);
        return this;
    }

    public EntityCraftRecipeBuilder xpAward(PerkNumber ... xpAward) {
        this.xpAward.addValues((PerkNumber[])xpAward);
        return this;
    }

    public EntityCraftRecipeBuilder itemMappers(String name, RecipeMapperBuilder ... recipeMappers) {
        this.itemMappers.addValues((ItemMapper[])new ItemMapper[]{new ItemMapper(name, recipeMappers)});
        return this;
    }

    public EntityCraftRecipeBuilder inputs(RecipeElement ... inputs) {
        this.inputs.addValues((RecipeElement[])inputs);
        return this;
    }

    public EntityCraftRecipeBuilder outputs(RecipeElement ... outputs) {
        this.outputs.addValues((RecipeElement[])outputs);
        return this;
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        CraftRecipeBuilder.validateItemMappers(this.getName(), this.itemMappers, this.inputs, this.outputs);
        super.write(writer, indent, key);
    }
}

