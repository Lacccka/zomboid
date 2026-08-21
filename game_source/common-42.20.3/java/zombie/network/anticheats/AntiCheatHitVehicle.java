/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.characters.IsoGameCharacter;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.network.anticheats.AbstractAntiCheat;
import zombie.network.packets.INetworkPacket;
import zombie.vehicles.BaseVehicle;

public class AntiCheatHitVehicle
extends AbstractAntiCheat {
    @Override
    public String validate(UdpConnection connection, INetworkPacket packet) {
        String result = super.validate(connection, packet);
        if (!(packet instanceof IAntiCheat)) {
            DebugType.Multiplayer.error("Invalid packet-type=%s for anti-cheat=%s", packet.getClass().getSimpleName(), this.getClass().getSimpleName());
            return "";
        }
        IAntiCheat field = (IAntiCheat)((Object)packet);
        BaseVehicle vehicle = field.getVehicle();
        if (vehicle == null) {
            return "vehicle not found";
        }
        IsoGameCharacter driver = vehicle.getDriverRegardlessOfTow();
        if (driver == null) {
            return "driver not found";
        }
        if (!connection.hasPlayer(driver.getOnlineID())) {
            return "driver is not authorized";
        }
        return result;
    }

    public static interface IAntiCheat {
        public BaseVehicle getVehicle();
    }
}

