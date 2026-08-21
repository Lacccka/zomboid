/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class VehicleWheelBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Boolean> front = this.property("front");
    private final Writeable.ListProperty<Float> offset = this.listProperty("offset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<Float> radius = this.property("radius");
    private final Writeable.Property<Float> width = this.property("width");

    public VehicleWheelBuilder(String name) {
        super(name);
    }

    public VehicleWheelBuilder front(boolean front) {
        this.front.setValue(front);
        return this;
    }

    public VehicleWheelBuilder offset(float x, float y, float z) {
        this.offset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehicleWheelBuilder radius(float radius) {
        this.radius.setValue(Float.valueOf(radius));
        return this;
    }

    public VehicleWheelBuilder width(float width) {
        this.width.setValue(Float.valueOf(width));
        return this;
    }
}

