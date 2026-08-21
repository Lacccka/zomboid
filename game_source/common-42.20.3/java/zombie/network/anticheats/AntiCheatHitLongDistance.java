/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.network.anticheats.AbstractAntiCheat;
import zombie.network.packets.INetworkPacket;

public class AntiCheatHitLongDistance
extends AbstractAntiCheat {
    private static final float PREDICTION_COMPENSATION_RANGE = 1.2f;

    @Override
    public String validate(UdpConnection connection, INetworkPacket packet) {
        String result = super.validate(connection, packet);
        if (!(packet instanceof IAntiCheat)) {
            DebugType.Multiplayer.error("Invalid packet-type=%s for anti-cheat=%s", packet.getClass().getSimpleName(), this.getClass().getSimpleName());
            return "";
        }
        IAntiCheat field = (IAntiCheat)((Object)packet);
        float distance = field.getDistance();
        float range = (float)(connection.getRelevantRange() * 8) * 1.2f;
        if (distance > range) {
            return String.format("distance=%f > range=%f", Float.valueOf(distance), Float.valueOf(range));
        }
        return result;
    }

    public static interface IAntiCheat {
        public float getDistance();
    }
}

