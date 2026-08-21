/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.SoundKey;

public class VehicleSwitchSeatBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<String> anim = this.property("anim");
    private final Writeable.Property<Float> rate = this.property("rate");
    private final Writeable.Property<SoundKey> sound = this.property("sound");

    public VehicleSwitchSeatBuilder(String name) {
        super(name);
    }

    public VehicleSwitchSeatBuilder sound(SoundKey sound) {
        this.sound.setValue(sound);
        return this;
    }

    public VehicleSwitchSeatBuilder anim(String anim) {
        this.anim.setValue(anim);
        return this;
    }

    public VehicleSwitchSeatBuilder rate(float rate) {
        this.rate.setValue(Float.valueOf(rate));
        return this;
    }
}

