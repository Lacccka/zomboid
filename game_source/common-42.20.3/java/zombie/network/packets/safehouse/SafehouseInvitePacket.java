/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.packets.safehouse;

import zombie.Lua.LuaEventManager;
import zombie.characters.Capability;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.iso.areas.SafeHouse;
import zombie.network.PacketSetting;
import zombie.network.PacketTypes;
import zombie.network.anticheats.AntiCheat;
import zombie.network.anticheats.AntiCheatSafeHouseOwner;
import zombie.network.fields.SafehouseInvite;
import zombie.network.packets.INetworkPacket;

@PacketSetting(ordering=0, priority=1, reliability=2, requiredCapability=Capability.LoginOnServer, handlingType=3, anticheats={AntiCheat.SafeHouseOwner})
public class SafehouseInvitePacket
extends SafehouseInvite
implements INetworkPacket,
AntiCheatSafeHouseOwner.IAntiCheat {
    @Override
    public void setData(Object ... values2) {
        this.set((SafeHouse)values2[0], (String)values2[1], (String)values2[2]);
    }

    @Override
    public void processClient(UdpConnection connection) {
        LuaEventManager.triggerEvent("ReceiveSafehouseInvite", this.getSafehouse(), this.getOwner(), this.getUsername());
    }

    @Override
    public void processServer(PacketTypes.PacketType packetType, UdpConnection connection) {
        if (SafeHouse.hasSafehouse(this.getUsername()) != null) {
            DebugType.Multiplayer.warn("player is already member or owner of safehouse");
            return;
        }
        this.getSafehouse().addInvite(this.getUsername());
        this.sendToClient(packetType, this.getUsername());
    }
}

