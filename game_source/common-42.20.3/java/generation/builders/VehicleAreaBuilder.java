/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class VehicleAreaBuilder
extends AbstractPropertyBuilder {
    private final Writeable.ListProperty<Float> xywh = this.listProperty("xywh", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);

    public VehicleAreaBuilder(String name) {
        super(name);
    }

    public VehicleAreaBuilder xywh(float x, float y, float w, float h) {
        this.xywh.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(w), Float.valueOf(h)});
        return this;
    }
}

