/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.AlarmClockClothingItemBuilder;
import generation.builders.AlarmClockItemBuilder;
import generation.builders.AnimalItemBuilder;
import generation.builders.ClothingItemBuilder;
import generation.builders.ComponentBuilder;
import generation.builders.ContainerItemBuilder;
import generation.builders.DrainableItemBuilder;
import generation.builders.EvolvedRecipeHelper;
import generation.builders.FoodItemBuilder;
import generation.builders.KeyItemBuilder;
import generation.builders.LiteratureItemBuilder;
import generation.builders.MapItemBuilder;
import generation.builders.MoveableItemBuilder;
import generation.builders.RadioItemBuilder;
import generation.builders.WeaponItemBuilder;
import generation.builders.WeaponPartItemBuilder;
import generation.builders.Writeable;
import generation.builders.validation.AnimationXmlValidator;
import generation.builders.validation.ClothingItemXmlValidator;
import generation.builders.validation.SerializableMethod;
import generation.builders.validation.SoundParameterValidator;
import generation.builders.validation.TranslationKeyValidator;
import java.util.Arrays;
import zombie.characterTextures.BloodClothingType;
import zombie.inventory.InventoryItem;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.AmmoType;
import zombie.scripting.objects.AnimalFeedType;
import zombie.scripting.objects.AttachmentType;
import zombie.scripting.objects.BookSubject;
import zombie.scripting.objects.CraftRecipeKey;
import zombie.scripting.objects.CustomContextMenu;
import zombie.scripting.objects.DigType;
import zombie.scripting.objects.EatType;
import zombie.scripting.objects.EntityKey;
import zombie.scripting.objects.FoodType;
import zombie.scripting.objects.ItemBodyLocation;
import zombie.scripting.objects.ItemDisplayCategory;
import zombie.scripting.objects.ItemFabricType;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemTag;
import zombie.scripting.objects.ItemType;
import zombie.scripting.objects.LearnedRecipeConstantKey;
import zombie.scripting.objects.MagazineSubject;
import zombie.scripting.objects.MetaRecipe;
import zombie.scripting.objects.ModelKey;
import zombie.scripting.objects.PourType;
import zombie.scripting.objects.ReadType;
import zombie.scripting.objects.SeasonRecipe;
import zombie.scripting.objects.SoundKey;
import zombie.scripting.objects.SoundMapKey;
import zombie.scripting.objects.SwingAnim;

public class ItemBuilder<T extends ItemBuilder<T>>
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<ItemType> itemType = this.property("ItemType");
    private final Writeable.Property<Boolean> activatedItem = this.property("ActivatedItem");
    private final Writeable.ListProperty<ComponentBuilder> addComponent = this.listProperty("component", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<SoundKey> aimReleaseSound = this.property("AimReleaseSound");
    private final Writeable.Property<Integer> aimingtime = this.property("Aimingtime");
    private final Writeable.Property<SoundKey> alarmSound = this.property("AlarmSound");
    private final Writeable.Property<Float> alcoholPower = this.property("AlcoholPower");
    private final Writeable.Property<Boolean> alcoholic = this.property("Alcoholic");
    private final Writeable.Property<Boolean> alwaysWelcomeGift = this.property("AlwaysWelcomeGift");
    private final Writeable.Property<AmmoType> ammoType = this.property("AmmoType");
    private final Writeable.Property<AnimalFeedType> animalFeedType = this.property("AnimalFeedType");
    private final Writeable.Property<String> attachmentReplacement = this.property("AttachmentReplacement");
    private final Writeable.Property<AttachmentType> attachmentType = this.property("AttachmentType");
    private final Writeable.ListProperty<AttachmentType> attachmentsProvided = this.listProperty("AttachmentsProvided", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Float> bandagePower = this.property("BandagePower");
    private final Writeable.ListProperty<BloodClothingType> bloodLocation = this.listProperty("BloodLocation", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<ItemBodyLocation> bodyLocation = this.property("BodyLocation");
    private final Writeable.ListProperty<BookSubject> bookSubject = this.listProperty("book_subject", ";", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<Integer> boredomChange = this.property("BoredomChange");
    private final Writeable.Property<Integer> brakeForce = this.property("brakeForce");
    private final Writeable.Property<SoundKey> breakSound = this.property("BreakSound");
    private final Writeable.Property<SoundKey> bringToBearSound = this.property("BringToBearSound");
    private final Writeable.Property<SoundKey> bulletHitArmourSound = this.property("BulletHitArmourSound");
    private final Writeable.Property<Float> calories = this.property("Calories");
    private final Writeable.Property<Boolean> canBandage = this.property("CanBandage");
    private final Writeable.Property<ItemBodyLocation> canBeEquipped = this.property("CanBeEquipped");
    private final Writeable.Property<Boolean> canBeRemote = this.property("CanBeRemote");
    private final Writeable.Property<Boolean> canHaveHoles = this.property("CanHaveHoles");
    private final Writeable.Property<Boolean> canStack = this.property("CanStack");
    private final Writeable.Property<Boolean> canStoreWater = this.property("CanStoreWater");
    private final Writeable.Property<Boolean> cannedFood = this.property("CannedFood");
    private final Writeable.Property<Boolean> cantBeFrozen = this.property("CantBeFrozen");
    private final Writeable.Property<Boolean> cantEat = this.property("CantEat");
    private final Writeable.Property<Integer> capacity = this.property("Capacity");
    private final Writeable.Property<Float> carbohydrates = this.property("Carbohydrates");
    private final Writeable.Property<Integer> chanceToFall = this.property("ChanceToFall");
    private final Writeable.Property<Integer> chanceToSpawnDamaged = this.property("ChanceToSpawnDamaged");
    private final Writeable.Property<String> closeKillMove = this.property("CloseKillMove");
    private final Writeable.Property<String> clothingExtraSubmenu = this.property("ClothingExtraSubmenu");
    private final Writeable.Property<ClothingItemXmlValidator> clothingItem = this.property("ClothingItem");
    private final Writeable.ListProperty<ItemKey> clothingItemExtra = this.listProperty("ClothingItemExtra", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.ListProperty<TranslationKeyValidator> clothingItemExtraOption = this.listProperty("ClothingItemExtraOption", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Integer> colorBlue = this.property("ColorBlue");
    private final Writeable.Property<Integer> colorGreen = this.property("ColorGreen");
    private final Writeable.Property<Integer> colorRed = this.property("ColorRed");
    private final Writeable.Property<Float> combatSpeedModifier = this.property("CombatSpeedModifier");
    private final Writeable.Property<Boolean> conditionAffectsCapacity = this.property("ConditionAffectsCapacity");
    private final Writeable.Property<Integer> conditionLowerChanceOneIn = this.property("ConditionLowerChanceOneIn");
    private final Writeable.Property<Float> conditionLowerOffroad = this.property("ConditionLowerOffroad");
    private final Writeable.Property<Float> conditionLowerStandard = this.property("ConditionLowerStandard");
    private final Writeable.Property<Integer> conditionMax = this.property("ConditionMax");
    private final Writeable.Property<String> consolidateOption = this.property("ConsolidateOption");
    private final Writeable.Property<String> containerName = this.property("ContainerName");
    private final Writeable.Property<SoundKey> cookingSound = this.property("CookingSound");
    private final Writeable.Property<Float> corpseSicknessDefense = this.property("CorpseSicknessDefense");
    private final Writeable.Property<Boolean> cosmetic = this.property("Cosmetic");
    private final Writeable.Property<Integer> count = this.property("count");
    private final Writeable.Property<CustomContextMenu> customContextMenu = this.property("CustomContextMenu");
    private final Writeable.Property<SoundKey> customDrinkSound = this.property("CustomDrinkSound");
    private final Writeable.Property<SoundKey> customEatSound = this.property("CustomEatSound");
    private final Writeable.Property<SoundKey> damagedSound = this.property("DamagedSound");
    private final Writeable.Property<DigType> digType = this.property("DigType");
    private final Writeable.Property<Boolean> disappearOnUse = this.property("DisappearOnUse");
    private final Writeable.Property<Float> discomfortModifier = this.property("DiscomfortModifier");
    private final Writeable.Property<ItemDisplayCategory> displayCategory = this.property("DisplayCategory");
    private final Writeable.Property<String> displayName = this.property("DisplayName");
    private final Writeable.Property<SoundKey> dropSound = this.property("DropSound");
    private final Writeable.Property<EatType> eatType = this.property("EatType");
    private final Writeable.Property<Integer> eattime = this.property("Eattime");
    private final Writeable.Property<SoundKey> ejectAmmoSound = this.property("EjectAmmoSound");
    private final Writeable.Property<SoundKey> ejectAmmoStartSound = this.property("EjectAmmoStartSound");
    private final Writeable.Property<SoundKey> ejectAmmoStopSound = this.property("EjectAmmoStopSound");
    private final Writeable.Property<Float> engineLoudness = this.property("engineLoudness");
    private final Writeable.Property<SoundKey> equipSound = this.property("EquipSound");
    private final Writeable.Property<Boolean> equippedNoSprint = this.property("EquippedNoSprint");
    private final Writeable.ListProperty<EvolvedRecipeHelper> evolvedRecipe = this.listProperty("EvolvedRecipe", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<String> evolvedRecipeName = this.property("EvolvedRecipeName");
    private final Writeable.Property<SoundKey> explosionSound = this.property("ExplosionSound");
    private final Writeable.Property<ItemFabricType> fabricType = this.property("FabricType");
    private final Writeable.Property<Float> fatigueChange = this.property("fatigueChange");
    private final Writeable.Property<SoundKey> fillFromDispenserSound = this.property("FillFromDispenserSound");
    private final Writeable.Property<SoundKey> fillFromLakeSound = this.property("FillFromLakeSound");
    private final Writeable.Property<SoundKey> fillFromTapSound = this.property("FillFromTapSound");
    private final Writeable.Property<SoundKey> fillFromToiletSound = this.property("FillFromToiletSound");
    private final Writeable.Property<Float> fireFuelRatio = this.property("FireFuelRatio");
    private final Writeable.Property<Boolean> fishingLure = this.property("FishingLure");
    private final Writeable.Property<String> fluid = this.property("fluid");
    private final Writeable.Property<FoodType> foodType = this.property("FoodType");
    private final Writeable.ListProperty<ItemKey> gunType = this.listProperty("GunType", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Integer> headCondition = this.property("HeadCondition");
    private final Writeable.Property<Float> headConditionLowerChanceMultiplier = this.property("HeadConditionLowerChanceMultiplier");
    private final Writeable.Property<Integer> headConditionMax = this.property("HeadConditionMax");
    private final Writeable.Property<Float> hearingModifier = this.property("HearingModifier");
    private final Writeable.Property<Boolean> hidden = this.property("hidden");
    private final Writeable.Property<Float> hungerChange = this.property("HungerChange");
    private final Writeable.Property<String> icon = this.property("Icon");
    private final Writeable.Property<String> iconColorMask = this.property("IconColorMask");
    private final Writeable.Property<String> iconFluidMask = this.property("IconFluidMask");
    private final Writeable.ListProperty<String> iconsForTexture = this.listProperty("IconsForTexture", ";", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<SoundKey> insertAmmoSound = this.property("InsertAmmoSound");
    private final Writeable.Property<SoundKey> insertAmmoStartSound = this.property("InsertAmmoStartSound");
    private final Writeable.Property<SoundKey> insertAmmoStopSound = this.property("InsertAmmoStopSound");
    private final Writeable.Property<Float> insulation = this.property("Insulation");
    private final Writeable.Property<Boolean> isCookable = this.property("IsCookable");
    private final Writeable.Property<Boolean> isDung = this.property("IsDung");
    private final Writeable.Property<Boolean> isWaterSource = this.property("IsWaterSource");
    private final Writeable.Property<ItemKey> itemAfterCleaning = this.property("ItemAfterCleaning");
    private final Writeable.Property<ItemKey> itemWhenDry = this.property("ItemWhenDry");
    private final Writeable.Property<Boolean> keepOnDeplete = this.property("KeepOnDeplete");
    private final Writeable.Property<Integer> lightDistance = this.property("LightDistance");
    private final Writeable.Property<Float> lightStrength = this.property("LightStrength");
    private final Writeable.Property<Float> lipids = this.property("Lipids");
    private final Writeable.Property<Float> lowLightBonus = this.property("LowLightBonus");
    private final Writeable.Property<String> makeUpType = this.property("MakeUpType");
    private final Writeable.Property<Boolean> manuallyRemoveSpentRounds = this.property("ManuallyRemoveSpentRounds");
    private final Writeable.Property<Integer> maxAmmo = this.property("MaxAmmo");
    private final Writeable.Property<Integer> maxCapacity = this.property("MaxCapacity");
    private final Writeable.Property<Float> maxItemSize = this.property("MaxItemSize");
    private final Writeable.Property<Boolean> mechanicsItem = this.property("MechanicsItem");
    private final Writeable.Property<String> mediaCategory = this.property("MediaCategory");
    private final Writeable.Property<Boolean> medical = this.property("Medical");
    private final Writeable.Property<Float> metalValue = this.property("MetalValue");
    private final Writeable.Property<Boolean> needToBeClosedOnceReload = this.property("needtobeclosedoncereload");
    private final Writeable.Property<Integer> noiseDuration = this.property("NoiseDuration");
    private final Writeable.Property<String> onBreak = this.property("OnBreak");
    private final Writeable.Property<String> onCreate = this.property("OnCreate");
    private final Writeable.Property<CraftRecipeKey> openingRecipe = this.property("OpeningRecipe");
    private final Writeable.Property<CraftRecipeKey> doubleClickRecipe = this.property("DoubleClickRecipe");
    private final Writeable.Property<Integer> originX = this.property("OriginX");
    private final Writeable.Property<Integer> originY = this.property("OriginY");
    private final Writeable.Property<Integer> originZ = this.property("originZ");
    private final Writeable.Property<Boolean> packaged = this.property("Packaged");
    private final Writeable.Property<SoundKey> placeMultipleSound = this.property("PlaceMultipleSound");
    private final Writeable.Property<SoundKey> placeOneSound = this.property("PlaceOneSound");
    private final Writeable.Property<PourType> pourType = this.property("PourType");
    private final Writeable.Property<String> primaryAnimMask = this.property("primaryAnimMask");
    private final Writeable.Property<Boolean> protectFromRainWhenEquipped = this.property("ProtectFromRainWhenEquipped");
    private final Writeable.Property<Float> proteins = this.property("Proteins");
    private final Writeable.Property<Float> rainFactor = this.property("RainFactor");
    private final Writeable.Property<ReadType> readType = this.property("ReadType");
    private final Writeable.Property<Float> reduceInfectionPower = this.property("ReduceInfectionPower");
    private final Writeable.Property<Boolean> remoteController = this.property("RemoteController");
    private final Writeable.Property<Integer> remoteRange = this.property("RemoteRange");
    private final Writeable.Property<Boolean> removeOnBroken = this.property("RemoveOnBroken");
    private final Writeable.Property<Boolean> removeUnhappinessWhenCooked = this.property("RemoveUnhappinessWhenCooked");
    private final Writeable.Property<ClothingMask> replaceInPrimaryHand = this.property("ReplaceInPrimaryHand");
    private final Writeable.Property<ClothingMask> replaceInSecondHand = this.property("ReplaceInSecondHand");
    private final Writeable.Property<ItemKey> replaceOnUse = this.property("ReplaceOnUse");
    private final Writeable.Property<ItemKey> replaceOnExtinguish = this.property("ReplaceOnExtinguish");
    private final Writeable.ListProperty<ItemKey> requireInHandOrInventory = this.listProperty("RequireInHandOrInventory", "/", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Boolean> requiresEquippedBothHands = this.property("RequiresEquippedBothHands");
    private final Writeable.ListProperty<CraftRecipeKey> researchablerecipes = this.listProperty("Researchablerecipes", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Float> runSpeedModifier = this.property("RunSpeedModifier");
    private final Writeable.Property<Float> scaleWorldIcon = this.property("ScaleWorldIcon");
    private final Writeable.Property<String> secondaryAnimMask = this.property("secondaryAnimMask");
    private final Writeable.Property<Float> sharpness = this.property("Sharpness");
    private final Writeable.Property<Float> shoutMultiplier = this.property("ShoutMultiplier");
    private final Writeable.Property<SoundKey> shoutType = this.property("ShoutType");
    private final Writeable.ListProperty<String> soundMap = this.listProperty("SoundMap", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<SoundParameterValidator> soundParameter = this.property("SoundParameter");
    private final Writeable.Property<Integer> soundRadius = this.property("SoundRadius");
    private final Writeable.Property<Integer> soundVolume = this.property("SoundVolume");
    private final Writeable.Property<ItemKey> spawnWith = this.property("SpawnWith");
    private final Writeable.Property<Boolean> spice = this.property("Spice");
    private final Writeable.Property<ModelKey> staticModel = this.property("StaticModel");
    private final Writeable.ListProperty<ModelKey> staticModelsByIndex = this.listProperty("StaticModelsByIndex", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Float> stopPower = this.property("StopPower");
    private final Writeable.Property<Integer> stressChange = this.property("StressChange");
    private final Writeable.Property<Boolean> survivalGear = this.property("SurvivalGear");
    private final Writeable.Property<Float> suspensionCompression = this.property("suspensionCompression");
    private final Writeable.Property<Float> suspensionDamping = this.property("suspensionDamping");
    private final Writeable.Property<SwingAnim> swingAnim = this.property("SwingAnim");
    private final Writeable.ListProperty<ItemTag> tags = this.listProperty("Tags", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.ListProperty<Object> learnedRecipes = this.listProperty("LearnedRecipes", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<String> tooltip = this.property("Tooltip");
    private final Writeable.Property<Boolean> torchCone = this.property("TorchCone");
    private final Writeable.Property<Float> torchDot = this.property("TorchDot");
    private final Writeable.Property<Boolean> trap = this.property("Trap");
    private final Writeable.Property<Boolean> twoHandWeapon = this.property("TwoHandWeapon");
    private final Writeable.Property<SoundKey> unequipSound = this.property("UnequipSound");
    private final Writeable.Property<Integer> unhappyChange = this.property("UnhappyChange");
    private final Writeable.Property<Boolean> useWhileEquipped = this.property("UseWhileEquipped");
    private final Writeable.Property<Boolean> useWorldItem = this.property("UseWorldItem");
    private final Writeable.Property<Integer> vehicleType = this.property("VehicleType");
    private final Writeable.Property<String> vehiclePartModel = this.property("VehiclePartModel");
    private final Writeable.Property<Float> visionModifier = this.property("VisionModifier");
    private final Writeable.Property<Boolean> visualAid = this.property("VisualAid");
    private final Writeable.Property<SoundKey> weaponHitArmourSound = this.property("WeaponHitArmourSound");
    private final Writeable.Property<Float> weight = this.property("Weight");
    private final Writeable.Property<Float> weightEmpty = this.property("WeightEmpty");
    private final Writeable.Property<Float> weightModifier = this.property("WeightModifier");
    private final Writeable.Property<Integer> weightReduction = this.property("WeightReduction");
    private final Writeable.Property<Boolean> wet = this.property("Wet");
    private final Writeable.Property<Float> wetCooldown = this.property("WetCooldown");
    private final Writeable.Property<Float> wheelFriction = this.property("wheelFriction");
    private final Writeable.Property<Float> windResistance = this.property("WindResistance");
    private final Writeable.Property<ItemKey> withDrainable = this.property("WithDrainable");
    private final Writeable.Property<ItemKey> withoutDrainable = this.property("WithoutDrainable");
    private final Writeable.Property<String> worldObjectSprite = this.property("WorldObjectSprite");
    private final Writeable.Property<Boolean> worldRender = this.property("WorldRender");
    private final Writeable.Property<ModelKey> worldStaticModel = this.property("WorldStaticModel");
    private final Writeable.ListProperty<ModelKey> worldStaticModelsByIndex = this.listProperty("WorldStaticModelsByIndex", ";", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<Integer> foodSicknessChange = this.property("FoodSicknessChange");
    private final Writeable.Property<Integer> inverseCoughProbability = this.property("InverseCoughProbability");
    private final Writeable.Property<Integer> inverseCoughProbabilitySmoker = this.property("InverseCoughProbabilitySmoker");
    private final Writeable.ListProperty<MagazineSubject> magazineSubject = this.listProperty("magazine_subject", ";", Writeable.ListProperty.Flags.KEEP_DUPLICATES);

    public static AlarmClockItemBuilder alarmClock(ItemKey item) {
        return new AlarmClockItemBuilder(item);
    }

    public static AlarmClockClothingItemBuilder alarmClockClothing(ItemKey item) {
        return new AlarmClockClothingItemBuilder(item);
    }

    public static AnimalItemBuilder animal(ItemKey item) {
        return new AnimalItemBuilder(item);
    }

    public static ClothingItemBuilder clothing(ItemKey item) {
        return new ClothingItemBuilder(item);
    }

    public static ContainerItemBuilder container(ItemKey item) {
        return new ContainerItemBuilder(item);
    }

    public static DrainableItemBuilder drainable(ItemKey item) {
        return new DrainableItemBuilder(item);
    }

    public static FoodItemBuilder food(ItemKey item) {
        return new FoodItemBuilder(item);
    }

    public static KeyItemBuilder key(ItemKey item) {
        return new KeyItemBuilder(item);
    }

    public static LiteratureItemBuilder literature(ItemKey item) {
        return new LiteratureItemBuilder(item);
    }

    public static MapItemBuilder map(ItemKey item) {
        return new MapItemBuilder(item);
    }

    public static MoveableItemBuilder moveable(ItemKey item) {
        return new MoveableItemBuilder(item);
    }

    public static <T extends ItemBuilder<T>> ItemBuilder<T> normal(ItemKey item) {
        return new ItemBuilder<T>(item);
    }

    public static RadioItemBuilder radio(ItemKey item) {
        return new RadioItemBuilder(item);
    }

    public static WeaponItemBuilder weapon(ItemKey item) {
        return new WeaponItemBuilder(item);
    }

    public static WeaponPartItemBuilder weaponPart(ItemKey item) {
        return new WeaponPartItemBuilder(item);
    }

    protected ItemBuilder(ItemKey item) {
        super(ScriptType.Item, item.id());
    }

    public T itemType(ItemType itemType) {
        this.itemType.setValue(itemType);
        return (T)this;
    }

    public T activatedItem(boolean activatedItem) {
        this.activatedItem.setValue(activatedItem);
        return (T)this;
    }

    public T addComponent(ComponentBuilder addComponent) {
        this.addComponent.addValues((ComponentBuilder[])new ComponentBuilder[]{addComponent});
        return (T)this;
    }

    public T aimReleaseSound(SoundKey aimReleaseSound) {
        this.aimReleaseSound.setValue(aimReleaseSound);
        return (T)this;
    }

    public T aimingtime(int aimingtime) {
        this.aimingtime.setValue(aimingtime);
        return (T)this;
    }

    public T alarmSound(SoundKey alarmSound) {
        this.alarmSound.setValue(alarmSound);
        return (T)this;
    }

    public T alcoholPower(float alcoholPower) {
        this.alcoholPower.setValue(Float.valueOf(alcoholPower));
        return (T)this;
    }

    public T alcoholic(boolean alcoholic) {
        this.alcoholic.setValue(alcoholic);
        return (T)this;
    }

    public T alwaysWelcomeGift(boolean alwaysWelcomeGift) {
        this.alwaysWelcomeGift.setValue(alwaysWelcomeGift);
        return (T)this;
    }

    public T ammoType(AmmoType ammoType) {
        this.ammoType.setValue(ammoType);
        return (T)this;
    }

    public T animalFeedType(AnimalFeedType animalFeedType) {
        this.animalFeedType.setValue(animalFeedType);
        return (T)this;
    }

    public T attachmentReplacement(String attachmentReplacement) {
        this.attachmentReplacement.setValue(attachmentReplacement);
        return (T)this;
    }

    public T bookSubjects(BookSubject ... bookSubjects) {
        this.bookSubject.addValues((BookSubject[])bookSubjects);
        return (T)this;
    }

    public T attachmentType(AttachmentType attachmentType) {
        this.attachmentType.setValue(attachmentType);
        return (T)this;
    }

    public T attachmentsProvided(AttachmentType ... attachmentsProvided) {
        this.attachmentsProvided.addValues((AttachmentType[])attachmentsProvided);
        return (T)this;
    }

    public T bandagePower(float bandagePower) {
        this.bandagePower.setValue(Float.valueOf(bandagePower));
        return (T)this;
    }

    public T bloodLocation(BloodClothingType ... bloodLocation) {
        this.bloodLocation.addValues((BloodClothingType[])bloodLocation);
        return (T)this;
    }

    public T bodyLocation(ItemBodyLocation bodyLocation) {
        this.bodyLocation.setValue(bodyLocation);
        return (T)this;
    }

    public T boredomChange(int boredomChange) {
        this.boredomChange.setValue(boredomChange);
        return (T)this;
    }

    public T brakeForce(int brakeForce) {
        this.brakeForce.setValue(brakeForce);
        return (T)this;
    }

    public T breakSound(SoundKey breakSound) {
        this.breakSound.setValue(breakSound);
        return (T)this;
    }

    public T bringToBearSound(SoundKey bringToBearSound) {
        this.bringToBearSound.setValue(bringToBearSound);
        return (T)this;
    }

    public T bulletHitArmourSound(SoundKey bulletHitArmourSound) {
        this.bulletHitArmourSound.setValue(bulletHitArmourSound);
        return (T)this;
    }

    public T calories(float calories) {
        this.calories.setValue(Float.valueOf(calories));
        return (T)this;
    }

    public T canBandage(boolean canBandage) {
        this.canBandage.setValue(canBandage);
        return (T)this;
    }

    public T canBeEquipped(ItemBodyLocation canBeEquipped) {
        this.canBeEquipped.setValue(canBeEquipped);
        return (T)this;
    }

    public T canBeRemote(boolean canBeRemote) {
        this.canBeRemote.setValue(canBeRemote);
        return (T)this;
    }

    public T canHaveHoles(boolean canHaveHoles) {
        this.canHaveHoles.setValue(canHaveHoles);
        return (T)this;
    }

    public T canStack(boolean canStack) {
        this.canStack.setValue(canStack);
        return (T)this;
    }

    public T canStoreWater(boolean canStoreWater) {
        this.canStoreWater.setValue(canStoreWater);
        return (T)this;
    }

    public T cannedFood(boolean cannedFood) {
        this.cannedFood.setValue(cannedFood);
        return (T)this;
    }

    public T cantBeFrozen(boolean cantBeFrozen) {
        this.cantBeFrozen.setValue(cantBeFrozen);
        return (T)this;
    }

    public T cantEat(boolean cantEat) {
        this.cantEat.setValue(cantEat);
        return (T)this;
    }

    public T capacity(int capacity) {
        this.capacity.setValue(capacity);
        return (T)this;
    }

    public T carbohydrates(float carbohydrates) {
        this.carbohydrates.setValue(Float.valueOf(carbohydrates));
        return (T)this;
    }

    public T chanceToFall(int chanceToFall) {
        this.chanceToFall.setValue(chanceToFall);
        return (T)this;
    }

    public T chanceToSpawnDamaged(int chanceToSpawnDamaged) {
        this.chanceToSpawnDamaged.setValue(chanceToSpawnDamaged);
        return (T)this;
    }

    public T closeKillMove(String closeKillMove) {
        this.closeKillMove.setValue(closeKillMove);
        return (T)this;
    }

    public T clothingExtraSubmenu(String clothingExtraSubmenu) {
        this.clothingExtraSubmenu.setValue(clothingExtraSubmenu);
        return (T)this;
    }

    public T clothingItem(String clothingItem) {
        this.clothingItem.setValue(ClothingItemXmlValidator.of(clothingItem));
        return (T)this;
    }

    public T clothingItemExtra(ItemKey ... clothingItemExtra) {
        this.clothingItemExtra.addValues((ItemKey[])clothingItemExtra);
        return (T)this;
    }

    public T clothingItemExtraOption(String ... clothingItemExtraOption) {
        this.clothingItemExtraOption.addValues((TranslationKeyValidator[])((TranslationKeyValidator[])Arrays.stream(clothingItemExtraOption).map(v -> TranslationKeyValidator.of("ContextMenu_%s", v)).toArray(TranslationKeyValidator[]::new)));
        return (T)this;
    }

    public T colorBlue(int colorBlue) {
        this.colorBlue.setValue(colorBlue);
        return (T)this;
    }

    public T colorGreen(int colorGreen) {
        this.colorGreen.setValue(colorGreen);
        return (T)this;
    }

    public T colorRed(int colorRed) {
        this.colorRed.setValue(colorRed);
        return (T)this;
    }

    public T combatSpeedModifier(float combatSpeedModifier) {
        this.combatSpeedModifier.setValue(Float.valueOf(combatSpeedModifier));
        return (T)this;
    }

    public T conditionAffectsCapacity(boolean conditionAffectsCapacity) {
        this.conditionAffectsCapacity.setValue(conditionAffectsCapacity);
        return (T)this;
    }

    public T conditionLowerChanceOneIn(int conditionLowerChanceOneIn) {
        this.conditionLowerChanceOneIn.setValue(conditionLowerChanceOneIn);
        return (T)this;
    }

    public T conditionLowerOffroad(float conditionLowerOffroad) {
        this.conditionLowerOffroad.setValue(Float.valueOf(conditionLowerOffroad));
        return (T)this;
    }

    public T conditionLowerStandard(float conditionLowerStandard) {
        this.conditionLowerStandard.setValue(Float.valueOf(conditionLowerStandard));
        return (T)this;
    }

    public T conditionMax(int conditionMax) {
        this.conditionMax.setValue(conditionMax);
        return (T)this;
    }

    public T consolidateOption(String consolidateOption) {
        this.consolidateOption.setValue(consolidateOption);
        return (T)this;
    }

    public T containerName(String containerName) {
        this.containerName.setValue(containerName);
        return (T)this;
    }

    public T cookingSound(SoundKey cookingSound) {
        this.cookingSound.setValue(cookingSound);
        return (T)this;
    }

    public T corpseSicknessDefense(float corpseSicknessDefense) {
        this.corpseSicknessDefense.setValue(Float.valueOf(corpseSicknessDefense));
        return (T)this;
    }

    public T cosmetic(boolean cosmetic) {
        this.cosmetic.setValue(cosmetic);
        return (T)this;
    }

    public T count(int count) {
        this.count.setValue(count);
        return (T)this;
    }

    public T customContextMenu(CustomContextMenu customContextMenu) {
        this.customContextMenu.setValue(customContextMenu);
        return (T)this;
    }

    public T customDrinkSound(SoundKey customDrinkSound) {
        this.customDrinkSound.setValue(customDrinkSound);
        return (T)this;
    }

    public T customEatSound(SoundKey customEatSound) {
        this.customEatSound.setValue(customEatSound);
        return (T)this;
    }

    public T damagedSound(SoundKey damagedSound) {
        this.damagedSound.setValue(damagedSound);
        return (T)this;
    }

    public T digType(DigType digType) {
        this.digType.setValue(digType);
        return (T)this;
    }

    public T disappearOnUse(boolean disappearOnUse) {
        this.disappearOnUse.setValue(disappearOnUse);
        return (T)this;
    }

    public T discomfortModifier(float discomfortModifier) {
        this.discomfortModifier.setValue(Float.valueOf(discomfortModifier));
        return (T)this;
    }

    public T displayCategory(ItemDisplayCategory displayCategory) {
        this.displayCategory.setValue(displayCategory);
        return (T)this;
    }

    public T displayName(String displayName) {
        this.displayName.setValue(displayName);
        return (T)this;
    }

    public T dropSound(SoundKey dropSound) {
        this.dropSound.setValue(dropSound);
        return (T)this;
    }

    public T eatType(EatType eatType) {
        this.eatType.setValue(eatType);
        return (T)this;
    }

    public T eattime(int eattime) {
        this.eattime.setValue(eattime);
        return (T)this;
    }

    public T ejectAmmoSound(SoundKey ejectAmmoSound) {
        this.ejectAmmoSound.setValue(ejectAmmoSound);
        return (T)this;
    }

    public T ejectAmmoStartSound(SoundKey ejectAmmoStartSound) {
        this.ejectAmmoStartSound.setValue(ejectAmmoStartSound);
        return (T)this;
    }

    public T ejectAmmoStopSound(SoundKey ejectAmmoStopSound) {
        this.ejectAmmoStopSound.setValue(ejectAmmoStopSound);
        return (T)this;
    }

    public T engineLoudness(float engineLoudness) {
        this.engineLoudness.setValue(Float.valueOf(engineLoudness));
        return (T)this;
    }

    public T equipSound(SoundKey equipSound) {
        this.equipSound.setValue(equipSound);
        return (T)this;
    }

    public T equippedNoSprint(boolean equippedNoSprint) {
        this.equippedNoSprint.setValue(equippedNoSprint);
        return (T)this;
    }

    public T evolvedRecipe(EvolvedRecipeHelper ... evolvedRecipe) {
        this.evolvedRecipe.addValues((EvolvedRecipeHelper[])evolvedRecipe);
        return (T)this;
    }

    public T evolvedRecipeName(String evolvedRecipeName) {
        this.evolvedRecipeName.setValue(evolvedRecipeName);
        return (T)this;
    }

    public T explosionSound(SoundKey explosionSound) {
        this.explosionSound.setValue(explosionSound);
        return (T)this;
    }

    public T fabricType(ItemFabricType fabricType) {
        this.fabricType.setValue(fabricType);
        return (T)this;
    }

    public T fatigueChange(float fatigueChange) {
        this.fatigueChange.setValue(Float.valueOf(fatigueChange));
        return (T)this;
    }

    public T fillFromDispenserSound(SoundKey fillFromDispenserSound) {
        this.fillFromDispenserSound.setValue(fillFromDispenserSound);
        return (T)this;
    }

    public T fillFromLakeSound(SoundKey fillFromLakeSound) {
        this.fillFromLakeSound.setValue(fillFromLakeSound);
        return (T)this;
    }

    public T fillFromTapSound(SoundKey fillFromTapSound) {
        this.fillFromTapSound.setValue(fillFromTapSound);
        return (T)this;
    }

    public T fillFromToiletSound(SoundKey fillFromToiletSound) {
        this.fillFromToiletSound.setValue(fillFromToiletSound);
        return (T)this;
    }

    public T fireFuelRatio(float fireFuelRatio) {
        this.fireFuelRatio.setValue(Float.valueOf(fireFuelRatio));
        return (T)this;
    }

    public T fishingLure(boolean fishingLure) {
        this.fishingLure.setValue(fishingLure);
        return (T)this;
    }

    public T fluid(String fluid) {
        this.fluid.setValue(fluid);
        return (T)this;
    }

    public T foodType(FoodType foodType) {
        this.foodType.setValue(foodType);
        return (T)this;
    }

    public ItemBuilder<T> gunType(ItemKey ... gunType) {
        for (ItemKey item : gunType) {
            if (item.itemType() == ItemType.WEAPON) continue;
            throw new RuntimeException("Invalid item type: " + String.valueOf(item.itemType()));
        }
        this.gunType.addValues((ItemKey[])gunType);
        return this;
    }

    public T headCondition(int headCondition) {
        this.headCondition.setValue(headCondition);
        return (T)this;
    }

    public T headConditionLowerChanceMultiplier(float headConditionLowerChanceMultiplier) {
        this.headConditionLowerChanceMultiplier.setValue(Float.valueOf(headConditionLowerChanceMultiplier));
        return (T)this;
    }

    public T headConditionMax(int headConditionMax) {
        this.headConditionMax.setValue(headConditionMax);
        return (T)this;
    }

    public T hearingModifier(float hearingModifier) {
        this.hearingModifier.setValue(Float.valueOf(hearingModifier));
        return (T)this;
    }

    public T hidden(boolean hidden) {
        this.hidden.setValue(hidden);
        return (T)this;
    }

    public T hungerChange(float hungerChange) {
        this.hungerChange.setValue(Float.valueOf(hungerChange));
        return (T)this;
    }

    public T icon(String icon) {
        this.icon.setValue(icon);
        return (T)this;
    }

    public T iconColorMask(String iconColorMask) {
        this.iconColorMask.setValue(iconColorMask);
        return (T)this;
    }

    public T iconFluidMask(String iconFluidMask) {
        this.iconFluidMask.setValue(iconFluidMask);
        return (T)this;
    }

    public T iconsForTexture(String ... iconsForTexture) {
        this.iconsForTexture.addValues((String[])iconsForTexture);
        return (T)this;
    }

    public T insertAmmoSound(SoundKey insertAmmoSound) {
        this.insertAmmoSound.setValue(insertAmmoSound);
        return (T)this;
    }

    public T insertAmmoStartSound(SoundKey insertAmmoStartSound) {
        this.insertAmmoStartSound.setValue(insertAmmoStartSound);
        return (T)this;
    }

    public T insertAmmoStopSound(SoundKey insertAmmoStopSound) {
        this.insertAmmoStopSound.setValue(insertAmmoStopSound);
        return (T)this;
    }

    public T insulation(float insulation) {
        this.insulation.setValue(Float.valueOf(insulation));
        return (T)this;
    }

    public T isCookable(boolean isCookable) {
        this.isCookable.setValue(isCookable);
        return (T)this;
    }

    public T isDung(boolean isDung) {
        this.isDung.setValue(isDung);
        return (T)this;
    }

    public T isWaterSource(boolean isWaterSource) {
        this.isWaterSource.setValue(isWaterSource);
        return (T)this;
    }

    public T itemAfterCleaning(ItemKey itemAfterCleaning) {
        this.itemAfterCleaning.setValue(itemAfterCleaning);
        return (T)this;
    }

    public T itemWhenDry(ItemKey itemWhenDry) {
        this.itemWhenDry.setValue(itemWhenDry);
        return (T)this;
    }

    public T keepOnDeplete(boolean keepOnDeplete) {
        this.keepOnDeplete.setValue(keepOnDeplete);
        return (T)this;
    }

    public T lightDistance(int lightDistance) {
        this.lightDistance.setValue(lightDistance);
        return (T)this;
    }

    public T lightStrength(float lightStrength) {
        this.lightStrength.setValue(Float.valueOf(lightStrength));
        return (T)this;
    }

    public T lipids(float lipids) {
        this.lipids.setValue(Float.valueOf(lipids));
        return (T)this;
    }

    public T lowLightBonus(float lowLightBonus) {
        this.lowLightBonus.setValue(Float.valueOf(lowLightBonus));
        return (T)this;
    }

    public T makeUpType(String makeUpType) {
        this.makeUpType.setValue(makeUpType);
        return (T)this;
    }

    public T manuallyRemoveSpentRounds(boolean manuallyRemoveSpentRounds) {
        this.manuallyRemoveSpentRounds.setValue(manuallyRemoveSpentRounds);
        return (T)this;
    }

    public T maxAmmo(int maxAmmo) {
        this.maxAmmo.setValue(maxAmmo);
        return (T)this;
    }

    public T maxCapacity(int maxCapacity) {
        this.maxCapacity.setValue(maxCapacity);
        return (T)this;
    }

    public T maxItemSize(float maxItemSize) {
        this.maxItemSize.setValue(Float.valueOf(maxItemSize));
        return (T)this;
    }

    public T mechanicsItem(boolean mechanicsItem) {
        this.mechanicsItem.setValue(mechanicsItem);
        return (T)this;
    }

    public T mediaCategory(String mediaCategory) {
        this.mediaCategory.setValue(mediaCategory);
        return (T)this;
    }

    public T medical(boolean medical) {
        this.medical.setValue(medical);
        return (T)this;
    }

    public T metalValue(float metalValue) {
        this.metalValue.setValue(Float.valueOf(metalValue));
        return (T)this;
    }

    public T needToBeClosedOnceReload(boolean needToBeClosedOnceReload) {
        this.needToBeClosedOnceReload.setValue(needToBeClosedOnceReload);
        return (T)this;
    }

    public T noiseDuration(int noiseDuration) {
        this.noiseDuration.setValue(noiseDuration);
        return (T)this;
    }

    public T onBreak(String onBreak) {
        this.onBreak.setValue(onBreak);
        return (T)this;
    }

    public T onCreate(String onCreate) {
        this.onCreate.setValue(onCreate);
        return (T)this;
    }

    public <E extends InventoryItem> T onCreate(SerializableMethod.Consumer<E> onCreate) {
        this.onCreate.setValue(SerializableMethod.asLuaString(onCreate));
        return (T)this;
    }

    public T openingRecipe(CraftRecipeKey openingRecipe) {
        this.openingRecipe.setValue(openingRecipe);
        return (T)this;
    }

    public T doubleClickRecipe(CraftRecipeKey doubleClickRecipe) {
        this.doubleClickRecipe.setValue(doubleClickRecipe);
        return (T)this;
    }

    public T originX(int originX) {
        this.originX.setValue(originX);
        return (T)this;
    }

    public T originY(int originY) {
        this.originY.setValue(originY);
        return (T)this;
    }

    public T originZ(int originZ) {
        this.originZ.setValue(originZ);
        return (T)this;
    }

    public T packaged(boolean packaged) {
        this.packaged.setValue(packaged);
        return (T)this;
    }

    public T placeMultipleSound(SoundKey placeMultipleSound) {
        this.placeMultipleSound.setValue(placeMultipleSound);
        return (T)this;
    }

    public T placeOneSound(SoundKey placeOneSound) {
        this.placeOneSound.setValue(placeOneSound);
        return (T)this;
    }

    public T pourType(PourType pourType) {
        this.pourType.setValue(pourType);
        return (T)this;
    }

    public T primaryAnimMask(String primaryAnimMask) {
        this.primaryAnimMask.setValue(primaryAnimMask);
        return (T)this;
    }

    public T protectFromRainWhenEquipped(boolean protectFromRainWhenEquipped) {
        this.protectFromRainWhenEquipped.setValue(protectFromRainWhenEquipped);
        return (T)this;
    }

    public T proteins(float proteins) {
        this.proteins.setValue(Float.valueOf(proteins));
        return (T)this;
    }

    public T rainFactor(float rainFactor) {
        this.rainFactor.setValue(Float.valueOf(rainFactor));
        return (T)this;
    }

    public T readType(ReadType readType) {
        this.readType.setValue(readType);
        return (T)this;
    }

    public T reduceInfectionPower(float reduceInfectionPower) {
        this.reduceInfectionPower.setValue(Float.valueOf(reduceInfectionPower));
        return (T)this;
    }

    public T remoteController(boolean remoteController) {
        this.remoteController.setValue(remoteController);
        return (T)this;
    }

    public T remoteRange(int remoteRange) {
        this.remoteRange.setValue(remoteRange);
        return (T)this;
    }

    public T removeOnBroken(boolean removeOnBroken) {
        this.removeOnBroken.setValue(removeOnBroken);
        return (T)this;
    }

    public T removeUnhappinessWhenCooked(boolean removeUnhappinessWhenCooked) {
        this.removeUnhappinessWhenCooked.setValue(removeUnhappinessWhenCooked);
        return (T)this;
    }

    public T replaceInPrimaryHand(String clothing, String mask) {
        this.replaceInPrimaryHand.setValue(ClothingMask.of(clothing, mask));
        return (T)this;
    }

    public T replaceInSecondHand(String clothing, String mask) {
        this.replaceInSecondHand.setValue(ClothingMask.of(clothing, mask));
        return (T)this;
    }

    public T replaceOnUse(ItemKey replaceOnUse) {
        this.replaceOnUse.setValue(replaceOnUse);
        return (T)this;
    }

    public T replaceOnExtinguish(ItemKey replaceOnExtinguish) {
        this.replaceOnExtinguish.setValue(replaceOnExtinguish);
        return (T)this;
    }

    public T requireInHandOrInventory(ItemKey ... requireInHandOrInventory) {
        this.requireInHandOrInventory.addValues((ItemKey[])requireInHandOrInventory);
        return (T)this;
    }

    public T requiresEquippedBothHands(boolean requiresEquippedBothHands) {
        this.requiresEquippedBothHands.setValue(requiresEquippedBothHands);
        return (T)this;
    }

    public T researchablerecipes(CraftRecipeKey ... researchablerecipes) {
        this.researchablerecipes.addValues((CraftRecipeKey[])researchablerecipes);
        return (T)this;
    }

    public T runSpeedModifier(float runSpeedModifier) {
        this.runSpeedModifier.setValue(Float.valueOf(runSpeedModifier));
        return (T)this;
    }

    public T scaleWorldIcon(float scaleWorldIcon) {
        this.scaleWorldIcon.setValue(Float.valueOf(scaleWorldIcon));
        return (T)this;
    }

    public T secondaryAnimMask(String secondaryAnimMask) {
        this.secondaryAnimMask.setValue(secondaryAnimMask);
        return (T)this;
    }

    public T sharpness(float sharpness) {
        this.sharpness.setValue(Float.valueOf(sharpness));
        return (T)this;
    }

    public T shoutMultiplier(float shoutMultiplier) {
        this.shoutMultiplier.setValue(Float.valueOf(shoutMultiplier));
        return (T)this;
    }

    public T shoutType(SoundKey shoutType) {
        this.shoutType.setValue(shoutType);
        return (T)this;
    }

    public T soundMap(String soundMap) {
        this.soundMap.addValues((String[])new String[]{soundMap});
        return (T)this;
    }

    public T soundMap(SoundMapKey key, SoundKey value) {
        this.soundMap.addValues((String[])new String[]{key.toString() + " " + value.toString()});
        return (T)this;
    }

    public <R extends Enum<R>> T soundParameter(String key, R value) {
        this.soundParameter.setValue(SoundParameterValidator.of(key, value));
        return (T)this;
    }

    public T soundRadius(int soundRadius) {
        this.soundRadius.setValue(soundRadius);
        return (T)this;
    }

    public T soundVolume(int soundVolume) {
        this.soundVolume.setValue(soundVolume);
        return (T)this;
    }

    public T spawnWith(ItemKey spawnWith) {
        this.spawnWith.setValue(spawnWith);
        return (T)this;
    }

    public T spice(boolean spice) {
        this.spice.setValue(spice);
        return (T)this;
    }

    public T staticModel(ModelKey staticModel) {
        this.staticModel.setValue(staticModel);
        return (T)this;
    }

    public T staticModelsByIndex(ModelKey ... staticModelsByIndex) {
        this.staticModelsByIndex.addValues((ModelKey[])staticModelsByIndex);
        return (T)this;
    }

    public T stopPower(float stopPower) {
        this.stopPower.setValue(Float.valueOf(stopPower));
        return (T)this;
    }

    public T stressChange(int stressChange) {
        this.stressChange.setValue(stressChange);
        return (T)this;
    }

    public T survivalGear(boolean survivalGear) {
        this.survivalGear.setValue(survivalGear);
        return (T)this;
    }

    public T suspensionCompression(float suspensionCompression) {
        this.suspensionCompression.setValue(Float.valueOf(suspensionCompression));
        return (T)this;
    }

    public T suspensionDamping(float suspensionDamping) {
        this.suspensionDamping.setValue(Float.valueOf(suspensionDamping));
        return (T)this;
    }

    public T swingAnim(SwingAnim swingAnim) {
        this.swingAnim.setValue(swingAnim);
        return (T)this;
    }

    public T tags(ItemTag ... tags) {
        this.tags.addValues((ItemTag[])tags);
        return (T)this;
    }

    public T learnedRecipes(Object ... learnedRecipes) {
        for (Object o : learnedRecipes) {
            if (o instanceof CraftRecipeKey || o instanceof SeasonRecipe || o instanceof EntityKey || o instanceof MetaRecipe || o instanceof LearnedRecipeConstantKey) continue;
            System.out.println("Unknown learnedRecipe: " + String.valueOf(o));
        }
        this.learnedRecipes.addValues((Object[])learnedRecipes);
        return (T)this;
    }

    public T tooltip(String tooltip) {
        this.tooltip.setValue(tooltip);
        return (T)this;
    }

    public T torchCone(boolean torchCone) {
        this.torchCone.setValue(torchCone);
        return (T)this;
    }

    public T torchDot(float torchDot) {
        this.torchDot.setValue(Float.valueOf(torchDot));
        return (T)this;
    }

    public T trap(boolean trap) {
        this.trap.setValue(trap);
        return (T)this;
    }

    public T twoHandWeapon(boolean twoHandWeapon) {
        this.twoHandWeapon.setValue(twoHandWeapon);
        return (T)this;
    }

    public T unequipSound(SoundKey unequipSound) {
        this.unequipSound.setValue(unequipSound);
        return (T)this;
    }

    public T unhappyChange(int unhappyChange) {
        this.unhappyChange.setValue(unhappyChange);
        return (T)this;
    }

    public T useWhileEquipped(boolean useWhileEquipped) {
        this.useWhileEquipped.setValue(useWhileEquipped);
        return (T)this;
    }

    public T useWorldItem(boolean useWorldItem) {
        this.useWorldItem.setValue(useWorldItem);
        return (T)this;
    }

    public T vehicleType(int vehicleType) {
        this.vehicleType.setValue(vehicleType);
        return (T)this;
    }

    public T vehiclePartModel(String vehiclePartModel) {
        this.vehiclePartModel.setValue(vehiclePartModel);
        return (T)this;
    }

    public T visionModifier(float visionModifier) {
        this.visionModifier.setValue(Float.valueOf(visionModifier));
        return (T)this;
    }

    public T visualAid(boolean visualAid) {
        this.visualAid.setValue(visualAid);
        return (T)this;
    }

    public T weaponHitArmourSound(SoundKey weaponHitArmourSound) {
        this.weaponHitArmourSound.setValue(weaponHitArmourSound);
        return (T)this;
    }

    public T weight(float weight) {
        this.weight.setValue(Float.valueOf(weight));
        return (T)this;
    }

    public T weightEmpty(float weightEmpty) {
        this.weightEmpty.setValue(Float.valueOf(weightEmpty));
        return (T)this;
    }

    public T weightModifier(float weightModifier) {
        this.weightModifier.setValue(Float.valueOf(weightModifier));
        return (T)this;
    }

    public T weightReduction(int weightReduction) {
        this.weightReduction.setValue(weightReduction);
        return (T)this;
    }

    public T wet(boolean wet) {
        this.wet.setValue(wet);
        return (T)this;
    }

    public T wetCooldown(float wetCooldown) {
        this.wetCooldown.setValue(Float.valueOf(wetCooldown));
        return (T)this;
    }

    public T wheelFriction(float wheelFriction) {
        this.wheelFriction.setValue(Float.valueOf(wheelFriction));
        return (T)this;
    }

    public T windResistance(float windResistance) {
        this.windResistance.setValue(Float.valueOf(windResistance));
        return (T)this;
    }

    public T withDrainable(ItemKey withDrainable) {
        this.withDrainable.setValue(withDrainable);
        return (T)this;
    }

    public T withoutDrainable(ItemKey withoutDrainable) {
        this.withoutDrainable.setValue(withoutDrainable);
        return (T)this;
    }

    public T worldObjectSprite(String worldObjectSprite) {
        this.worldObjectSprite.setValue(worldObjectSprite);
        return (T)this;
    }

    public T worldRender(boolean worldRender) {
        this.worldRender.setValue(worldRender);
        return (T)this;
    }

    public T worldStaticModel(ModelKey worldStaticModel) {
        this.worldStaticModel.setValue(worldStaticModel);
        return (T)this;
    }

    public T worldStaticModelsByIndex(ModelKey ... worldStaticModelsByIndex) {
        this.worldStaticModelsByIndex.addValues((ModelKey[])worldStaticModelsByIndex);
        return (T)this;
    }

    public T foodSicknessChange(int foodSicknessChange) {
        this.foodSicknessChange.setValue(foodSicknessChange);
        return (T)this;
    }

    public T inverseCoughProbability(int inverseCoughProbability) {
        this.inverseCoughProbability.setValue(inverseCoughProbability);
        return (T)this;
    }

    public T inverseCoughProbabilitySmoker(int inverseCoughProbabilitySmoker) {
        this.inverseCoughProbabilitySmoker.setValue(inverseCoughProbabilitySmoker);
        return (T)this;
    }

    public T magazineSubjects(MagazineSubject ... magazineSubjects) {
        this.magazineSubject.addValues((MagazineSubject[])magazineSubjects);
        return (T)this;
    }

    private record ClothingMask(ClothingItemXmlValidator clothing, AnimationXmlValidator mask) {
        private static ClothingMask of(String clothing, String mask) {
            return new ClothingMask(ClothingItemXmlValidator.of(clothing), AnimationXmlValidator.mask(mask));
        }

        @Override
        public String toString() {
            return "%s %s".formatted(this.clothing, this.mask);
        }
    }
}

