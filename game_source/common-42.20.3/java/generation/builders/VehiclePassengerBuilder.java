/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.VehicleAnimBuilder;
import generation.builders.VehiclePositionBuilder;
import generation.builders.VehicleSwitchSeatBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.VehicleArea;

public class VehiclePassengerBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<String> door = this.property("door");
    private final Writeable.Property<VehicleArea> area = this.property("area");
    private final Writeable.Property<String> door2 = this.property("door2");
    private final Writeable.ListProperty<VehiclePositionBuilder> positions = this.listProperty("position", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleSwitchSeatBuilder> switchseat = this.listProperty("switchSeat", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<Boolean> hasRoof = this.property("hasRoof");
    private final Writeable.ListProperty<VehicleAnimBuilder> anim = this.listProperty("anim", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public VehiclePassengerBuilder(String name) {
        super(name);
    }

    public VehiclePassengerBuilder door(String door) {
        this.door.setValue(door);
        return this;
    }

    public VehiclePassengerBuilder area(VehicleArea area) {
        this.area.setValue(area);
        return this;
    }

    public VehiclePassengerBuilder addAnim(VehicleAnimBuilder anim) {
        this.anim.addValues((VehicleAnimBuilder[])new VehicleAnimBuilder[]{anim});
        return this;
    }

    public VehiclePassengerBuilder addSwitchSeat(VehicleSwitchSeatBuilder switchseat) {
        this.switchseat.addValues((VehicleSwitchSeatBuilder[])new VehicleSwitchSeatBuilder[]{switchseat});
        return this;
    }

    public VehiclePassengerBuilder addPosition(VehiclePositionBuilder positions) {
        this.positions.addValues((VehiclePositionBuilder[])new VehiclePositionBuilder[]{positions});
        return this;
    }

    public VehiclePassengerBuilder door2(String door2) {
        this.door2.setValue(door2);
        return this;
    }

    public VehiclePassengerBuilder hasRoof(boolean hasRoof) {
        this.hasRoof.setValue(hasRoof);
        return this;
    }
}

