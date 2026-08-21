/*
 * Decompiled with CFR 0.152.
 */
package zombie.core;

import java.util.ArrayList;
import java.util.List;
import se.krka.kahlua.vm.KahluaTable;
import zombie.GameTime;
import zombie.Lua.LuaEventManager;
import zombie.Lua.LuaManager;
import zombie.characters.IsoPlayer;
import zombie.core.Core;
import zombie.core.TransactionManager;
import zombie.debug.DebugType;
import zombie.inventory.InventoryItem;
import zombie.inventory.InventoryItemFactory;
import zombie.inventory.ItemContainer;
import zombie.inventory.types.Clothing;
import zombie.inventory.types.DrainableComboItem;
import zombie.inventory.types.Food;
import zombie.inventory.types.InventoryContainer;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoUtils;
import zombie.iso.objects.IsoDeadBody;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.network.GameServer;
import zombie.network.JSONField;
import zombie.network.PacketTypes;
import zombie.network.fields.ContainerID;
import zombie.network.fields.character.PlayerID;
import zombie.network.packets.IDescriptor;
import zombie.network.packets.INetworkPacket;
import zombie.scripting.objects.CharacterTrait;
import zombie.util.StringUtils;

public abstract class Transaction
implements IDescriptor {
    protected static byte lastId;
    @JSONField
    protected byte id;
    @JSONField
    protected TransactionState state;
    @JSONField
    protected final List<TransactionEntry> entries = new ArrayList<TransactionEntry>();
    @JSONField
    protected final PlayerID playerId = new PlayerID();
    @JSONField
    protected String extra;
    @JSONField
    protected IsoDirections direction;
    protected float xoff;
    protected float yoff;
    protected float zoff;
    @JSONField
    protected long startTime;
    @JSONField
    protected long endTime;
    public IsoGridSquare square;

    public void set(IsoPlayer player, List<InventoryItem> items, ItemContainer source2, ItemContainer destination, String extra, IsoDirections direction, float posX, float posY, float posZ) {
        this.state = GameServer.server ? TransactionState.Accept : TransactionState.Request;
        this.playerId.set(player);
        this.entries.clear();
        for (InventoryItem item : items) {
            TransactionEntry entry = new TransactionEntry(TransactionManager.getItemId(item, source2));
            if (source2.getType().equals("object")) {
                entry.sourceId.set(source2, source2.getParent());
            } else if (source2.getType().equals("floor")) {
                if (item.getWorldItem() != null) {
                    entry.sourceId.set(source2, item.getWorldItem());
                } else {
                    entry.sourceId.set(source2, item.deadBodyObject);
                }
            } else {
                entry.sourceId.set(source2);
            }
            if (destination == null) {
                entry.destinationId.set(null);
            } else if (destination.getType().equals("object")) {
                if (source2 != null && destination.getParent() == source2.getParent()) {
                    entry.destinationId.set(destination, destination.getParent());
                } else {
                    entry.destinationId.setObject(destination, destination.getParent(), destination.sourceGrid);
                }
                this.direction = direction;
                this.xoff = posX;
                this.yoff = posY;
                this.zoff = posZ;
            } else if (destination.getType().equals("floor")) {
                entry.destinationId.setFloor(destination, player.getCurrentSquare());
            } else {
                entry.destinationId.set(destination);
            }
            this.entries.add(entry);
        }
        this.extra = extra;
        this.startTime = GameTime.getServerTimeMills();
        long l = this.endTime = GameServer.server ? this.startTime + (long)this.getDuration() : this.startTime;
        if (lastId == 0) {
            lastId = (byte)(lastId + 1);
        }
        byte by = lastId;
        lastId = (byte)(by + 1);
        this.id = by;
    }

    public void setTimeData() {
        this.startTime = GameTime.getServerTimeMills();
        this.endTime = this.startTime + (long)this.getDuration();
    }

    public boolean update() {
        for (TransactionEntry entry : this.entries) {
            if (this.updateItem(entry)) continue;
            return false;
        }
        return true;
    }

    private boolean updateItem(TransactionEntry entry) {
        InventoryItem item;
        entry.sourceId.findObject();
        entry.destinationId.findObject();
        if (entry.sourceId.containerType == ContainerID.ContainerType.IsoObject) {
            if (entry.destinationId.containerType == ContainerID.ContainerType.Undefined) {
                LuaEventManager.triggerEvent("OnProcessTransaction", "scrapMoveable", this.playerId.getPlayer(), null, entry.sourceId, entry.destinationId, null);
                return true;
            }
            if (entry.sourceId.hashCode() != entry.destinationId.hashCode()) {
                LuaEventManager.triggerEvent("OnProcessTransaction", "pickUpMoveable", this.playerId.getPlayer(), null, entry.sourceId, entry.destinationId, null);
                return true;
            }
            KahluaTable tbl = LuaManager.platform.newTable();
            tbl.rawset("direction", (Object)this.direction.name());
            LuaEventManager.triggerEvent("OnProcessTransaction", "rotateMoveable", this.playerId.getPlayer(), null, entry.sourceId, entry.destinationId, tbl);
            return true;
        }
        if (entry.itemId == -1 && entry.sourceId.containerType == ContainerID.ContainerType.DeadBody) {
            item = ((IsoDeadBody)entry.sourceId.getObject()).getItem();
        } else {
            ItemContainer itemContainer = entry.sourceId.getContainer();
            if (itemContainer instanceof ItemContainer) {
                ItemContainer container = itemContainer;
                item = container.getItemWithID(entry.itemId);
                container.setExplored(true);
                container.setHasBeenLooted(true);
            } else {
                return false;
            }
        }
        if (item != null) {
            boolean isReplacedOnCooked = false;
            if (item instanceof Food) {
                Food food = (Food)item;
                food.setChef(this.playerId.getPlayer().getUsername());
                boolean bl = isReplacedOnCooked = food.getReplaceOnCooked() != null;
            }
            if (item instanceof DrainableComboItem) {
                DrainableComboItem drainableComboItem = (DrainableComboItem)item;
                boolean bl = isReplacedOnCooked = drainableComboItem.getReplaceOnCooked() != null;
            }
            if (!isReplacedOnCooked) {
                item.update();
            }
            if (item instanceof Clothing) {
                Clothing clothing = (Clothing)item;
                clothing.updateWetness();
            }
        }
        if (entry.destinationId.containerType == ContainerID.ContainerType.IsoObject) {
            KahluaTable tbl = LuaManager.platform.newTable();
            tbl.rawset("direction", (Object)this.direction.name());
            LuaEventManager.triggerEvent("OnProcessTransaction", "placeMoveable", this.playerId.getPlayer(), item, entry.sourceId, entry.destinationId, tbl);
            return true;
        }
        if (ContainerID.ContainerType.Floor == entry.destinationId.containerType && ContainerID.ContainerType.PlayerInventory == entry.sourceId.containerType) {
            IsoPlayer player = this.playerId.getPlayer();
            player.removeAttachedItem(item);
            if (player.isEquipped(item)) {
                player.removeFromHands(item);
                player.removeWornItem(item, false);
                LuaEventManager.triggerEvent("OnClothingUpdated", player);
                player.updateHandEquips();
            }
            KahluaTable tbl = LuaManager.platform.newTable();
            tbl.rawset("square", (Object)this.square);
            LuaEventManager.triggerEvent("OnProcessTransaction", "dropOnFloor", this.playerId.getPlayer(), item, entry.sourceId, entry.destinationId, tbl);
            return true;
        }
        if (!StringUtils.isNullOrEmpty(this.extra)) {
            int newItemId = entry.itemId;
            if (item != null && entry.sourceId.containerType == ContainerID.ContainerType.PlayerInventory && entry.sourceId.getContainer() == entry.destinationId.getContainer()) {
                DebugType.Objects.noise("Extra option for \"%s\"", this.extra);
                IsoPlayer player = this.playerId.getPlayer();
                if (player != null) {
                    player.removeFromHands(item);
                    player.removeWornItem(item, false);
                    player.getInventory().Remove(item);
                    InventoryItem newItem = InventoryItemFactory.CreateItem(item, this.extra);
                    player.getInventory().AddItem(newItem);
                    if (newItem instanceof InventoryContainer && newItem.canBeEquipped() != null) {
                        player.setWornItem(newItem.canBeEquipped(), newItem);
                    } else if (newItem.IsClothing()) {
                        player.setWornItem(newItem.getBodyLocation(), newItem);
                    }
                    newItemId = newItem.id;
                    GameServer.sendRemoveItemFromContainer(entry.sourceId.getContainer(), item);
                    GameServer.sendAddItemToContainer(entry.destinationId.getContainer(), newItem);
                    player.updateHandEquips();
                    GameServer.sendSyncClothing(player, newItem.getBodyLocation(), newItem);
                }
            }
            return entry.destinationId.getContainer().getItemWithID(newItemId) != null && entry.sourceId.getContainer().getItemWithID(entry.itemId) == null;
        }
        if (entry.sourceId.containerType == ContainerID.ContainerType.WorldObject && entry.itemId == -1) {
            if (entry.sourceId.getObject() == null) {
                return false;
            }
            item = ((IsoWorldInventoryObject)entry.sourceId.getObject()).getItem();
        }
        if (entry.destinationId.getContainer() == null) {
            return false;
        }
        if (entry.sourceId.containerType == ContainerID.ContainerType.Vehicle && (double)IsoUtils.DistanceTo(this.playerId.getPlayer().getX(), this.playerId.getPlayer().getY(), entry.sourceId.getVehicle().getX(), entry.sourceId.getVehicle().getY()) > 5.0) {
            return false;
        }
        if (entry.destinationId.containerType == ContainerID.ContainerType.Vehicle && (double)IsoUtils.DistanceTo(this.playerId.getPlayer().getX(), this.playerId.getPlayer().getY(), entry.destinationId.getVehicle().getX(), entry.destinationId.getVehicle().getY()) > 5.0) {
            return false;
        }
        if (item != null) {
            if (entry.sourceId.containerType == ContainerID.ContainerType.WorldObject && entry.itemId == -1) {
                if (item instanceof Clothing) {
                    Clothing clothing = (Clothing)item;
                    clothing.flushWetness();
                }
                GameServer.RemoveItemFromMap(entry.sourceId.getObject());
            } else if (entry.itemId == -1 && entry.sourceId.containerType == ContainerID.ContainerType.DeadBody) {
                INetworkPacket.sendToAll(PacketTypes.PacketType.RemoveCorpseFromMap, entry.sourceId.getObject());
                entry.sourceId.getObject().getSquare().removeCorpse((IsoDeadBody)entry.sourceId.getObject(), true);
            } else {
                IsoPlayer player = this.playerId.getPlayer();
                player.removeAttachedItem(item);
                if (player.isEquipped(item)) {
                    player.removeFromHands(item);
                    player.removeWornItem(item, false);
                    LuaEventManager.triggerEvent("OnClothingUpdated", player);
                    player.updateHandEquips();
                    GameServer.sendSyncClothing(player, null, item);
                }
                if (entry.sourceId.getContainer().getCharacter() instanceof IsoPlayer) {
                    INetworkPacket.send((IsoPlayer)entry.sourceId.getContainer().getCharacter(), PacketTypes.PacketType.RemoveInventoryItemFromContainer, entry.sourceId.getContainer(), item);
                } else {
                    INetworkPacket.sendToRelative(PacketTypes.PacketType.RemoveInventoryItemFromContainer, entry.sourceId.x, (float)entry.sourceId.y, entry.sourceId.getContainer(), item);
                }
            }
            entry.sourceId.getContainer().Remove(item);
            if (!TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).lastItemFullType.equals(item.getFullType())) {
                TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).itemsCount = 0;
            }
            TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).lastItemFullType = item.getFullType() == null ? "" : item.getFullType();
            TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).itemsLastTransactionTime = System.currentTimeMillis();
            entry.destinationId.getContainer().addItem(item);
            if (entry.destinationId.containerType == ContainerID.ContainerType.Floor) {
                IsoWorldInventoryObject worldInvObj = (IsoWorldInventoryObject)entry.destinationId.getObject();
                worldInvObj.square.AddWorldInventoryItem(item, worldInvObj.xoff, worldInvObj.yoff, worldInvObj.zoff, true);
            } else {
                if (entry.sourceId.containerType == ContainerID.ContainerType.WorldObject || entry.sourceId.containerType == ContainerID.ContainerType.Floor) {
                    item.setWorldItem(null);
                    entry.sourceId.set(null);
                }
                if (entry.destinationId.getContainer().getCharacter() instanceof IsoPlayer) {
                    INetworkPacket.send((IsoPlayer)entry.destinationId.getContainer().getCharacter(), PacketTypes.PacketType.AddInventoryItemToContainer, entry.destinationId.getContainer(), item);
                } else {
                    INetworkPacket.sendToRelative(PacketTypes.PacketType.AddInventoryItemToContainer, entry.destinationId.x, (float)entry.destinationId.y, entry.destinationId.getContainer(), item);
                }
            }
            if (entry.sourceId.containerType == ContainerID.ContainerType.DeadBody && entry.itemId == -1) {
                if (entry.destinationId.containerType == ContainerID.ContainerType.PlayerInventory) {
                    IsoPlayer player = this.playerId.getPlayer();
                    player.setPrimaryHandItem(item);
                    player.setSecondaryHandItem(item);
                    player.updateHandEquips();
                } else {
                    DebugType.Objects.error("A player " + this.playerId.getDescription() + " sent invalid transaction " + this.getDuration());
                }
            }
        } else {
            return false;
        }
        return entry.sourceId.containerType != ContainerID.ContainerType.Floor || !((double)IsoUtils.DistanceTo(entry.sourceId.x, entry.sourceId.y, this.playerId.getPlayer().getX(), this.playerId.getPlayer().getY()) > 1.1);
    }

    private float getDuration() {
        float totalMaxTime = 0.0f;
        for (TransactionEntry entry : this.entries) {
            float maxTime;
            InventoryItem item;
            ItemContainer source2 = entry.sourceId.getContainer();
            ItemContainer destination = entry.destinationId.getContainer();
            IsoPlayer player = this.playerId.getPlayer();
            if (source2 != null && destination != null && player != null) {
                item = entry.sourceId.getContainer().getItemWithID(entry.itemId);
                if (!destination.getType().equals("TradeUI") && !source2.getType().equals("TradeUI")) {
                    maxTime = 120.0f;
                    float destCapacityDelta = 1.0f;
                    if (source2 == player.getInventory()) {
                        if (destination.isInCharacterInventory(player)) {
                            destCapacityDelta = destination.getCapacityWeight() / destination.getMaxWeight();
                        } else {
                            maxTime = 50.0f;
                        }
                    } else if (!source2.isInCharacterInventory(player) && destination.isInCharacterInventory(player)) {
                        maxTime = 50.0f;
                    }
                    if (destCapacityDelta < 0.4f) {
                        destCapacityDelta = 0.4f;
                    }
                    if (item != null) {
                        float w = item.getActualWeight();
                        if (w > 3.0f) {
                            w = 3.0f;
                        }
                        maxTime = maxTime * w * destCapacityDelta;
                    }
                    if (Core.getInstance().getGameMode().equals("LastStand")) {
                        maxTime *= 0.3f;
                    }
                    if (destination.getType().equals("floor")) {
                        if (source2 == player.getInventory()) {
                            maxTime *= 0.1f;
                        } else if (!source2.isInCharacterInventory(player)) {
                            maxTime *= 0.2f;
                        }
                    }
                    if (player.hasTrait(CharacterTrait.DEXTROUS)) {
                        maxTime *= 0.5f;
                    }
                    if (player.hasTrait(CharacterTrait.ALL_THUMBS) || player.isWearingAwkwardGloves()) {
                        maxTime *= 2.0f;
                    }
                } else {
                    maxTime = 0.0f;
                }
            } else {
                maxTime = 50.0f;
            }
            item = null;
            if (entry.sourceId.containerType == ContainerID.ContainerType.WorldObject && entry.itemId == -1 && entry.sourceId.getObject() != null) {
                item = ((IsoWorldInventoryObject)entry.sourceId.getObject()).getItem();
            }
            if (item != null && item.getWeight() <= 0.1f && (TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).itemsLastTransactionTime == 0L || System.currentTimeMillis() - TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).itemsLastTransactionTime < 2000L)) {
                if (TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).itemsCount < 19 && TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).lastItemFullType.equals(item.getFullType())) {
                    maxTime = 0.0f;
                    ++TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).itemsCount;
                } else {
                    TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).itemsCount = 0;
                    TransactionManager.getLightweightData((IsoPlayer)this.playerId.getPlayer()).lastItemFullType = "";
                }
            }
            if (entry.sourceId.getContainer() == entry.destinationId.getContainer() && ContainerID.ContainerType.PlayerInventory == entry.sourceId.containerType && entry.itemId != -1 && !StringUtils.isNullOrEmpty(this.extra)) {
                maxTime = 0.0f;
            }
            totalMaxTime = Math.max(maxTime, totalMaxTime);
        }
        return totalMaxTime * 20.0f;
    }

    public void setState(TransactionState state) {
        this.state = state;
        DebugType.Objects.noise(this);
    }

    public void setDuration(long duration) {
        this.endTime = this.startTime + duration;
    }

    public String toString() {
        return this.getDescription();
    }

    public static enum TransactionState {
        Reject,
        Request,
        Accept,
        Done;

    }

    protected static class TransactionEntry {
        public final Integer itemId;
        public final ContainerID sourceId = new ContainerID();
        public final ContainerID destinationId = new ContainerID();

        public TransactionEntry(Integer itemId) {
            this.itemId = itemId;
        }
    }
}

