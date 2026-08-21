/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zombie.GameTime;
import zombie.Lua.LuaManager;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.inventory.types.DrainableComboItem;
import zombie.iso.IsoObject;
import zombie.network.GameClient;
import zombie.radio.ZomboidRadio;
import zombie.scripting.objects.VehicleScript;
import zombie.util.StringUtils;
import zombie.util.Type;
import zombie.util.lambda.Invokers;
import zombie.util.list.PZArrayList;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.VehicleDoor;
import zombie.vehicles.VehicleEngine;
import zombie.vehicles.VehicleLight;
import zombie.vehicles.VehiclePart;
import zombie.vehicles.VehiclePartOwner;
import zombie.vehicles.VehicleWindow;

public final class VehicleParts {
    private VehiclePartOwner owner;
    private final List<VehiclePart> parts = new ArrayList<VehiclePart>();
    private final Map<String, VehiclePart> partsById = new HashMap<String, VehiclePart>();
    private VehiclePart battery;
    private VehiclePart engine;

    public void setOwner(VehiclePartOwner owner) {
        VehiclePartOwner ownerOld = this.owner;
        this.owner = owner;
        IsoObject ownerObject = Type.tryCastTo(owner, IsoObject.class);
        for (int i = 0; i < this.parts.size(); ++i) {
            VehiclePart part = this.parts.get(i);
            part.vehicle = owner;
            ItemContainer itemContainer = part.getItemContainer();
            if (itemContainer instanceof ItemContainer) {
                ItemContainer container = itemContainer;
                container.setParent(ownerObject);
            }
            if (part.engine == null) continue;
            part.engine.replaceListener(ownerOld, owner);
        }
    }

    public VehiclePartOwner getOwner() {
        return this.owner;
    }

    public BaseVehicle getVehicle() {
        return Type.tryCastTo(this.getOwner(), BaseVehicle.class);
    }

    public VehicleScript getScript() {
        return this.getOwner().getScript();
    }

    public void clear() {
        this.parts.clear();
        this.partsById.clear();
        this.battery = null;
        this.engine = null;
    }

    public int size() {
        return this.getPartCount();
    }

    public boolean isEmpty() {
        return this.getPartCount() == 0;
    }

    public boolean contains(VehiclePart part) {
        return this.parts.contains(part);
    }

    public int indexOf(VehiclePart part) {
        return this.parts.indexOf(part);
    }

    public void add(VehiclePart part) {
        this.parts.add(part);
        if (!StringUtils.isNullOrWhitespace(part.getId()) && !this.partsById.containsKey(part.getId())) {
            this.partsById.put(part.getId(), part);
        }
        if (zombie.scripting.objects.VehiclePart.BATTERY.toString().equals(part.getId())) {
            this.battery = part;
        }
        if (zombie.scripting.objects.VehiclePart.ENGINE.toString().equals(part.getId())) {
            this.engine = part;
        }
    }

    public VehiclePart get(int index) {
        return this.getPartByIndex(index);
    }

    public int getPartCount() {
        return this.parts.size();
    }

    public VehiclePart getPartByIndex(int index) {
        if (index < 0 || index >= this.parts.size()) {
            return null;
        }
        return this.parts.get(index);
    }

    public VehiclePart getPartByPartId(zombie.scripting.objects.VehiclePart id) {
        if (id == null) {
            return null;
        }
        return this.getPartById(id.toString());
    }

    public VehiclePart getPartById(String id) {
        if (id == null) {
            return null;
        }
        return this.partsById.getOrDefault(id, null);
    }

    public int getPartIndex(String id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < this.parts.size(); ++i) {
            VehicleScript.Part scriptPart = this.parts.get(i).getScriptPart();
            if (scriptPart == null || !id.equals(scriptPart.id)) continue;
            return i;
        }
        return -1;
    }

    public int getNumberOfPartsWithContainers() {
        if (this.getScript() == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < this.getScript().getPartCount(); ++i) {
            if (this.getScript().getPart((int)i).container == null) continue;
            ++count;
        }
        return count;
    }

    public VehiclePart getBattery() {
        return this.battery;
    }

    public float getBatteryCharge() {
        VehiclePart battery = this.getBattery();
        if (battery != null && battery.getInventoryItem() instanceof DrainableComboItem) {
            return ((InventoryItem)battery.getInventoryItem()).getCurrentUsesFloat();
        }
        return 0.0f;
    }

    public VehiclePart getEngine() {
        return this.engine;
    }

    public int getEngineCondition() {
        return this.getEngine() == null ? 0 : this.getEngine().getCondition();
    }

    public boolean isEngineWorking() {
        for (int i = 0; i < this.parts.size(); ++i) {
            VehiclePart part = this.parts.get(i);
            String functionName = part.getLuaFunction("checkEngine");
            if (functionName == null || Boolean.TRUE.equals(this.callLuaBoolean(functionName, this, part))) continue;
            return false;
        }
        return true;
    }

    public VehiclePart getTrunkDoorPart() {
        return this.getPartByPartId(zombie.scripting.objects.VehiclePart.TRUNK_DOOR);
    }

    public VehiclePart getTrunkPart() {
        VehiclePart truckBedPart = this.getPartByPartId(zombie.scripting.objects.VehiclePart.TRUCK_BED);
        if (truckBedPart != null) {
            return truckBedPart;
        }
        return this.getPartByPartId(zombie.scripting.objects.VehiclePart.TRUCK_BED_OPEN);
    }

    public VehiclePart getTrailerTrunkPart() {
        return this.getPartByPartId(zombie.scripting.objects.VehiclePart.TRAILER_TRUNK);
    }

    public VehiclePart getPartForSeatContainer(int seat) {
        if (this.getScript() == null || seat < 0 || seat >= this.getScript().getPassengerCount()) {
            return null;
        }
        for (int i = 0; i < this.getPartCount(); ++i) {
            VehiclePart part = this.getPartByIndex(i);
            if (part.getContainerSeatNumber() != seat) continue;
            return part;
        }
        return null;
    }

    public <T> PZArrayList<ItemContainer> getVehicleItemContainers(T paramToCompare, Invokers.Params2.Boolean.ICallback<T, ItemContainer> isValidPredicate) {
        PZArrayList<ItemContainer> containerList = new PZArrayList<ItemContainer>(ItemContainer.class, 10);
        return this.getVehicleItemContainers(paramToCompare, isValidPredicate, containerList);
    }

    public <T> PZArrayList<ItemContainer> getVehicleItemContainers(T paramToCompare, Invokers.Params2.Boolean.ICallback<T, ItemContainer> isValidPredicate, PZArrayList<ItemContainer> containerList) {
        for (int i = 0; i < this.parts.size(); ++i) {
            boolean canStore;
            VehiclePart part = this.parts.get(i);
            ItemContainer partContainer = part.getItemContainer();
            if (partContainer == null || !(canStore = isValidPredicate.accept(paramToCompare, partContainer))) continue;
            containerList.addUniqueReference(partContainer);
        }
        return containerList;
    }

    public void createParts() {
        for (int i = 0; i < this.parts.size(); ++i) {
            VehiclePart part = this.parts.get(i);
            if (this.isPartMissingUninstallableItem(part)) {
                part.created = false;
            }
            if (this.shouldPartHaveNoItem(part)) {
                part.item = null;
            }
            if (part.created) continue;
            part.created = true;
            String functionName = part.getLuaFunction("create");
            if (functionName == null) {
                part.setRandomCondition(null);
                continue;
            }
            this.callLuaVoid(functionName, this.getOwner(), part);
            if (part.getCondition() != -1) continue;
            part.setRandomCondition(null);
        }
    }

    private boolean isPartMissingUninstallableItem(VehiclePart part) {
        ArrayList<String> itemType = part.getItemType();
        return part.created && itemType != null && !itemType.isEmpty() && part.getInventoryItem() == null && part.getTable("install") == null;
    }

    private boolean shouldPartHaveNoItem(VehiclePart part) {
        ArrayList<String> itemType = part.getItemType();
        return itemType == null || itemType.isEmpty();
    }

    public void initParts() {
        for (int i = 0; i < this.parts.size(); ++i) {
            VehiclePart part = this.parts.get(i);
            String functionName = part.getLuaFunction("init");
            if (functionName == null) continue;
            this.callLuaVoid(functionName, this.getOwner(), part);
        }
    }

    public void setScript(VehicleScript script) {
        VehiclePart part;
        int i;
        if (script == null) {
            this.clear();
            return;
        }
        ArrayList<VehiclePart> oldParts = new ArrayList<VehiclePart>(this.parts);
        this.clear();
        for (i = 0; i < script.getPartCount(); ++i) {
            VehicleScript.Part scriptPart = script.getPart(i);
            VehiclePart part2 = null;
            for (int j = 0; j < oldParts.size(); ++j) {
                VehiclePart oldPart = oldParts.get(j);
                if (oldPart.getScriptPart() != null && scriptPart.id.equals(oldPart.getScriptPart().id)) {
                    part2 = oldPart;
                    break;
                }
                if (oldPart.partId == null || !scriptPart.id.equals(oldPart.partId)) continue;
                part2 = oldPart;
                break;
            }
            if (part2 == null) {
                part2 = new VehiclePart(this.getOwner());
            }
            part2.setScriptPart(scriptPart);
            part2.category = scriptPart.category;
            part2.specificItem = scriptPart.specificItem;
            part2.setDurability(scriptPart.getDurability());
            if (scriptPart.container == null || scriptPart.container.contentType != null) {
                part2.setItemContainer(null);
            } else {
                if (part2.getItemContainer() == null) {
                    ItemContainer container = new ItemContainer(scriptPart.id, null, this.getVehicle());
                    part2.setItemContainer(container);
                    container.id = 0;
                }
                part2.getItemContainer().capacity = scriptPart.container.capacity;
            }
            if (scriptPart.door == null) {
                part2.door = null;
            } else if (part2.door == null) {
                part2.door = new VehicleDoor(part2);
                part2.door.init(scriptPart.door);
            }
            if (zombie.scripting.objects.VehiclePart.ENGINE.toString().equals(part2.getId())) {
                if (part2.engine == null) {
                    part2.engine = new VehicleEngine(part2);
                    part2.engine.addListener(this.getOwner());
                }
            } else {
                part2.engine = null;
            }
            if (scriptPart.window == null) {
                part2.window = null;
            } else if (part2.window == null) {
                part2.window = new VehicleWindow(part2);
                part2.window.init(scriptPart.window);
            } else {
                part2.window.openable = scriptPart.window.openable;
            }
            part2.parent = null;
            if (part2.children != null) {
                part2.children.clear();
            }
            this.add(part2);
        }
        for (i = 0; i < script.getPartCount(); ++i) {
            part = this.parts.get(i);
            VehicleScript.Part scriptPart = part.getScriptPart();
            if (scriptPart.parent == null) continue;
            part.parent = this.getPartById(scriptPart.parent);
            if (part.parent == null) continue;
            part.parent.addChild(part);
        }
        for (i = 0; i < script.getPartCount(); ++i) {
            part = this.parts.get(i);
            part.setInventoryItem(part.item);
        }
    }

    public boolean updatePart(VehiclePart part) {
        String functionName;
        part.updateSignalDevice();
        VehicleLight light = part.getLight();
        if (light != null && part.getId().contains("Headlight")) {
            part.setLightActive(this.getOwner().getHeadlightsOn() && part.getInventoryItem() != null && this.getBatteryCharge() > 0.0f);
        }
        if ((functionName = part.getLuaFunction("update")) == null) {
            return false;
        }
        float worldAgeHours = (float)GameTime.getInstance().getWorldAgeHours();
        part.setLastUpdated(GameTime.checkHours(part.getLastUpdated(), worldAgeHours));
        float elapsedHours = worldAgeHours - part.getLastUpdated();
        if ((int)(elapsedHours * 60.0f) > 0) {
            part.setLastUpdated(worldAgeHours);
            this.callLuaVoid(functionName, this.getOwner(), part, elapsedHours * 60.0f);
            return true;
        }
        return false;
    }

    public boolean update() {
        if (GameClient.client) {
            for (int i = 0; i < this.getPartCount(); ++i) {
                VehiclePart part = this.getPartByIndex(i);
                part.updateSignalDevice();
            }
            return false;
        }
        boolean didUpdate = false;
        for (int i = 0; i < this.getPartCount(); ++i) {
            VehiclePart part = this.getPartByIndex(i);
            if (!this.updatePart(part) || didUpdate) continue;
            didUpdate = true;
        }
        return didUpdate;
    }

    public void addToWorld() {
        for (int i = 0; i < this.parts.size(); ++i) {
            VehiclePart part = this.parts.get(i);
            if (part.getItemContainer() != null) {
                part.getItemContainer().addItemsToProcessItems();
            }
            if (part.getDeviceData() == null) continue;
            ZomboidRadio.getInstance().RegisterDevice(part);
        }
    }

    public void removeFromWorld() {
        for (int i = 0; i < this.parts.size(); ++i) {
            VehiclePart part = this.parts.get(i);
            if (part.getItemContainer() != null) {
                part.getItemContainer().removeItemsFromProcessItems();
            }
            if (part.getDeviceData() == null) continue;
            part.getDeviceData().cleanSoundsAndEmitter();
            ZomboidRadio.getInstance().UnRegisterDevice(part);
        }
    }

    private void callLuaVoid(String functionName, Object arg1, Object arg2) {
        Object functionObj = LuaManager.getFunctionObject(functionName);
        if (functionObj == null) {
            return;
        }
        LuaManager.caller.protectedCallVoid(LuaManager.thread, functionObj, arg1, arg2);
    }

    private void callLuaVoid(String functionName, Object arg1) {
        Object functionObj = LuaManager.getFunctionObject(functionName);
        if (functionObj == null) {
            return;
        }
        LuaManager.caller.protectedCallVoid(LuaManager.thread, functionObj, arg1);
    }

    private void callLuaVoid(String functionName, Object arg1, Object arg2, Object arg3) {
        Object functionObj = LuaManager.getFunctionObject(functionName);
        if (functionObj == null) {
            return;
        }
        LuaManager.caller.protectedCallVoid(LuaManager.thread, functionObj, arg1, arg2, arg3);
    }

    private Boolean callLuaBoolean(String functionName, Object arg, Object arg2) {
        Object functionObj = LuaManager.getFunctionObject(functionName);
        if (functionObj == null) {
            return null;
        }
        return LuaManager.caller.protectedCallBoolean(LuaManager.thread, functionObj, arg, arg2);
    }

    private Boolean callLuaBoolean(String functionName, Object arg, Object arg2, Object arg3) {
        Object functionObj = LuaManager.getFunctionObject(functionName);
        if (functionObj == null) {
            return null;
        }
        return LuaManager.caller.protectedCallBoolean(LuaManager.thread, functionObj, arg, arg2, arg3);
    }
}

