/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.AbstractScriptTypeBuilder;
import generation.builders.VehicleAreaBuilder;
import generation.builders.VehicleLightbarBuilder;
import generation.builders.VehicleModelAttachmentBuilder;
import generation.builders.VehicleModelBuilder;
import generation.builders.VehiclePartBuilder;
import generation.builders.VehiclePassengerBuilder;
import generation.builders.VehiclePhysicsBuilder;
import generation.builders.VehicleSkinBuilder;
import generation.builders.VehicleSoundBuilder;
import generation.builders.VehicleWheelBuilder;
import generation.builders.Writeable;
import generation.builders.validation.PackTextureValidator;
import zombie.scripting.ScriptType;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.VehicleEngineRpmType;
import zombie.scripting.objects.VehicleKey;
import zombie.scripting.objects.VehiclePart;
import zombie.scripting.objects.VehiclePassenger;
import zombie.scripting.objects.VehicleTemplateKey;

public class VehicleBuilder
extends AbstractScriptTypeBuilder {
    private final Writeable.Property<Boolean> hasLighter = this.property("hasLighter");
    private final Writeable.Property<Boolean> isSmallVehicle = this.property("isSmallVehicle");
    private final Writeable.Property<Boolean> notKillCrops = this.property("notKillCrops");
    private final Writeable.Property<Boolean> neverSpawnKey = this.property("neverSpawnKey");
    private final Writeable.Property<Boolean> useChassisPhysicsCollision = this.property("useChassisPhysicsCollision");
    private final Writeable.Property<Float> animalTrailerSize = this.property("animalTrailerSize");
    private final Writeable.Property<Integer> engineForce = this.property("engineForce");
    private final Writeable.ListProperty<Float> extents = this.listProperty("extents", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<Integer> mass = this.property("mass");
    private final Writeable.Property<Float> maxSpeed = this.property("maxSpeed");
    private final Writeable.Property<Float> maxSuspensionTravelCm = this.property("maxSuspensionTravelCm");
    private final Writeable.Property<Integer> brakingForce = this.property("brakingForce");
    private final Writeable.Property<Integer> rearEndHealth = this.property("rearEndHealth");
    private final Writeable.Property<Float> playerDamageProtection = this.property("playerDamageProtection");
    private final Writeable.Property<Float> rollInfluence = this.property("rollInfluence");
    private final Writeable.Property<Float> steeringClamp = this.property("steeringClamp");
    private final Writeable.Property<Float> steeringIncrement = this.property("steeringIncrement");
    private final Writeable.Property<Float> stoppingMovementForce = this.property("stoppingMovementForce");
    private final Writeable.Property<Float> suspensionCompression = this.property("suspensionCompression");
    private final Writeable.Property<Float> suspensionDamping = this.property("suspensionDamping");
    private final Writeable.Property<Float> suspensionRestLength = this.property("suspensionRestLength");
    private final Writeable.Property<Float> suspensionStiffness = this.property("suspensionStiffness");
    private final Writeable.Property<Float> wheelFriction = this.property("wheelFriction");
    private final Writeable.Property<Integer> engineLoudness = this.property("engineLoudness");
    private final Writeable.Property<Integer> engineQuality = this.property("engineQuality");
    private final Writeable.Property<Integer> engineRepairLevel = this.property("engineRepairLevel");
    private final Writeable.Property<Integer> frontEndHealth = this.property("frontEndHealth");
    private final Writeable.Property<Integer> mechanicType = this.property("mechanicType");
    private final Writeable.Property<Integer> seats = this.property("seats");
    private final Writeable.Property<Integer> gearRatioCount = this.property("gearRatioCount");
    private final Writeable.Property<Float> gearRatioR = this.property("gearRatioR");
    private final Writeable.Property<Float> gearRatio1 = this.property("gearRatio1");
    private final Writeable.Property<Float> gearRatio2 = this.property("gearRatio2");
    private final Writeable.Property<Float> gearRatio3 = this.property("gearRatio3");
    private final Writeable.Property<Float> gearRatio4 = this.property("gearRatio4");
    private final Writeable.Property<Float> gearRatio5 = this.property("gearRatio5");
    private final Writeable.Property<Integer> specialKeyRingChance = this.property("specialKeyRingChance");
    private final Writeable.Property<Integer> specialLootChance = this.property("specialLootChance");
    private final Writeable.Property<VehicleKey> carMechanicsOverlay = this.property("carMechanicsOverlay", VehicleKey::toStringBase);
    private final Writeable.Property<VehicleKey> carModelName = this.property("carModelName");
    private final Writeable.ListProperty<Float> centerOfMassOffset = this.listProperty("centerOfMassOffset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.Property<VehicleEngineRpmType> engineRpmType = this.property("engineRPMType");
    private final Writeable.ListProperty<Float> extentsOffset = this.listProperty("extentsOffset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> physicsChassisShape = this.listProperty("physicsChassisShape", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> shadowExtents = this.listProperty("shadowExtents", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<Float> shadowOffset = this.listProperty("shadowOffset", " ", Writeable.ListProperty.Flags.KEEP_DUPLICATES);
    private final Writeable.ListProperty<ItemKey> specialKeyRing = this.listProperty("specialKeyRing", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<String> zombieType = this.property("zombieType");
    private final Writeable.ListProperty<VehicleTemplateKey> templateVehicle = this.listProperty("template!", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<String> template = this.listProperty("template", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<PackTextureValidator> textureMask = this.property("textureMask");
    private final Writeable.Property<PackTextureValidator> textureLights = this.property("textureLights");
    private final Writeable.Property<PackTextureValidator> textureDamage1Overlay = this.property("textureDamage1Overlay");
    private final Writeable.Property<PackTextureValidator> textureDamage2Overlay = this.property("textureDamage2Overlay");
    private final Writeable.Property<PackTextureValidator> textureDamage1Shell = this.property("textureDamage1Shell");
    private final Writeable.Property<PackTextureValidator> textureDamage2Shell = this.property("textureDamage2Shell");
    private final Writeable.Property<PackTextureValidator> textureRust = this.property("textureRust");
    private final Writeable.ListProperty<VehicleModelAttachmentBuilder> attachment = this.listProperty("attachment", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleModelBuilder> model = this.listProperty("model", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleSkinBuilder> skin = this.listProperty("skin", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleSoundBuilder> sound = this.listProperty("sound", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleWheelBuilder> wheel = this.listProperty("wheel", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleWheelBuilder> crawlThroughWheel = this.listProperty("crawlThroughWheel", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehiclePassengerBuilder> passenger = this.listProperty("passenger", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleAreaBuilder> area = this.listProperty("area", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehiclePartBuilder> part = this.listProperty("part", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehicleLightbarBuilder> lightbar = this.listProperty("lightbar", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.ListProperty<VehiclePhysicsBuilder> physics = this.listProperty("physics", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<Float> offRoadEfficiency = this.property("offRoadEfficiency");
    private final Writeable.Property<Integer> seatNumber = this.property("seatNumber");

    public VehicleBuilder addArea(VehicleAreaBuilder area) {
        this.area.addValues((VehicleAreaBuilder[])new VehicleAreaBuilder[]{area});
        return this;
    }

    public VehicleBuilder addAttachment(VehicleModelAttachmentBuilder attachment) {
        this.attachment.addValues((VehicleModelAttachmentBuilder[])new VehicleModelAttachmentBuilder[]{attachment});
        return this;
    }

    public VehicleBuilder addLightbar(VehicleLightbarBuilder lightbar) {
        this.lightbar.addValues((VehicleLightbarBuilder[])new VehicleLightbarBuilder[]{lightbar});
        return this;
    }

    public VehicleBuilder addModel(VehicleModelBuilder model) {
        this.model.addValues((VehicleModelBuilder[])new VehicleModelBuilder[]{model});
        return this;
    }

    public VehicleBuilder addPart(VehiclePartBuilder part) {
        this.part.addValues((VehiclePartBuilder[])new VehiclePartBuilder[]{part});
        return this;
    }

    public VehicleBuilder addPassenger(VehiclePassengerBuilder passenger) {
        this.passenger.addValues((VehiclePassengerBuilder[])new VehiclePassengerBuilder[]{passenger});
        return this;
    }

    public VehicleBuilder addPhysics(VehiclePhysicsBuilder physics) {
        this.physics.addValues((VehiclePhysicsBuilder[])new VehiclePhysicsBuilder[]{physics});
        return this;
    }

    public VehicleBuilder addSkin(VehicleSkinBuilder skin) {
        this.skin.addValues((VehicleSkinBuilder[])new VehicleSkinBuilder[]{skin});
        return this;
    }

    public VehicleBuilder addSound(VehicleSoundBuilder sound) {
        this.sound.addValues((VehicleSoundBuilder[])new VehicleSoundBuilder[]{sound});
        return this;
    }

    public VehicleBuilder addWheel(VehicleWheelBuilder wheel) {
        this.wheel.addValues((VehicleWheelBuilder[])new VehicleWheelBuilder[]{wheel});
        return this;
    }

    public VehicleBuilder addCrawlThroughWheel(VehicleWheelBuilder wheel) {
        this.crawlThroughWheel.addValues((VehicleWheelBuilder[])new VehicleWheelBuilder[]{wheel});
        return this;
    }

    public VehicleBuilder animalTrailerSize(float animalTrailerSize) {
        this.animalTrailerSize.setValue(Float.valueOf(animalTrailerSize));
        return this;
    }

    public VehicleBuilder brakingForce(int brakingForce) {
        this.brakingForce.setValue(brakingForce);
        return this;
    }

    public VehicleBuilder carMechanicsOverlay(VehicleKey carMechanicsOverlay) {
        this.carMechanicsOverlay.setValue(carMechanicsOverlay);
        return this;
    }

    public VehicleBuilder carModelName(VehicleKey carModelName) {
        this.carModelName.setValue(carModelName);
        return this;
    }

    public VehicleBuilder centerOfMassOffset(float x, float y, float z) {
        this.centerOfMassOffset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehicleBuilder engineForce(int engineForce) {
        this.engineForce.setValue(engineForce);
        return this;
    }

    public VehicleBuilder engineLoudness(int engineLoudness) {
        this.engineLoudness.setValue(engineLoudness);
        return this;
    }

    public VehicleBuilder engineQuality(int engineQuality) {
        this.engineQuality.setValue(engineQuality);
        return this;
    }

    public VehicleBuilder engineRpmType(VehicleEngineRpmType engineRpmType) {
        this.engineRpmType.setValue(engineRpmType);
        return this;
    }

    public VehicleBuilder engineRepairLevel(int engineRepairLevel) {
        this.engineRepairLevel.setValue(engineRepairLevel);
        return this;
    }

    public VehicleBuilder extents(float x, float y, float z) {
        this.extents.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehicleBuilder extentsOffset(float x, float y) {
        this.extentsOffset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y)});
        return this;
    }

    public VehicleBuilder frontEndHealth(int frontEndHealth) {
        this.frontEndHealth.setValue(frontEndHealth);
        return this;
    }

    public VehicleBuilder gearRatio1(float gearRatio1) {
        this.gearRatio1.setValue(Float.valueOf(gearRatio1));
        return this;
    }

    public VehicleBuilder gearRatio2(float gearRatio2) {
        this.gearRatio2.setValue(Float.valueOf(gearRatio2));
        return this;
    }

    public VehicleBuilder gearRatio3(float gearRatio3) {
        this.gearRatio3.setValue(Float.valueOf(gearRatio3));
        return this;
    }

    public VehicleBuilder gearRatio4(float gearRatio4) {
        this.gearRatio4.setValue(Float.valueOf(gearRatio4));
        return this;
    }

    public VehicleBuilder gearRatio5(float gearRatio5) {
        this.gearRatio5.setValue(Float.valueOf(gearRatio5));
        return this;
    }

    public VehicleBuilder gearRatioCount(int gearRatioCount) {
        this.gearRatioCount.setValue(gearRatioCount);
        return this;
    }

    public VehicleBuilder gearRatioR(float gearRatioR) {
        this.gearRatioR.setValue(Float.valueOf(gearRatioR));
        return this;
    }

    public VehicleBuilder hasLighter(boolean hasLighter) {
        this.hasLighter.setValue(hasLighter);
        return this;
    }

    public VehicleBuilder isSmallVehicle(boolean isSmallVehicle) {
        this.isSmallVehicle.setValue(isSmallVehicle);
        return this;
    }

    public VehicleBuilder mass(int mass) {
        this.mass.setValue(mass);
        return this;
    }

    public VehicleBuilder maxSpeed(float maxSpeed) {
        this.maxSpeed.setValue(Float.valueOf(maxSpeed));
        return this;
    }

    public VehicleBuilder maxSuspensionTravelCm(float maxSuspensionTravelCm) {
        this.maxSuspensionTravelCm.setValue(Float.valueOf(maxSuspensionTravelCm));
        return this;
    }

    public VehicleBuilder mechanicType(int mechanicType) {
        this.mechanicType.setValue(mechanicType);
        return this;
    }

    public VehicleBuilder notKillCrops(boolean notKillCrops) {
        this.notKillCrops.setValue(notKillCrops);
        return this;
    }

    public VehicleBuilder neverSpawnKey(boolean neverSpawnKey) {
        this.neverSpawnKey.setValue(neverSpawnKey);
        return this;
    }

    public VehicleBuilder offRoadEfficiency(float offRoadEfficiency) {
        this.offRoadEfficiency.setValue(Float.valueOf(offRoadEfficiency));
        return this;
    }

    public VehicleBuilder physicsChassisShape(float x, float y, float z) {
        this.physicsChassisShape.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
        return this;
    }

    public VehicleBuilder playerDamageProtection(float playerDamageProtection) {
        this.playerDamageProtection.setValue(Float.valueOf(playerDamageProtection));
        return this;
    }

    public VehicleBuilder rearEndHealth(int rearEndHealth) {
        this.rearEndHealth.setValue(rearEndHealth);
        return this;
    }

    public VehicleBuilder rollInfluence(float rollInfluence) {
        this.rollInfluence.setValue(Float.valueOf(rollInfluence));
        return this;
    }

    public VehicleBuilder seatNumber(int seatNumber) {
        this.seatNumber.setValue(seatNumber);
        return this;
    }

    public VehicleBuilder seats(int seats) {
        this.seats.setValue(seats);
        return this;
    }

    public VehicleBuilder shadowExtents(float x, float y) {
        this.shadowExtents.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y)});
        return this;
    }

    public VehicleBuilder shadowOffset(float x, float y) {
        this.shadowOffset.addValues((Float[])new Float[]{Float.valueOf(x), Float.valueOf(y)});
        return this;
    }

    public VehicleBuilder specialKeyRing(ItemKey ... specialKeyRing) {
        this.specialKeyRing.addValues((ItemKey[])specialKeyRing);
        return this;
    }

    public VehicleBuilder specialKeyRingChance(int specialKeyRingChance) {
        this.specialKeyRingChance.setValue(specialKeyRingChance);
        return this;
    }

    public VehicleBuilder specialLootChance(int specialLootChance) {
        this.specialLootChance.setValue(specialLootChance);
        return this;
    }

    public VehicleBuilder steeringClamp(float steeringClamp) {
        this.steeringClamp.setValue(Float.valueOf(steeringClamp));
        return this;
    }

    public VehicleBuilder steeringIncrement(float steeringIncrement) {
        this.steeringIncrement.setValue(Float.valueOf(steeringIncrement));
        return this;
    }

    public VehicleBuilder stoppingMovementForce(float stoppingMovementForce) {
        this.stoppingMovementForce.setValue(Float.valueOf(stoppingMovementForce));
        return this;
    }

    public VehicleBuilder suspensionCompression(float suspensionCompression) {
        this.suspensionCompression.setValue(Float.valueOf(suspensionCompression));
        return this;
    }

    public VehicleBuilder suspensionDamping(float suspensionDamping) {
        this.suspensionDamping.setValue(Float.valueOf(suspensionDamping));
        return this;
    }

    public VehicleBuilder suspensionRestLength(float suspensionRestLength) {
        this.suspensionRestLength.setValue(Float.valueOf(suspensionRestLength));
        return this;
    }

    public VehicleBuilder suspensionStiffness(float suspensionStiffness) {
        this.suspensionStiffness.setValue(Float.valueOf(suspensionStiffness));
        return this;
    }

    public VehicleBuilder template(VehicleTemplateKey template, VehiclePart part) {
        this.template.addValues((String[])new String[]{"%s/part/%s".formatted(new Object[]{template, part})});
        return this;
    }

    public VehicleBuilder template(VehicleTemplateKey template, VehiclePassenger passenger) {
        this.template.addValues((String[])new String[]{"%s/passenger/%s".formatted(new Object[]{template, passenger})});
        return this;
    }

    public VehicleBuilder template(VehicleTemplateKey template) {
        this.template.addValues((String[])new String[]{template.toString()});
        return this;
    }

    public VehicleBuilder templateVehicle(VehicleTemplateKey ... templateVehicle) {
        this.templateVehicle.addValues((VehicleTemplateKey[])templateVehicle);
        return this;
    }

    public VehicleBuilder textureDamage1Overlay(String textureDamage1Overlay) {
        this.textureDamage1Overlay.setValue(PackTextureValidator.of(textureDamage1Overlay, ""));
        return this;
    }

    public VehicleBuilder textureDamage1Shell(String textureDamage1Shell) {
        this.textureDamage1Shell.setValue(PackTextureValidator.of(textureDamage1Shell, ""));
        return this;
    }

    public VehicleBuilder textureDamage2Overlay(String textureDamage2Overlay) {
        this.textureDamage2Overlay.setValue(PackTextureValidator.of(textureDamage2Overlay, ""));
        return this;
    }

    public VehicleBuilder textureDamage2Shell(String textureDamage2Shell) {
        this.textureDamage2Shell.setValue(PackTextureValidator.of(textureDamage2Shell, ""));
        return this;
    }

    public VehicleBuilder textureLights(String textureLights) {
        this.textureLights.setValue(PackTextureValidator.of(textureLights, ""));
        return this;
    }

    public VehicleBuilder textureMask(String textureMask) {
        this.textureMask.setValue(PackTextureValidator.of(textureMask, ""));
        return this;
    }

    public VehicleBuilder textureRust(String textureRust) {
        this.textureRust.setValue(PackTextureValidator.of(textureRust, ""));
        return this;
    }

    public VehicleBuilder useChassisPhysicsCollision(boolean useChassisPhysicsCollision) {
        this.useChassisPhysicsCollision.setValue(useChassisPhysicsCollision);
        return this;
    }

    public VehicleBuilder wheelFriction(float wheelFriction) {
        this.wheelFriction.setValue(Float.valueOf(wheelFriction));
        return this;
    }

    public VehicleBuilder zombieType(String zombieType) {
        this.zombieType.setValue(zombieType);
        return this;
    }

    public static VehicleBuilder withId(VehicleKey id) {
        return new VehicleBuilder(id.id());
    }

    public static VehicleBuilder withId(String id) {
        return new VehicleBuilder(id);
    }

    public static VehicleBuilder withId(VehicleTemplateKey id) {
        return new VehicleBuilder(id);
    }

    public VehicleBuilder(VehicleTemplateKey name) {
        super("template", "%s %s".formatted("vehicle", name.id()));
    }

    public VehicleBuilder(String name) {
        super(ScriptType.Vehicle, name);
    }
}

