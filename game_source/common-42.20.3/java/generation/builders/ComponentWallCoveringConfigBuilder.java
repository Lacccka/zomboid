/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.SignType;
import zombie.scripting.objects.WallCoveringType;

public class ComponentWallCoveringConfigBuilder
extends AbstractDynamicOrderPropertyBuilder
implements ComponentBuilder {
    private final Writeable.Property<WallCoveringType> coveringType = this.property("type");
    private final Writeable.Property<String> paintType = this.property("paintType");
    private final Writeable.Property<SignType> signType = this.property("sign");

    public ComponentWallCoveringConfigBuilder() {
        super("WallCoveringConfig");
    }

    public ComponentWallCoveringConfigBuilder type(WallCoveringType coveringType) {
        this.coveringType.setValue(coveringType);
        return this;
    }

    public ComponentWallCoveringConfigBuilder paintType(String paintType) {
        this.paintType.setValue(paintType);
        return this;
    }

    public ComponentWallCoveringConfigBuilder signType(SignType signType) {
        this.signType.setValue(signType);
        return this;
    }
}

