/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class CopyFramesBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Integer> frame = this.property("frame", 1);
    private final Writeable.Property<String> source = this.property("source");
    private final Writeable.Property<Integer> sourceFrame1 = this.property("sourceFrame1", 1);
    private final Writeable.Property<Integer> sourceFrame2 = this.property("sourceFrame2", 1);

    public CopyFramesBuilder frame(int frame) {
        this.frame.setValue(frame);
        return this;
    }

    public CopyFramesBuilder sourceFrame1(int sourceFrame1) {
        this.sourceFrame1.setValue(sourceFrame1);
        return this;
    }

    public CopyFramesBuilder sourceFrame2(int sourceFrame2) {
        this.sourceFrame2.setValue(sourceFrame2);
        return this;
    }

    public CopyFramesBuilder source(String source2) {
        this.source.setValue(source2);
        return this;
    }
}

