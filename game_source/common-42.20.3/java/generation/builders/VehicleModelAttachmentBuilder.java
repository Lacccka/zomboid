/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class VehicleModelAttachmentBuilder
extends AbstractPropertyBuilder {
    private final Writeable.ListProperty<Float> offset = this.listProperty("offset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> rotate = this.listProperty("rotate", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<Float> scale = this.property("scale");
    private final Writeable.Property<String> bone = this.property("bone");
    private final Writeable.Property<Float> zoffset = this.property("zoffset");
    private final Writeable.Property<String> canAttach = this.property("canAttach");
    private final Writeable.Property<Boolean> updateconstraint = this.property("updateconstraint");

    public VehicleModelAttachmentBuilder(String name) {
        super(name);
    }

    public VehicleModelAttachmentBuilder offset(float x, float y, float z) {
        this.offset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehicleModelAttachmentBuilder rotate(float x, float y, float z) {
        this.rotate.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehicleModelAttachmentBuilder scale(float scale) {
        this.scale.setValue(Float.valueOf(scale));
        return this;
    }

    public VehicleModelAttachmentBuilder bone(String bone) {
        this.bone.setValue(bone);
        return this;
    }

    public VehicleModelAttachmentBuilder zoffset(float zoffset) {
        this.zoffset.setValue(Float.valueOf(zoffset));
        return this;
    }

    public VehicleModelAttachmentBuilder canAttach(String canAttach) {
        this.canAttach.setValue(canAttach);
        return this;
    }

    public VehicleModelAttachmentBuilder updateconstraint(boolean updateconstraint) {
        this.updateconstraint.setValue(updateconstraint);
        return this;
    }
}

