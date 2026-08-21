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

public class AntiCheatSafeHouseOwner
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
        if (!connection.hasPlayer(field.getSafehouse().getOwner())) {
            return "owner not found";
        }
        return result;
    }

    public static interface IAntiCheat {
        public SafeHouse getSafehouse();
    }
}

