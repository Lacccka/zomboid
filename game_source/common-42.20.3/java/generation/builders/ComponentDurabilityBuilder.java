/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.Writeable;
import zombie.iso.enums.MaterialType;

public class ComponentDurabilityBuilder
extends AbstractPropertyBuilder
implements ComponentBuilder {
    private final Writeable.Property<MaterialType> material = this.property("Material");
    private final Writeable.Property<Float> maxHitPoints = this.property("MaxHitPoints");

    public ComponentDurabilityBuilder() {
        super("Durability");
    }

    public ComponentDurabilityBuilder material(MaterialType material) {
        this.material.setValue(material);
        return this;
    }

    public ComponentDurabilityBuilder maxHitPoints(float maxHitPoints) {
        this.maxHitPoints.setValue(Float.valueOf(maxHitPoints));
        return this;
    }
}

