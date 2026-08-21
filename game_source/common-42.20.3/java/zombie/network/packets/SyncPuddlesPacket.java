/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets;

import zombie.characters.Capability;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoPuddles;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=2)
public class SyncPuddlesPacket
implements INetworkPacket {
    @JSONField
    private float wetGround;
    @JSONField
    private float puddlesSize;
    @JSONField
    private float muddyPuddles;

    @Override
    public void setData(Object ... values2) {
        this.wetGround = ((IsoPuddles)values2[0]).getWetGroundFinalValue();
        this.puddlesSize = ((IsoPuddles)values2[0]).getPuddlesSizeFinalValue();
        this.muddyPuddles = ((IsoPuddles)values2[0]).getMuddyPuddlesFinalValue();
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putFloat(this.wetGround);
        b.putFloat(this.puddlesSize);
        b.putFloat(this.muddyPuddles);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.wetGround = b.getFloat();
        this.puddlesSize = b.getFloat();
        this.muddyPuddles = b.getFloat();
    }

    @Override
    public void processClient(UdpConnection connection) {
        IsoPuddles.getInstance().applyNetworkUpdate(this.wetGround, this.puddlesSize, this.muddyPuddles);
    }
}

