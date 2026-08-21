/*
 * Decompiled with CFR 0.152.
 */
package zombie.scripting.objects;

import generation.builders.validation.TranslationKeyValidator;
import zombie.UsedFromLua;
import zombie.core.Core;
import zombie.scripting.objects.Registries;
import zombie.scripting.objects.RegistryReset;
import zombie.scripting.objects.ResourceLocation;

@UsedFromLua
public class ContainerType {
    public static final ContainerType BARBECUE = ContainerType.registerBase("barbecue");
    public static final ContainerType BARBECUE_PROPANE = ContainerType.registerBase("barbecuepropane");
    public static final ContainerType BIN = ContainerType.registerBase("bin");
    public static final ContainerType BRAZIER = ContainerType.registerBase("brazier");
    public static final ContainerType CAMPFIRE = ContainerType.registerBase("campfire");
    public static final ContainerType CARDBOARD_BOX = ContainerType.registerBase("cardboardbox");
    public static final ContainerType CASH_REGISTER = ContainerType.registerBase("cashregister");
    public static final ContainerType CLOTHING_DRYER = ContainerType.registerBase("clothingdryer");
    public static final ContainerType CLOTHING_DRYER_BASIC = ContainerType.registerBase("clothingdryerbasic");
    public static final ContainerType CLOTHING_RACK = ContainerType.registerBase("clothingrack");
    public static final ContainerType CLOTHING_WASHER = ContainerType.registerBase("clothingwasher");
    public static final ContainerType COFFEE_MAKER = ContainerType.registerBase("coffeemaker");
    public static final ContainerType COFFIN = ContainerType.registerBase("coffin");
    public static final ContainerType COMPOSTER = ContainerType.registerBase("composter");
    public static final ContainerType CORN = ContainerType.registerBase("corn");
    public static final ContainerType COUNTER = ContainerType.registerBase("counter");
    public static final ContainerType CRATE = ContainerType.registerBase("crate");
    public static final ContainerType CUPBOARD = ContainerType.registerBase("cupboard");
    public static final ContainerType DESK = ContainerType.registerBase("desk");
    public static final ContainerType DISHES_CABINET = ContainerType.registerBase("dishescabinet");
    public static final ContainerType DISHWASHER = ContainerType.registerBase("dishwasher");
    public static final ContainerType DISPLAY_CASE = ContainerType.registerBase("displaycase");
    public static final ContainerType DISPLAY_CASE_BUTCHER = ContainerType.registerBase("displaycasebutcher");
    public static final ContainerType DISPLAY_CASE_BAKERY = ContainerType.registerBase("displaycasebakery");
    public static final ContainerType DOG_HOUSE = ContainerType.registerBase("doghouse");
    public static final ContainerType DRESSER = ContainerType.registerBase("dresser");
    public static final ContainerType DUMPSTER = ContainerType.registerBase("dumpster");
    public static final ContainerType FILING_CABINET = ContainerType.registerBase("filingcabinet");
    public static final ContainerType FIREPLACE = ContainerType.registerBase("fireplace");
    public static final ContainerType FLOOR = ContainerType.registerBase("floor");
    public static final ContainerType FREEZER = ContainerType.registerBase("freezer");
    public static final ContainerType FRIDGE = ContainerType.registerBase("fridge");
    public static final ContainerType FRUIT_BUSH_A = ContainerType.registerBase("fruitbusha");
    public static final ContainerType FRUIT_BUSH_B = ContainerType.registerBase("fruitbushb");
    public static final ContainerType FRUIT_BUSH_C = ContainerType.registerBase("fruitbushc");
    public static final ContainerType FRUIT_BUSH_D = ContainerType.registerBase("fruitbushd");
    public static final ContainerType FRUIT_BUSH_E = ContainerType.registerBase("fruitbushe");
    public static final ContainerType GARAGE_STORAGE = ContainerType.registerBase("garagestorage");
    public static final ContainerType GROCER_STANDg = ContainerType.registerBase("grocerstand");
    public static final ContainerType GLOVE_BOX = ContainerType.registerBase("GloveBox");
    public static final ContainerType ICE_CREAM = ContainerType.registerBase("icecream");
    public static final ContainerType INVENTORY_MALE = ContainerType.registerBase("inventoryfemale");
    public static final ContainerType INVENTORY_FEMALE = ContainerType.registerBase("inventorymale");
    public static final ContainerType LOCKER = ContainerType.registerBase("locker");
    public static final ContainerType LOGS = ContainerType.registerBase("logs");
    public static final ContainerType MANNEQUIN = ContainerType.registerBase("Mannequin");
    public static final ContainerType MEDICINE = ContainerType.registerBase("medicine");
    public static final ContainerType METAL_SHELVES = ContainerType.registerBase("metal_shelves");
    public static final ContainerType MICROWAVE = ContainerType.registerBase("microwave");
    public static final ContainerType MILITARY_CRATE = ContainerType.registerBase("militarycrate");
    public static final ContainerType MILITARY_LOCKER = ContainerType.registerBase("militarylocker");
    public static final ContainerType NEWSPAPER_DISPATCH = ContainerType.registerBase("newspaper_dispatch");
    public static final ContainerType NEWSPAPER_HERALD = ContainerType.registerBase("newspaper_herald");
    public static final ContainerType NEWSPAPER_KNEWS = ContainerType.registerBase("newspaper_knews");
    public static final ContainerType NEWSPAPER_TIMES = ContainerType.registerBase("newspaper_times");
    public static final ContainerType OFFICE_DRAWERS = ContainerType.registerBase("officedrawers");
    public static final ContainerType OVERHEAD = ContainerType.registerBase("overhead");
    public static final ContainerType PLANK_STASH = ContainerType.registerBase("plankstash");
    public static final ContainerType POSTBOX = ContainerType.registerBase("postbox");
    public static final ContainerType RESTAURANT_DISPLAY = ContainerType.registerBase("restaurantdisplay");
    public static final ContainerType SEAT_FRONT_LEFT = ContainerType.registerBase("SeatFrontLeft");
    public static final ContainerType SEAT_FRONT_RIGHT = ContainerType.registerBase("SeatFrontRight");
    public static final ContainerType SEAT_MIDDLE_LEFT = ContainerType.registerBase("SeatMiddleLeft");
    public static final ContainerType SEAT_MIDDLE_RIGHT = ContainerType.registerBase("SeatMiddleRight");
    public static final ContainerType SEAT_REAR_LEFT = ContainerType.registerBase("SeatRearLeft");
    public static final ContainerType SEAT_REAR_RIGHT = ContainerType.registerBase("SeatRearRight");
    public static final ContainerType SHELTER = ContainerType.registerBase("shelter");
    public static final ContainerType SHELVES = ContainerType.registerBase("shelves");
    public static final ContainerType SHELVES_MAG = ContainerType.registerBase("shelvesmag");
    public static final ContainerType SIDETABLE = ContainerType.registerBase("sidetable");
    public static final ContainerType SMALL_BOX = ContainerType.registerBase("smallbox");
    public static final ContainerType SMALL_CRATE = ContainerType.registerBase("smallcrate");
    public static final ContainerType STONE_FURNACE = ContainerType.registerBase("stonefurnace");
    public static final ContainerType STOVE = ContainerType.registerBase("stove");
    public static final ContainerType SURVIVOR_CRATE = ContainerType.registerBase("SurvivorCrate");
    public static final ContainerType TENT = ContainerType.registerBase("tent");
    public static final ContainerType TOASTER = ContainerType.registerBase("toaster");
    public static final ContainerType TOOL_CABINET = ContainerType.registerBase("toolcabinet");
    public static final ContainerType TROUGH = ContainerType.registerBase("trough");
    public static final ContainerType TRUCK_BED = ContainerType.registerBase("TruckBed");
    public static final ContainerType TRUCK_BED_OPEN = ContainerType.registerBase("TruckBedOpen");
    public static final ContainerType VENDING_GT = ContainerType.registerBase("vendingGt");
    public static final ContainerType VENDING_SNACK = ContainerType.registerBase("vendingsnack");
    public static final ContainerType VENDING_POP = ContainerType.registerBase("vendingpop");
    public static final ContainerType WARDROVE = ContainerType.registerBase("wardrobe");
    public static final ContainerType WOOD_STOVE = ContainerType.registerBase("woodstove");
    private final String translationName;

    private ContainerType(String translationName) {
        this.translationName = "IGUI_ContainerTitle_" + translationName;
    }

    public static ContainerType get(ResourceLocation id) {
        return Registries.CONTAINER_TYPE.get(id);
    }

    public String toString() {
        return Registries.CONTAINER_TYPE.getLocation(this).toString();
    }

    public String getTranslationName() {
        return this.translationName;
    }

    public static ContainerType register(String id) {
        return ContainerType.register(false, id);
    }

    private static ContainerType registerBase(String id) {
        return ContainerType.register(true, id);
    }

    private static ContainerType register(boolean allowDefaultNamespace, String id) {
        return Registries.CONTAINER_TYPE.register(RegistryReset.createLocation(id, allowDefaultNamespace), new ContainerType(id));
    }

    static {
        if (Core.IS_DEV) {
            for (ContainerType containerType : Registries.CONTAINER_TYPE) {
                TranslationKeyValidator.of(containerType.translationName);
            }
        }
    }
}

