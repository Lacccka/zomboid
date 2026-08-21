/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.SoundKey;

public class VehicleLightbarBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<String> texture = this.property("texture");
    private final Writeable.Property<SoundKey> soundSiren = this.property("soundSiren");
    private final Writeable.ListProperty<Float> leftCol = this.listProperty("leftCol", ";", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> rightCol = this.listProperty("rightCol", ";", Writeable.ListProperty.Flags.KEEP_DUPLICATES);

    public VehicleLightbarBuilder texture(String texture) {
        this.texture.setValue(texture);
        return this;
    }

    public VehicleLightbarBuilder soundSiren(SoundKey soundSiren) {
        this.soundSiren.setValue(soundSiren);
        return this;
    }

    public VehicleLightbarBuilder leftCol(float r, float g, float b) {
        this.leftCol.addValues((Float[])new Float[]{Float.valueOf(r), Float.valueOf(g), Float.valueOf(b)});
        return this;
    }

    public VehicleLightbarBuilder rightCol(float r, float g, float b) {
        this.rightCol.addValues((Float[])new Float[]{Float.valueOf(r), Float.valueOf(g), Float.valueOf(b)});
        return this;
    }
}

