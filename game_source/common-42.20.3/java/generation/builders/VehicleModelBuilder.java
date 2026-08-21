/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ModelKey;

public class VehicleModelBuilder
extends AbstractDynamicOrderPropertyBuilder {
    private final Writeable.ListProperty<Float> rotate = this.listProperty("rotate", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<ModelKey> file = this.property("file");
    private final Writeable.Property<Float> scale = this.property("scale");
    private final Writeable.ListProperty<Float> offset = this.listProperty("offset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<String> attachmentParent = this.property("attachmentParent");
    private final Writeable.Property<String> attachmentSelf = this.property("attachmentSelf");
    private final Writeable.Property<Boolean> ignoreVehicleScale = this.property("ignoreVehicleScale");

    public VehicleModelBuilder() {
    }

    public VehicleModelBuilder(String name) {
        super(name);
    }

    public VehicleModelBuilder offset(float x, float y, float z) {
        this.offset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehicleModelBuilder rotate(float x, float y, float z) {
        this.rotate.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehicleModelBuilder scale(float scale) {
        this.scale.setValue(Float.valueOf(scale));
        return this;
    }

    public VehicleModelBuilder file(ModelKey file) {
        this.file.setValue(file);
        return this;
    }

    public VehicleModelBuilder attachmentParent(String attachmentParent) {
        this.attachmentParent.setValue(attachmentParent);
        return this;
    }

    public VehicleModelBuilder attachmentSelf(String attachmentSelf) {
        this.attachmentSelf.setValue(attachmentSelf);
        return this;
    }

    public VehicleModelBuilder ignoreVehicleScale(boolean ignoreVehicleScale) {
        this.ignoreVehicleScale.setValue(ignoreVehicleScale);
        return this;
    }
}

