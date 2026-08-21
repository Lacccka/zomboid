/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class CopyFrameBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Integer> frame = this.property("frame", 1);
    private final Writeable.Property<String> source = this.property("source");
    private final Writeable.Property<Integer> sourceFrame = this.property("sourceFrame", 1);

    public CopyFrameBuilder frame(int frame) {
        this.frame.setValue(frame);
        return this;
    }

    public CopyFrameBuilder sourceFrame(int sourceFrame) {
        this.sourceFrame.setValue(sourceFrame);
        return this;
    }

    public CopyFrameBuilder source(String source2) {
        this.source.setValue(source2);
        return this;
    }
}

