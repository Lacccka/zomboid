/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters.component;

import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.characters.NetworkZombieAI;
import zombie.characters.component.NetworkComponent;
import zombie.core.raknet.UdpConnection;

public class NetworkZombieComponent
extends NetworkComponent {
    private UdpConnection authOwner;
    private IsoPlayer authOwnerPlayer;
    private final NetworkZombieAI networkAi;

    public NetworkZombieComponent(IsoZombie zombie) {
        this.networkAi = new NetworkZombieAI(zombie);
    }

    @Override
    public boolean isRemote() {
        return this.authOwner == null;
    }

    @Override
    public void updateNetworkAI() {
    }

    public UdpConnection getAuthOwner() {
        return this.authOwner;
    }

    public void setAuthOwner(UdpConnection authOwner) {
        this.authOwner = authOwner;
    }

    public IsoPlayer getOwnerPlayer() {
        return this.authOwnerPlayer;
    }

    public void setOwnerPlayer(IsoPlayer player) {
        this.authOwnerPlayer = player;
    }

    @Override
    public NetworkZombieAI getNetworkAI() {
        return this.networkAi;
    }
}

