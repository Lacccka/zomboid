/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets;

import gnu.trove.list.array.TShortArrayList;
import java.io.IOException;
import zombie.characters.Capability;
import zombie.core.logger.ExceptionLogger;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.iso.IsoChunk;
import zombie.network.IConnection;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.ServerMap;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=3, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=1)
public class ChunkObjectStateRequestPacket
implements INetworkPacket {
    private TShortArrayList chunkObjectState;

    @Override
    public void setData(Object ... values2) {
        this.chunkObjectState = (TShortArrayList)values2[0];
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        int size = b.getInt();
        for (int i = 0; i < size; i += 2) {
            short wy;
            short wx = b.getShort();
            IsoChunk chunk = ServerMap.instance.getChunk(wx, wy = b.getShort());
            if (chunk == null) {
                connection.addChunkObjectState(wx);
                connection.addChunkObjectState(wy);
                continue;
            }
            ByteBufferWriter bbw = connection.startPacket();
            PacketTypes.PacketType.ChunkObjectStateResponse.doPacket(bbw);
            bbw.putShort(wx);
            bbw.putShort(wy);
            try {
                if (chunk.saveObjectState(bbw.bb)) {
                    PacketTypes.PacketType.ChunkObjectStateResponse.send(connection);
                    continue;
                }
                connection.cancelPacket();
                continue;
            }
            catch (IOException e) {
                ExceptionLogger.logException(e);
                connection.cancelPacket();
            }
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putInt(this.chunkObjectState.size());
        for (int i = 0; i < this.chunkObjectState.size(); i += 2) {
            b.putShort(this.chunkObjectState.get(i));
            b.putShort(this.chunkObjectState.get(i + 1));
        }
    }
}

