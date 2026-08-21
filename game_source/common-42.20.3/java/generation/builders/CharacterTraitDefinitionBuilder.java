/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.CharacterProfessionDefinitionBuilder;
import generation.builders.Writeable;
import zombie.characters.skills.PerkFactory;
import zombie.core.textures.Texture;
import zombie.debug.DebugType;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.CharacterTrait;

public class CharacterTraitDefinitionBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<CharacterTrait> characterTrait = this.property("CharacterTrait");
    private final Writeable.Property<String> uiName = this.property("UIName");
    private final Writeable.Property<Integer> cost = this.property("Cost");
    private final Writeable.Property<String> uiDescription = this.property("UIDescription");
    private final Writeable.Property<Boolean> profession = this.property("IsProfessionTrait", false);
    private final Writeable.Property<Boolean> disabledInMultiplayer = this.property("DisabledInMultiplayer", false);
    private final Writeable.ListProperty<CharacterTrait> grantedTraits = this.listProperty("GrantedTraits", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.ListProperty<String> grantedRecipes = this.listProperty("GrantedRecipes", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.ListProperty<XPBoost> xpBoosts = this.listProperty("XPBoosts", ";", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<CharacterTrait> mutuallyExclusiveTraits = this.listProperty("MutuallyExclusiveTraits", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Texture> texture = this.property("Texture");

    public CharacterTraitDefinitionBuilder(CharacterTrait characterTrait) {
        super(ScriptType.CharacterTraitDefinition, characterTrait.toString());
        this.characterTrait.setValue(characterTrait);
    }

    public static CharacterTraitDefinitionBuilder withId(CharacterTrait characterTrait) {
        return new CharacterTraitDefinitionBuilder(characterTrait);
    }

    public CharacterTraitDefinitionBuilder characterTrait(CharacterTrait characterTrait) {
        this.characterTrait.setValue(characterTrait);
        return this;
    }

    public CharacterTraitDefinitionBuilder uiName(String name) {
        this.uiName.setValue(name);
        return this;
    }

    public CharacterTraitDefinitionBuilder cost(int cost) {
        this.cost.setValue(cost);
        return this;
    }

    public CharacterTraitDefinitionBuilder uiDescription(String description) {
        this.uiDescription.setValue(description);
        return this;
    }

    public CharacterTraitDefinitionBuilder profession(boolean profession) {
        this.profession.setValue(profession);
        return this;
    }

    public CharacterTraitDefinitionBuilder disabledInMultiplayer(boolean disabledInMultiplayer) {
        this.disabledInMultiplayer.setValue(disabledInMultiplayer);
        return this;
    }

    public CharacterTraitDefinitionBuilder addXPBoost(PerkFactory.Perk perk, int level) {
        if (perk == null || perk == PerkFactory.Perks.None || perk == PerkFactory.Perks.MAX) {
            DebugType.General.warn("Invalid perk passed to CharacterTraitBuilder.addXPBoost trait=%s perk=%s", this.uiName, perk);
            return this;
        }
        this.xpBoosts.addValues((XPBoost[])new XPBoost[]{new XPBoost(perk, level)});
        return this;
    }

    public CharacterTraitDefinitionBuilder addGrantedTrait(CharacterTrait characterTrait) {
        this.grantedTraits.addValues((CharacterTrait[])new CharacterTrait[]{characterTrait});
        return this;
    }

    public CharacterTraitDefinitionBuilder addGrantedRecipes(Object ... objects) {
        CharacterProfessionDefinitionBuilder.grantedRecipeHelper(this.grantedRecipes, objects);
        return this;
    }

    public CharacterTraitDefinitionBuilder addMutuallyExclusiveTrait(CharacterTrait other) {
        this.mutuallyExclusiveTraits.addValues((CharacterTrait[])new CharacterTrait[]{other});
        return this;
    }

    public CharacterTraitDefinitionBuilder texture(Texture texture) {
        this.texture.setValue(texture);
        return this;
    }

    public record XPBoost(PerkFactory.Perk perk, int boost) {
        @Override
        public String toString() {
            return "%s=%d".formatted(this.perk, this.boost);
        }
    }
}

