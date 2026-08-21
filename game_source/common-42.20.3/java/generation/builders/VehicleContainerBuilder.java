/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.SoundKey;
import zombie.scripting.objects.SoundMapKey;

public class VehicleContainerBuilder
extends AbstractDynamicOrderPropertyBuilder {
    private final Writeable.Property<Integer> capacity = this.property("capacity");
    private final Writeable.Property<Boolean> conditionAffectsCapacity = this.property("conditionAffectsCapacity");
    private final Writeable.Property<String> test = this.property("test");
    private final Writeable.Property<String> contentType = this.property("contentType");
    private final Writeable.Property<String> seat = this.property("seat");
    private final Writeable.ListProperty<String> soundMap = this.listProperty("soundMap", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public VehicleContainerBuilder seat(String seat) {
        this.seat.setValue(seat);
        return this;
    }

    public VehicleContainerBuilder capacity(int capacity) {
        this.capacity.setValue(capacity);
        return this;
    }

    public VehicleContainerBuilder test(String test) {
        this.test.setValue(test);
        return this;
    }

    public VehicleContainerBuilder contentType(String contentType) {
        this.contentType.setValue(contentType);
        return this;
    }

    public VehicleContainerBuilder conditionAffectsCapacity(boolean conditionAffectsCapacity) {
        this.conditionAffectsCapacity.setValue(conditionAffectsCapacity);
        return this;
    }

    public VehicleContainerBuilder soundMap(SoundMapKey key, SoundKey value) {
        this.soundMap.addValues((String[])new String[]{key.toString() + " " + value.toString()});
        return this;
    }
}

