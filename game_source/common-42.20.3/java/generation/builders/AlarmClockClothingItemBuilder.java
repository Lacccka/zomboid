/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.ItemKey;

public class AlarmClockClothingItemBuilder
extends ItemBuilder<AlarmClockClothingItemBuilder> {
    private final Writeable.Property<Integer> biteDefense = this.property("BiteDefense");
    private final Writeable.Property<Integer> bulletDefense = this.property("BulletDefense");
    private final Writeable.Property<Boolean> canHaveHoles = this.property("CanHaveHoles");
    private final Writeable.Property<Integer> chanceToFall = this.property("ChanceToFall");
    private final Writeable.Property<Float> combatSpeedModifier = this.property("CombatSpeedModifier");
    private final Writeable.Property<Integer> conditionLowerChanceOneIn = this.property("ConditionLowerChanceOneIn");
    private final Writeable.Property<Float> insulation = this.property("Insulation");
    private final Writeable.Property<Float> neckProtectionModifier = this.property("NeckProtectionModifier");
    private final Writeable.Property<String> palettes = this.property("Palettes");
    private final Writeable.Property<Boolean> removeOnBroken = this.property("RemoveOnBroken");
    private final Writeable.Property<Float> runSpeedModifier = this.property("RunSpeedModifier");
    private final Writeable.Property<Integer> scratchDefense = this.property("ScratchDefense");
    private final Writeable.Property<Float> stompPower = this.property("StompPower");
    private final Writeable.Property<Float> temperature = this.property("Temperature");
    private final Writeable.Property<Float> waterResistance = this.property("WaterResistance");
    private final Writeable.Property<Float> weightWet = this.property("WeightWet");
    private final Writeable.Property<Float> windResistance = this.property("WindResistance");

    public AlarmClockClothingItemBuilder(ItemKey item) {
        super(item);
    }

    public AlarmClockClothingItemBuilder biteDefense(int biteDefense) {
        this.biteDefense.setValue(biteDefense);
        return this;
    }

    public AlarmClockClothingItemBuilder bulletDefense(int bulletDefense) {
        this.bulletDefense.setValue(bulletDefense);
        return this;
    }

    @Override
    public AlarmClockClothingItemBuilder canHaveHoles(boolean canHaveHoles) {
        this.canHaveHoles.setValue(canHaveHoles);
        return this;
    }

    @Override
    public AlarmClockClothingItemBuilder chanceToFall(int chanceToFall) {
        this.chanceToFall.setValue(chanceToFall);
        return this;
    }

    @Override
    public AlarmClockClothingItemBuilder combatSpeedModifier(float combatSpeedModifier) {
        this.combatSpeedModifier.setValue(Float.valueOf(combatSpeedModifier));
        return this;
    }

    @Override
    public AlarmClockClothingItemBuilder conditionLowerChanceOneIn(int conditionLowerChanceOneIn) {
        this.conditionLowerChanceOneIn.setValue(conditionLowerChanceOneIn);
        return this;
    }

    @Override
    public AlarmClockClothingItemBuilder insulation(float insulation) {
        this.insulation.setValue(Float.valueOf(insulation));
        return this;
    }

    public AlarmClockClothingItemBuilder neckProtectionModifier(float neckProtectionModifier) {
        this.neckProtectionModifier.setValue(Float.valueOf(neckProtectionModifier));
        return this;
    }

    public AlarmClockClothingItemBuilder palettes(String palettes) {
        this.palettes.setValue(palettes);
        return this;
    }

    @Override
    public AlarmClockClothingItemBuilder removeOnBroken(boolean removeOnBroken) {
        this.removeOnBroken.setValue(removeOnBroken);
        return this;
    }

    @Override
    public AlarmClockClothingItemBuilder runSpeedModifier(float runSpeedModifier) {
        this.runSpeedModifier.setValue(Float.valueOf(runSpeedModifier));
        return this;
    }

    public AlarmClockClothingItemBuilder scratchDefense(int scratchDefense) {
        this.scratchDefense.setValue(scratchDefense);
        return this;
    }

    public AlarmClockClothingItemBuilder stompPower(float stompPower) {
        this.stompPower.setValue(Float.valueOf(stompPower));
        return this;
    }

    public AlarmClockClothingItemBuilder temperature(float temperature) {
        this.temperature.setValue(Float.valueOf(temperature));
        return this;
    }

    public AlarmClockClothingItemBuilder waterResistance(float waterResistance) {
        this.waterResistance.setValue(Float.valueOf(waterResistance));
        return this;
    }

    public AlarmClockClothingItemBuilder weightWet(float weightWet) {
        this.weightWet.setValue(Float.valueOf(weightWet));
        return this;
    }

    @Override
    public AlarmClockClothingItemBuilder windResistance(float windResistance) {
        this.windResistance.setValue(Float.valueOf(windResistance));
        return this;
    }
}

