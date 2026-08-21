/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class RpmDataBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Integer> gearChange = this.property("gearChange");
    private final Writeable.Property<Integer> afterGearChange = this.property("afterGearChange");

    public RpmDataBuilder gearChange(int gearChange) {
        this.gearChange.setValue(gearChange);
        return this;
    }

    public RpmDataBuilder afterGearChange(int afterGearChange) {
        this.afterGearChange.setValue(afterGearChange);
        return this;
    }
}

