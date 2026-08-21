/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.BlendWhiteListBuilder;
import generation.builders.FluidPoisonBuilder;
import generation.builders.FluidPropertiesBuilder;
import generation.builders.Writeable;
import zombie.core.Color;
import zombie.core.Colors;
import zombie.entity.components.fluids.FluidCategory;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.FluidKey;

public class FluidBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.ListProperty<BlendWhiteListBuilder> blendWhiteList = this.listProperty("BlendWhiteList", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<FluidCategory> categories = this.listProperty("Categories", Writeable.ListProperty.Flags.HIDE_KEY);
    private final Writeable.ListProperty<FluidPoisonBuilder> poison = this.listProperty("Poison", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<FluidPropertiesBuilder> properties = this.listProperty("Properties", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<String> colorReference = this.property("ColorReference");
    private final Writeable.Property<String> displayName = this.property("DisplayName");

    public static FluidBuilder withId(FluidKey id) {
        return new FluidBuilder(id.toString());
    }

    private FluidBuilder(String name) {
        super(ScriptType.FluidDefinition, name);
    }

    public FluidBuilder addBlendWhiteList(BlendWhiteListBuilder blendWhiteList) {
        this.blendWhiteList.addValues((BlendWhiteListBuilder[])new BlendWhiteListBuilder[]{blendWhiteList});
        return this;
    }

    public FluidBuilder addCategories(FluidCategory ... categories) {
        this.categories.addValues((FluidCategory[])categories);
        return this;
    }

    public FluidBuilder addPoison(FluidPoisonBuilder poison) {
        this.poison.addValues((FluidPoisonBuilder[])new FluidPoisonBuilder[]{poison});
        return this;
    }

    public FluidBuilder addProperties(FluidPropertiesBuilder properties) {
        this.properties.addValues((FluidPropertiesBuilder[])new FluidPropertiesBuilder[]{properties});
        return this;
    }

    public FluidBuilder colorReference(Color colorReference) {
        String name = Colors.getNameFromColor(colorReference);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Invalid color reference: " + String.valueOf(colorReference));
        }
        this.colorReference.setValue(name);
        return this;
    }

    public FluidBuilder displayName(String displayName) {
        this.displayName.setValue(displayName);
        return this;
    }
}

