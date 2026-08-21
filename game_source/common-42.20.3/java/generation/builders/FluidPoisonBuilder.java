/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;
import zombie.entity.components.fluids.PoisonEffect;

public class FluidPoisonBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<PoisonEffect> maxEffect = this.property("maxEffect");
    private final Writeable.Property<Float> minAmount = this.property("minAmount");
    private final Writeable.Property<Float> diluteRatio = this.property("diluteRatio");

    public FluidPoisonBuilder minAmount(float minAmount) {
        this.minAmount.setValue(Float.valueOf(minAmount));
        return this;
    }

    public FluidPoisonBuilder diluteRatio(float diluteRatio) {
        this.diluteRatio.setValue(Float.valueOf(diluteRatio));
        return this;
    }

    public FluidPoisonBuilder maxEffect(PoisonEffect maxEffect) {
        this.maxEffect.setValue(maxEffect);
        return this;
    }
}

