/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleNetworkSound;

import zombie.characters.Capability;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.network.IConnection;
import zombie.network.PacketSetting;
import zombie.network.packets.INetworkPacket;
import zombie.vehicleNetworkSound.SharedVehicleState;
import zombie.vehicleNetworkSound.client.Manager;

@PacketSetting(ordering=8, priority=2, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=6)
public final class UpdateVehiclePacket
extends SharedVehicleState
implements INetworkPacket {
    private int changeBits;

    @Override
    public void setData(Object ... values2) {
        SharedVehicleState state = (SharedVehicleState)values2[0];
        this.changeBits = (Integer)values2[1];
        this.set(state);
    }

    @Override
    public void parse(ByteBufferReader bb, IConnection connection) {
        this.changeBits = super.parseUpdate(bb);
    }

    @Override
    public void write(ByteBufferWriter bb) {
        super.write(bb, this.changeBits);
    }

    @Override
    public void processClientLoading(UdpConnection connection) {
        this.processClient(connection);
    }

    @Override
    public void processClient(UdpConnection connection) {
        Manager mgr = Manager.getInstance();
        mgr.updateVehicle(this, this.changeBits);
    }
}

