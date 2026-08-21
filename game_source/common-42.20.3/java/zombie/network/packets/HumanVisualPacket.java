/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets;

import java.io.IOException;
import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.core.logger.ExceptionLogger;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.fields.character.PlayerID;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class HumanVisualPacket
implements INetworkPacket {
    @JSONField
    private final PlayerID playerId = new PlayerID();

    @Override
    public void setData(Object ... values2) {
        this.playerId.set((IsoPlayer)values2[0]);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.playerId.parse(b, connection);
        if (this.playerId.isConsistent(connection)) {
            try {
                this.playerId.getPlayer().getHumanVisual().load(b.bb, 249);
            }
            catch (IOException e) {
                ExceptionLogger.logException(e);
            }
        }
    }

    @Override
    public void write(ByteBufferWriter b) {
        this.playerId.write(b);
        try {
            this.playerId.getPlayer().getHumanVisual().save(b.bb);
        }
        catch (IOException e) {
            ExceptionLogger.logException(e);
        }
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        return this.playerId.isConsistent(connection);
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        this.sendToClients(PacketTypes.PacketType.HumanVisual, connection);
    }

    @Override
    public void processClient(UdpConnection connection) {
        this.playerId.getPlayer().resetModelNextFrame();
    }
}

