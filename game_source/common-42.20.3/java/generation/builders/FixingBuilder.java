/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.PerkNumber;
import generation.builders.Writeable;
import java.util.Arrays;
import java.util.stream.Collectors;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.ItemKey;

public class FixingBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<Float> conditionModifier = this.property("ConditionModifier");
    private final Writeable.ListProperty<FixingMaterialRequirements> fixer = this.listProperty("Fixer", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<ItemCount> globalItem = this.property("GlobalItem");
    private final Writeable.ListProperty<ItemKey> require = this.listProperty("Require", ";", new Writeable.ListProperty.Flags[0]);

    public static FixingBuilder withId(String id) {
        return new FixingBuilder(id);
    }

    public FixingBuilder(String name) {
        super(ScriptType.Fixing, name);
    }

    public FixingBuilder conditionModifier(float conditionModifier) {
        this.conditionModifier.setValue(Float.valueOf(conditionModifier));
        return this;
    }

    public FixingBuilder fixer(ItemKey value, PerkNumber ... perks) {
        return this.fixer(value, 1, perks);
    }

    public FixingBuilder fixer(ItemKey value, int count, PerkNumber ... requirements) {
        this.fixer.addValues((FixingMaterialRequirements[])new FixingMaterialRequirements[]{new FixingMaterialRequirements(new ItemCount(value, count), requirements)});
        return this;
    }

    public FixingBuilder globalItem(ItemKey globalItem, int count) {
        this.globalItem.setValue(new ItemCount(globalItem, count));
        return this;
    }

    public FixingBuilder require(ItemKey ... require) {
        this.require.addValues((ItemKey[])require);
        return this;
    }

    private record FixingMaterialRequirements(ItemCount itemCount, PerkNumber[] requirements) {
        @Override
        public String toString() {
            int count = this.itemCount.count();
            return "%s%s; %s".formatted(this.itemCount.item(), count > 1 ? "=%s".formatted(count) : "", Arrays.stream(this.requirements).map(pn -> "%s=%s".formatted(pn.perk(), pn.number())).collect(Collectors.joining(";")));
        }
    }

    private record ItemCount(ItemKey item, int count) {
        @Override
        public String toString() {
            return "%s=%d".formatted(this.item, this.count);
        }
    }
}

