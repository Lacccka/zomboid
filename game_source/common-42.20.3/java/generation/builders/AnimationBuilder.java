/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.CopyFrameBuilder;
import generation.builders.CopyFramesBuilder;
import generation.builders.Writeable;
import zombie.scripting.ScriptType;

public class AnimationBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.ListProperty<CopyFrameBuilder> copyFrame = this.listProperty("CopyFrame", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<CopyFramesBuilder> copyFrames = this.listProperty("CopyFrames", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public static AnimationBuilder withId(String id) {
        return new AnimationBuilder(id);
    }

    public AnimationBuilder(String name) {
        super(ScriptType.RuntimeAnimation, name);
    }

    public AnimationBuilder addCopyFrame(CopyFrameBuilder copyFrame) {
        this.copyFrame.addValues((CopyFrameBuilder[])new CopyFrameBuilder[]{copyFrame});
        return this;
    }

    public AnimationBuilder addCopyFrames(CopyFramesBuilder copyFrames) {
        this.copyFrames.addValues((CopyFramesBuilder[])new CopyFramesBuilder[]{copyFrames});
        return this;
    }
}

