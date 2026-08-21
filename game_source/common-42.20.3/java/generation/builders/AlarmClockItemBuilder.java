/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.SoundKey;

public class AlarmClockItemBuilder
extends ItemBuilder<AlarmClockItemBuilder> {
    private final Writeable.Property<SoundKey> alarmSound = this.property("AlarmSound");
    private final Writeable.Property<Integer> soundRadius = this.property("SoundRadius");

    public AlarmClockItemBuilder(ItemKey item) {
        super(item);
    }

    @Override
    public AlarmClockItemBuilder alarmSound(SoundKey alarmSound) {
        this.alarmSound.setValue(alarmSound);
        return this;
    }

    @Override
    public AlarmClockItemBuilder soundRadius(int soundRadius) {
        this.soundRadius.setValue(soundRadius);
        return this;
    }
}

