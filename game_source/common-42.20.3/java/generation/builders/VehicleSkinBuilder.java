/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import generation.builders.validation.PackTextureValidator;

public class VehicleSkinBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<PackTextureValidator> texture = this.property("texture");

    public VehicleSkinBuilder texture(String texture) {
        this.texture.setValue(PackTextureValidator.of(texture, ""));
        return this;
    }
}

