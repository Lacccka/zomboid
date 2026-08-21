/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;

public class EnergyBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.ListProperty<Float> color = this.listProperty("Color", ":", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<String> displayName = this.property("DisplayName");
    private final Writeable.Property<String> horizontalBarTexture = this.property("horizontalBarTexture");
    private final Writeable.Property<String> iconTexture = this.property("iconTexture");
    private final Writeable.Property<String> verticalBarTexture = this.property("verticalBarTexture");

    public static EnergyBuilder withId(String id) {
        return new EnergyBuilder(id);
    }

    public EnergyBuilder(String name) {
        super("energy", name);
    }

    public EnergyBuilder color(float r, float g, float b) {
        this.color.addValues((Float[])new Float[]{Float.valueOf(r), Float.valueOf(g), Float.valueOf(b)});
        return this;
    }

    public EnergyBuilder displayName(String displayName) {
        this.displayName.setValue(displayName);
        return this;
    }

    public EnergyBuilder horizontalBarTexture(String horizontalBarTexture) {
        this.horizontalBarTexture.setValue(horizontalBarTexture);
        return this;
    }

    public EnergyBuilder iconTexture(String iconTexture) {
        this.iconTexture.setValue(iconTexture);
        return this;
    }

    public EnergyBuilder verticalBarTexture(String verticalBarTexture) {
        this.verticalBarTexture.setValue(verticalBarTexture);
        return this;
    }
}

