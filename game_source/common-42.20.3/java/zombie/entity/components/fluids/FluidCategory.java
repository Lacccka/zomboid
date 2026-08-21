/*
 * Decompiled with CFR 0.152.
 */
package zombie.entity.components.fluids;

import java.util.ArrayList;
import java.util.HashMap;
import zombie.UsedFromLua;
import zombie.core.Translator;

@UsedFromLua
public enum FluidCategory {
    Beverage(1),
    Alcoholic(2),
    Hazardous(3),
    Medical(4),
    Industrial(5),
    Colors(6),
    Dyes(7),
    HairDyes(8),
    Paint(9),
    Fuel(10),
    Poisons(11),
    Water(12);

    private static final HashMap<Byte, FluidCategory> idMap;
    private static final ArrayList<FluidCategory> list;
    private final byte id;

    private FluidCategory(byte id) {
        this.id = id;
    }

    public byte getId() {
        return this.id;
    }

    public static FluidCategory FromId(byte id) {
        return idMap.get(id);
    }

    public static ArrayList<FluidCategory> getList() {
        return list;
    }

    public String getName() {
        return Translator.getText("IGUI_FluidCategory_" + String.valueOf((Object)this), new Object[0]);
    }

    static {
        idMap = new HashMap();
        list = new ArrayList();
        for (FluidCategory category : FluidCategory.values()) {
            idMap.put(category.id, category);
            list.add(category);
        }
    }
}

