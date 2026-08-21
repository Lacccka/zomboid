/*
 * Decompiled with CFR 0.152.
 */
package generation.builders;

import generation.builders.ItemBuilder;
import generation.builders.ModelWeaponPartBuilder;
import generation.builders.Writeable;
import zombie.scripting.objects.DamageCategory;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemTag;
import zombie.scripting.objects.ModelKey;
import zombie.scripting.objects.SoundKey;
import zombie.scripting.objects.WeaponCategory;
import zombie.scripting.objects.WeaponFireMode;
import zombie.scripting.objects.WeaponReloadType;
import zombie.scripting.objects.WeaponSubCategory;

public class WeaponItemBuilder
extends ItemBuilder<WeaponItemBuilder> {
    private final Writeable.Property<Float> aimingMod = this.property("AimingMod");
    private final Writeable.Property<Integer> aimingPerkCritModifier = this.property("AimingPerkCritModifier");
    private final Writeable.Property<Integer> aimingPerkHitChanceModifier = this.property("AimingPerkHitChanceModifier");
    private final Writeable.Property<Float> aimingPerkMinAngleModifier = this.property("AimingPerkMinAngleModifier");
    private final Writeable.Property<Integer> aimingPerkRangeModifier = this.property("AimingPerkRangeModifier");
    private final Writeable.Property<Integer> aimingTime = this.property("AimingTime");
    private final Writeable.Property<Boolean> alwaysKnockdown = this.property("AlwaysKnockdown");
    private final Writeable.Property<ItemKey> ammoBox = this.property("AmmoBox");
    private final Writeable.Property<Boolean> angleFalloff = this.property("AngleFalloff");
    private final Writeable.Property<Float> baseSpeed = this.property("BaseSpeed");
    private final Writeable.Property<SoundKey> bulletOutSound = this.property("BulletOutSound");
    private final Writeable.Property<Boolean> canBarricade = this.property("CanBarricade");
    private final Writeable.Property<Boolean> canBePlaced = this.property("CanBePlaced");
    private final Writeable.Property<Boolean> canBeReused = this.property("CanBeReused");
    private final Writeable.Property<Boolean> cantAttackWithLowestEndurance = this.property("CantAttackWithLowestEndurance");
    private final Writeable.ListProperty<WeaponCategory> categories = this.listProperty("Categories", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<SoundKey> clickSound = this.property("ClickSound");
    private final Writeable.Property<Integer> clipSize = this.property("ClipSize");
    private final Writeable.Property<Float> critDmgMultiplier = this.property("CritDmgMultiplier");
    private final Writeable.Property<Float> criticalChance = this.property("CriticalChance");
    private final Writeable.Property<Float> cyclicRateMultiplier = this.property("CyclicRateMultiplier");
    private final Writeable.Property<DamageCategory> damageCategory = this.property("DamageCategory");
    private final Writeable.Property<Boolean> damageMakeHole = this.property("DamageMakeHole");
    private final Writeable.Property<Integer> doorDamage = this.property("DoorDamage");
    private final Writeable.Property<SoundKey> doorHitSound = this.property("DoorHitSound");
    private final Writeable.Property<Float> enduranceMod = this.property("EnduranceMod");
    private final Writeable.Property<Integer> explosionDuration = this.property("ExplosionDuration");
    private final Writeable.Property<Integer> explosionPower = this.property("ExplosionPower");
    private final Writeable.Property<Integer> explosionRange = this.property("ExplosionRange");
    private final Writeable.Property<Integer> explosionTimer = this.property("ExplosionTimer");
    private final Writeable.Property<Float> extraDamage = this.property("extraDamage");
    private final Writeable.Property<WeaponFireMode> fireMode = this.property("FireMode");
    private final Writeable.ListProperty<WeaponFireMode> fireModePossibilities = this.listProperty("FireModePossibilities", "/", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Integer> firePower = this.property("FirePower");
    private final Writeable.Property<Integer> fireRange = this.property("FireRange");
    private final Writeable.Property<Integer> fireStartingEnergy = this.property("FireStartingEnergy");
    private final Writeable.Property<Integer> fireStartingChance = this.property("FireStartingChance");
    private final Writeable.Property<Boolean> haveChamber = this.property("HaveChamber");
    private final Writeable.Property<Float> hitAngleMod = this.property("HitAngleMod");
    private final Writeable.Property<Integer> hitChance = this.property("HitChance");
    private final Writeable.Property<SoundKey> hitFloorSound = this.property("HitFloorSound");
    private final Writeable.Property<SoundKey> hitSound = this.property("HitSound");
    private final Writeable.Property<String> idleAnim = this.property("IdleAnim");
    private final Writeable.Property<SoundKey> impactSound = this.property("ImpactSound");
    private final Writeable.Property<Boolean> insertAllBulletsReload = this.property("InsertAllBulletsReload");
    private final Writeable.Property<Boolean> isAimedFirearm = this.property("IsAimedFirearm");
    private final Writeable.Property<Boolean> isAimedHandWeapon = this.property("IsAimedHandWeapon");
    private final Writeable.Property<Integer> jamGunChance = this.property("JamGunChance");
    private final Writeable.Property<Boolean> knockBackOnNoDeath = this.property("KnockBackOnNoDeath");
    private final Writeable.Property<Float> knockdownMod = this.property("KnockdownMod");
    private final Writeable.Property<ItemKey> magazineType = this.property("MagazineType");
    private final Writeable.Property<Float> maxDamage = this.property("MaxDamage");
    private final Writeable.Property<Integer> maxHitcount = this.property("MaxHitcount");
    private final Writeable.Property<Float> maxRange = this.property("MaxRange");
    private final Writeable.Property<Float> maxSightRange = this.property("MaxSightRange");
    private final Writeable.Property<Float> minAngle = this.property("MinAngle");
    private final Writeable.Property<Float> minDamage = this.property("MinDamage");
    private final Writeable.Property<Float> minRange = this.property("MinRange");
    private final Writeable.Property<Integer> minSightRange = this.property("MinSightRange");
    private final Writeable.Property<Float> minimumSwingtime = this.property("MinimumSwingtime");
    private final Writeable.ListProperty<ModelWeaponPartBuilder> modelWeaponPart = this.listProperty("ModelWeaponPart", Writeable.ListProperty.Flags.HIDE_OUTER_BLOCK);
    private final Writeable.Property<Boolean> multipleHitConditionAffected = this.property("MultipleHitConditionAffected");
    private final Writeable.Property<ModelKey> muzzleFlashModelKey = this.property("MuzzleFlashModelKey");
    private final Writeable.Property<Float> npcSoundBoost = this.property("NPCSoundBoost");
    private final Writeable.Property<Integer> noiseRange = this.property("NoiseRange");
    private final Writeable.Property<ItemTag> otherHandRequire = this.property("OtherHandRequire");
    private final Writeable.Property<Boolean> otherHandUse = this.property("OtherHandUse");
    private final Writeable.Property<ItemKey> physicsObject = this.property("PhysicsObject");
    private final Writeable.Property<Boolean> piercingBullets = this.property("PiercingBullets");
    private final Writeable.Property<String> placedSprite = this.property("PlacedSprite");
    private final Writeable.Property<Float> projectileSpread = this.property("ProjectileSpread");
    private final Writeable.Property<Float> projectileWeightCenter = this.property("ProjectileWeightCenter");
    private final Writeable.Property<Integer> projectilecount = this.property("Projectilecount");
    private final Writeable.Property<Float> pushBackMod = this.property("PushBackMod");
    private final Writeable.Property<Boolean> rackAfterShoot = this.property("RackAfterShoot");
    private final Writeable.Property<SoundKey> rackSound = this.property("RackSound");
    private final Writeable.Property<Boolean> rangeFalloff = this.property("RangeFalloff");
    private final Writeable.Property<Boolean> ranged = this.property("Ranged");
    private final Writeable.Property<Integer> recoilDelay = this.property("RecoilDelay");
    private final Writeable.Property<Integer> reloadtime = this.property("Reloadtime");
    private final Writeable.Property<String> runAnim = this.property("RunAnim");
    private final Writeable.Property<Integer> sensorRange = this.property("SensorRange");
    private final Writeable.Property<Boolean> shareEndurance = this.property("ShareEndurance");
    private final Writeable.Property<SoundKey> shellFallSound = this.property("ShellFallSound");
    private final Writeable.Property<Integer> smokeRange = this.property("SmokeRange");
    private final Writeable.Property<Float> soundGain = this.property("SoundGain");
    private final Writeable.Property<Integer> soundRadius = this.property("SoundRadius");
    private final Writeable.Property<Integer> soundVolume = this.property("SoundVolume");
    private final Writeable.Property<Boolean> splatBloodOnNoDeath = this.property("SplatBloodOnNoDeath");
    private final Writeable.Property<Integer> splatNumber = this.property("SplatNumber");
    private final Writeable.Property<Integer> splatSize = this.property("SplatSize");
    private final Writeable.Property<WeaponSubCategory> subCategory = this.property("SubCategory");
    private final Writeable.Property<Float> swingAmountBeforeImpact = this.property("SwingAmountBeforeImpact");
    private final Writeable.Property<SoundKey> swingSound = this.property("SwingSound");
    private final Writeable.Property<Float> swingtime = this.property("Swingtime");
    private final Writeable.Property<Float> toHitModifier = this.property("ToHitModifier");
    private final Writeable.Property<Integer> treeDamage = this.property("TreeDamage");
    private final Writeable.Property<Integer> triggerExplosionTimer = this.property("triggerExplosionTimer");
    private final Writeable.Property<Boolean> useEndurance = this.property("UseEndurance");
    private final Writeable.Property<Boolean> useSelf = this.property("UseSelf");
    private final Writeable.Property<Float> weaponLength = this.property("WeaponLength");
    private final Writeable.Property<WeaponReloadType> weaponReloadType = this.property("WeaponReloadType");
    private final Writeable.Property<ModelKey> weaponSprite = this.property("WeaponSprite");
    private final Writeable.ListProperty<ModelKey> weaponSpritesByIndex = this.listProperty("WeaponSpritesByIndex", ";", new Writeable.ListProperty.Flags[0]);
    private final Writeable.Property<Integer> weaponWeight = this.property("WeaponWeight");

    public WeaponItemBuilder(ItemKey item) {
        super(item);
    }

    public WeaponItemBuilder aimingMod(float aimingMod) {
        this.aimingMod.setValue(Float.valueOf(aimingMod));
        return this;
    }

    public WeaponItemBuilder aimingPerkCritModifier(int aimingPerkCritModifier) {
        this.aimingPerkCritModifier.setValue(aimingPerkCritModifier);
        return this;
    }

    public WeaponItemBuilder aimingPerkHitChanceModifier(int aimingPerkHitChanceModifier) {
        this.aimingPerkHitChanceModifier.setValue(aimingPerkHitChanceModifier);
        return this;
    }

    public WeaponItemBuilder aimingPerkMinAngleModifier(float aimingPerkMinAngleModifier) {
        this.aimingPerkMinAngleModifier.setValue(Float.valueOf(aimingPerkMinAngleModifier));
        return this;
    }

    public WeaponItemBuilder aimingPerkRangeModifier(int aimingPerkRangeModifier) {
        this.aimingPerkRangeModifier.setValue(aimingPerkRangeModifier);
        return this;
    }

    public WeaponItemBuilder aimingTime(int aimingTime) {
        this.aimingTime.setValue(aimingTime);
        return this;
    }

    public WeaponItemBuilder alwaysKnockdown(boolean alwaysKnockdown) {
        this.alwaysKnockdown.setValue(alwaysKnockdown);
        return this;
    }

    public WeaponItemBuilder ammoBox(ItemKey ammoBox) {
        this.ammoBox.setValue(ammoBox);
        return this;
    }

    public WeaponItemBuilder angleFalloff(boolean angleFalloff) {
        this.angleFalloff.setValue(angleFalloff);
        return this;
    }

    public WeaponItemBuilder baseSpeed(float baseSpeed) {
        this.baseSpeed.setValue(Float.valueOf(baseSpeed));
        return this;
    }

    public WeaponItemBuilder bulletOutSound(SoundKey bulletOutSound) {
        this.bulletOutSound.setValue(bulletOutSound);
        return this;
    }

    public WeaponItemBuilder canBarricade(boolean canBarricade) {
        this.canBarricade.setValue(canBarricade);
        return this;
    }

    public WeaponItemBuilder canBePlaced(boolean canBePlaced) {
        this.canBePlaced.setValue(canBePlaced);
        return this;
    }

    public WeaponItemBuilder canBeReused(boolean canBeReused) {
        this.canBeReused.setValue(canBeReused);
        return this;
    }

    public WeaponItemBuilder cantAttackWithLowestEndurance(boolean cantAttackWithLowestEndurance) {
        this.cantAttackWithLowestEndurance.setValue(cantAttackWithLowestEndurance);
        return this;
    }

    public WeaponItemBuilder categories(WeaponCategory ... categories) {
        this.categories.addValues((WeaponCategory[])categories);
        return this;
    }

    public WeaponItemBuilder clickSound(SoundKey clickSound) {
        this.clickSound.setValue(clickSound);
        return this;
    }

    public WeaponItemBuilder clipSize(int clipSize) {
        this.clipSize.setValue(clipSize);
        return this;
    }

    public WeaponItemBuilder critDmgMultiplier(float critDmgMultiplier) {
        this.critDmgMultiplier.setValue(Float.valueOf(critDmgMultiplier));
        return this;
    }

    public WeaponItemBuilder criticalChance(float criticalChance) {
        this.criticalChance.setValue(Float.valueOf(criticalChance));
        return this;
    }

    public WeaponItemBuilder cyclicRateMultiplier(float cyclicRateMultiplier) {
        this.cyclicRateMultiplier.setValue(Float.valueOf(cyclicRateMultiplier));
        return this;
    }

    public WeaponItemBuilder damageCategory(DamageCategory damageCategory) {
        this.damageCategory.setValue(damageCategory);
        return this;
    }

    public WeaponItemBuilder damageMakeHole(boolean damageMakeHole) {
        this.damageMakeHole.setValue(damageMakeHole);
        return this;
    }

    public WeaponItemBuilder doorDamage(int doorDamage) {
        this.doorDamage.setValue(doorDamage);
        return this;
    }

    public WeaponItemBuilder doorHitSound(SoundKey doorHitSound) {
        this.doorHitSound.setValue(doorHitSound);
        return this;
    }

    public WeaponItemBuilder enduranceMod(float enduranceMod) {
        this.enduranceMod.setValue(Float.valueOf(enduranceMod));
        return this;
    }

    public WeaponItemBuilder explosionDuration(int explosionDuration) {
        this.explosionDuration.setValue(explosionDuration);
        return this;
    }

    public WeaponItemBuilder explosionPower(int explosionPower) {
        this.explosionPower.setValue(explosionPower);
        return this;
    }

    public WeaponItemBuilder explosionRange(int explosionRange) {
        this.explosionRange.setValue(explosionRange);
        return this;
    }

    public WeaponItemBuilder explosionTimer(int explosionTimer) {
        this.explosionTimer.setValue(explosionTimer);
        return this;
    }

    public WeaponItemBuilder extraDamage(float extraDamage) {
        this.extraDamage.setValue(Float.valueOf(extraDamage));
        return this;
    }

    public WeaponItemBuilder fireMode(WeaponFireMode fireMode) {
        this.fireMode.setValue(fireMode);
        return this;
    }

    public WeaponItemBuilder fireModePossibilities(WeaponFireMode ... fireModePossibilities) {
        this.fireModePossibilities.addValues((WeaponFireMode[])fireModePossibilities);
        return this;
    }

    public WeaponItemBuilder firePower(int firePower) {
        this.firePower.setValue(firePower);
        return this;
    }

    public WeaponItemBuilder fireRange(int fireRange) {
        this.fireRange.setValue(fireRange);
        return this;
    }

    public WeaponItemBuilder fireStartingEnergy(int fireStartingEnergy) {
        this.fireStartingEnergy.setValue(fireStartingEnergy);
        return this;
    }

    public WeaponItemBuilder fireStartingChance(int fireStartingChance) {
        this.fireStartingChance.setValue(fireStartingChance);
        return this;
    }

    public WeaponItemBuilder haveChamber(boolean haveChamber) {
        this.haveChamber.setValue(haveChamber);
        return this;
    }

    public WeaponItemBuilder hitAngleMod(float hitAngleMod) {
        this.hitAngleMod.setValue(Float.valueOf(hitAngleMod));
        return this;
    }

    public WeaponItemBuilder hitChance(int hitChance) {
        this.hitChance.setValue(hitChance);
        return this;
    }

    public WeaponItemBuilder hitFloorSound(SoundKey hitFloorSound) {
        this.hitFloorSound.setValue(hitFloorSound);
        return this;
    }

    public WeaponItemBuilder hitSound(SoundKey hitSound) {
        this.hitSound.setValue(hitSound);
        return this;
    }

    public WeaponItemBuilder idleAnim(String idleAnim) {
        this.idleAnim.setValue(idleAnim);
        return this;
    }

    public WeaponItemBuilder impactSound(SoundKey impactSound) {
        this.impactSound.setValue(impactSound);
        return this;
    }

    public WeaponItemBuilder insertAllBulletsReload(boolean insertAllBulletsReload) {
        this.insertAllBulletsReload.setValue(insertAllBulletsReload);
        return this;
    }

    public WeaponItemBuilder isAimedFirearm(boolean isAimedFirearm) {
        this.isAimedFirearm.setValue(isAimedFirearm);
        return this;
    }

    public WeaponItemBuilder isAimedHandWeapon(boolean isAimedHandWeapon) {
        this.isAimedHandWeapon.setValue(isAimedHandWeapon);
        return this;
    }

    public WeaponItemBuilder jamGunChance(int jamGunChance) {
        this.jamGunChance.setValue(jamGunChance);
        return this;
    }

    public WeaponItemBuilder knockBackOnNoDeath(boolean knockBackOnNoDeath) {
        this.knockBackOnNoDeath.setValue(knockBackOnNoDeath);
        return this;
    }

    public WeaponItemBuilder knockdownMod(float knockdownMod) {
        this.knockdownMod.setValue(Float.valueOf(knockdownMod));
        return this;
    }

    public WeaponItemBuilder magazineType(ItemKey magazineType) {
        this.magazineType.setValue(magazineType);
        return this;
    }

    public WeaponItemBuilder maxDamage(float maxDamage) {
        this.maxDamage.setValue(Float.valueOf(maxDamage));
        return this;
    }

    public WeaponItemBuilder maxHitcount(int maxHitcount) {
        this.maxHitcount.setValue(maxHitcount);
        return this;
    }

    public WeaponItemBuilder maxRange(float maxRange) {
        this.maxRange.setValue(Float.valueOf(maxRange));
        return this;
    }

    public WeaponItemBuilder maxSightRange(float maxSightRange) {
        this.maxSightRange.setValue(Float.valueOf(maxSightRange));
        return this;
    }

    public WeaponItemBuilder minAngle(float minAngle) {
        this.minAngle.setValue(Float.valueOf(minAngle));
        return this;
    }

    public WeaponItemBuilder minDamage(float minDamage) {
        this.minDamage.setValue(Float.valueOf(minDamage));
        return this;
    }

    public WeaponItemBuilder minRange(float minRange) {
        this.minRange.setValue(Float.valueOf(minRange));
        return this;
    }

    public WeaponItemBuilder minSightRange(int minSightRange) {
        this.minSightRange.setValue(minSightRange);
        return this;
    }

    public WeaponItemBuilder minimumSwingtime(float minimumSwingtime) {
        this.minimumSwingtime.setValue(Float.valueOf(minimumSwingtime));
        return this;
    }

    public WeaponItemBuilder modelWeaponPart(ModelWeaponPartBuilder ... modelWeaponPart) {
        this.modelWeaponPart.addValues((ModelWeaponPartBuilder[])modelWeaponPart);
        return this;
    }

    public WeaponItemBuilder multipleHitConditionAffected(boolean multipleHitConditionAffected) {
        this.multipleHitConditionAffected.setValue(multipleHitConditionAffected);
        return this;
    }

    public WeaponItemBuilder muzzleFlashModelKey(ModelKey muzzleFlashModelKey) {
        this.muzzleFlashModelKey.setValue(muzzleFlashModelKey);
        return this;
    }

    public WeaponItemBuilder npcSoundBoost(float npcSoundBoost) {
        this.npcSoundBoost.setValue(Float.valueOf(npcSoundBoost));
        return this;
    }

    public WeaponItemBuilder noiseRange(int noiseRange) {
        this.noiseRange.setValue(noiseRange);
        return this;
    }

    public WeaponItemBuilder otherHandRequire(ItemTag otherHandRequire) {
        this.otherHandRequire.setValue(otherHandRequire);
        return this;
    }

    public WeaponItemBuilder otherHandUse(boolean otherHandUse) {
        this.otherHandUse.setValue(otherHandUse);
        return this;
    }

    public WeaponItemBuilder physicsObject(ItemKey physicsObject) {
        this.physicsObject.setValue(physicsObject);
        return this;
    }

    public WeaponItemBuilder piercingBullets(boolean piercingBullets) {
        this.piercingBullets.setValue(piercingBullets);
        return this;
    }

    public WeaponItemBuilder placedSprite(String placedSprite) {
        this.placedSprite.setValue(placedSprite);
        return this;
    }

    public WeaponItemBuilder projectileSpread(float projectileSpread) {
        this.projectileSpread.setValue(Float.valueOf(projectileSpread));
        return this;
    }

    public WeaponItemBuilder projectileWeightCenter(float projectileWeightCenter) {
        this.projectileWeightCenter.setValue(Float.valueOf(projectileWeightCenter));
        return this;
    }

    public WeaponItemBuilder projectilecount(int projectilecount) {
        this.projectilecount.setValue(projectilecount);
        return this;
    }

    public WeaponItemBuilder pushBackMod(float pushBackMod) {
        this.pushBackMod.setValue(Float.valueOf(pushBackMod));
        return this;
    }

    public WeaponItemBuilder rackAfterShoot(boolean rackAfterShoot) {
        this.rackAfterShoot.setValue(rackAfterShoot);
        return this;
    }

    public WeaponItemBuilder rackSound(SoundKey rackSound) {
        this.rackSound.setValue(rackSound);
        return this;
    }

    public WeaponItemBuilder rangeFalloff(boolean rangeFalloff) {
        this.rangeFalloff.setValue(rangeFalloff);
        return this;
    }

    public WeaponItemBuilder ranged(boolean ranged) {
        this.ranged.setValue(ranged);
        return this;
    }

    public WeaponItemBuilder recoilDelay(int recoilDelay) {
        this.recoilDelay.setValue(recoilDelay);
        return this;
    }

    public WeaponItemBuilder reloadtime(int reloadtime) {
        this.reloadtime.setValue(reloadtime);
        return this;
    }

    public WeaponItemBuilder runAnim(String runAnim) {
        this.runAnim.setValue(runAnim);
        return this;
    }

    public WeaponItemBuilder sensorRange(int sensorRange) {
        this.sensorRange.setValue(sensorRange);
        return this;
    }

    public WeaponItemBuilder shareEndurance(boolean shareEndurance) {
        this.shareEndurance.setValue(shareEndurance);
        return this;
    }

    public WeaponItemBuilder shellFallSound(SoundKey shellFallSound) {
        this.shellFallSound.setValue(shellFallSound);
        return this;
    }

    public WeaponItemBuilder smokeRange(int smokeRange) {
        this.smokeRange.setValue(smokeRange);
        return this;
    }

    public WeaponItemBuilder soundGain(float soundGain) {
        this.soundGain.setValue(Float.valueOf(soundGain));
        return this;
    }

    @Override
    public WeaponItemBuilder soundRadius(int soundRadius) {
        this.soundRadius.setValue(soundRadius);
        return this;
    }

    @Override
    public WeaponItemBuilder soundVolume(int soundVolume) {
        this.soundVolume.setValue(soundVolume);
        return this;
    }

    public WeaponItemBuilder splatBloodOnNoDeath(boolean splatBloodOnNoDeath) {
        this.splatBloodOnNoDeath.setValue(splatBloodOnNoDeath);
        return this;
    }

    public WeaponItemBuilder splatNumber(int splatNumber) {
        this.splatNumber.setValue(splatNumber);
        return this;
    }

    public WeaponItemBuilder splatSize(int splatSize) {
        this.splatSize.setValue(splatSize);
        return this;
    }

    public WeaponItemBuilder subCategory(WeaponSubCategory subCategory) {
        this.subCategory.setValue(subCategory);
        return this;
    }

    public WeaponItemBuilder swingAmountBeforeImpact(float swingAmountBeforeImpact) {
        this.swingAmountBeforeImpact.setValue(Float.valueOf(swingAmountBeforeImpact));
        return this;
    }

    public WeaponItemBuilder swingSound(SoundKey swingSound) {
        this.swingSound.setValue(swingSound);
        return this;
    }

    public WeaponItemBuilder swingtime(float swingtime) {
        this.swingtime.setValue(Float.valueOf(swingtime));
        return this;
    }

    public WeaponItemBuilder toHitModifier(float toHitModifier) {
        this.toHitModifier.setValue(Float.valueOf(toHitModifier));
        return this;
    }

    public WeaponItemBuilder treeDamage(int treeDamage) {
        this.treeDamage.setValue(treeDamage);
        return this;
    }

    public WeaponItemBuilder triggerExplosionTimer(int triggerExplosionTimer) {
        this.triggerExplosionTimer.setValue(triggerExplosionTimer);
        return this;
    }

    public WeaponItemBuilder useEndurance(boolean useEndurance) {
        this.useEndurance.setValue(useEndurance);
        return this;
    }

    public WeaponItemBuilder useSelf(boolean useSelf) {
        this.useSelf.setValue(useSelf);
        return this;
    }

    public WeaponItemBuilder weaponLength(float weaponLength) {
        this.weaponLength.setValue(Float.valueOf(weaponLength));
        return this;
    }

    public WeaponItemBuilder weaponReloadType(WeaponReloadType weaponReloadType) {
        this.weaponReloadType.setValue(weaponReloadType);
        return this;
    }

    public WeaponItemBuilder weaponSprite(ModelKey weaponSprite) {
        this.weaponSprite.setValue(weaponSprite);
        return this;
    }

    public WeaponItemBuilder weaponSpritesByIndex(ModelKey ... weaponSpritesByIndex) {
        this.weaponSpritesByIndex.addValues((ModelKey[])weaponSpritesByIndex);
        return this;
    }

    public WeaponItemBuilder weaponWeight(int weaponWeight) {
        this.weaponWeight.setValue(weaponWeight);
        return this;
    }
}

