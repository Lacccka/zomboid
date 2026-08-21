/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class SoundClipBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<String> event = this.property("event");
    private final Writeable.Property<Float> volume = this.property("volume");
    private final Writeable.Property<Float> pitch = this.property("pitch");
    private final Writeable.Property<String> file = this.property("file");
    private final Writeable.Property<Integer> distanceMin = this.property("distanceMin");
    private final Writeable.Property<Integer> distanceMax = this.property("distanceMax");
    private final Writeable.Property<Float> reverbFactor = this.property("reverbFactor");
    private final Writeable.Property<Float> reverbMaxRange = this.property("reverbMaxRange");
    private final Writeable.Property<Boolean> stopImmediate = this.property("stopImmediate");

    public SoundClipBuilder event(String event) {
        this.event.setValue(event);
        return this;
    }

    public SoundClipBuilder volume(float volume) {
        this.volume.setValue(Float.valueOf(volume));
        return this;
    }

    public SoundClipBuilder pitch(float pitch) {
        this.pitch.setValue(Float.valueOf(pitch));
        return this;
    }

    public SoundClipBuilder file(String file) {
        this.file.setValue(file);
        return this;
    }

    public SoundClipBuilder distanceMin(int distanceMin) {
        this.distanceMin.setValue(distanceMin);
        return this;
    }

    public SoundClipBuilder distanceMax(int distanceMax) {
        this.distanceMax.setValue(distanceMax);
        return this;
    }

    public SoundClipBuilder reverbFactor(float reverbFactor) {
        this.reverbFactor.setValue(Float.valueOf(reverbFactor));
        return this;
    }

    public SoundClipBuilder reverbMaxRange(float reverbMaxRange) {
        this.reverbMaxRange.setValue(Float.valueOf(reverbMaxRange));
        return this;
    }

    public SoundClipBuilder stopImmediate(boolean b) {
        this.stopImmediate.setValue(b);
        return this;
    }
}

