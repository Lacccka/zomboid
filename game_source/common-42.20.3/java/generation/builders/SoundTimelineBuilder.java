/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;

public class SoundTimelineBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<Integer> electricityOn = this.property("ElectricityOn");
    private final Writeable.Property<Integer> idle = this.property("idle");

    public static SoundTimelineBuilder withId(String id) {
        return new SoundTimelineBuilder(id);
    }

    public SoundTimelineBuilder(String name) {
        super(ScriptType.SoundTimeline, name);
    }

    public SoundTimelineBuilder electricityOn(int electricityOn) {
        this.electricityOn.setValue(electricityOn);
        return this;
    }

    public SoundTimelineBuilder idle(int idle) {
        this.idle.setValue(idle);
        return this;
    }
}

