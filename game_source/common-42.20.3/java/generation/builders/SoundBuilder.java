/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.SoundClipBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.SoundCategory;
import zombie.scripting.objects.SoundKey;
import zombie.scripting.objects.SoundMasters;

public class SoundBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.ListProperty<SoundClipBuilder> clip = this.listProperty("clip", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<SoundCategory> category = this.property("category");
    private final Writeable.Property<Boolean> loop = this.property("loop");
    private final Writeable.Property<SoundMasters> master = this.property("master");
    private final Writeable.Property<Integer> maxInstancesPerEmitter = this.property("maxInstancesPerEmitter");
    private final Writeable.Property<Float> pitch = this.property("pitch");

    public static SoundBuilder withId(SoundKey id) {
        return new SoundBuilder(id.toString());
    }

    private SoundBuilder(String name) {
        super(ScriptType.Sound, name);
    }

    public SoundBuilder addClip(SoundClipBuilder clip) {
        this.clip.addValues((SoundClipBuilder[])new SoundClipBuilder[]{clip});
        return this;
    }

    public SoundBuilder category(SoundCategory category) {
        this.category.setValue(category);
        return this;
    }

    public SoundBuilder loop(boolean loop) {
        this.loop.setValue(loop);
        return this;
    }

    public SoundBuilder master(SoundMasters master) {
        this.master.setValue(master);
        return this;
    }

    public SoundBuilder maxInstancesPerEmitter(int maxInstancesPerEmitter) {
        this.maxInstancesPerEmitter.setValue(maxInstancesPerEmitter);
        return this;
    }

    public SoundBuilder pitch(float pitch) {
        this.pitch.setValue(Float.valueOf(pitch));
        return this;
    }
}

