/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.VehicleArea;

public class VehiclePositionBuilder
extends AbstractPropertyBuilder {
    private final Writeable.ListProperty<Float> offset = this.listProperty("offset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> rotate = this.listProperty("rotate", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<VehicleArea> area = this.property("area");

    public VehiclePositionBuilder(String name) {
        super(name);
    }

    public VehiclePositionBuilder offset(float x, float y, float z) {
        this.offset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehiclePositionBuilder rotate(float x, float y, float z) {
        this.rotate.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehiclePositionBuilder area(VehicleArea area) {
        this.area.setValue(area);
        return this;
    }
}

