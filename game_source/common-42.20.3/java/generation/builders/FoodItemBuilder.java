/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.Writeable;
import generation.builders.validation.SerializableMethod;
import zombie.characters.IsoGameCharacter;
import zombie.inventory.types.Food;
import zombie.scripting.objects.FoodType;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.SoundKey;

public class FoodItemBuilder
extends ItemBuilder<FoodItemBuilder> {
    private final Writeable.Property<Boolean> badCold = this.property("BadCold");
    private final Writeable.Property<Boolean> badInMicrowave = this.property("BadInMicrowave");
    private final Writeable.Property<Float> calories = this.property("Calories");
    private final Writeable.Property<Boolean> cantBeFrozen = this.property("CantBeFrozen");
    private final Writeable.Property<Float> carbohydrates = this.property("Carbohydrates");
    private final Writeable.Property<SoundKey> customEatSound = this.property("CustomEatSound");
    private final Writeable.Property<Boolean> dangerousUncooked = this.property("DangerousUncooked");
    private final Writeable.Property<Integer> daysFresh = this.property("DaysFresh");
    private final Writeable.Property<Integer> daysTotallyRotten = this.property("DaysTotallyRotten");
    private final Writeable.Property<Float> enduranceChange = this.property("enduranceChange");
    private final Writeable.Property<Integer> fluReduction = this.property("fluReduction");
    private final Writeable.Property<FoodType> foodType = this.property("FoodType");
    private final Writeable.Property<Boolean> goodHot = this.property("GoodHot");
    private final Writeable.Property<String> herbalistType = this.property("HerbalistType");
    private final Writeable.Property<Float> hungerChange = this.property("HungerChange");
    private final Writeable.Property<Boolean> isCookable = this.property("IsCookable");
    private final Writeable.Property<Float> lipids = this.property("Lipids");
    private final Writeable.Property<Integer> minutesToBurn = this.property("MinutesToBurn");
    private final Writeable.Property<Integer> minutesToCook = this.property("MinutesToCook");
    private final Writeable.Property<String> onCooked = this.property("OnCooked");
    private final Writeable.Property<String> onEat = this.property("OnEat");
    private final Writeable.Property<Boolean> packaged = this.property("Packaged");
    private final Writeable.Property<Integer> painReduction = this.property("painReduction");
    private final Writeable.Property<Boolean> poison = this.property("Poison");
    private final Writeable.Property<Integer> poisonDetectionLevel = this.property("PoisonDetectionLevel");
    private final Writeable.Property<Integer> poisonPower = this.property("PoisonPower");
    private final Writeable.Property<Float> proteins = this.property("Proteins");
    private final Writeable.Property<Integer> foodSicknessChange = this.property("FoodSicknessChange");
    private final Writeable.Property<Boolean> removeNegativeEffectOnCooked = this.property("RemoveNegativeEffectOnCooked");
    private final Writeable.Property<ItemKey> replaceOnCooked = this.property("ReplaceOnCooked");
    private final Writeable.Property<ItemKey> replaceOnRotten = this.property("ReplaceOnRotten");
    private final Writeable.Property<ItemKey> replaceOnUse = this.property("ReplaceOnUse");
    private final Writeable.Property<Boolean> spice = this.property("Spice");
    private final Writeable.Property<Float> thirstChange = this.property("ThirstChange");
    private final Writeable.Property<Float> useDelta = this.property("UseDelta", this::formatFloat);
    private final Writeable.Property<Integer> useForPoison = this.property("UseForPoison");

    public FoodItemBuilder(ItemKey item) {
        super(item);
    }

    public FoodItemBuilder badCold(boolean badCold) {
        this.badCold.setValue(badCold);
        return this;
    }

    public FoodItemBuilder badInMicrowave(boolean badInMicrowave) {
        this.badInMicrowave.setValue(badInMicrowave);
        return this;
    }

    @Override
    public FoodItemBuilder calories(float calories) {
        this.calories.setValue(Float.valueOf(calories));
        return this;
    }

    @Override
    public FoodItemBuilder cantBeFrozen(boolean cantBeFrozen) {
        this.cantBeFrozen.setValue(cantBeFrozen);
        return this;
    }

    @Override
    public FoodItemBuilder carbohydrates(float carbohydrates) {
        this.carbohydrates.setValue(Float.valueOf(carbohydrates));
        return this;
    }

    @Override
    public FoodItemBuilder customEatSound(SoundKey customEatSound) {
        this.customEatSound.setValue(customEatSound);
        return this;
    }

    public FoodItemBuilder dangerousUncooked(boolean dangerousUncooked) {
        this.dangerousUncooked.setValue(dangerousUncooked);
        return this;
    }

    public FoodItemBuilder daysFresh(int daysFresh) {
        this.daysFresh.setValue(daysFresh);
        return this;
    }

    public FoodItemBuilder daysTotallyRotten(int daysTotallyRotten) {
        this.daysTotallyRotten.setValue(daysTotallyRotten);
        return this;
    }

    public FoodItemBuilder enduranceChange(float enduranceChange) {
        this.enduranceChange.setValue(Float.valueOf(enduranceChange));
        return this;
    }

    public FoodItemBuilder fluReduction(int fluReduction) {
        this.fluReduction.setValue(fluReduction);
        return this;
    }

    @Override
    public FoodItemBuilder foodType(FoodType foodType) {
        this.foodType.setValue(foodType);
        return this;
    }

    public FoodItemBuilder goodHot(boolean goodHot) {
        this.goodHot.setValue(goodHot);
        return this;
    }

    public FoodItemBuilder herbalistType(String herbalistType) {
        this.herbalistType.setValue(herbalistType);
        return this;
    }

    @Override
    public FoodItemBuilder hungerChange(float hungerChange) {
        this.hungerChange.setValue(Float.valueOf(hungerChange));
        return this;
    }

    @Override
    public FoodItemBuilder isCookable(boolean isCookable) {
        this.isCookable.setValue(isCookable);
        return this;
    }

    @Override
    public FoodItemBuilder lipids(float lipids) {
        this.lipids.setValue(Float.valueOf(lipids));
        return this;
    }

    public FoodItemBuilder minutesToBurn(int minutesToBurn) {
        this.minutesToBurn.setValue(minutesToBurn);
        return this;
    }

    public FoodItemBuilder minutesToCook(int minutesToCook) {
        this.minutesToCook.setValue(minutesToCook);
        return this;
    }

    public FoodItemBuilder onCooked(SerializableMethod.Consumer<Food> onCooked) {
        this.onCooked.setValue(SerializableMethod.asLuaString(onCooked));
        return this;
    }

    public FoodItemBuilder onEat(String onEat) {
        this.onEat.setValue(onEat);
        return this;
    }

    public FoodItemBuilder onEat(SerializableMethod.Consumer3<Food, IsoGameCharacter, Float> onEat) {
        this.onEat.setValue(SerializableMethod.asLuaString(onEat));
        return this;
    }

    @Override
    public FoodItemBuilder packaged(boolean packaged) {
        this.packaged.setValue(packaged);
        return this;
    }

    public FoodItemBuilder painReduction(int painReduction) {
        this.painReduction.setValue(painReduction);
        return this;
    }

    public FoodItemBuilder poison(boolean poison) {
        this.poison.setValue(poison);
        return this;
    }

    public FoodItemBuilder poisonDetectionLevel(int poisonDetectionLevel) {
        this.poisonDetectionLevel.setValue(poisonDetectionLevel);
        return this;
    }

    public FoodItemBuilder poisonPower(int poisonPower) {
        this.poisonPower.setValue(poisonPower);
        return this;
    }

    @Override
    public FoodItemBuilder proteins(float proteins) {
        this.proteins.setValue(Float.valueOf(proteins));
        return this;
    }

    @Override
    public FoodItemBuilder foodSicknessChange(int foodSicknessChange) {
        this.foodSicknessChange.setValue(foodSicknessChange);
        return this;
    }

    public FoodItemBuilder removeNegativeEffectOnCooked(boolean removeNegativeEffectOnCooked) {
        this.removeNegativeEffectOnCooked.setValue(removeNegativeEffectOnCooked);
        return this;
    }

    public FoodItemBuilder replaceOnCooked(ItemKey replaceOnCooked) {
        this.replaceOnCooked.setValue(replaceOnCooked);
        return this;
    }

    public FoodItemBuilder replaceOnRotten(ItemKey replaceOnRotten) {
        this.replaceOnRotten.setValue(replaceOnRotten);
        return this;
    }

    @Override
    public FoodItemBuilder replaceOnUse(ItemKey replaceOnUse) {
        this.replaceOnUse.setValue(replaceOnUse);
        return this;
    }

    @Override
    public FoodItemBuilder spice(boolean spice) {
        this.spice.setValue(spice);
        return this;
    }

    public FoodItemBuilder thirstChange(float thirstChange) {
        this.thirstChange.setValue(Float.valueOf(thirstChange));
        return this;
    }

    public FoodItemBuilder useDelta(float useDelta) {
        this.useDelta.setValue(Float.valueOf(useDelta));
        return this;
    }

    public FoodItemBuilder useForPoison(int useForPoison) {
        this.useForPoison.setValue(useForPoison);
        return this;
    }
}

