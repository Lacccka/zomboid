/*
 * Decompiled with CFR 0.152.
 */
package zombie.core;

import generation.builders.validation.TranslationKeyValidator;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;
import zombie.UsedFromLua;
import zombie.core.Core;
import zombie.core.Translator;
import zombie.core.input.Input;
import zombie.scripting.objects.Registries;
import zombie.scripting.objects.RegistryReset;
import zombie.scripting.objects.ResourceLocation;
import zombie.util.StringUtils;

@UsedFromLua
public class SurvivalGuideEntry {
    public static final SurvivalGuideEntry MOVEMENT = SurvivalGuideEntry.registerBase("movement");
    public static final SurvivalGuideEntry MOVEMENT_WALKING_RUNNING = SurvivalGuideEntry.registerBase("walkingrunning", "movement", List.of("Forward", "Left", "Backward", "Right", "Run", "Sprint"), List.of("<JOYPAD:LStick,28,28>", "<JOYPAD:RTrigger,28,28>", "<JOYPAD:RTrigger,28,28> + <JOYPAD:BButton,28,28>"));
    public static final SurvivalGuideEntry MOVEMENT_CLIMBING = SurvivalGuideEntry.registerBase("climbing", "movement", List.of("Interact"), List.of("<JOYPAD:AButton,28,28>"));
    public static final SurvivalGuideEntry MOVEMENT_VAULTING = SurvivalGuideEntry.registerBase("vaulting", "movement", List.of("Interact"), List.of("<JOYPAD:AButton,28,28>"));
    public static final SurvivalGuideEntry MOVEMENT_AIMING = SurvivalGuideEntry.registerBase("aiming", "movement", List.of("Aim"), List.of("<JOYPAD:LStick,28,28>"));
    public static final SurvivalGuideEntry MOVEMENT_STRAFING = SurvivalGuideEntry.registerBase("strafing", "movement");
    public static final SurvivalGuideEntry MOVEMENT_SNEAK_FENCE = SurvivalGuideEntry.registerBase("sneak_fence", "movement", List.of("Crouch"), List.of("<JOYPAD:LStick,28,28>"));
    public static final SurvivalGuideEntry INVENTORY = SurvivalGuideEntry.registerBase("inventory");
    public static final SurvivalGuideEntry INVENTORY_BACKPACKS = SurvivalGuideEntry.registerBase("backpacks", "inventory", List.of("IGUI_Key_LSHIFT", "IGUI_Key_LCONTROL"), List.of("IGUI_Key_LSHIFT", "IGUI_Key_LCONTROL"));
    public static final SurvivalGuideEntry INVENTORY_DOUBLE_CLICK = SurvivalGuideEntry.registerBase("double_click", "inventory");
    public static final SurvivalGuideEntry INVENTORY_WEIGHT = SurvivalGuideEntry.registerBase("weight_distribution", "inventory");
    public static final SurvivalGuideEntry INTERACTABLE = SurvivalGuideEntry.registerBase("interactable");
    public static final SurvivalGuideEntry RIGHT_CLICK_INTERACT = SurvivalGuideEntry.registerBase("right_click_interact", "interactable", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry DOORS = SurvivalGuideEntry.registerBase("doors", "interactable", List.of("Interact", "IGUI_mouse_btn_0"), List.of("<JOYPAD:AButton,28,28>"));
    public static final SurvivalGuideEntry WINDOWS = SurvivalGuideEntry.registerBase("windows", "interactable", List.of("Interact", "Interact"), List.of("<JOYPAD:BButton,28,28>", "<JOYPAD:AButton,28,28>"));
    public static final SurvivalGuideEntry CURTAINS = SurvivalGuideEntry.registerBase("curtains", "interactable", List.of("IGUI_Key_LSHIFT", "IGUI_mouse_btn_0"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry LIGHTS = SurvivalGuideEntry.registerBase("lights", "interactable", List.of("IGUI_mouse_btn_0"), List.of("<JOYPAD:AButton,28,28>"));
    public static final SurvivalGuideEntry MAP = SurvivalGuideEntry.registerBase("map", "interactable", List.of("Map", "<IMAGE:media/ui/Sidebar/48/Map_On_48.png,35,28>"), List.of("<JOYPAD:DPadRight,28,28>", "<IMAGE:media/ui/Sidebar/48/Map_On_48.png,35,28>"));
    public static final SurvivalGuideEntry TELEVISION = SurvivalGuideEntry.registerBase("television", "interactable");
    public static final SurvivalGuideEntry BAD_GASES = SurvivalGuideEntry.registerBase("bad_gases", "interactable");
    public static final SurvivalGuideEntry COMBAT = SurvivalGuideEntry.registerBase("combat");
    public static final SurvivalGuideEntry COMBAT_EQUIP_PRIMARY = SurvivalGuideEntry.registerBase("equip_primary", "combat", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:AButton,28,28>"));
    public static final SurvivalGuideEntry COMBAT_MELEE_ATTACK = SurvivalGuideEntry.registerBase("melee_attack", "combat", List.of("Aim", "Attack/Click", "Melee"), List.of("<JOYPAD:RStick,28,28>", "<JOYPAD:RTrigger,28,28>", "<JOYPAD:RTrigger,28,28>"));
    public static final SurvivalGuideEntry COMBAT_SHOVE = SurvivalGuideEntry.registerBase("shove", "combat", List.of("Melee", "Melee"), List.of("<JOYPAD:LTrigger,28,28>", "<JOYPAD:RTrigger,28,28>"));
    public static final SurvivalGuideEntry STEALTH_KILL = SurvivalGuideEntry.registerBase("stealth_kill", "combat", List.of("Attack/Click"), List.of("<JOYPAD:RTrigger,28,28>"));
    public static final SurvivalGuideEntry COMBAT_SHOOTING = SurvivalGuideEntry.registerBase("shooting", "combat", List.of("Aim", "Attack/Click"), List.of("<JOYPAD:RStick,28,28>", "<JOYPAD:RTrigger,28,28>"));
    public static final SurvivalGuideEntry RELOAD = SurvivalGuideEntry.registerBase("reload", "combat", List.of("ReloadWeapon", "IGUI_mouse_btn_1", "ReloadWeapon"), List.of("<JOYPAD:LBumper,28,28>", "<JOYPAD:AButton,28,28>", "<JOYPAD:LBumper,28,28>"));
    public static final SurvivalGuideEntry ZOMBIE_ATTACKS = SurvivalGuideEntry.registerBase("zombie_attacks", "combat");
    public static final SurvivalGuideEntry SHOUTING = SurvivalGuideEntry.registerBase("shouting", "combat", List.of("Shout"), List.of("<JOYPAD:DPadDown,28,28>", "<IMAGE:media/ui/Traits/trait_talkative.png,28,28>"));
    public static final SurvivalGuideEntry HOTBAR = SurvivalGuideEntry.registerBase("hotbar", "combat", List.of("IGUI_mouse_btn_1", "IGUI_mouse_btn_0"), List.of("<JOYPAD:AButton,28,28>", "<JOYPAD:DPadLeft,28,28>"));
    public static final SurvivalGuideEntry FENCE_DEFENSE = SurvivalGuideEntry.registerBase("fence_defense", "combat");
    public static final SurvivalGuideEntry FOOD_AND_WATER = SurvivalGuideEntry.registerBase("food_and_water");
    public static final SurvivalGuideEntry OPEN_CANS = SurvivalGuideEntry.registerBase("open_cans", "food_and_water", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry FOOD_PREPARATION = SurvivalGuideEntry.registerBase("food_preparation", "food_and_water", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry COOKING = SurvivalGuideEntry.registerBase("cooking", "food_and_water", List.of("<JOYPAD:XButton,28,28>"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry LIQUID = SurvivalGuideEntry.registerBase("liquid", "food_and_water", List.of("IGUI_mouse_btn_1", "<IMAGE:media/ui/survival_guide_spiffo/mood_nauseous.png,28,28>"), List.of("<JOYPAD:XButton,28,28>", "<IMAGE:media/ui/survival_guide_spiffo/mood_nauseous.png,28,28>"));
    public static final SurvivalGuideEntry TREAT_WATER = SurvivalGuideEntry.registerBase("treat_water", "food_and_water");
    public static final SurvivalGuideEntry CHARACTER = SurvivalGuideEntry.registerBase("character");
    public static final SurvivalGuideEntry MOODLES = SurvivalGuideEntry.registerBase("moodles", "character", List.of("<IMAGE:media/ui/survival_guide_spiffo/green_status.png,28,28>", "<IMAGE:media/ui/survival_guide_spiffo/red_status.png,28,28>", "<IMAGE:media/ui/survival_guide_spiffo/mood_dead.png,28,28>"), List.of("<IMAGE:media/ui/survival_guide_spiffo/green_status.png,28,28>", "<IMAGE:media/ui/survival_guide_spiffo/red_status.png,28,28>", "<IMAGE:media/ui/survival_guide_spiffo/mood_dead.png,28,28>"));
    public static final SurvivalGuideEntry EATING_DRINKING = SurvivalGuideEntry.registerBase("eating_drinking", "character", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry FIRST_AID = SurvivalGuideEntry.registerBase("first_aid", "character", List.of("Toggle Health Panel", "<IMAGE:media/ui/Sidebar/48/Heart_On_48.png,35,28>", "IGUI_mouse_btn_1", "<IMAGE:media/ui/survival_guide_spiffo/mood_first_aid.png,28,28>"), List.of("<JOYPAD:Menu,28,28>", "<IMAGE:media/ui/Sidebar/48/Heart_On_48.png,35,28>", "<JOYPAD:LBumper,28,28>", "<JOYPAD:RBumper,28,28>", "<JOYPAD:AButton,28,28>", "<IMAGE:media/ui/survival_guide_spiffo/mood_first_aid.png,28,28>"));
    public static final SurvivalGuideEntry REST = SurvivalGuideEntry.registerBase("rest", "character", List.of("<IMAGE:media/ui/survival_guide_spiffo/mood_sleep.png,28,28>", "<IMAGE:media/ui/survival_guide_spiffo/mood_fatigue.png,28,28>"), List.of("<IMAGE:media/ui/survival_guide_spiffo/mood_sleep.png,28,28>", "<IMAGE:media/ui/survival_guide_spiffo/mood_fatigue.png,28,28>"));
    public static final SurvivalGuideEntry EXERCISE = SurvivalGuideEntry.registerBase("exercise", "character");
    public static final SurvivalGuideEntry SKILL_BOOKS = SurvivalGuideEntry.registerBase("skill_books", "character");
    public static final SurvivalGuideEntry BAD_SMELLS = SurvivalGuideEntry.registerBase("bad_smells", "character", List.of("<IMAGE:media/ui/survival_guide_spiffo/mood_nauseous.png,28,28>"), List.of("<IMAGE:media/ui/survival_guide_spiffo/mood_nauseous.png,28,28>"));
    public static final SurvivalGuideEntry CRAFTING = SurvivalGuideEntry.registerBase("crafting");
    public static final SurvivalGuideEntry CRAFTING_MENU = SurvivalGuideEntry.registerBase("crafting_menu", "crafting", List.of("Crafting UI", "<IMAGE:media/ui/Sidebar/48/Carpentry_On_48.png,35,28>", "IGUI_Key_LSHIFT", "IGUI_mouse_btn_1"), List.of("<JOYPAD:Menu,28,28>", "<IMAGE:media/ui/Sidebar/48/Carpentry_On_48.png,35,28>"));
    public static final SurvivalGuideEntry CRAFTING_INVENTORY = SurvivalGuideEntry.registerBase("crafting_inventory", "crafting", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry CRAFT_ON_SURFACE = SurvivalGuideEntry.registerBase("craft_on_surface", "crafting", List.of("<IMAGE:media/ui/Sidebar/48/Carpentry_On_48.png,35,28>"), List.of("<IMAGE:media/ui/Sidebar/48/Carpentry_On_48.png,35,28>"));
    public static final SurvivalGuideEntry SHEET_ROPES = SurvivalGuideEntry.registerBase("sheet_ropes", "crafting", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry BUILD_MENU = SurvivalGuideEntry.registerBase("build_menu", "crafting", List.of("<IMAGE:media/ui/Sidebar/48/Build_On_48.png,35,28>"), List.of("<JOYPAD:Menu,28,28>", "<IMAGE:media/ui/Sidebar/48/Build_On_48.png,35,28>"));
    public static final SurvivalGuideEntry BARRICADES = SurvivalGuideEntry.registerBase("barricades", "crafting", List.of("<IMAGE:media/ui/Sidebar/48/Build_On_48.png,35,28>", "IGUI_mouse_btn_1"), List.of("<IMAGE:media/ui/Sidebar/48/Build_On_48.png,35,28>", "<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry BUILD_WALLS = SurvivalGuideEntry.registerBase("build_walls", "crafting");
    public static final SurvivalGuideEntry CRAFTING_STATION = SurvivalGuideEntry.registerBase("crafting_station", "crafting");
    public static final SurvivalGuideEntry VEHICLES = SurvivalGuideEntry.registerBase("vehicles");
    public static final SurvivalGuideEntry START_VEHICLE = SurvivalGuideEntry.registerBase("start_vehicle", "vehicles", List.of("Forward", "Forward", "Left", "Backward", "Right"), List.of("<JOYPAD:RTrigger,28,28>", "<JOYPAD:LStick,28,28>"));
    public static final SurvivalGuideEntry VEHICLE_RADIAL_MENU = SurvivalGuideEntry.registerBase("vehicle_radial_menu", "vehicles", List.of("VehicleRadialMenu"), List.of("<JOYPAD:DPadUp,28,28>", "<JOYPAD:LStick,28,28>"));
    public static final SurvivalGuideEntry GAS_REFILL = SurvivalGuideEntry.registerBase("gas_refill", "vehicles", List.of("VehicleRadialMenu"), List.of("<JOYPAD:DPadUp,28,28>", "<JOYPAD:LStick,28,28>"));
    public static final SurvivalGuideEntry MECHANICS_MENU = SurvivalGuideEntry.registerBase("mechanics_menu", "vehicles", List.of("Interact", "VehicleRadialMenu", "<IMAGE:media/ui/vehicles/vehicle_repair.png,28,28>"), List.of("<JOYPAD:AButton,28,28>", "<JOYPAD:DPadUp,28,28>", "<IMAGE:media/ui/vehicles/vehicle_repair.png,28,28>"));
    public static final SurvivalGuideEntry TRAILERS = SurvivalGuideEntry.registerBase("trailers", "vehicles", List.of("VehicleRadialMenu", "<IMAGE:media/ui/ZoomIn.png,28,28>", "<IMAGE:media/ui/ZoomOut.png,28,28>"), List.of("<JOYPAD:DPadUp,28,28>", "<IMAGE:media/ui/ZoomIn.png,28,28>", "<IMAGE:media/ui/ZoomOut.png,28,28>"));
    public static final SurvivalGuideEntry VEHICLE_STORAGE = SurvivalGuideEntry.registerBase("vehicle_storage", "vehicles");
    public static final SurvivalGuideEntry WEATHER = SurvivalGuideEntry.registerBase("weather");
    public static final SurvivalGuideEntry SEASONS_AND_WEATHER = SurvivalGuideEntry.registerBase("seasons_and_weather", "weather");
    public static final SurvivalGuideEntry TEMPERATURE = SurvivalGuideEntry.registerBase("temperature", "weather");
    public static final SurvivalGuideEntry FORAGING_MINING = SurvivalGuideEntry.registerBase("foraging_mining");
    public static final SurvivalGuideEntry FORAGING = SurvivalGuideEntry.registerBase("foraging", "foraging_mining", List.of("<IMAGE:media/ui/Sidebar/48/Search_On_48.png,35,28>"), List.of("<JOYPAD:Menu,28,28>", "<IMAGE:media/ui/Sidebar/48/Search_On_48.png,35,28>", "<JOYPAD:AButton,28,28>"));
    public static final SurvivalGuideEntry MINING = SurvivalGuideEntry.registerBase("mining", "foraging_mining", List.of("Interact"), List.of("<JOYPAD:RTrigger,28,28>"));
    public static final SurvivalGuideEntry FARMING = SurvivalGuideEntry.registerBase("farming");
    public static final SurvivalGuideEntry OPEN_SEEDS = SurvivalGuideEntry.registerBase("open_seeds", "farming", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry DIG_FURROW = SurvivalGuideEntry.registerBase("dig_furrow", "farming", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry SOW_SEEDS = SurvivalGuideEntry.registerBase("sow_seeds", "farming", List.of("IGUI_mouse_btn_1", "Interact"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry HARVESTING = SurvivalGuideEntry.registerBase("harvesting", "farming", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry RANCHING = SurvivalGuideEntry.registerBase("ranching");
    public static final SurvivalGuideEntry ANIMAL_ZONE = SurvivalGuideEntry.registerBase("animal_zone", "ranching", List.of("<IMAGE:media/ui/Sidebar/48/AnimalZone_On_48.png,35,28>"), List.of("<JOYPAD:Menu,28,28>", "<IMAGE:media/ui/Sidebar/48/AnimalZone_On_48.png,35,28>"));
    public static final SurvivalGuideEntry ANIMAL_MENU = SurvivalGuideEntry.registerBase("animal_menu", "ranching", List.of("AnimalRadialMenu", "IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>", "<JOYPAD:DPadUp,28,28>"));
    public static final SurvivalGuideEntry ANIMAL_UPKEEP = SurvivalGuideEntry.registerBase("animal_upkeep", "ranching");
    public static final SurvivalGuideEntry ANIMAL_STRESS = SurvivalGuideEntry.registerBase("animal_stress", "ranching");
    public static final SurvivalGuideEntry ANIMAL_ROPE = SurvivalGuideEntry.registerBase("animal_rope", "ranching", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry ANIMAL_HUTCH = SurvivalGuideEntry.registerBase("animal_hutch", "ranching", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry BUTCHERING = SurvivalGuideEntry.registerBase("butchering", "ranching", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry PETTING = SurvivalGuideEntry.registerBase("petting", "ranching");
    public static final SurvivalGuideEntry FISHING = SurvivalGuideEntry.registerBase("fishing");
    public static final SurvivalGuideEntry FISHING_ZONE = SurvivalGuideEntry.registerBase("fishing_zone", "fishing", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry ADD_BAIT = SurvivalGuideEntry.registerBase("add_bait", "fishing", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry CAST_AND_CATCH = SurvivalGuideEntry.registerBase("cast_and_catch", "fishing", List.of("Aim", "Attack/Click", "Attack/Click"), List.of("<JOYPAD:RStick,28,28>", "<JOYPAD:RTrigger,28,28>", "<JOYPAD:RTrigger,28,28>"));
    public static final SurvivalGuideEntry TRAPPING = SurvivalGuideEntry.registerBase("trapping", "fishing", List.of("IGUI_mouse_btn_1", "IGUI_mouse_btn_1"), List.of("<JOYPAD:AButton,28,28>", "<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry CLEANING = SurvivalGuideEntry.registerBase("cleaning");
    public static final SurvivalGuideEntry BURN_CORPSES = SurvivalGuideEntry.registerBase("burn_corpse", "cleaning", List.of("Interact"), List.of("<JOYPAD:AButton,28,28>"));
    public static final SurvivalGuideEntry CLEANING_AREA = SurvivalGuideEntry.registerBase("cleaning_area", "cleaning", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    public static final SurvivalGuideEntry CLEAN_SELF = SurvivalGuideEntry.registerBase("clean_self", "cleaning");
    public static final SurvivalGuideEntry LAUNDRY = SurvivalGuideEntry.registerBase("laundry", "cleaning");
    public static final SurvivalGuideEntry MULTIPLAYER = SurvivalGuideEntry.registerBase("multiplayer");
    public static final SurvivalGuideEntry ACTIVATE_PVP = SurvivalGuideEntry.registerBase("activate_pvp", "multiplayer", List.of("<IMAGE:media/ui/Sidebar/48/Safety_Tintable_48.png,35,28>"), List.of("<IMAGE:media/ui/Sidebar/48/Safety_Tintable_48.png,35,28>"));
    public static final SurvivalGuideEntry FACTION_MENU = SurvivalGuideEntry.registerBase("faction_menu", "multiplayer", List.of("<IMAGE:media/ui/Sidebar/48/Client_Icon_On_48.png,28,28>"), List.of("<IMAGE:media/ui/Sidebar/48/Client_Icon_On_48.png,28,28>"));
    public static final SurvivalGuideEntry MULTIPLAYER_CHAT = SurvivalGuideEntry.registerBase("multiplayer_chat", "multiplayer", List.of("Toggle chat"), List.of("Toggle chat"));
    public static final SurvivalGuideEntry MEDICAL_CHECK = SurvivalGuideEntry.registerBase("medical_check", "multiplayer", List.of("IGUI_mouse_btn_1"), List.of("<JOYPAD:XButton,28,28>"));
    private final String id;
    private final String title;
    private final String description;
    private final List<String> keys;
    private final List<String> joypadKeys;
    private final String thumbnail;
    private final String video;
    private final String subCategory;
    private String categoryImage;

    private SurvivalGuideEntry(String id, String subCategory, List<String> keys2, List<String> joypadKeys) {
        this.id = id;
        this.title = "SurvivalGuide_%s_title".formatted(id);
        this.subCategory = subCategory;
        this.description = "SurvivalGuide_%s_description".formatted(id);
        if (!StringUtils.isNullOrEmpty(subCategory)) {
            this.thumbnail = "media/ui/playstyleIcons/%s.png".formatted(id);
            this.video = "survival_guide/sg_%s.bik".formatted(id);
        } else {
            this.categoryImage = "SurvivalGuide_%s_category_image".formatted(id);
            this.thumbnail = "";
            this.video = "";
        }
        this.keys = keys2;
        this.joypadKeys = joypadKeys;
    }

    public static SurvivalGuideEntry get(ResourceLocation id) {
        return Registries.SURVIVAL_GUIDE_ENTRY.get(id);
    }

    public static List<SurvivalGuideEntry> getAll() {
        return Registries.SURVIVAL_GUIDE_ENTRY.values();
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public String getVideo() {
        return this.video;
    }

    public String getSubCategory() {
        return this.subCategory;
    }

    public List<String> getKeys() {
        return this.keys;
    }

    public List<String> getJoypadKeys() {
        return this.joypadKeys;
    }

    public @Nullable String getText(boolean hasJoystick) {
        String text = hasJoystick ? Translator.getTextOrNull(this.description + "_joypad", this.joypadKeys.stream().map(SurvivalGuideEntry::getKeyName).toArray()) : Translator.getText(this.description, this.keys.stream().map(SurvivalGuideEntry::getKeyName).toArray());
        if (text == null || text.equals("<IGNORE>")) {
            return null;
        }
        return text;
    }

    private static String getKeyName(String key) {
        if (key.contains("IGUI_")) {
            return Translator.getText(key, new Object[0]);
        }
        if (!key.contains("<JOYPAD") && !key.contains("<IMAGE")) {
            Core core = Core.getInstance();
            Object keyTxt = Input.getKeyName(core.getKey(key));
            int altKey = core.getAltKey(key);
            if (altKey > 0) {
                keyTxt = (String)keyTxt + " " + Translator.getText("ContextMenu_or", new Object[0]) + " " + Input.getKeyName(altKey);
            }
            return keyTxt;
        }
        return key;
    }

    public String toString() {
        return this.id;
    }

    public static SurvivalGuideEntry register(String id, String subCategory, List<String> keys2, List<String> joypadKeys) {
        return SurvivalGuideEntry.register(false, id, subCategory, keys2, joypadKeys);
    }

    private static SurvivalGuideEntry registerBase(String id) {
        return SurvivalGuideEntry.registerBase(id, null);
    }

    private static SurvivalGuideEntry registerBase(String id, String subCategory) {
        return SurvivalGuideEntry.register(true, id, subCategory, Collections.emptyList(), Collections.emptyList());
    }

    private static SurvivalGuideEntry registerBase(String id, String subCategory, List<String> keys2, List<String> joypadKeys) {
        return SurvivalGuideEntry.register(true, id, subCategory, keys2, joypadKeys);
    }

    private static SurvivalGuideEntry register(boolean allowDefaultNamespace, String id, String subCategory, List<String> keys2, List<String> joypadKeys) {
        return Registries.SURVIVAL_GUIDE_ENTRY.register(RegistryReset.createLocation(id, allowDefaultNamespace), new SurvivalGuideEntry(id, subCategory, keys2, joypadKeys));
    }

    public String getCategoryImage() {
        return this.categoryImage;
    }

    static {
        if (Core.IS_DEV) {
            for (SurvivalGuideEntry entry : Registries.SURVIVAL_GUIDE_ENTRY) {
                TranslationKeyValidator.of(entry.title);
                if (StringUtils.isNullOrEmpty(entry.getSubCategory())) continue;
                TranslationKeyValidator.of(entry.description);
            }
        }
    }
}

