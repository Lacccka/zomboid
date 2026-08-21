/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.Writeable;

public class FluidPropertiesBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<Float> fatigueChange = this.property("fatigueChange");
    private final Writeable.Property<Float> hungerChange = this.property("HungerChange");
    private final Writeable.Property<Float> stressChange = this.property("StressChange");
    private final Writeable.Property<Float> thirstChange = this.property("ThirstChange");
    private final Writeable.Property<Float> unhappyChange = this.property("UnhappyChange");
    private final Writeable.Property<Float> calories = this.property("Calories");
    private final Writeable.Property<Float> carbohydrates = this.property("Carbohydrates");
    private final Writeable.Property<Float> lipids = this.property("Lipids");
    private final Writeable.Property<Float> proteins = this.property("Proteins");
    private final Writeable.Property<Float> alcohol = this.property("alcohol");
    private final Writeable.Property<Float> fluReduction = this.property("fluReduction");
    private final Writeable.Property<Float> painReduction = this.property("painReduction");
    private final Writeable.Property<Float> enduranceChange = this.property("enduranceChange");
    private final Writeable.Property<Integer> foodSicknessChange = this.property("foodSicknessChange");

    public FluidPropertiesBuilder unhappyChange(float unHappyChange) {
        this.unhappyChange.setValue(Float.valueOf(unHappyChange));
        return this;
    }

    public FluidPropertiesBuilder stressChange(float stressChange) {
        this.stressChange.setValue(Float.valueOf(stressChange));
        return this;
    }

    public FluidPropertiesBuilder fatigueChange(float fatigueChange) {
        this.fatigueChange.setValue(Float.valueOf(fatigueChange));
        return this;
    }

    public FluidPropertiesBuilder thirstChange(float thirstChange) {
        this.thirstChange.setValue(Float.valueOf(thirstChange));
        return this;
    }

    public FluidPropertiesBuilder alcohol(float alcohol) {
        this.alcohol.setValue(Float.valueOf(alcohol));
        return this;
    }

    public FluidPropertiesBuilder hungerChange(float hungerChange) {
        this.hungerChange.setValue(Float.valueOf(hungerChange));
        return this;
    }

    public FluidPropertiesBuilder calories(float calories) {
        this.calories.setValue(Float.valueOf(calories));
        return this;
    }

    public FluidPropertiesBuilder carbohydrates(float carbohydrates) {
        this.carbohydrates.setValue(Float.valueOf(carbohydrates));
        return this;
    }

    public FluidPropertiesBuilder lipids(float lipids) {
        this.lipids.setValue(Float.valueOf(lipids));
        return this;
    }

    public FluidPropertiesBuilder proteins(float proteins) {
        this.proteins.setValue(Float.valueOf(proteins));
        return this;
    }

    public FluidPropertiesBuilder fluReduction(float fluReduction) {
        this.fluReduction.setValue(Float.valueOf(fluReduction));
        return this;
    }

    public FluidPropertiesBuilder painReduction(float painReduction) {
        this.painReduction.setValue(Float.valueOf(painReduction));
        return this;
    }

    public FluidPropertiesBuilder enduranceChange(float enduranceChange) {
        this.enduranceChange.setValue(Float.valueOf(enduranceChange));
        return this;
    }

    public FluidPropertiesBuilder foodSicknessChange(int foodSicknessChange) {
        this.foodSicknessChange.setValue(foodSicknessChange);
        return this;
    }
}

