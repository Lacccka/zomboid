/*
 * Decompiled with CFR 0.152.
 */
package zombie.randomizedWorld.randomizedBuilding;

import java.util.ArrayList;
import zombie.core.properties.IsoPropertyType;
import zombie.core.properties.PropertyContainer;
import zombie.core.random.Rand;
import zombie.inventory.InventoryItem;
import zombie.inventory.InventoryItemFactory;
import zombie.inventory.ItemContainer;
import zombie.inventory.ItemPickerJava;
import zombie.inventory.types.HandWeapon;
import zombie.inventory.types.InventoryContainer;
import zombie.iso.BuildingDef;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.sprite.IsoSprite;
import zombie.randomizedWorld.randomizedBuilding.RandomizedBuildingBase;
import zombie.scripting.objects.FaceType;
import zombie.scripting.objects.ItemKey;
import zombie.util.list.WeightedList;

public final class RBSWATStation
extends RandomizedBuildingBase {
    private static final int PROBABILITY_RIFLE_M16 = 40;
    private static final int PROBABILITY_RIFLE_MSR7T = 60;
    private static final int PROBABILITY_SHOTGUN_JS3T = 80;
    private static final int PROBABILITY_AMMO_308 = 60;
    private static final int PROBABILITY_AMMO_556 = 20;
    private static final int PROBABILITY_AMMO_9mm = 60;
    private static final int PROBABILITY_AMMO_45 = 20;
    private static final int PROBABILITY_AMMO_SHOTGUN = 80;
    private static final int PROBABILITY_SPAWN_RIFLE = 20;
    private static final int PROBABILITY_SPAWN_ARMOR = 5;
    private static final int PROBABILITY_SPAWN_BAG = 4;
    private static final WeightedList<ItemKey> RIFLES = new WeightedList();
    private static final WeightedList<ItemKey> AMMO_CANS = new WeightedList();

    @Override
    public void randomizeBuilding(BuildingDef def) {
        if (RIFLES.isEmpty()) {
            RIFLES.add(ItemKey.Weapon.ASSAULT_RIFLE, 40);
            RIFLES.add(ItemKey.Weapon.JS3T_SHOTGUN, 80);
            RIFLES.add(ItemKey.Weapon.MSR7T_RIFLE, 60);
        }
        if (AMMO_CANS.isEmpty()) {
            AMMO_CANS.add(ItemKey.Container.BAG_AMMO_BOX, 20);
            AMMO_CANS.add(ItemKey.Container.BAG_AMMO_BOX_45, 20);
            AMMO_CANS.add(ItemKey.Container.BAG_AMMO_BOX_308, 60);
            AMMO_CANS.add(ItemKey.Container.BAG_AMMO_BOX_9MM, 60);
            AMMO_CANS.add(ItemKey.Container.BAG_AMMO_BOX_SHOTGUN_SHELLS, 80);
        }
        ArrayList<IsoObject> objList = RBSWATStation.getBuildingObjectsSimple(def);
        for (IsoObject obj : objList) {
            ItemContainer container;
            IsoGridSquare sq = obj.getSquare();
            IsoSprite sprite = obj.getSprite();
            if (sprite == null || !RBSWATStation.roomValid(sq)) continue;
            PropertyContainer props = obj.getSprite().getProperties();
            String facing = props.get(IsoPropertyType.FACING);
            boolean facingE = FaceType.E.toString().equals(facing);
            boolean facingW = FaceType.W.toString().equals(facing);
            boolean facingN = FaceType.N.toString().equals(facing);
            boolean facingS = FaceType.S.toString().equals(facing);
            if (props.has(IsoPropertyType.CUSTOM_NAME) && props.get(IsoPropertyType.CUSTOM_NAME).contains("Gun")) {
                RBSWATStation.doGunShelfRifles(facingE, sq, RIFLES, 20);
                continue;
            }
            if (!RBSWATStation.validSurface(sq)) continue;
            if (props.has(IsoPropertyType.GROUP_NAME) && props.get(IsoPropertyType.GROUP_NAME).contains("Oak") && Rand.NextBool(5)) {
                RBSWATStation.doBodyArmorTable(facingS, sq);
                continue;
            }
            if (props.has(IsoPropertyType.GROUP_NAME) && props.get(IsoPropertyType.GROUP_NAME).contains("Smooth")) {
                if (Rand.NextBool(5)) {
                    RBSWATStation.doBodyArmorTable2(facingE, sq);
                    continue;
                }
                if (Rand.NextBool(4)) {
                    RBSWATStation.doWeaponBagTable(facingE, sq);
                    continue;
                }
            }
            if ((container = obj.getContainer()) == null || !obj.getContainer().getType().equals("locker")) continue;
            RBSWATStation.doAmmoCans(props, facingE, facingW, facingN, sq, AMMO_CANS);
        }
    }

    private static void doBodyArmorTable(boolean facingS, IsoGridSquare sq) {
        int armorSlots = 2;
        int helmetSlots = 2;
        float xOffset = facingS ? 0.64f : 0.42f;
        float xOffset2 = facingS ? 0.32f : 0.48f;
        float yOffset = facingS ? 0.28f : 0.68f;
        float yOffset2 = facingS ? 0.28f : 0.4f;
        float zOffset = 0.33f;
        float xRotation = 0.0f;
        float yRotation = 0.0f;
        float zRotation = facingS ? 270.0f : 0.0f;
        float zRotation2 = facingS ? 0.0f : 90.0f;
        for (int j = 0; j < 2; ++j) {
            Object vest = InventoryItemFactory.CreateItem(ItemKey.Clothing.VEST_BULLET_SWAT);
            sq.AddWorldInventoryItem((InventoryItem)vest, xOffset, yOffset, 0.33f, false, true);
            RBSWATStation.setWorldRotation(vest, 0.0f, 0.0f, zRotation);
            if (facingS) {
                yOffset += 0.36f;
                continue;
            }
            xOffset += 0.36f;
        }
        for (int i = 0; i < 2; ++i) {
            Object helm = InventoryItemFactory.CreateItem(ItemKey.Clothing.HAT_SWAT);
            sq.AddWorldInventoryItem((InventoryItem)helm, xOffset2, yOffset2, 0.33f, false, true);
            RBSWATStation.setWorldRotation(helm, 0.0f, 0.0f, zRotation2);
            if (facingS) {
                yOffset2 += 0.36f;
                continue;
            }
            xOffset2 += 0.36f;
        }
    }

    private static void doBodyArmorTable2(boolean facingE, IsoGridSquare sq) {
        int armorSlots = 2;
        int helmetSlots = 2;
        float xOffset = facingE ? 0.62f : 0.32f;
        float xOffset2 = facingE ? 0.32f : 0.38f;
        float yOffset = facingE ? 0.32f : 0.64f;
        float yOffset2 = facingE ? 0.38f : 0.32f;
        float zOffset = 0.25f;
        float xRotation = 0.0f;
        float yRotation = 0.0f;
        float zRotation = facingE ? 270.0f : 0.0f;
        float zRotation2 = facingE ? 0.0f : 90.0f;
        for (int j = 0; j < 2; ++j) {
            Object vest = InventoryItemFactory.CreateItem(ItemKey.Clothing.VEST_BULLET_SWAT);
            sq.AddWorldInventoryItem((InventoryItem)vest, xOffset, yOffset, 0.25f, false, true);
            RBSWATStation.setWorldRotation(vest, 0.0f, 0.0f, zRotation);
            if (facingE) {
                yOffset += 0.36f;
                continue;
            }
            xOffset += 0.36f;
        }
        for (int i = 0; i < 2; ++i) {
            Object helm = InventoryItemFactory.CreateItem(ItemKey.Clothing.HAT_SWAT);
            sq.AddWorldInventoryItem((InventoryItem)helm, xOffset2, yOffset2, 0.25f, false, true);
            RBSWATStation.setWorldRotation(helm, 0.0f, 0.0f, zRotation2);
            if (facingE) {
                yOffset2 += 0.36f;
                continue;
            }
            xOffset2 += 0.36f;
        }
    }

    private static void doWeaponBagTable(boolean facingE, IsoGridSquare sq) {
        float xOffsetGun = facingE ? 0.64f : 0.54f;
        float xOffsetBag = facingE ? 0.32f : 0.52f;
        float yOffsetGun = facingE ? 0.52f : 0.72f;
        float yOffsetBag = facingE ? 0.56f : 0.4f;
        float zOffset = 0.25f;
        float xRotation = 0.0f;
        float yRotation = 0.0f;
        float zRotationBag = facingE ? 0.0f : 90.0f;
        float zRotationGun = facingE ? 270.0f : 0.0f;
        HandWeapon gun = RBSWATStation.spawnRifle(RIFLES.getRandom());
        sq.AddWorldInventoryItem(gun, xOffsetGun, yOffsetGun, 0.25f, false, true);
        RBSWATStation.setWorldRotation(gun, 0.0f, 0.0f, zRotationGun);
        InventoryContainer bag = (InventoryContainer)InventoryItemFactory.CreateItem(ItemKey.Container.BAG_SWAT);
        sq.AddWorldInventoryItem(bag, xOffsetBag, yOffsetBag, 0.25f, false, true);
        ItemPickerJava.rollContainerItem(bag, null, ItemPickerJava.getItemPickerContainers().get(bag.getType()));
        RBSWATStation.setWorldRotation(bag, 0.0f, 0.0f, zRotationBag);
    }

    private static boolean validSurface(IsoGridSquare sq) {
        for (int i = 0; i < sq.getObjects().size(); ++i) {
            IsoSprite sprite = sq.getObjects().get(i).getSprite();
            PropertyContainer props1 = sprite.getProperties();
            if (!props1.has(IsoPropertyType.CUSTOM_NAME) || !props1.get(IsoPropertyType.CUSTOM_NAME).contains("Phone") && !props1.get(IsoPropertyType.CUSTOM_NAME).contains("Lamp") && !props1.get(IsoPropertyType.CUSTOM_NAME).contains("Radio")) continue;
            return false;
        }
        return true;
    }

    private static boolean roomValid(IsoGridSquare sq) {
        return sq.getRoom() != null && sq.getRoom().getName().equals("policeswat");
    }

    @Override
    public boolean isValid(BuildingDef def, boolean force) {
        return def.getRoom("policeswat") != null;
    }

    public RBSWATStation() {
        this.name = "SWAT Station";
        this.setAlwaysDo(true);
    }
}

