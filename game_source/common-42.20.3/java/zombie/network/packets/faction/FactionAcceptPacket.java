/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.faction;

import zombie.Lua.LuaEventManager;
import zombie.characters.Capability;
import zombie.characters.Faction;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.network.IConnection;
import zombie.network.JSONField;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.chat.ChatServer;
import zombie.network.packets.INetworkPacket;
import zombie.network.packets.faction.FactionInvitePacket;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class FactionAcceptPacket
extends FactionInvitePacket
implements INetworkPacket {
    @JSONField
    private boolean isAccepted;

    @Override
    public void setData(Object ... values2) {
        super.set((Faction)values2[0], (String)values2[1], (String)values2[2]);
        this.isAccepted = (Boolean)values2[3];
    }

    @Override
    public void write(ByteBufferWriter b) {
        super.write(b);
        b.putBoolean(this.isAccepted);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        super.parse(b, connection);
        this.isAccepted = b.getBoolean();
    }

    @Override
    public void processClient(UdpConnection connection) {
        if (this.isAccepted) {
            this.getFaction().addPlayer(this.invite.getUsername());
            LuaEventManager.triggerEvent("AcceptedFactionInvite", this.getFaction().getName(), this.invite.getHost());
        }
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        if (!connection.getRole().hasCapability(Capability.FactionCheat) && !connection.hasPlayer(this.invite.getUsername())) {
            DebugType.Multiplayer.error("player not found");
            return;
        }
        if (Faction.isAlreadyInFaction(this.invite.getUsername())) {
            DebugType.Multiplayer.warn("player is already member or owner of faction");
            return;
        }
        if (!this.getFaction().hasInvite(this.invite.getUsername())) {
            DebugType.Multiplayer.warn("invite is not found");
            return;
        }
        if (this.isAccepted) {
            this.getFaction().addPlayer(this.invite.getUsername());
        }
        this.getFaction().removeInvite(this.invite.getUsername());
        INetworkPacket.sendToAll(PacketTypes.PacketType.FactionSync, this.getFaction());
        this.sendToClients(packetType, null);
        if (this.isAccepted) {
            ChatServer.getInstance().syncFactionChatMembers(this.getFaction().getName(), this.getFaction().getOwner(), this.getFaction().getPlayers());
        }
    }
}

