/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets;

import zombie.Lua.LuaEventManager;
import zombie.characters.Capability;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.network.GameServer;
import zombie.network.IConnection;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.fields.character.PlayerID;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class RequestMedicalCheckPacket
implements INetworkPacket {
    private static final int MAX_CHECK_DISTANCE = 2;
    private RequestType type;
    private final PlayerID target = new PlayerID();
    private final PlayerID requester = new PlayerID();

    @Override
    public void setData(Object ... values2) {
        this.type = (RequestType)((Object)values2[0]);
        this.target.set((IsoPlayer)values2[1]);
        this.requester.set((IsoPlayer)values2[2]);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        this.type = b.getEnum(RequestType.class);
        this.target.parse(b, connection);
        this.requester.parse(b, connection);
    }

    @Override
    public void write(ByteBufferWriter b) {
        b.putEnum(this.type);
        this.target.write(b);
        this.requester.write(b);
    }

    @Override
    public void processClient(UdpConnection connection) {
        switch (this.type.ordinal()) {
            case 0: {
                LuaEventManager.triggerEvent("RequestMedicalCheck", this.target.getPlayer(), this.requester.getPlayer());
                break;
            }
            case 1: {
                LuaEventManager.triggerEvent("AcceptedMedicalCheck", this.target.getPlayer(), this.requester.getPlayer());
            }
        }
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        UdpConnection targetConnection = GameServer.getConnectionFromPlayer(this.type == RequestType.ASK ? this.target.getPlayer() : this.requester.getPlayer());
        if (targetConnection == null || RequestMedicalCheckPacket.isCheckForbidden(this.target.getPlayer(), this.requester.getPlayer())) {
            return;
        }
        this.sendToClient(PacketTypes.PacketType.RequestMedicalCheck, targetConnection);
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        return this.target.getPlayer() != null && this.requester.getPlayer() != null;
    }

    public static boolean isCheckForbidden(IsoPlayer playerOne, IsoPlayer playerTwo) {
        return Math.abs(playerOne.getX() - playerTwo.getX()) > 2.0f || Math.abs(playerOne.getY() - playerTwo.getY()) > 2.0f;
    }

    public static enum RequestType {
        ASK,
        ACCEPT;

    }
}

