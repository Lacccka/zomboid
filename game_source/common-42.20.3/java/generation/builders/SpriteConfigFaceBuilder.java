/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.SpriteConfigFaceLayerBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.FaceType;

public class SpriteConfigFaceBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Integer> lightOffsetX = this.property("lightOffsetX");
    private final Writeable.Property<Integer> lightOffsetY = this.property("lightOffsetY");
    private final Writeable.Property<Integer> lightOffsetZ = this.property("lightOffsetZ");
    private final Writeable.ListProperty<SpriteConfigFaceLayerBuilder> layers = this.listProperty("layer", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public SpriteConfigFaceBuilder(FaceType face) {
        super(face.toString());
    }

    public SpriteConfigFaceBuilder addLayer(SpriteConfigFaceLayerBuilder row) {
        this.layers.addValues((SpriteConfigFaceLayerBuilder[])new SpriteConfigFaceLayerBuilder[]{row});
        return this;
    }

    public SpriteConfigFaceBuilder lightOffsetX(int lightOffsetX) {
        this.lightOffsetX.setValue(lightOffsetX);
        return this;
    }

    public SpriteConfigFaceBuilder lightOffsetY(int lightOffsetY) {
        this.lightOffsetY.setValue(lightOffsetY);
        return this;
    }

    public SpriteConfigFaceBuilder lightOffsetZ(int lightOffsetZ) {
        this.lightOffsetZ.setValue(lightOffsetZ);
        return this;
    }
}

