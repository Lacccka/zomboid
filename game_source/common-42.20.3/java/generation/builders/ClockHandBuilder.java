/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import generation.builders.validation.PackTextureValidator;

public class ClockHandBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Float> length = this.property("length");
    private final Writeable.Property<Float> thickness = this.property("thickness");
    private final Writeable.Property<PackTextureValidator> texture = this.property("texture");
    private final Writeable.ListProperty<Integer> textureInfo = this.listProperty("textureInfo", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> rgba = this.listProperty("rgba", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);

    public ClockHandBuilder(String name) {
        super(name);
    }

    public ClockHandBuilder length(float length) {
        this.length.setValue(Float.valueOf(length));
        return this;
    }

    public ClockHandBuilder thickness(float thickness) {
        this.thickness.setValue(Float.valueOf(thickness));
        return this;
    }

    public ClockHandBuilder texture(String texture) {
        this.texture.setValue(PackTextureValidator.of(texture, ""));
        return this;
    }

    public ClockHandBuilder textureInfo(int width, int height, int axisX, int axisY) {
        this.textureInfo.addValues((Integer[])new Integer[]{width, height, axisX, axisY});
        return this;
    }

    public ClockHandBuilder rgba(float r, float g, float b, float a) {
        this.rgba.addValues((Float[])new Float[]{Float.valueOf(r), Float.valueOf(g), Float.valueOf(b), Float.valueOf(a)});
        return this;
    }
}

