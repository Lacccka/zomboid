/*
 * Decompiled with CFR 0.152.
 */
package zombie.core;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import zombie.GameTime;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.core.Transaction;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoUtils;
import zombie.iso.IsoWorld;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.network.PacketTypes;
import zombie.network.fields.ContainerID;
import zombie.network.packets.ItemTransactionPacket;
import zombie.util.StringUtils;
import zombie.vehicles.BaseVehicle;

public class TransactionManager {
    public static final byte success = 0;
    public static final byte reject = 1;
    private static final long TRANSACTION_TIMEOUT_MS = 20000L;
    private static final long TRANSACTION_TIMEOUT_BUFFER_MS = 10000L;
    public static final TransactionManager instance = new TransactionManager();
    private static final ConcurrentLinkedQueue<Transaction> transactions = new ConcurrentLinkedQueue();
    private static final HashMap<IsoPlayer, LightweightData> lightweightData = new HashMap();

    public static void add(Transaction transaction) {
        transactions.add(transaction);
    }

    public static LightweightData getLightweightData(IsoPlayer player) {
        if (!lightweightData.containsKey(player)) {
            lightweightData.put(player, new LightweightData());
        }
        return lightweightData.get(player);
    }

    public static void update() {
        if (GameServer.server) {
            for (Transaction transaction2 : transactions) {
                if (transaction2.state != Transaction.TransactionState.Accept || transaction2.endTime > GameTime.getServerTimeMills()) continue;
                try {
                    if (transaction2.update()) {
                        transaction2.setState(Transaction.TransactionState.Done);
                        if (!(transaction2 instanceof ItemTransactionPacket)) continue;
                        ItemTransactionPacket itemTransactionPacket = (ItemTransactionPacket)transaction2;
                        UdpConnection connection = GameServer.getConnectionFromPlayer(transaction2.playerId.getPlayer());
                        if (connection == null || !connection.isFullyConnected()) continue;
                        ByteBufferWriter bbw2 = connection.startPacket();
                        PacketTypes.PacketType.ItemTransaction.doPacket(bbw2);
                        itemTransactionPacket.write(bbw2);
                        PacketTypes.PacketType.ItemTransaction.send(connection);
                        continue;
                    }
                    transaction2.setState(Transaction.TransactionState.Reject);
                }
                catch (RuntimeException e) {
                    DebugType.General.printException(e, "transaction.update() threw. Rejecting transaction: " + String.valueOf(transaction2), LogSeverity.Error);
                    transaction2.setState(Transaction.TransactionState.Reject);
                }
            }
            transactions.removeIf(r -> r.state == Transaction.TransactionState.Done || r.state == Transaction.TransactionState.Reject);
        } else if (GameClient.client) {
            long now = GameTime.getServerTimeMills();
            Stream<Transaction> transactionForDelete = transactions.stream().filter(t -> {
                long duration = t.endTime - t.startTime;
                long timeout2 = duration > 0L ? duration + 10000L : 20000L;
                return now > t.startTime + timeout2;
            });
            transactionForDelete.forEach(transaction -> {
                transactions.remove(transaction);
                DebugType.Objects.noise("Timeout: %s", transaction);
            });
        }
    }

    private static boolean chainContainsContainingItem(ItemContainer start, int itemId) {
        if (itemId == -1) {
            return false;
        }
        ItemContainer c = start;
        for (int i = 0; i < 2; ++i) {
            if (c == null) {
                return false;
            }
            if (c.id == itemId) {
                return true;
            }
            if (c.getContainingItem() == null) {
                return false;
            }
            if (c.getContainingItem().getID() == itemId) {
                return true;
            }
            c = c.getContainingItem().getContainer();
        }
        return false;
    }

    public static byte isConsistent(int itemId, InventoryItem worldItem, ItemContainer source2, ItemContainer destination, String extra, ItemTransactionPacket dropTransaction, IsoPlayer player) {
        String description = String.format("item=%d source=%s destination=%s %s", itemId, source2 == null ? "null" : source2, destination == null ? "null" : destination, dropTransaction == null ? "" : dropTransaction.getDescription());
        if (!(destination != null || dropTransaction != null && dropTransaction.entries.stream().anyMatch(x -> x.sourceId.containerType == ContainerID.ContainerType.IsoObject) && dropTransaction.entries.stream().anyMatch(x -> x.destinationId.containerType == ContainerID.ContainerType.IsoObject))) {
            DebugType.Objects.noise("Inconsistent: destination container can't be found (%s)", description);
            return 1;
        }
        if (itemId != -1 && source2 != null) {
            if (!source2.containsID(itemId)) {
                DebugType.Objects.noise("Inconsistent: source container is not contain the item (%s)", description);
                return 1;
            }
            if (destination != null && !destination.isItemAllowed(source2.getItemWithID(itemId))) {
                DebugType.Objects.noise("Inconsistent: destination container can't contain the item (%s)", description);
                return 1;
            }
            if (source2.getType().equals("floor") && source2.getItemWithID(itemId) != null && source2.getItemWithID(itemId).getWorldItem() == null && source2.getItemWithID((int)itemId).deadBodyObject == null) {
                DebugType.Objects.noise("Inconsistent: this item is not on the floor (%s)", description);
                return 1;
            }
            if (destination != null && destination.getCharacter() != null && source2.getParent() instanceof BaseVehicle && (double)IsoUtils.DistanceTo(destination.getCharacter().getX(), destination.getCharacter().getY(), source2.getParent().getX(), source2.getParent().getY()) > 5.0) {
                return 1;
            }
            if (source2.getCharacter() != null && destination.getParent() instanceof BaseVehicle && (double)IsoUtils.DistanceTo(source2.getCharacter().getX(), source2.getCharacter().getY(), destination.getParent().getX(), destination.getParent().getY()) > 5.0) {
                return 1;
            }
        }
        float destinationWeightInTransaction = 0.0f;
        for (Transaction transaction : transactions) {
            for (Transaction.TransactionEntry entry : transaction.entries) {
                InventoryItem item;
                if (itemId != -1) {
                    if (destination != null && destination.getCharacter() != null) {
                        if (itemId == entry.itemId && extra == null || entry.sourceId.getContainer() == null || TransactionManager.chainContainsContainingItem(entry.sourceId.getContainer(), itemId) || TransactionManager.chainContainsContainingItem(source2, entry.itemId) || TransactionManager.chainContainsContainingItem(entry.destinationId.getContainer(), itemId)) {
                            DebugType.Objects.noise("Inconsistent: item to inventory (%s) t=(%s)", description, transaction);
                            return 1;
                        }
                    } else if (destination != null && (itemId == entry.itemId || entry.sourceId.isContainerTheSame(itemId, destination) && entry.itemId == -1 || TransactionManager.chainContainsContainingItem(entry.destinationId.getContainer(), itemId) || TransactionManager.chainContainsContainingItem(destination, entry.itemId))) {
                        DebugType.Objects.noise("Inconsistent: item from inventory (%s) t=(%s)", description, transaction);
                        return 1;
                    }
                } else if (destination.getCharacter() != null && source2 != null && (entry.destinationId.isContainerTheSame(itemId, source2) || entry.sourceId.isContainerTheSame(itemId, source2))) {
                    DebugType.Objects.noise("Inconsistent: object to inventory (%s) t=(%s)", description, transaction);
                    return 1;
                }
                if (entry.itemId == -1 || entry.sourceId.getContainer() == null || entry.destinationId.getContainer() == null || !entry.destinationId.isContainerTheSame(itemId, destination) || !entry.sourceId.getContainer().containsID(entry.itemId) || (item = entry.sourceId.getContainer().getItemWithID(entry.itemId)) == null) continue;
                destinationWeightInTransaction += item.getUnequippedWeight();
            }
        }
        if (itemId != -1 && source2 != null) {
            InventoryItem item = source2.getItemWithID(itemId);
            if (destination != null && "floor".equals(destination.getType())) {
                IsoGameCharacter chr = source2.getCharacter() != null ? source2.getCharacter() : player;
                IsoGridSquare dropSquare = TransactionManager.getNotFullFloorSquare(chr, item, destination);
                if (dropSquare == null) {
                    DebugType.Objects.warn("Inconsistent: destination square does not contain enough space for the item (%s)", description);
                    return 1;
                }
                if (dropTransaction != null) {
                    dropTransaction.square = dropSquare;
                }
                return 0;
            }
            if (source2 == destination && source2.getParent() instanceof IsoPlayer && item != null && item.getClothingItemExtraOption() != null && !item.getClothingItemExtraOption().isEmpty() && !StringUtils.isNullOrEmpty(extra)) {
                return 0;
            }
            if (item == null) {
                DebugType.Multiplayer.error("Transaction failed: item is null");
                return 1;
            }
            if (destination != null && destination.getParent() instanceof BaseVehicle && !destination.hasRoomFor((IsoGameCharacter)player, item.getUnequippedWeight() + destinationWeightInTransaction)) {
                DebugType.Objects.noise("Inconsistent: destination container is not contain enough space for the item (%s)", description);
                return 1;
            }
            if (destination != null && !(destination.getParent() instanceof IsoGameCharacter) && !(destination.getParent() instanceof BaseVehicle) && (float)destination.getEffectiveCapacity(player) < destination.getCapacityWeight() + item.getUnequippedWeight() + destinationWeightInTransaction) {
                DebugType.Objects.noise("Inconsistent: destination container is not contain enough space for the item (%s)", description);
                return 1;
            }
        }
        if (worldItem != null && destination != null) {
            if (!destination.isItemAllowed(worldItem)) {
                DebugType.Objects.noise("Inconsistent: destination container can't contain the item (%s)", description);
                return 1;
            }
            float weightAddedToFloor = worldItem.getUnequippedWeight() + destinationWeightInTransaction;
            if (destination.hasWorldItem() && worldItem.isOnGroundOrInsideBagOnSquare(destination.getWorldItem().getSquare())) {
                weightAddedToFloor = destinationWeightInTransaction;
            }
            if (!destination.hasRoomFor(player, worldItem.getUnequippedWeight() + destinationWeightInTransaction, weightAddedToFloor)) {
                DebugType.Objects.noise("Inconsistent: destination container is not contain enough space for the item (%s)", description);
                return 1;
            }
            if (!(destination.getParent() instanceof IsoGameCharacter) && !(destination.getParent() instanceof BaseVehicle) && (float)destination.getEffectiveCapacity(player) < destination.getCapacityWeight() + worldItem.getUnequippedWeight() + destinationWeightInTransaction) {
                DebugType.Objects.noise("Inconsistent: destination container is not contain enough space for the item (%s)", description);
                return 1;
            }
        }
        return 0;
    }

    public static int getItemId(InventoryItem item, ItemContainer source2) {
        if (item == null) {
            return -1;
        }
        if (source2.getType().equals("floor") && item.getWorldItem() != null) {
            return -1;
        }
        return item.id;
    }

    private static boolean floorHasRoomFor(IsoGridSquare square, IsoGameCharacter character, InventoryItem item, ItemContainer destContainer) {
        float capacity = destContainer.getEffectiveCapacity(character);
        float totalWeight = square.getTotalWeightOfItemsOnFloor();
        if (item.isOnGroundOrInsideBagOnSquare(square)) {
            totalWeight -= item.getUnequippedWeight();
        }
        if (totalWeight >= capacity) {
            return false;
        }
        if (transactions.stream().anyMatch(t -> t.square == square)) {
            return false;
        }
        if (ItemContainer.floatingPointCorrection(totalWeight) + item.getUnequippedWeight() <= capacity) {
            return true;
        }
        return item.getUnequippedWeight() >= capacity;
    }

    private static boolean canDropOnFloor(IsoGridSquare square, IsoGameCharacter character) {
        if (square == null) {
            return false;
        }
        if (!square.TreatAsSolidFloor()) {
            return false;
        }
        if (square.isSolid() || square.isSolidTrans()) {
            return false;
        }
        IsoGridSquare current = character.getCurrentSquare();
        if (current != null && square != current) {
            if (current.isBlockedTo(square) || current.isWindowTo(square)) {
                return false;
            }
            if (current.HasStairs() != square.HasStairs()) {
                return false;
            }
            if (current.HasStairs() && !current.isSameStaircase(square.getX(), square.getY(), square.getZ())) {
                return false;
            }
        }
        return true;
    }

    private static IsoGridSquare getNotFullFloorSquare(IsoGameCharacter character, InventoryItem item, ItemContainer destContainer) {
        IsoGridSquare square = character.getCurrentSquare();
        if (TransactionManager.canDropOnFloor(square, character) && TransactionManager.floorHasRoomFor(square, character, item, destContainer)) {
            return square;
        }
        for (int dy = -1; dy <= 1; ++dy) {
            for (int dx = -1; dx <= 1; ++dx) {
                if (dx == 0 && dy == 0 || !TransactionManager.canDropOnFloor(square = IsoWorld.instance.getCell().getGridSquare(character.getX() + (float)dx, character.getY() + (float)dy, character.getZ()), character) || !TransactionManager.floorHasRoomFor(square, character, item, destContainer)) continue;
                return square;
            }
        }
        return null;
    }

    public static boolean isRejected(byte id) {
        return transactions.stream().filter(r -> id == r.id).allMatch(r -> r.state == Transaction.TransactionState.Reject);
    }

    public static boolean isDone(byte id) {
        return transactions.stream().filter(r -> id == r.id).allMatch(r -> r.state == Transaction.TransactionState.Done);
    }

    public static void cancelAllRelevantToUser(IsoPlayer player) {
        for (Transaction transaction : transactions) {
            if (!transaction.entries.stream().anyMatch(x -> x.sourceId.containerType == ContainerID.ContainerType.PlayerInventory && x.sourceId.playerId.getID() == player.onlineId) && !transaction.entries.stream().anyMatch(x -> x.destinationId.containerType == ContainerID.ContainerType.PlayerInventory && x.destinationId.playerId.getID() == player.onlineId)) continue;
            transaction.setState(Transaction.TransactionState.Reject);
        }
    }

    public static int getDuration(byte id) {
        Optional<Transaction> res = transactions.stream().filter(r -> id == r.id).findFirst();
        if (res.isEmpty()) {
            return -1;
        }
        Transaction t = res.get();
        return (int)(t.endTime - t.startTime);
    }

    public static byte createItemTransaction(IsoPlayer player, List<InventoryItem> items, ItemContainer source2, ItemContainer destination) {
        if (items.stream().allMatch(x -> TransactionManager.isConsistent(TransactionManager.getItemId(x, source2), x, source2, destination, null, null, player) == 0)) {
            ItemTransactionPacket transaction = new ItemTransactionPacket();
            try {
                transaction.set(player, items, source2, destination, null, IsoDirections.N, 0.0f, 0.0f, 0.0f);
                ByteBufferWriter bbw2 = GameClient.connection.startPacket();
                PacketTypes.PacketType.ItemTransaction.doPacket(bbw2);
                transaction.write(bbw2);
                PacketTypes.PacketType.ItemTransaction.send(GameClient.connection);
                TransactionManager.add(transaction);
            }
            catch (Exception e) {
                DebugType.General.printException(e, "createItemTransaction: error while creating package. player=" + String.valueOf(player) + " items count=" + items.size() + " source=" + String.valueOf(source2) + " destination=" + String.valueOf(destination), LogSeverity.Error);
                return 0;
            }
            return transaction.id;
        }
        return 0;
    }

    public static void removeItemTransaction(byte id, boolean isCanceled) {
        if (GameClient.client) {
            if (id != 0) {
                if (isCanceled) {
                    ItemTransactionPacket transaction = new ItemTransactionPacket();
                    transaction.id = id;
                    transaction.setState(Transaction.TransactionState.Reject);
                    ByteBufferWriter bbw2 = GameClient.connection.startPacket();
                    PacketTypes.PacketType.ItemTransaction.doPacket(bbw2);
                    transaction.write(bbw2);
                    PacketTypes.PacketType.ItemTransaction.send(GameClient.connection);
                }
                transactions.removeIf(r -> r.id == id);
            }
        } else if (GameServer.server) {
            transactions.removeIf(r -> r.id == id);
        }
    }

    public void setStateFromPacket(ItemTransactionPacket packet) {
        for (Transaction transaction : transactions) {
            if (packet.id != transaction.id) continue;
            transaction.setState(packet.state);
            transaction.setDuration(packet.duration);
            break;
        }
    }

    public static class LightweightData {
        public String lastItemFullType = "";
        public int itemsCount;
        public long itemsLastTransactionTime;
    }
}

