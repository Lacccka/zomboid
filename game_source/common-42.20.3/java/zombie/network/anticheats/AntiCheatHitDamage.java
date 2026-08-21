/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugType;
import zombie.network.anticheats.AbstractAntiCheat;
import zombie.network.fields.hit.Hit;
import zombie.network.packets.INetworkPacket;

public class AntiCheatHitDamage
extends AbstractAntiCheat {
    private static final int MAX_DAMAGE = 100;

    @Override
    public String validate(UdpConnection connection, INetworkPacket packet) {
        String result = super.validate(connection, packet);
        if (!(packet instanceof IAntiCheat)) {
            DebugType.Multiplayer.error("Invalid packet-type=%s for anti-cheat=%s", packet.getClass().getSimpleName(), this.getClass().getSimpleName());
            return "";
        }
        IAntiCheat field = (IAntiCheat)((Object)packet);
        float damage = field.getHit().getDamage();
        if (damage > 100.0f) {
            return String.format("damage=%f is too big", Float.valueOf(damage));
        }
        return result;
    }

    public static interface IAntiCheat {
        public Hit getHit();
    }
}

