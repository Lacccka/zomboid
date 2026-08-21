/*
 * Decompiled with CFR 0.152.
 */
package zombie.randomizedWorld.randomizedBuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import se.krka.kahlua.vm.KahluaTable;
import zombie.Lua.LuaManager;
import zombie.SandboxOptions;
import zombie.UsedFromLua;
import zombie.VirtualZombieManager;
import zombie.ZombieSpawnRecorder;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.characters.SurvivorDesc;
import zombie.core.Core;
import zombie.core.properties.IsoPropertyType;
import zombie.core.properties.PropertyContainer;
import zombie.core.random.Rand;
import zombie.core.skinnedmodel.visual.HumanVisual;
import zombie.core.skinnedmodel.visual.IHumanVisual;
import zombie.core.skinnedmodel.visual.ItemVisuals;
import zombie.core.stash.StashSystem;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.inventory.InventoryItem;
import zombie.inventory.InventoryItemFactory;
import zombie.inventory.ItemContainer;
import zombie.inventory.ItemPickerJava;
import zombie.inventory.ItemSpawner;
import zombie.inventory.types.Food;
import zombie.inventory.types.HandWeapon;
import zombie.inventory.types.InventoryContainer;
import zombie.inventory.types.WeaponPart;
import zombie.iso.BuildingDef;
import zombie.iso.IsoCell;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.RoomDef;
import zombie.iso.SpawnPoints;
import zombie.iso.areas.IsoBuilding;
import zombie.iso.objects.IsoBarricade;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoWindow;
import zombie.iso.objects.interfaces.BarricadeAble;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.randomizedWorld.RandomizedWorldBase;
import zombie.randomizedWorld.randomizedBuilding.RBKateAndBaldspot;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.ItemTag;
import zombie.util.StringUtils;
import zombie.util.list.WeightedList;

@UsedFromLua
public class RandomizedBuildingBase
extends RandomizedWorldBase {
    private int chance;
    private static int totalChance;
    private static final HashMap<RandomizedBuildingBase, Integer> rbMap;
    protected static final int KBBuildingX = 10744;
    protected static final int KBBuildingY = 9409;
    private boolean alwaysDo;
    public static int maximumRoomCount;
    private static final HashMap<String, String> weaponsList;

    public void randomizeBuilding(BuildingDef def) {
        def.alarmed = false;
    }

    public void init() {
        if (!weaponsList.isEmpty()) {
            return;
        }
        weaponsList.put("Base.Shotgun", "Base.ShotgunShellsBox");
        weaponsList.put("Base.Pistol", "Base.Bullets9mmBox");
        weaponsList.put("Base.Pistol2", "Base.Bullets45Box");
        weaponsList.put("Base.Pistol3", "Base.Bullets44Box");
        weaponsList.put("Base.VarmintRifle", "Base.556Box");
        weaponsList.put("Base.HuntingRifle", "Base.308Box");
    }

    public static void initAllRBMapChance() {
        for (int i = 0; i < IsoWorld.instance.getRandomizedBuildingList().size(); ++i) {
            totalChance += IsoWorld.instance.getRandomizedBuildingList().get(i).getChance();
            rbMap.put(IsoWorld.instance.getRandomizedBuildingList().get(i), IsoWorld.instance.getRandomizedBuildingList().get(i).getChance());
        }
    }

    public boolean isValid(BuildingDef def, boolean force) {
        this.debugLine = "";
        if (GameClient.client) {
            return false;
        }
        if (StashSystem.isStashBuilding(def)) {
            this.debugLine = "Stash buildings are invalid";
            return false;
        }
        if (def.isAllExplored() && !force) {
            return false;
        }
        if (!GameServer.server) {
            if (!force && IsoPlayer.getInstance().getSquare() != null && IsoPlayer.getInstance().getSquare().getBuilding() != null && IsoPlayer.getInstance().getSquare().getBuilding().def == def) {
                this.customizeStartingHouse(IsoPlayer.getInstance().getSquare().getBuilding().def);
                return false;
            }
        } else if (!force) {
            for (int i = 0; i < GameServer.Players.size(); ++i) {
                IsoPlayer player = GameServer.Players.get(i);
                if (player.getSquare() == null || player.getSquare().getBuilding() == null || player.getSquare().getBuilding().def != def) continue;
                return false;
            }
        }
        boolean bedroom = false;
        boolean kitchen = false;
        boolean bathroom = false;
        for (int i = 0; i < def.rooms.size(); ++i) {
            RoomDef room = def.rooms.get(i);
            if ("bedroom".equals(room.name)) {
                bedroom = true;
            }
            if ("kitchen".equals(room.name) || "livingroom".equals(room.name)) {
                kitchen = true;
            }
            if (!"bathroom".equals(room.name)) continue;
            bathroom = true;
        }
        if (!bedroom) {
            this.debugLine = this.debugLine + "no bedroom ";
        }
        if (!bathroom) {
            this.debugLine = this.debugLine + "no bathroom ";
        }
        if (!kitchen) {
            this.debugLine = this.debugLine + "no living room or kitchen ";
        }
        return bedroom && bathroom && kitchen;
    }

    private void customizeStartingHouse(BuildingDef def) {
    }

    public int getMinimumDays() {
        return this.minimumDays;
    }

    public void setMinimumDays(int minimumDays) {
        this.minimumDays = minimumDays;
    }

    public int getMinimumRooms() {
        return this.minimumRooms;
    }

    public void setMinimumRooms(int minimumRooms) {
        this.minimumRooms = minimumRooms;
    }

    public static void ChunkLoaded(IsoBuilding building) {
        boolean debugSpam = false;
        if (!GameClient.client && building.def != null && !building.def.seen && building.def.isFullyStreamedIn()) {
            int i;
            boolean tooManyRooms;
            int roomCount = building.rooms.size();
            boolean bl = tooManyRooms = roomCount > maximumRoomCount;
            if (GameServer.server && GameServer.Players.isEmpty()) {
                return;
            }
            for (int i2 = 0; i2 < roomCount; ++i2) {
                if (!building.rooms.get((int)i2).def.explored) continue;
                return;
            }
            building.def.seen = true;
            if (!building.def.isAnyChunkNewlyLoaded()) {
                return;
            }
            ArrayList<RandomizedBuildingBase> forcedStory = new ArrayList<RandomizedBuildingBase>();
            if (!tooManyRooms) {
                for (i = 0; i < IsoWorld.instance.getRandomizedBuildingList().size(); ++i) {
                    RandomizedBuildingBase testTable = IsoWorld.instance.getRandomizedBuildingList().get(i);
                    if (testTable.reallyAlwaysForce && testTable.isValid(building.def, false)) {
                        testTable.randomizeBuilding(building.def);
                        continue;
                    }
                    if (!testTable.isAlwaysDo() || !testTable.isValid(building.def, false)) continue;
                    forcedStory.add(testTable);
                }
            }
            if (tooManyRooms) {
                DebugLog.log("Building is too large for a  Building Story with " + roomCount + " rooms  at " + building.def.x + ", " + building.def.y + " and is rejected.");
                return;
            }
            if (building.def.x == 10744 && building.def.y == 9409 && Rand.Next(100) < 31) {
                RBKateAndBaldspot rb = new RBKateAndBaldspot();
                ((RandomizedBuildingBase)rb).randomizeBuilding(building.def);
                return;
            }
            if (!forcedStory.isEmpty()) {
                for (i = 0; i < forcedStory.size(); ++i) {
                    RandomizedBuildingBase rb = (RandomizedBuildingBase)forcedStory.get(i);
                    if (rb == null) continue;
                    rb.randomizeBuilding(building.def);
                }
            }
            if (SpawnPoints.instance.isSpawnBuilding(building.getDef())) {
                return;
            }
            RandomizedBuildingBase rb = IsoWorld.instance.getRBBasic();
            if ("Tutorial".equals(Core.gameMode)) {
                return;
            }
            try {
                int chance = 10;
                switch (SandboxOptions.instance.survivorHouseChance.getValue()) {
                    case 1: {
                        return;
                    }
                    case 2: {
                        chance -= 5;
                        break;
                    }
                    case 4: {
                        chance += 5;
                        break;
                    }
                    case 5: {
                        chance += 10;
                        break;
                    }
                    case 6: {
                        chance += 20;
                    }
                }
                if (SandboxOptions.instance.survivorHouseChance.getValue() == 7 || Rand.Next(100) <= chance) {
                    if (totalChance == 0) {
                        RandomizedBuildingBase.initAllRBMapChance();
                    }
                    if ((rb = RandomizedBuildingBase.getRandomStory()) == null) {
                        return;
                    }
                }
                if (rb.isValid(building.def, false) && rb.isTimeValid(false)) {
                    rb.randomizeBuilding(building.def);
                }
            }
            catch (Exception ex) {
                DebugType.General.printException(ex, LogSeverity.Error);
            }
        }
    }

    public int getChance() {
        return this.getChance(null);
    }

    public int getChance(IsoGridSquare sq) {
        if (Objects.equals(this.name, "Rat Infested House")) {
            int ratFactor = SandboxOptions.instance.getCurrentRatIndex() / 10;
            if (ratFactor < 0) {
                ratFactor = 1;
            }
            return ratFactor;
        }
        if (Objects.equals(this.name, "Trashed Building")) {
            return SandboxOptions.instance.getCurrentLootedChance(sq);
        }
        return this.chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public boolean isAlwaysDo() {
        return this.alwaysDo;
    }

    public void setAlwaysDo(boolean alwaysDo) {
        this.alwaysDo = alwaysDo;
    }

    private static RandomizedBuildingBase getRandomStory() {
        int choice = Rand.Next(totalChance);
        Iterator<RandomizedBuildingBase> it = rbMap.keySet().iterator();
        int subTotal = 0;
        while (it.hasNext()) {
            RandomizedBuildingBase testTable = it.next();
            if (choice >= (subTotal += rbMap.get(testTable).intValue())) continue;
            return testTable;
        }
        return null;
    }

    @Override
    public ArrayList<IsoZombie> addZombiesOnSquare(int totalZombies, String outfit, Integer femaleChance, IsoGridSquare square) {
        if (IsoWorld.getZombiesDisabled() || "Tutorial".equals(Core.gameMode)) {
            return null;
        }
        ArrayList<IsoZombie> result = new ArrayList<IsoZombie>();
        for (int j = 0; j < totalZombies; ++j) {
            VirtualZombieManager.instance.choices.clear();
            VirtualZombieManager.instance.choices.add(square);
            IsoZombie zombie = VirtualZombieManager.instance.createRealZombieAlways(IsoDirections.getRandom(), false);
            if (zombie == null) continue;
            if ("Kate".equals(outfit) || "Bob".equals(outfit) || "Raider".equals(outfit)) {
                zombie.doDirtBloodEtc = false;
            }
            if (femaleChance != null) {
                zombie.setFemaleEtc(Rand.Next(100) < femaleChance);
            }
            if (outfit != null) {
                zombie.dressInPersistentOutfit(outfit);
                zombie.dressInRandomOutfit = false;
            } else {
                zombie.dressInRandomOutfit = true;
            }
            result.add(zombie);
        }
        ZombieSpawnRecorder.instance.record(result, this.getClass().getSimpleName());
        return result;
    }

    public ArrayList<IsoZombie> addZombies(BuildingDef def, int totalZombies, String outfit, Integer femaleChance, RoomDef room) {
        IsoGridSquare sq;
        boolean randomizeRoom = room == null;
        ArrayList<IsoZombie> result = new ArrayList<IsoZombie>();
        if (IsoWorld.getZombiesDisabled() || "Tutorial".equals(Core.gameMode)) {
            return result;
        }
        if (room == null) {
            room = this.getRandomRoom(def, 6);
        }
        int min = 2;
        int max = room.area / 2;
        if (totalZombies == 0) {
            if (SandboxOptions.instance.zombies.getValue() == 1) {
                max += 4;
            } else if (SandboxOptions.instance.zombies.getValue() == 2) {
                max += 3;
            } else if (SandboxOptions.instance.zombies.getValue() == 3) {
                max += 2;
            } else if (SandboxOptions.instance.zombies.getValue() == 5) {
                max -= 4;
            }
            if (max > 8) {
                max = 8;
            }
            if (max < min) {
                max = min + 1;
            }
        } else {
            max = min = totalZombies;
        }
        int rand = Rand.Next(min, max);
        for (int j = 0; j < rand && (sq = RandomizedBuildingBase.getRandomSpawnSquare(room)) != null; ++j) {
            VirtualZombieManager.instance.choices.clear();
            VirtualZombieManager.instance.choices.add(sq);
            IsoZombie zombie = VirtualZombieManager.instance.createRealZombieAlways(IsoDirections.getRandom(), false);
            if (zombie == null) continue;
            if (femaleChance != null) {
                zombie.setFemaleEtc(Rand.Next(100) < femaleChance);
            }
            if (outfit != null) {
                zombie.dressInPersistentOutfit(outfit);
                zombie.dressInRandomOutfit = false;
            } else {
                zombie.dressInRandomOutfit = true;
            }
            result.add(zombie);
            if (!randomizeRoom) continue;
            room = this.getRandomRoom(def, 6);
        }
        ZombieSpawnRecorder.instance.record(result, this.getClass().getSimpleName());
        return result;
    }

    public HandWeapon addRandomRangedWeapon(ItemContainer container, boolean addBulletsInGun, boolean addBoxInContainer, boolean attachPart) {
        ArrayList<String> weapons;
        String selectedWeapon;
        HandWeapon weapon;
        if (weaponsList == null || weaponsList.isEmpty()) {
            this.init();
        }
        if ((weapon = this.addWeapon(selectedWeapon = (weapons = new ArrayList<String>(weaponsList.keySet())).get(Rand.Next(0, weapons.size())), addBulletsInGun)) == null) {
            return null;
        }
        if (addBoxInContainer) {
            container.addItem((InventoryItem)InventoryItemFactory.CreateItem(weaponsList.get(selectedWeapon)));
        }
        if (attachPart) {
            KahluaTable weaponDistrib = (KahluaTable)LuaManager.env.rawget("WeaponUpgrades");
            if (weaponDistrib == null) {
                return null;
            }
            KahluaTable weaponUpgrade = (KahluaTable)weaponDistrib.rawget(weapon.getType());
            if (weaponUpgrade == null) {
                return null;
            }
            int upgrades = Rand.Next(1, weaponUpgrade.len() + 1);
            for (int u = 1; u <= upgrades; ++u) {
                int r = Rand.Next(weaponUpgrade.len()) + 1;
                WeaponPart part = (WeaponPart)InventoryItemFactory.CreateItem((String)weaponUpgrade.rawget(r));
                if (part == null || part.getScriptItem().obsolete) continue;
                weapon.attachWeaponPart(part);
            }
        }
        return weapon;
    }

    public void spawnItemsInContainers(BuildingDef def, String distribName, int chance) {
        ArrayList<ItemContainer> container = new ArrayList<ItemContainer>();
        ItemPickerJava.ItemPickerRoom contDistrib = ItemPickerJava.rooms.get(distribName);
        IsoCell cell = IsoWorld.instance.currentCell;
        for (int x = def.x - 1; x < def.x2 + 1; ++x) {
            for (int y = def.y - 1; y < def.y2 + 1; ++y) {
                for (int z = -32; z < 31; ++z) {
                    IsoGridSquare sq = cell.getGridSquare(x, y, z);
                    if (sq == null) continue;
                    for (int o = 0; o < sq.getObjects().size(); ++o) {
                        IsoObject obj = sq.getObjects().get(o);
                        if (Rand.Next(100) > chance || obj.getContainer() == null || sq.getRoom() == null || sq.getRoom().getName() == null || !contDistrib.containers.containsKey(obj.getContainer().getType())) continue;
                        obj.getContainer().clear();
                        container.add(obj.getContainer());
                        obj.getContainer().setExplored(true);
                    }
                }
            }
        }
        for (int i = 0; i < container.size(); ++i) {
            ItemContainer cont = (ItemContainer)container.get(i);
            ItemPickerJava.fillContainerType(contDistrib, cont, "", null);
            ItemPickerJava.updateOverlaySprite(cont.getParent());
            if (!GameServer.server) continue;
            GameServer.sendItemsInContainer(cont.getParent(), cont);
        }
    }

    protected void removeAllZombies(BuildingDef def) {
        for (int x = def.x - 1; x < def.x + def.x2 + 1; ++x) {
            for (int y = def.y - 1; y < def.y + def.y2 + 1; ++y) {
                for (int z = -32; z < 31; ++z) {
                    IsoGridSquare sq = RandomizedBuildingBase.getSq(x, y, z);
                    if (sq == null) continue;
                    for (int i = 0; i < sq.getMovingObjects().size(); ++i) {
                        sq.getMovingObjects().remove(i);
                        --i;
                    }
                }
            }
        }
    }

    public IsoWindow getWindow(IsoGridSquare sq) {
        for (int o = 0; o < sq.getObjects().size(); ++o) {
            IsoObject obj = sq.getObjects().get(o);
            if (!(obj instanceof IsoWindow)) continue;
            IsoWindow isoWindow = (IsoWindow)obj;
            return isoWindow;
        }
        return null;
    }

    public IsoDoor getDoor(IsoGridSquare sq) {
        for (int o = 0; o < sq.getObjects().size(); ++o) {
            IsoObject obj = sq.getObjects().get(o);
            if (!(obj instanceof IsoDoor)) continue;
            IsoDoor isoDoor = (IsoDoor)obj;
            return isoDoor;
        }
        return null;
    }

    public void addBarricade(IsoGridSquare sq, int numPlanks) {
        for (int o = 0; o < sq.getObjects().size(); ++o) {
            IsoWindow isoWindow;
            int b;
            boolean addOpposite;
            IsoBarricade barricade;
            IsoGridSquare outside;
            IsoObject obj = sq.getObjects().get(o);
            if (obj instanceof IsoDoor) {
                IsoDoor isoDoor = (IsoDoor)obj;
                if (!isoDoor.isBarricadeAllowed()) continue;
                IsoGridSquare isoGridSquare = outside = sq.getRoom() == null ? sq : isoDoor.getOppositeSquare();
                if (outside != null && outside.getRoom() == null && (barricade = IsoBarricade.AddBarricadeToObject((BarricadeAble)isoDoor, addOpposite = outside != sq)) != null) {
                    for (b = 0; b < numPlanks; ++b) {
                        barricade.addPlank(null, null);
                    }
                    if (GameServer.server) {
                        barricade.transmitCompleteItemToClients();
                    }
                }
            }
            if (!(obj instanceof IsoWindow) || !(isoWindow = (IsoWindow)obj).isBarricadeAllowed() || (barricade = IsoBarricade.AddBarricadeToObject((BarricadeAble)isoWindow, addOpposite = (outside = sq.getRoom() == null ? sq : isoWindow.getOppositeSquare()) != sq)) == null) continue;
            for (b = 0; b < numPlanks; ++b) {
                barricade.addPlank(null, null);
            }
            if (!GameServer.server) continue;
            barricade.transmitCompleteItemToClients();
        }
    }

    public InventoryItem addWorldItem(String item, IsoGridSquare sq, float xoffset, float yoffset, float zoffset) {
        return this.addWorldItem(item, sq, xoffset, yoffset, zoffset, 0);
    }

    public InventoryItem addWorldItem(String item, IsoGridSquare sq, float xoffset, float yoffset, float zoffset, boolean randomRotation) {
        if (randomRotation) {
            return this.addWorldItem(item, sq, xoffset, yoffset, zoffset, Rand.Next(360));
        }
        return this.addWorldItem(item, sq, xoffset, yoffset, zoffset, 0);
    }

    public InventoryItem addWorldItem(String item, IsoGridSquare sq, float xoffset, float yoffset, float zoffset, int worldZ) {
        if (item == null || sq == null) {
            return null;
        }
        if (SandboxOptions.instance.removeStoryLoot.getValue() && ItemPickerJava.getLootModifier(item) == 0.0f) {
            return null;
        }
        Object invItem = InventoryItemFactory.CreateItem(item);
        if (invItem != null) {
            ((InventoryItem)invItem).setAutoAge();
            ((InventoryItem)invItem).setWorldZRotation(worldZ);
            if (((InventoryItem)invItem).hasTag(ItemTag.SPAWN_COOKED)) {
                Object functionObj;
                ((InventoryItem)invItem).setCooked(true);
                if (!StringUtils.isNullOrEmpty(((Food)invItem).getOnCooked()) && (functionObj = LuaManager.getFunctionObject(((Food)invItem).getOnCooked())) != null) {
                    LuaManager.caller.pcallvoid(LuaManager.thread, functionObj, invItem);
                }
            }
            if (invItem instanceof HandWeapon) {
                HandWeapon handWeapon = (HandWeapon)invItem;
                if (Rand.Next(100) < 90) {
                    handWeapon.randomizeFirearmAsLoot();
                }
            }
            if (((InventoryItem)invItem).hasTag(ItemTag.SHOW_CONDITION) || invItem instanceof HandWeapon || ((InventoryItem)invItem).hasSharpness()) {
                ((InventoryItem)invItem).randomizeGeneralCondition();
            }
            ((InventoryItem)invItem).unsealIfNotFull();
            if (invItem instanceof InventoryContainer) {
                InventoryContainer inventoryContainer = (InventoryContainer)invItem;
                inventoryContainer.getItemContainer().setExplored(true);
            }
            return ItemSpawner.spawnItem(invItem, sq, xoffset, yoffset, zoffset);
        }
        return null;
    }

    public InventoryItem addWorldItem(String item, IsoGridSquare sq, IsoObject obj) {
        return this.addWorldItem(item, sq, obj, false);
    }

    public InventoryItem addWorldItem(String item, IsoGridSquare sq, IsoObject obj, boolean randomRotation) {
        Object invItem;
        if (item == null || sq == null || !sq.hasAdjacentCanStandSquare()) {
            return null;
        }
        if (SandboxOptions.instance.removeStoryLoot.getValue() && ItemPickerJava.getLootModifier(item) == 0.0f) {
            return null;
        }
        float z = 0.0f;
        if (obj != null) {
            z = obj.getSurfaceOffsetNoTable() / 96.0f;
        }
        if ((invItem = InventoryItemFactory.CreateItem(item)) != null) {
            ((InventoryItem)invItem).setAutoAge();
            if (randomRotation) {
                ((InventoryItem)invItem).randomizeWorldZRotation();
            }
            return ItemSpawner.spawnItem(invItem, sq, Rand.Next(0.3f, 0.9f), Rand.Next(0.3f, 0.9f), z);
        }
        return null;
    }

    public boolean isTableFor3DItems(IsoObject obj, IsoGridSquare sq) {
        return sq.hasAdjacentCanStandSquare() && obj.getSurfaceOffsetNoTable() > 0.0f && obj.getContainer() == null && !sq.hasWater() && !obj.hasFluid() && obj.getProperties().get("BedType") == null;
    }

    public InventoryItem trySpawnStoryItem(String itemType, IsoGridSquare square, IsoObject obj) {
        if (SandboxOptions.instance.removeStoryLoot.getValue() && ItemPickerJava.getLootModifier(itemType) == 0.0f) {
            return null;
        }
        return this.addWorldItem("PlasticFork", square, obj);
    }

    public static ArrayList<IsoObject> getBuildingObjectsSimple(BuildingDef def) {
        List<IsoGridSquare> defSquares = def.getSquares();
        ArrayList<IsoObject> objList = new ArrayList<IsoObject>();
        for (IsoGridSquare sq : defSquares) {
            for (int i = 0; i < sq.getObjects().size(); ++i) {
                if (sq.getObjects().get(i) == null) continue;
                objList.add(sq.getObjects().get(i));
            }
        }
        return objList;
    }

    public static ArrayList<IsoObject> getBuildingObjects(BuildingDef def) {
        ArrayList<IsoObject> objList = new ArrayList<IsoObject>();
        ArrayList<IsoGridSquare> buildingSquares = RandomizedBuildingBase.getBuildingSquares(def);
        for (int i = 0; i < buildingSquares.size(); ++i) {
            IsoGridSquare sq = buildingSquares.get(i);
            for (int j = 0; j < sq.getObjects().size(); ++j) {
                if (sq.getObjects().get(j) == null) continue;
                objList.add(sq.getObjects().get(j));
            }
        }
        return objList;
    }

    public static ArrayList<IsoGridSquare> getBuildingSquares(BuildingDef def) {
        ArrayList<RoomDef> rooms = def.getRooms();
        ArrayList<IsoGridSquare> buildingSquares = new ArrayList<IsoGridSquare>();
        for (int i = 0; i < rooms.size(); ++i) {
            RoomDef room = rooms.get(i);
            ArrayList<RoomDef.RoomRect> rects = room.getRects();
            for (int j = 0; j < rects.size(); ++j) {
                RoomDef.RoomRect rect = rects.get(j);
                ArrayList<IsoGridSquare> rectSquares = RandomizedBuildingBase.getRectSquares(rect, room);
                buildingSquares.addAll(rectSquares);
            }
        }
        return buildingSquares;
    }

    public static ArrayList<IsoGridSquare> getRectSquares(RoomDef.RoomRect rect, RoomDef room) {
        ArrayList<IsoGridSquare> squares = new ArrayList<IsoGridSquare>();
        for (int x = rect.getX(); x < rect.getX2(); ++x) {
            for (int y = rect.getY(); y < rect.getY2(); ++y) {
                IsoGridSquare sq = IsoWorld.instance.currentCell.getGridSquare(x, y, room.getZ());
                if (sq == null || sq.getRoom() == null || !sq.getRoom().getName().contains("gunstore")) continue;
                squares.add(sq);
            }
        }
        return squares;
    }

    public static void setWorldRotation(InventoryItem item, float xRotation, float yRotation, float zRotation) {
        item.setWorldXRotation(xRotation);
        item.setWorldYRotation(yRotation);
        item.setWorldZRotation(zRotation);
        if (GameServer.server && item.getWorldItem() != null) {
            item.getWorldItem().transmitCompleteItemToClients();
        }
    }

    public static void addClip(HandWeapon gun) {
        if (gun.usesExternalMagazine() && !gun.isContainsClip()) {
            gun.setContainsClip(true);
        }
    }

    public static HandWeapon spawnPistol(ItemKey gunType) {
        HandWeapon gun = (HandWeapon)InventoryItemFactory.CreateItem(gunType);
        RandomizedBuildingBase.rollWeaponUpgrades(gun);
        RandomizedBuildingBase.addClip(gun);
        return gun;
    }

    public static HandWeapon spawnRifle(ItemKey gunType) {
        HandWeapon gun = (HandWeapon)InventoryItemFactory.CreateItem(gunType);
        RandomizedBuildingBase.rollWeaponUpgrades(gun);
        RandomizedBuildingBase.addClip(gun);
        return gun;
    }

    public static void doGunShelfRifles(boolean facingE, IsoGridSquare sq, WeightedList<ItemKey> rifleTypes, int spawnChance) {
        int slotNumber = 4;
        float xOffset = facingE ? 0.12f : 0.56f;
        float yOffset = facingE ? 0.56f : 0.12f;
        float zOffset = 0.48f;
        float xRotation = facingE ? 90.0f : 270.0f;
        float yRotation = facingE ? 90.0f : 0.0f;
        float zRotation = facingE ? 180.0f : 0.0f;
        for (int n = 0; n < 4; ++n) {
            if (Rand.Next(100) >= spawnChance) continue;
            HandWeapon gun = RandomizedBuildingBase.spawnRifle(rifleTypes.getRandom());
            sq.AddWorldInventoryItem(gun, xOffset, yOffset, zOffset, false, true);
            RandomizedBuildingBase.setWorldRotation(gun, xRotation, yRotation, zRotation);
            zOffset += 0.08f;
        }
    }

    public static void doGunShelfHandguns(boolean facingE, IsoGridSquare sq, WeightedList<ItemKey> pistolTypes, WeightedList<ItemKey> rifleTypes, int spawnChancePistol, int spawnChanceRifle) {
        int column1Slots = 4;
        float xOffset = facingE ? 0.12f : 0.2f;
        float yOffset = facingE ? 0.92f : 0.12f;
        float zOffset = 0.48f;
        float xRotation = facingE ? 90.0f : 270.0f;
        float yRotation = facingE ? 90.0f : 0.0f;
        float zRotation = facingE ? 180.0f : 0.0f;
        for (int n = 0; n < 4; ++n) {
            if (Rand.Next(100) >= spawnChancePistol) continue;
            HandWeapon gun = RandomizedBuildingBase.spawnPistol(pistolTypes.getRandom());
            sq.AddWorldInventoryItem(gun, xOffset, yOffset, zOffset, false, true);
            RandomizedBuildingBase.setWorldRotation(gun, xRotation, yRotation, zRotation);
            zOffset += 0.08f;
        }
        int column2Slots = 4;
        float xOffset2 = facingE ? 0.12f : 0.46f;
        float yOffset2 = facingE ? 0.64f : 0.12f;
        float zOffset2 = 0.48f;
        for (int n = 0; n < 4; ++n) {
            if (Rand.Next(100) >= spawnChancePistol) continue;
            HandWeapon gun = RandomizedBuildingBase.spawnPistol(pistolTypes.getRandom());
            sq.AddWorldInventoryItem(gun, xOffset2, yOffset2, zOffset2, false, true);
            RandomizedBuildingBase.setWorldRotation(gun, xRotation, yRotation, zRotation);
            zOffset2 += 0.08f;
        }
        int rifleSlots = 2;
        float xOffset3 = facingE ? 0.12f : 0.7f;
        float yOffset3 = facingE ? 0.2f : 0.12f;
        float zOffset3 = 0.6f;
        float xRotation3 = 0.0f;
        float yRotation3 = 90.0f;
        float zRotation3 = facingE ? 180.0f : 270.0f;
        for (int n = 0; n < 2; ++n) {
            if (Rand.Next(100) >= spawnChanceRifle) continue;
            HandWeapon gun = RandomizedBuildingBase.spawnRifle(rifleTypes.getRandom());
            sq.AddWorldInventoryItem(gun, xOffset3, yOffset3, 0.6f, false, true);
            RandomizedBuildingBase.setWorldRotation(gun, 0.0f, 90.0f, zRotation3);
            yOffset3 += facingE ? 0.18f : 0.0f;
            xOffset3 += !facingE ? 0.18f : 0.0f;
        }
    }

    public static void doAmmoCans(PropertyContainer props, boolean facingE, boolean facingW, boolean facingN, IsoGridSquare sq, WeightedList<ItemKey> ammoCans) {
        float xOffset;
        if (props.has(IsoPropertyType.GROUP_NAME) && !props.get(IsoPropertyType.GROUP_NAME).contains("Green")) {
            return;
        }
        int ammoSlots = Rand.NextInclusive(1, 6);
        float f = facingE ? 0.34f : (xOffset = facingW ? 0.82f : 0.2f);
        float yOffset = facingE ? 0.2f : (facingN ? 0.82f : (facingW ? 0.2f : 0.34f));
        float zOffset = 0.52f;
        float xRotation = 0.0f;
        float yRotation = 0.0f;
        float zRotation = facingE ? 90.0f : (facingN ? 0.0f : (facingW ? 270.0f : 180.0f));
        for (int j = 0; j < ammoSlots; ++j) {
            InventoryContainer ammoCan = (InventoryContainer)InventoryItemFactory.CreateItem(ammoCans.getRandom());
            sq.AddWorldInventoryItem(ammoCan, xOffset, yOffset, 0.52f, false, true);
            ItemPickerJava.rollContainerItem(ammoCan, null, ItemPickerJava.getItemPickerContainers().get(ammoCan.getType()));
            RandomizedBuildingBase.setWorldRotation(ammoCan, 0.0f, 0.0f, zRotation);
            if (facingE || facingW) {
                yOffset += 0.14f;
                continue;
            }
            xOffset += 0.14f;
        }
    }

    public static void doHandgunCounterDisplay(boolean facingE, boolean facingW, boolean facingN, IsoGridSquare sq, WeightedList<ItemKey> pistolTypes) {
        int j;
        float zRotation2;
        float yOffset2;
        float xOffset2;
        float xOffset;
        float f = facingE ? 0.8f : (facingW ? 0.42f : (xOffset = facingN ? 0.82f : 0.38f));
        float yOffset = facingE ? 0.8f : (facingW ? 0.4f : (facingN ? 0.44f : 0.8f));
        float zOffset = 0.38f;
        float xRotation = 0.0f;
        float yRotation = 0.0f;
        float zRotation = facingE ? 270.0f : (facingW ? 90.0f : (facingN ? 180.0f : 0.0f));
        HandWeapon gun = RandomizedBuildingBase.spawnPistol(pistolTypes.getRandom());
        sq.AddWorldInventoryItem(gun, xOffset, yOffset, 0.38f, false, true);
        RandomizedBuildingBase.setWorldRotation(gun, 0.0f, 0.0f, zRotation);
        if (!StringUtils.isNullOrEmpty(gun.getMagazineType())) {
            float f2 = facingE ? 0.88f : (xOffset2 = facingW ? 0.32f : 0.58f);
            float f3 = facingE ? 0.62f : (facingW ? 0.58f : (yOffset2 = facingN ? 0.34f : 0.84f));
            zRotation2 = facingE ? 180.0f : (facingW ? 0.0f : (facingN ? 90.0f : 270.0f));
            int clipSlots = Rand.NextInclusive(1, 2);
            for (j = 0; j < clipSlots; ++j) {
                Object clip = InventoryItemFactory.CreateItem(gun.getMagazineType());
                sq.AddWorldInventoryItem((InventoryItem)clip, xOffset2, yOffset2, 0.38f, false, true);
                RandomizedBuildingBase.setWorldRotation(clip, 0.0f, 0.0f, zRotation2);
                yOffset2 -= facingE ? 0.06f : 0.0f;
                yOffset2 += facingW ? 0.06f : 0.0f;
                xOffset2 -= facingN ? 0.06f : 0.0f;
                xOffset2 += !facingE && !facingW && !facingN ? 0.06f : 0.0f;
            }
        }
        if (!StringUtils.isNullOrEmpty(gun.getAmmoBox())) {
            float f4 = facingE ? 0.76f : (facingW ? 0.48f : (xOffset2 = facingN ? 0.32f : 0.84f));
            yOffset2 = facingE ? 0.38f : (facingW ? 0.84f : (facingN ? 0.48f : 0.78f));
            zRotation2 = facingE ? 270.0f : 0.0f;
            int boxSlots = Rand.NextInclusive(1, 2);
            for (j = 0; j < boxSlots; ++j) {
                Object ammoBox = InventoryItemFactory.CreateItem(gun.getAmmoBox());
                sq.AddWorldInventoryItem((InventoryItem)ammoBox, xOffset2, yOffset2, 0.38f, false, true);
                RandomizedBuildingBase.setWorldRotation(ammoBox, 0.0f, 0.0f, zRotation2);
                xOffset2 += facingE ? 0.12f : 0.0f;
                xOffset2 -= facingW ? 0.12f : 0.0f;
                yOffset2 -= facingN ? 0.12f : 0.0f;
                yOffset2 += !facingE && !facingW && !facingN ? 0.12f : 0.0f;
            }
        }
    }

    public static void doRifleCounterDisplay(boolean facingE, boolean facingW, boolean facingN, IsoGridSquare sq, WeightedList<ItemKey> rifleTypes) {
        float xOffset;
        float f = facingE ? 0.82f : (xOffset = facingW ? 0.42f : 0.6f);
        float yOffset = facingE ? 0.58f : (facingW ? 0.62f : (facingN ? 0.42f : 0.86f));
        float zOffset = 0.38f;
        float xRotation = 0.0f;
        float yRotation = 0.0f;
        float standardZRotation = facingE ? 270.0f : (facingW ? 90.0f : (facingN ? 180.0f : 0.0f));
        HandWeapon gun = RandomizedBuildingBase.spawnRifle(rifleTypes.getRandom());
        sq.AddWorldInventoryItem(gun, xOffset, yOffset, 0.38f, false, true);
        RandomizedBuildingBase.setWorldRotation(gun, 0.0f, 0.0f, standardZRotation);
        if (!StringUtils.isNullOrEmpty(gun.getAmmoBox())) {
            float xOffset2;
            float f2 = facingE ? 0.66f : (facingW ? 0.58f : (xOffset2 = facingN ? 0.88f : 0.32f));
            float yOffset2 = facingE ? 0.88f : (facingW ? 0.34f : (facingN ? 0.6f : 0.68f));
            int boxSlots = Rand.NextInclusive(1, 2);
            for (int j = 0; j < boxSlots; ++j) {
                Object ammoBox = InventoryItemFactory.CreateItem(gun.getAmmoBox());
                sq.AddWorldInventoryItem((InventoryItem)ammoBox, xOffset2, yOffset2, 0.38f, false, true);
                RandomizedBuildingBase.setWorldRotation(ammoBox, 0.0f, 0.0f, standardZRotation);
                yOffset2 -= facingE ? 0.22f : 0.0f;
                yOffset2 += facingW ? 0.22f : 0.0f;
                xOffset2 -= facingN ? 0.22f : 0.0f;
                xOffset2 += !facingE && !facingW && !facingN ? 0.22f : 0.0f;
            }
        }
    }

    public static void doCounterAmmoDisplay(boolean facingE, boolean facingW, boolean facingN, IsoGridSquare sq, WeightedList<ItemKey> ammoBoxes) {
        float xOffset;
        int columns = 3;
        float f = facingE ? 0.2f : (xOffset = facingW ? 0.94f : 0.24f);
        float yOffset = facingE ? 0.84f : (facingW ? 0.84f : (facingN ? 0.94f : 0.24f));
        float zOffset = 0.38f;
        float xRotation = 0.0f;
        float yRotation = 0.0f;
        float zRotation = facingE ? 270.0f : (facingW ? 90.0f : (facingN ? 180.0f : 0.0f));
        for (int j = 0; j < 3; ++j) {
            float xOffset2 = 0.0f;
            float yOffset2 = 0.0f;
            for (int k = 0; k < Rand.NextInclusive(1, 4); ++k) {
                if (xOffset2 == 0.0f || yOffset2 == 0.0f) {
                    xOffset2 = xOffset;
                    yOffset2 = yOffset;
                }
                Object ammoBox = InventoryItemFactory.CreateItem(ammoBoxes.getRandom());
                sq.AddWorldInventoryItem((InventoryItem)ammoBox, xOffset2, yOffset2, 0.38f, false, true);
                RandomizedBuildingBase.setWorldRotation(ammoBox, 0.0f, 0.0f, zRotation);
                xOffset2 += facingE ? 0.12f : 0.0f;
                xOffset2 -= facingW ? 0.12f : 0.0f;
                yOffset2 -= facingN ? 0.12f : 0.0f;
                yOffset2 += !facingE && !facingW && !facingN ? 0.12f : 0.0f;
            }
            if (facingE || facingW) {
                yOffset -= 0.28f;
                continue;
            }
            xOffset += 0.28f;
        }
    }

    public static void doCornerAmmoCans(boolean facingE, boolean facingW, boolean facingN, IsoGridSquare sq, WeightedList<ItemKey> ammoCases, WeightedList<ItemKey> ammoCans) {
        float xOffset2;
        float xOffset;
        int caseSlots = 2;
        float f = facingW ? 0.44f : (xOffset = facingN ? 0.44f : 0.38f);
        float yOffset = facingE ? 0.82f : (facingN ? 0.82f : 0.34f);
        float zOffset = 0.38f;
        float xRotation = 0.0f;
        float yRotation = 0.0f;
        float zRotation = 0.0f;
        for (int j = 0; j < 2; ++j) {
            InventoryContainer ammoCase = (InventoryContainer)InventoryItemFactory.CreateItem(ammoCases.getRandom());
            sq.AddWorldInventoryItem(ammoCase, xOffset, yOffset, 0.38f, false, true);
            ItemPickerJava.rollContainerItem(ammoCase, null, ItemPickerJava.getItemPickerContainers().get(ammoCase.getType()));
            RandomizedBuildingBase.setWorldRotation(ammoCase, 0.0f, 0.0f, 0.0f);
            xOffset += 0.38f;
        }
        int ammoSlots = 2;
        float f2 = facingW ? 0.82f : (xOffset2 = facingN ? 0.82f : 0.38f);
        float yOffset2 = facingE ? 0.52f : (facingN ? 0.52f : 0.64f);
        float zOffset2 = 0.38f;
        float xRotation2 = 0.0f;
        float yRotation2 = 0.0f;
        float zRotation2 = facingW ? 270.0f : (facingN ? 270.0f : 90.0f);
        for (int j = 0; j < 2; ++j) {
            InventoryContainer ammoCan = (InventoryContainer)InventoryItemFactory.CreateItem(ammoCans.getRandom());
            sq.AddWorldInventoryItem(ammoCan, xOffset2, yOffset2, 0.38f, false, true);
            ItemPickerJava.rollContainerItem(ammoCan, null, ItemPickerJava.getItemPickerContainers().get(ammoCan.getType()));
            RandomizedBuildingBase.setWorldRotation(ammoCan, 0.0f, 0.0f, zRotation2);
            if (facingE || facingN) {
                yOffset2 -= 0.14f;
                continue;
            }
            yOffset2 += 0.14f;
        }
    }

    public static void doBodyArmor(boolean facingE, IsoGridSquare sq, ItemKey vestType, int spawnChance) {
        int slotNumber = 4;
        int currentSlot = 1;
        float xOffset = facingE ? 0.12f : 0.28f;
        float yOffset = facingE ? 0.78f : 0.12f;
        float zOffset = 0.5f;
        float xRotation = 270.0f;
        float yRotation = facingE ? 270.0f : 0.0f;
        float zRotation = 0.0f;
        for (int j = 0; j < 4; ++j) {
            Object vest = InventoryItemFactory.CreateItem(vestType);
            if (currentSlot == 1) {
                RandomizedBuildingBase.spawnBodyArmor(vest, sq, xOffset, yOffset, zOffset, spawnChance);
                zOffset += 0.2f;
            } else if (currentSlot == 2) {
                RandomizedBuildingBase.spawnBodyArmor(vest, sq, xOffset, yOffset, zOffset, spawnChance);
                zOffset -= 0.2f;
                if (facingE) {
                    yOffset -= 0.44f;
                } else {
                    xOffset += 0.44f;
                }
            } else if (currentSlot == 3) {
                RandomizedBuildingBase.spawnBodyArmor(vest, sq, xOffset, yOffset, zOffset, spawnChance);
                zOffset += 0.2f;
            } else if (currentSlot == 4) {
                RandomizedBuildingBase.spawnBodyArmor(vest, sq, xOffset, yOffset, zOffset, spawnChance);
            }
            if (((InventoryItem)vest).getWorldItem() != null) {
                RandomizedBuildingBase.setWorldRotation(vest, 270.0f, yRotation, 0.0f);
            }
            ++currentSlot;
        }
    }

    public static void spawnBodyArmor(InventoryItem vest, IsoGridSquare sq, float xOffset, float yOffset, float zOffset, int spawnChance) {
        if (Rand.Next(100) < spawnChance) {
            sq.AddWorldInventoryItem(vest, xOffset, yOffset, zOffset, false, true);
        }
    }

    private static void rollWeaponUpgrades(HandWeapon gun) {
        if (gun.is(ItemKey.Weapon.VARMINT_RIFLE)) {
            WeaponPart scope = (WeaponPart)InventoryItemFactory.CreateItem(ItemKey.WeaponPart.X2_SCOPE);
            gun.attachWeaponPart(scope);
        }
        if (gun.is(ItemKey.Weapon.HUNTING_RIFLE) || gun.is(ItemKey.Weapon.MSR7T_RIFLE)) {
            ItemKey scopeType = ItemKey.WeaponPart.X4_SCOPE;
            if (Rand.Next(100) >= 60) {
                scopeType = ItemKey.WeaponPart.X8_SCOPE;
            }
            WeaponPart scope = (WeaponPart)InventoryItemFactory.CreateItem(scopeType);
            gun.attachWeaponPart(scope);
        }
        if (gun.is(ItemKey.Weapon.REVOLVER_LONG) && Rand.NextBool(4)) {
            gun.attachWeaponPart((WeaponPart)InventoryItemFactory.CreateItem(ItemKey.WeaponPart.X2_SCOPE));
        }
    }

    static {
        rbMap = new HashMap();
        maximumRoomCount = 500;
        weaponsList = new HashMap();
    }

    public static final class HumanCorpse
    extends IsoGameCharacter
    implements IHumanVisual {
        final HumanVisual humanVisual = new HumanVisual(this);
        final ItemVisuals itemVisuals = new ItemVisuals();
        public boolean isSkeleton;

        public HumanCorpse(IsoCell cell, float x, float y, float z) {
            super(cell, x, y, z);
            cell.getObjectList().remove(this);
            cell.getAddList().remove(this);
        }

        @Override
        public void dressInNamedOutfit(String outfitName) {
            this.getHumanVisual().dressInNamedOutfit(outfitName, this.itemVisuals);
            this.getHumanVisual().synchWithOutfit(this.getHumanVisual().getOutfit());
        }

        @Override
        public HumanVisual getHumanVisual() {
            return this.humanVisual;
        }

        @Override
        public HumanVisual getVisual() {
            return this.humanVisual;
        }

        @Override
        public void Dressup(SurvivorDesc desc) {
            this.wornItems.setFromItemVisuals(this.itemVisuals);
            this.wornItems.addItemsToItemContainer(this.inventory);
        }

        @Override
        public boolean isSkeleton() {
            return this.isSkeleton;
        }
    }
}

