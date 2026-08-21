/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets;

import java.util.ArrayList;
import java.util.List;
import zombie.characters.Capability;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.iso.WorldStreamer;
import zombie.network.IConnection;
import zombie.network.PacketSetting;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=4, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=0)
public class ChunkNotReadyPacket
implements INetworkPacket {
    private final List<Integer> requestNumbers = new ArrayList<Integer>();

    @Override
    public void setData(Object ... values2) {
        this.requestNumbers.clear();
        for (int i = 0; i < values2.length; ++i) {
            this.requestNumbers.add((Integer)values2[i]);
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putInt(this.requestNumbers.size());
        for (int i = 0; i < this.requestNumbers.size(); ++i) {
            b.putInt(this.requestNumbers.get(i));
        }
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        int count = b.getInt();
        for (int i = 0; i < count; ++i) {
            WorldStreamer.instance.receiveChunkNotReady(b.getInt());
        }
    }
}

