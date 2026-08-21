/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.anticheats.AbstractAntiCheat;

public class AntiCheatPlayerUpdate
extends AbstractAntiCheat {
    @Override
    public boolean update(UdpConnection connection) {
        super.update(connection);
        for (IsoPlayer player : connection.players) {
            if (player == null || !player.isDead() && player.getVehicle() == null) continue;
            connection.getValidator().playerUpdateTimeoutReset();
            return true;
        }
        return !this.antiCheat.isEnabled() || !this.isPlayerUpdateTimeout(connection);
    }

    private boolean isPlayerUpdateTimeout(UdpConnection connection) {
        connection.setReady(System.currentTimeMillis() > connection.connectionTimestamp);
        return connection.isReady() && connection.getValidator().playerUpdateTimeoutCheck();
    }
}

