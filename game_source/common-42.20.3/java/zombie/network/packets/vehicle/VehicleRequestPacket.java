/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.vehicle;

import gnu.trove.iterator.TShortShortIterator;
import gnu.trove.map.hash.TShortShortHashMap;
import zombie.characters.Capability;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.network.IConnection;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.packets.INetworkPacket;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.VehicleManager;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=1)
public class VehicleRequestPacket
implements INetworkPacket {
    private TShortShortHashMap vehicleRequests;
    private final TShortShortHashMap parsedRequests = new TShortShortHashMap();

    @Override
    public void setData(Object ... values2) {
        this.vehicleRequests = (TShortShortHashMap)values2[0];
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.parsedRequests.clear();
        int size = b.getShort();
        for (int i = 0; i < size; ++i) {
            short vehicleId = b.getShort();
            this.parsedRequests.put(vehicleId, b.getShort());
        }
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        TShortShortIterator iterator2 = this.parsedRequests.iterator();
        while (iterator2.hasNext()) {
            iterator2.advance();
            BaseVehicle vehicle = VehicleManager.instance.getVehicleByID(iterator2.key());
            if (vehicle == null) continue;
            short flag = iterator2.value();
            BaseVehicle.ServerVehicleState state = VehicleManager.instance.getVehicleState(connection, vehicle);
            if (flag == 16384) {
                if (connection.isRelevantTo(vehicle.getX(), vehicle.getY())) continue;
                INetworkPacket.send(connection, PacketTypes.PacketType.VehicleRemove, vehicle);
                continue;
            }
            state.flags = (short)(state.flags | flag);
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putShort(this.vehicleRequests.size());
        TShortShortIterator iterator2 = this.vehicleRequests.iterator();
        while (iterator2.hasNext()) {
            iterator2.advance();
            b.putShort(iterator2.key());
            b.putShort(iterator2.value());
        }
    }
}

