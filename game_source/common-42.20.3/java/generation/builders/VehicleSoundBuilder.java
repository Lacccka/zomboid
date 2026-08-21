/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import java.util.Arrays;
import java.util.stream.Collectors;
import zombie.scripting.objects.SoundKey;

public class VehicleSoundBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<String> alarmLoop = this.property("alarmLoop");
    private final Writeable.Property<SoundKey> backSignal = this.property("backSignal");
    private final Writeable.Property<SoundKey> engine = this.property("engine");
    private final Writeable.Property<SoundKey> engineStart = this.property("engineStart");
    private final Writeable.Property<SoundKey> engineTurnOff = this.property("engineTurnOff");
    private final Writeable.Property<SoundKey> handBrake = this.property("handBrake");
    private final Writeable.Property<SoundKey> horn = this.property("horn");
    private final Writeable.Property<SoundKey> ignitionFail = this.property("ignitionFail");

    public VehicleSoundBuilder backSignal(SoundKey backSignal) {
        this.backSignal.setValue(backSignal);
        return this;
    }

    public VehicleSoundBuilder alarmLoop(SoundKey ... alarmLoop) {
        this.alarmLoop.setValue(Arrays.stream(alarmLoop).map(SoundKey::toString).collect(Collectors.joining(" ")));
        return this;
    }

    public VehicleSoundBuilder engine(SoundKey engine) {
        this.engine.setValue(engine);
        return this;
    }

    public VehicleSoundBuilder engineStart(SoundKey engineStart) {
        this.engineStart.setValue(engineStart);
        return this;
    }

    public VehicleSoundBuilder engineTurnOff(SoundKey engineTurnOff) {
        this.engineTurnOff.setValue(engineTurnOff);
        return this;
    }

    public VehicleSoundBuilder handBrake(SoundKey horn) {
        this.handBrake.setValue(horn);
        return this;
    }

    public VehicleSoundBuilder horn(SoundKey horn) {
        this.horn.setValue(horn);
        return this;
    }

    public VehicleSoundBuilder ignitionFail(SoundKey ignitionFail) {
        this.ignitionFail.setValue(ignitionFail);
        return this;
    }
}

