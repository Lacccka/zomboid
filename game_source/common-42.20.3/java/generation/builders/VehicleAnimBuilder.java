/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.SoundKey;

public class VehicleAnimBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<SoundKey> sound = this.property("sound");
    private final Writeable.Property<String> anim = this.property("anim");
    private final Writeable.Property<Boolean> reverse = this.property("reverse");
    private final Writeable.Property<Boolean> animate = this.property("animate");
    private final Writeable.Property<Float> rate = this.property("rate");
    private final Writeable.ListProperty<Float> angle = this.listProperty("angle", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);

    public VehicleAnimBuilder(String name) {
        super(name);
    }

    public VehicleAnimBuilder sound(SoundKey sound) {
        this.sound.setValue(sound);
        return this;
    }

    public VehicleAnimBuilder anim(String anim) {
        this.anim.setValue(anim);
        return this;
    }

    public VehicleAnimBuilder animate(boolean animate) {
        this.animate.setValue(animate);
        return this;
    }

    public VehicleAnimBuilder reverse(boolean reverse) {
        this.reverse.setValue(reverse);
        return this;
    }

    public VehicleAnimBuilder rate(float rate) {
        this.rate.setValue(Float.valueOf(rate));
        return this;
    }

    public VehicleAnimBuilder angle(float x, float y, float z) {
        this.angle.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }
}

