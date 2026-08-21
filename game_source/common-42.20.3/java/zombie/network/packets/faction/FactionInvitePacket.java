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
import zombie.network.fields.FactionId;
import zombie.network.fields.Invite;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3)
public class FactionInvitePacket
extends FactionId
implements INetworkPacket {
    @JSONField
    protected final Invite invite = new Invite();

    public void set(Faction faction, String host, String username) {
        this.set(faction);
        this.invite.set(host, username);
    }

    @Override
    public void setData(Object ... values2) {
        this.set((Faction)values2[0], (String)values2[1], (String)values2[2]);
    }

    @Override
    public void write(ByteBufferWriter b) {
        super.write(b);
        this.invite.write(b);
    }

    @Override
    public void parse(ByteBufferReader b, IConnection connection) {
        super.parse(b, connection);
        this.invite.parse(b, connection);
    }

    @Override
    public boolean isConsistent(IConnection connection) {
        if (!super.isConsistent(connection)) {
            return false;
        }
        return this.invite.isConsistent(connection);
    }

    @Override
    public void processClient(UdpConnection connection) {
        LuaEventManager.triggerEvent("ReceiveFactionInvite", this.getFaction(), this.invite.getHost(), this.invite.getUsername());
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        if (!connection.getRole().hasCapability(Capability.FactionCheat) && !connection.hasPlayer(this.getFaction().getOwner())) {
            DebugType.Multiplayer.error("owner not found");
            return;
        }
        if (Faction.isAlreadyInFaction(this.invite.getUsername())) {
            DebugType.Multiplayer.warn("player is already member or owner of faction");
            return;
        }
        this.getFaction().addInvite(this.invite.getUsername());
        this.sendToClient(packetType, this.invite.getUsername());
    }
}

