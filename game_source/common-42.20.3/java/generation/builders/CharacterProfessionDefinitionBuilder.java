/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;
import java.lang.reflect.Modifier;
import java.lang.runtime.SwitchBootstraps;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import zombie.characters.skills.PerkFactory;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.CharacterProfession;
import zombie.scripting.objects.CharacterTrait;
import zombie.scripting.objects.CraftRecipeKey;
import zombie.scripting.objects.EntityKey;
import zombie.scripting.objects.LearnedRecipeConstantKey;
import zombie.scripting.objects.Registries;
import zombie.scripting.objects.Registry;
import zombie.scripting.objects.ResourceLocation;
import zombie.scripting.objects.SeasonRecipe;

public class CharacterProfessionDefinitionBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<CharacterProfession> characterProfession = this.property("CharacterProfession");
    private final Writeable.Property<String> uiName = this.property("UIName");
    private final Writeable.Property<Integer> cost = this.property("Cost");
    private final Writeable.Property<String> uiDescription = this.property("UIDescription");
    private final Writeable.ListProperty<CharacterTrait> grantedTraits = this.listProperty("GrantedTraits", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.ListProperty<String> grantedRecipes = this.listProperty("GrantedRecipes", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.ListProperty<XPBoost> xpBoosts = this.listProperty("XPBoosts", ";", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<String> iconPathName = this.property("IconPathName");

    public CharacterProfessionDefinitionBuilder(CharacterProfession characterProfession) {
        super(ScriptType.CharacterProfessionDefinition, Registries.CHARACTER_PROFESSION.getLocation(characterProfession).toString());
        this.characterProfession.setValue(characterProfession);
    }

    public static CharacterProfessionDefinitionBuilder withId(CharacterProfession characterProfession) {
        return new CharacterProfessionDefinitionBuilder(characterProfession);
    }

    public CharacterProfessionDefinitionBuilder characterProfession(CharacterProfession characterProfession) {
        this.characterProfession.setValue(characterProfession);
        return this;
    }

    public CharacterProfessionDefinitionBuilder uiName(String name) {
        this.uiName.setValue(name);
        return this;
    }

    public CharacterProfessionDefinitionBuilder cost(int cost) {
        this.cost.setValue(cost);
        return this;
    }

    public CharacterProfessionDefinitionBuilder uiDescription(String description) {
        this.uiDescription.setValue(description);
        return this;
    }

    public CharacterProfessionDefinitionBuilder addXPBoost(PerkFactory.Perk perk, int level) {
        if (perk == null || perk == PerkFactory.Perks.None || perk == PerkFactory.Perks.MAX) {
            return this;
        }
        this.xpBoosts.addValues((XPBoost[])new XPBoost[]{new XPBoost(perk, level)});
        return this;
    }

    public CharacterProfessionDefinitionBuilder addGrantedTrait(CharacterTrait characterTrait) {
        this.grantedTraits.addValues((CharacterTrait[])new CharacterTrait[]{characterTrait});
        return this;
    }

    public CharacterProfessionDefinitionBuilder iconPathName(String icon) {
        this.iconPathName.setValue(icon);
        return this;
    }

    public CharacterProfessionDefinitionBuilder addGrantedRecipes(Object ... objects) {
        CharacterProfessionDefinitionBuilder.grantedRecipeHelper(this.grantedRecipes, objects);
        return this;
    }

    public static void grantedRecipeHelper(Writeable.ListProperty<String> property, Object ... objects) {
        block6: for (Object o : objects) {
            Object object;
            Objects.requireNonNull(o);
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{SeasonRecipe.class, CraftRecipeKey.class, EntityKey.class, LearnedRecipeConstantKey.class}, (Object)object, n)) {
                case 0: {
                    SeasonRecipe r = (SeasonRecipe)object;
                    property.addValues((String[])new String[]{r.toString()});
                    continue block6;
                }
                case 1: {
                    CraftRecipeKey c = (CraftRecipeKey)object;
                    property.addValues((String[])new String[]{c.id()});
                    continue block6;
                }
                case 2: {
                    EntityKey e = (EntityKey)object;
                    property.addValues((String[])new String[]{e.id()});
                    continue block6;
                }
                case 3: {
                    LearnedRecipeConstantKey l = (LearnedRecipeConstantKey)object;
                    property.addValues((String[])new String[]{l.id()});
                    continue block6;
                }
                default: {
                    property.addValues((String[])new String[]{o.toString()});
                    CharacterProfessionDefinitionBuilder.maybeRegistry(o, Registries.SEASON_RECIPE, (obj, string) -> {
                        SeasonRecipe sr;
                        return obj instanceof SeasonRecipe && (sr = (SeasonRecipe)obj).toString().equalsIgnoreCase((String)string);
                    });
                    CharacterProfessionDefinitionBuilder.maybeConstant(CraftRecipeKey.class, o.toString(), (obj, string) -> {
                        if (!(obj instanceof CraftRecipeKey)) return false;
                        CraftRecipeKey $b$0 = (CraftRecipeKey)obj;
                        try {
                            String patt1$temp;
                            String id = patt1$temp = $b$0.id();
                            if (!id.equalsIgnoreCase((String)string)) return false;
                            return true;
                        }
                        catch (Throwable throwable) {
                            throw new MatchException(throwable.toString(), throwable);
                        }
                    });
                    CharacterProfessionDefinitionBuilder.maybeConstant(EntityKey.class, o.toString(), (obj, string) -> {
                        if (!(obj instanceof EntityKey)) return false;
                        EntityKey $b$0 = (EntityKey)obj;
                        try {
                            String patt1$temp;
                            String id = patt1$temp = $b$0.id();
                            if (!id.equalsIgnoreCase((String)string)) return false;
                            return true;
                        }
                        catch (Throwable throwable) {
                            throw new MatchException(throwable.toString(), throwable);
                        }
                    });
                    CharacterProfessionDefinitionBuilder.maybeConstant(LearnedRecipeConstantKey.class, o.toString(), (obj, string) -> {
                        if (!(obj instanceof LearnedRecipeConstantKey)) return false;
                        LearnedRecipeConstantKey $b$0 = (LearnedRecipeConstantKey)obj;
                        try {
                            String patt1$temp;
                            String id = patt1$temp = $b$0.id();
                            if (!id.equalsIgnoreCase((String)string)) return false;
                            return true;
                        }
                        catch (Throwable throwable) {
                            throw new MatchException(throwable.toString(), throwable);
                        }
                    });
                    System.err.println("Unknown kind of recipe: " + String.valueOf(o));
                }
            }
        }
    }

    private static void maybeRegistry(Object o, Registry<?> registry, BiPredicate<Object, String> objectStringBiPredicate1) {
        Object registryEntry = registry.get(ResourceLocation.of(o.toString()));
        CharacterProfessionDefinitionBuilder.maybeConstant(SeasonRecipe.class, o.toString(), objectStringBiPredicate1.or((obj, string) -> obj == registryEntry));
    }

    private static void maybeConstant(Class<?> clazz, String string, BiPredicate<Object, String> valueTest) {
        Arrays.stream(clazz.getDeclaredFields()).filter(f -> Modifier.isPublic(f.getModifiers())).filter(f -> Modifier.isStatic(f.getModifiers())).filter(f -> Modifier.isFinal(f.getModifiers())).forEach(field -> {
            try {
                if (field.getName().equalsIgnoreCase(string) || valueTest.test(field.get(null), string)) {
                    System.out.println("%s used as recipe, it might be: %s.%s".formatted(string, clazz.getSimpleName(), field.getName()));
                }
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        });
    }

    public record XPBoost(PerkFactory.Perk perk, int boost) {
        @Override
        public String toString() {
            return "%s=%d".formatted(this.perk, this.boost);
        }
    }
}

