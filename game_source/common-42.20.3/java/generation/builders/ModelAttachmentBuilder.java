/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import zombie.core.skinnedmodel.model.SkeletonBone;
import zombie.scripting.objects.ModelAttachmentId;

public class ModelAttachmentBuilder
extends AbstractPropertyBuilder {
    private final Writeable.ListProperty<Float> offset = this.listProperty("offset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> rotate = this.listProperty("rotate", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<Float> scale = this.property("scale");
    private final Writeable.Property<String> bone = this.property("bone");
    private final Writeable.Property<Float> zoffset = this.property("zoffset");

    public ModelAttachmentBuilder(String name) {
        super(name);
    }

    public ModelAttachmentBuilder(ModelAttachmentId attachmentId) {
        super(attachmentId.getId());
    }

    public ModelAttachmentBuilder offset(float x, float y, float z) {
        this.offset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public ModelAttachmentBuilder rotate(float x, float y, float z) {
        this.rotate.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public ModelAttachmentBuilder scale(float scale) {
        this.scale.setValue(Float.valueOf(scale));
        return this;
    }

    public ModelAttachmentBuilder bone(SkeletonBone bone) {
        this.bone.setValue(bone.getName());
        return this;
    }

    public ModelAttachmentBuilder bone(String bone) {
        this.bone.setValue(bone);
        return this;
    }

    public ModelAttachmentBuilder zoffset(float zoffset) {
        this.zoffset.setValue(Float.valueOf(zoffset));
        return this;
    }
}

