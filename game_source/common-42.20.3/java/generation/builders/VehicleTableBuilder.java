/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractPropertyBuilder;
import generation.builders.PerkNumber;
import generation.builders.VehicleTemplateItemBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.CraftRecipeKey;
import zombie.scripting.objects.VehicleArea;
import zombie.scripting.objects.VehiclePart;

public class VehicleTableBuilder
extends AbstractPropertyBuilder {
    private final Writeable.Property<VehicleArea> area = this.property("area");
    private final Writeable.Property<Float> xOffset = this.property("xOffset");
    private final Writeable.Property<Float> yOffset = this.property("yOffset");
    private final Writeable.Property<Integer> distance = this.property("distance");
    private final Writeable.Property<Float> intensity = this.property("intensity");
    private final Writeable.ListProperty<VehicleTemplateItemBuilder> item = this.listProperty("items", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Integer> time = this.property("time");
    private final Writeable.Property<String> professions = this.property("professions");
    private final Writeable.Property<PerkNumber> skills = this.property("skills");
    private final Writeable.Property<String> traits = this.property("traits");
    private final Writeable.Property<CraftRecipeKey> recipes = this.property("recipes");
    private final Writeable.Property<String> test = this.property("test");
    private final Writeable.Property<String> complete = this.property("complete");
    private final Writeable.Property<String> door = this.property("door");
    private final Writeable.ListProperty<VehiclePart> requireInstalled = this.listProperty("requireInstalled", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.ListProperty<VehiclePart> requireUninstalled = this.listProperty("requireUninstalled", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Boolean> requireEmpty = this.property("requireEmpty");
    private final Writeable.Property<Boolean> mechanicRequireKey = this.property("mechanicRequireKey");

    public VehicleTableBuilder(String name) {
        super(name);
    }

    public VehicleTableBuilder area(VehicleArea area) {
        this.area.setValue(area);
        return this;
    }

    public VehicleTableBuilder xOffset(float xOffset) {
        this.xOffset.setValue(Float.valueOf(xOffset));
        return this;
    }

    public VehicleTableBuilder yOffset(float yOffset) {
        this.yOffset.setValue(Float.valueOf(yOffset));
        return this;
    }

    public VehicleTableBuilder distance(int distance) {
        this.distance.setValue(distance);
        return this;
    }

    public VehicleTableBuilder intensity(float intensity) {
        this.intensity.setValue(Float.valueOf(intensity));
        return this;
    }

    public VehicleTableBuilder addItem(VehicleTemplateItemBuilder ... item) {
        this.item.addValues((VehicleTemplateItemBuilder[])item);
        return this;
    }

    public VehicleTableBuilder skills(PerkNumber skills) {
        this.skills.setValue(skills);
        return this;
    }

    public VehicleTableBuilder recipes(CraftRecipeKey recipes) {
        this.recipes.setValue(recipes);
        return this;
    }

    public VehicleTableBuilder time(int time) {
        this.time.setValue(time);
        return this;
    }

    public VehicleTableBuilder professions(String professions) {
        this.professions.setValue(professions);
        return this;
    }

    public VehicleTableBuilder traits(String traits) {
        this.traits.setValue(traits);
        return this;
    }

    public VehicleTableBuilder test(String test) {
        this.test.setValue(test);
        return this;
    }

    public VehicleTableBuilder complete(String complete) {
        this.complete.setValue(complete);
        return this;
    }

    public VehicleTableBuilder door(String door) {
        this.door.setValue(door);
        return this;
    }

    public VehicleTableBuilder requireInstalled(VehiclePart ... requireInstalled) {
        this.requireInstalled.addValues((VehiclePart[])requireInstalled);
        return this;
    }

    public VehicleTableBuilder requireUninstalled(VehiclePart ... requireUninstalled) {
        this.requireUninstalled.addValues((VehiclePart[])requireUninstalled);
        return this;
    }

    public VehicleTableBuilder requireEmpty(boolean requireEmpty) {
        this.requireEmpty.setValue(requireEmpty);
        return this;
    }

    public VehicleTableBuilder mechanicRequireKey(boolean mechanicRequireKey) {
        this.mechanicRequireKey.setValue(mechanicRequireKey);
        return this;
    }
}

