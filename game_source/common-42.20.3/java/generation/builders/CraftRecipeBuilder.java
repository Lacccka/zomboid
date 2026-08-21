/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import com.google.common.collect.Sets;
import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.ItemMapper;
import generation.builders.PerkNumber;
import generation.builders.RecipeElement;
import generation.builders.RecipeItemBuilder;
import generation.builders.RecipeMapperBuilder;
import generation.builders.RecipeOverlayMapperBuilder;
import generation.builders.Writeable;
import generation.builders.validation.SerializableMethod;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import zombie.characters.IsoGameCharacter;
import zombie.characters.skills.PerkFactory;
import zombie.entity.components.crafting.recipe.CraftRecipeData;
import zombie.inventory.InventoryItem;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.CraftRecipeCategory;
import zombie.scripting.objects.CraftRecipeGroup;
import zombie.scripting.objects.CraftRecipeKey;
import zombie.scripting.objects.CraftRecipeTag;
import zombie.scripting.objects.MetaRecipe;
import zombie.scripting.objects.TimedActionKey;

public class CraftRecipeBuilder
extends AbstractScriptTypeBuilder {
    protected final Writeable.ListProperty<CraftRecipeCategory> category = this.listProperty("category", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.Property<CraftRecipeGroup> group = this.property("recipeGroup");
    protected final Writeable.Property<String> overlayStyle = this.property("overlayStyle");
    protected final Writeable.ListProperty<RecipeOverlayMapperBuilder> overlayMapper = this.listProperty("overlayMapper", new Writeable.ListProperty.Flags[0]);
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
    protected final Writeable.Property<Integer> time = this.property("time");
    protected final Writeable.Property<TimedActionKey> timedAction = this.property("timedAction");
    protected final Writeable.ListProperty<PerkNumber> xpAward = this.listProperty("xpAward", ";", new Writeable.ListProperty.Flags[0]);
    protected final Writeable.ListProperty<ItemMapper> itemMappers = this.listProperty("itemMapper", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    protected final Writeable.ListProperty<RecipeElement> inputs = this.listProperty("inputs", Writeable.ListProperty.Flags.SHOW_IF_EMPTY);
    protected final Writeable.ListProperty<RecipeElement> outputs = this.listProperty("outputs", Writeable.ListProperty.Flags.SHOW_IF_EMPTY);

    public static CraftRecipeBuilder withId(CraftRecipeKey id) {
        return new CraftRecipeBuilder(id.toString());
    }

    private CraftRecipeBuilder(String name) {
        super(ScriptType.CraftRecipe, name);
    }

    public CraftRecipeBuilder category(CraftRecipeCategory ... category) {
        this.category.addValues((CraftRecipeCategory[])category);
        return this;
    }

    public CraftRecipeBuilder group(CraftRecipeGroup group) {
        this.group.setValue(group);
        return this;
    }

    public CraftRecipeBuilder overlayStyle(String overlayStyle) {
        this.overlayStyle.setValue(overlayStyle);
        return this;
    }

    public CraftRecipeBuilder overlayMapper(RecipeOverlayMapperBuilder ... overlayMappers) {
        this.overlayMapper.addValues((RecipeOverlayMapperBuilder[])overlayMappers);
        return this;
    }

    public CraftRecipeBuilder allowBatchCraft(boolean allowBatchCraft) {
        this.allowBatchCraft.setValue(allowBatchCraft);
        return this;
    }

    public CraftRecipeBuilder autoLearnAll(PerkNumber ... autoLearnAll) {
        this.autoLearnAll.addValues((PerkNumber[])autoLearnAll);
        return this;
    }

    public CraftRecipeBuilder autoLearnAny(PerkNumber ... autoLearnAny) {
        this.autoLearnAny.addValues((PerkNumber[])autoLearnAny);
        return this;
    }

    public CraftRecipeBuilder icon(String icon) {
        this.icon.setValue(icon);
        return this;
    }

    public CraftRecipeBuilder metaRecipe(MetaRecipe metaRecipe) {
        this.metaRecipe.setValue(metaRecipe);
        return this;
    }

    public CraftRecipeBuilder needToBeLearn(boolean needToBeLearn) {
        this.needToBeLearn.setValue(needToBeLearn);
        return this;
    }

    public CraftRecipeBuilder onCreate(SerializableMethod.Consumer2<CraftRecipeData, IsoGameCharacter> onCreate) {
        this.onCreate.setValue(SerializableMethod.asLuaString(onCreate));
        return this;
    }

    public CraftRecipeBuilder onTest(SerializableMethod.Function2<InventoryItem, IsoGameCharacter, Boolean> onTest) {
        this.onTest.setValue(SerializableMethod.asLuaString(onTest));
        return this;
    }

    public CraftRecipeBuilder researchAny(PerkFactory.Perk ... researchAny) {
        this.researchAny.addValues((PerkFactory.Perk[])researchAny);
        return this;
    }

    public CraftRecipeBuilder researchSkillLevel(int researchSkillLevel) {
        this.researchSkillLevel.setValue(researchSkillLevel);
        return this;
    }

    public CraftRecipeBuilder skillRequired(PerkNumber ... skillRequired) {
        this.skillRequired.addValues((PerkNumber[])skillRequired);
        return this;
    }

    public CraftRecipeBuilder tags(CraftRecipeTag ... tags) {
        this.tags.addValues((CraftRecipeTag[])tags);
        return this;
    }

    public CraftRecipeBuilder tooltip(String tooltip) {
        this.tooltip.setValue(tooltip);
        return this;
    }

    public CraftRecipeBuilder time(int time) {
        this.time.setValue(time);
        return this;
    }

    public CraftRecipeBuilder timedAction(TimedActionKey timedAction) {
        this.timedAction.setValue(timedAction);
        return this;
    }

    public CraftRecipeBuilder xpAward(PerkNumber ... xpAward) {
        this.xpAward.addValues((PerkNumber[])xpAward);
        return this;
    }

    public CraftRecipeBuilder itemMappers(String name, RecipeMapperBuilder ... recipeMappers) {
        this.itemMappers.addValues((ItemMapper[])new ItemMapper[]{new ItemMapper(name, recipeMappers)});
        return this;
    }

    public CraftRecipeBuilder inputs(RecipeElement ... inputs) {
        this.inputs.addValues((RecipeElement[])inputs);
        return this;
    }

    public CraftRecipeBuilder outputs(RecipeElement ... outputs) {
        this.outputs.addValues((RecipeElement[])outputs);
        return this;
    }

    @Override
    public void write(Writer writer, int indent, String key) throws IOException {
        CraftRecipeBuilder.validateItemMappers(this.getName(), this.itemMappers, this.inputs, this.outputs);
        boolean hasOverlayInItem = this.inputs.getValue().stream().filter(RecipeItemBuilder.class::isInstance).map(RecipeItemBuilder.class::cast).anyMatch(RecipeItemBuilder::hasOverlayMapper);
        if (this.overlayMapper.shouldWrite() != hasOverlayInItem) {
            if (hasOverlayInItem) {
                throw new IllegalStateException("Missing overlayMapper section in %s".formatted(this.getName()));
            }
            throw new IllegalStateException("Have overlayMapper section but no input using it in %s".formatted(this.getName()));
        }
        super.write(writer, indent, key);
    }

    public static void validateItemMappers(String name, Writeable.ListProperty<ItemMapper> itemMappers, Writeable.ListProperty<RecipeElement> inputs, Writeable.ListProperty<RecipeElement> outputs) {
        Set existingMappers = itemMappers.getValue().stream().map(ItemMapper::name).collect(Collectors.toCollection(HashSet::new));
        HashSet unusedMappers = Sets.newHashSet(existingMappers);
        unusedMappers.removeAll(CraftRecipeBuilder.validateMappersAtKey(name, inputs, existingMappers));
        unusedMappers.removeAll(CraftRecipeBuilder.validateMappersAtKey(name, outputs, existingMappers));
        if (!unusedMappers.isEmpty()) {
            throw new IllegalStateException("Unused mappers found: %s in %s".formatted(unusedMappers, name));
        }
    }

    private static Set<String> validateMappersAtKey(String name, Writeable.ListProperty<RecipeElement> key, Set<String> existingMappers) {
        Set usedMappers = key.getValue().stream().filter(RecipeItemBuilder.class::isInstance).map(RecipeItemBuilder.class::cast).flatMap(recipeItem -> recipeItem.getMappers().stream()).collect(Collectors.toCollection(HashSet::new));
        for (String mapper : usedMappers) {
            if (existingMappers.contains(mapper)) continue;
            throw new IllegalStateException("Missing mapper: %s in %s".formatted(mapper, name));
        }
        return usedMappers;
    }
}

