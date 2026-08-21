/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class VehicleTemplateWindowBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Boolean> openable = this.property("openable");

    public VehicleTemplateWindowBuilder openable(boolean openable) {
        this.openable.setValue(openable);
        return this;
    }
}

