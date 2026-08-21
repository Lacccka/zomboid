/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractDynamicOrderPropertyBuilder;
import generation.builders.VehicleAnimBuilder;
import generation.builders.VehicleContainerBuilder;
import generation.builders.VehicleLuaBuilder;
import generation.builders.VehicleModelBuilder;
import generation.builders.VehicleTableBuilder;
import generation.builders.VehicleTemplateDoorBuilder;
import generation.builders.VehicleTemplateWindowBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.VehicleArea;
import zombie.scripting.objects.VehicleCategory;
import zombie.scripting.objects.VehicleMechanicArea;
import zombie.scripting.objects.VehiclePart;
import zombie.scripting.objects.VehicleWheel;

public class VehiclePartBuilder
extends AbstractDynamicOrderPropertyBuilder {
    private final Writeable.Property<String> id = this.property("id");
    private final Writeable.Property<VehicleMechanicArea> mechanicArea = this.property("mechanicArea");
    private final Writeable.Property<VehicleArea> area = this.property("area");
    private final Writeable.Property<VehiclePart> parent = this.property("parent");
    private final Writeable.Property<VehicleCategory> category = this.property("category");
    private final Writeable.Property<Boolean> mechanicRequireKey = this.property("mechanicRequireKey");
    private final Writeable.ListProperty<VehicleTemplateWindowBuilder> window = this.listProperty("window", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK, Writeable.ListProperty.Flags.SHOW_IF_EMPTY);
    private final Writeable.Property<Integer> durability = this.property("durability");
    private final Writeable.ListProperty<VehicleTemplateDoorBuilder> door = this.listProperty("door", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK, Writeable.ListProperty.Flags.SHOW_IF_EMPTY);
    private final Writeable.Property<VehicleWheel> wheel = this.property("wheel");
    private final Writeable.Property<String> recipes = this.property("recipes");
    private final Writeable.Property<Boolean> specificItem = this.property("specificItem");
    private final Writeable.ListProperty<VehicleAnimBuilder> anim = this.listProperty("anim", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<String> itemType = this.listProperty("itemType", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Boolean> repairMechanic = this.property("repairMechanic");
    private final Writeable.ListProperty<VehicleContainerBuilder> container = this.listProperty("container", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleModelBuilder> model = this.listProperty("model", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleTableBuilder> table = this.listProperty("table", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<Boolean> hasLightsRear = this.property("hasLightsRear");
    private final Writeable.ListProperty<VehicleLuaBuilder> lua = this.listProperty("lua", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);

    public VehiclePartBuilder(String name, boolean globalPattern) {
        super((String)(globalPattern ? name + "*" : name));
    }

    public VehiclePartBuilder addWindow(VehicleTemplateWindowBuilder window) {
        this.window.addValues((VehicleTemplateWindowBuilder[])new VehicleTemplateWindowBuilder[]{window});
        return this;
    }

    public VehiclePartBuilder addDoor(VehicleTemplateDoorBuilder door) {
        this.door.addValues((VehicleTemplateDoorBuilder[])new VehicleTemplateDoorBuilder[]{door});
        return this;
    }

    public VehiclePartBuilder addModel(VehicleModelBuilder model) {
        this.model.addValues((VehicleModelBuilder[])new VehicleModelBuilder[]{model});
        return this;
    }

    public VehiclePartBuilder addAnim(VehicleAnimBuilder anim) {
        this.anim.addValues((VehicleAnimBuilder[])new VehicleAnimBuilder[]{anim});
        return this;
    }

    public VehiclePartBuilder addLua(VehicleLuaBuilder lua) {
        this.lua.addValues((VehicleLuaBuilder[])new VehicleLuaBuilder[]{lua});
        return this;
    }

    public VehiclePartBuilder addTable(VehicleTableBuilder table) {
        this.table.addValues((VehicleTableBuilder[])new VehicleTableBuilder[]{table});
        return this;
    }

    public VehiclePartBuilder addContainer(VehicleContainerBuilder container) {
        this.container.addValues((VehicleContainerBuilder[])new VehicleContainerBuilder[]{container});
        return this;
    }

    public VehiclePartBuilder id(String id) {
        this.id.setValue(id);
        return this;
    }

    public VehiclePartBuilder itemType(String ... itemType) {
        this.itemType.addValues((String[])itemType);
        return this;
    }

    public VehiclePartBuilder parent(VehiclePart parent) {
        this.parent.setValue(parent);
        return this;
    }

    public VehiclePartBuilder recipes(String recipes) {
        this.recipes.setValue(recipes);
        return this;
    }

    public VehiclePartBuilder area(VehicleArea area) {
        this.area.setValue(area);
        return this;
    }

    public VehiclePartBuilder mechanicArea(VehicleMechanicArea mechanicArea) {
        this.mechanicArea.setValue(mechanicArea);
        return this;
    }

    public VehiclePartBuilder wheel(VehicleWheel wheel) {
        this.wheel.setValue(wheel);
        return this;
    }

    public VehiclePartBuilder door(String door) {
        return this;
    }

    public VehiclePartBuilder window(String window) {
        return this;
    }

    public VehiclePartBuilder category(VehicleCategory category) {
        this.category.setValue(category);
        return this;
    }

    public VehiclePartBuilder specificItem(boolean specificItem) {
        this.specificItem.setValue(specificItem);
        return this;
    }

    public VehiclePartBuilder mechanicRequireKey(boolean mechanicRequireKey) {
        this.mechanicRequireKey.setValue(mechanicRequireKey);
        return this;
    }

    public VehiclePartBuilder repairMechanic(boolean repairMechanic) {
        this.repairMechanic.setValue(repairMechanic);
        return this;
    }

    public VehiclePartBuilder hasLightsRear(boolean hasLightsRear) {
        this.hasLightsRear.setValue(hasLightsRear);
        return this;
    }

    public VehiclePartBuilder durability(int durability) {
        this.durability.setValue(durability);
        return this;
    }
}

