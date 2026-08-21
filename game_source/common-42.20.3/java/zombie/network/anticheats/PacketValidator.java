/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.anticheats;

import zombie.core.raknet.UdpConnection;
import zombie.core.utils.UpdateLimit;
import zombie.network.GameServer;
import zombie.network.anticheats.AntiCheat;
import zombie.network.anticheats.SuspiciousActivity;

public class PacketValidator
extends SuspiciousActivity {
    private static final long PLAYER_UPDATE_TIMEOUT = 30000L;
    private final UpdateLimit ulPlayerUpdateTimeout = new UpdateLimit(30000L);

    public PacketValidator(UdpConnection connection) {
        super(connection);
    }

    public boolean playerUpdateTimeoutCheck() {
        return this.ulPlayerUpdateTimeout.Check();
    }

    public void playerUpdateTimeoutReset() {
        this.ulPlayerUpdateTimeout.Reset(30000L);
    }

    @Override
    public void update() {
        if (GameServer.fastForward || GameServer.isDelayedDisconnect(this.connection)) {
            this.playerUpdateTimeoutReset();
        } else {
            super.update();
            AntiCheat.update(this.connection);
            if (!this.connection.isFullyConnected()) {
                this.playerUpdateTimeoutReset();
            }
        }
    }
}

