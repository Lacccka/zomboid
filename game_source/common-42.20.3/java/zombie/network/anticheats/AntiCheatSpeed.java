/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.characters.Capability;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.network.ServerOptions;
import zombie.network.anticheats.AbstractAntiCheat;
import zombie.network.fields.IMovable;
import zombie.network.packets.INetworkPacket;

public class AntiCheatSpeed
extends AbstractAntiCheat {
    private static final int MAX_SPEED = 20;

    @Override
    public String validate(UdpConnection connection, INetworkPacket packet) {
        String result = super.validate(connection, packet);
        if (!(packet instanceof IAntiCheat)) {
            DebugType.Multiplayer.error("Invalid packet-type=%s for anti-cheat=%s", packet.getClass().getSimpleName(), this.getClass().getSimpleName());
            return "";
        }
        IAntiCheat field = (IAntiCheat)((Object)packet);
        int movableCount = field.getMovableCount();
        for (int i = 0; i < movableCount; ++i) {
            float limit;
            IMovable movable = field.getMovable(i);
            if (movable == null) continue;
            field.resetMovable();
            if (connection.getRole().hasCapability(Capability.TeleportToPlayer) || connection.getRole().hasCapability(Capability.TeleportToCoordinates) || connection.getRole().hasCapability(Capability.TeleportPlayerToAnotherPlayer) || connection.getRole().hasCapability(Capability.UseFastMoveCheat)) continue;
            float f = limit = movable.isVehicle() ? (float)ServerOptions.instance.speedLimit.getValue() : 20.0f;
            if (!(movable.getSpeed() > limit)) continue;
            return String.format("speed=%f > limit=%f", Float.valueOf(movable.getSpeed()), Float.valueOf(limit));
        }
        return result;
    }

    public static interface IAntiCheat {
        default public void resetMovable() {
        }

        public IMovable getMovable(int var1);

        public int getMovableCount();
    }
}

