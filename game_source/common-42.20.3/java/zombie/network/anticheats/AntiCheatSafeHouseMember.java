/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.characters.Capability;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.iso.areas.SafeHouse;
import zombie.network.anticheats.AbstractAntiCheat;
import zombie.network.packets.INetworkPacket;

public class AntiCheatSafeHouseMember
extends AbstractAntiCheat {
    @Override
    public String validate(UdpConnection connection, INetworkPacket packet) {
        String result = super.validate(connection, packet);
        if (connection.getRole().hasCapability(Capability.CanSetupSafehouses)) {
            return result;
        }
        if (!(packet instanceof IAntiCheat)) {
            DebugType.Multiplayer.error("Invalid packet-type=%s for anti-cheat=%s", packet.getClass().getSimpleName(), this.getClass().getSimpleName());
            return "";
        }
        IAntiCheat field = (IAntiCheat)((Object)packet);
        if (!connection.hasPlayer(field.getUsername()) && !connection.hasPlayer(field.getSafehouse().getOwner())) {
            return "player not found and sender is not owner";
        }
        SafeHouse safeHouse = SafeHouse.hasSafehouse(field.getUsername());
        if (safeHouse == null || safeHouse.getOnlineID() != field.getSafehouse().getOnlineID()) {
            return "player is not member or owner of safehouse";
        }
        return result;
    }

    public static interface IAntiCheat {
        public SafeHouse getSafehouse();

        public String getUsername();
    }
}

