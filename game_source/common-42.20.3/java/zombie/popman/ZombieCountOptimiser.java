/*
 * Decompiled with CFR 0.152.
 */
package zombie.popman;

import java.util.ArrayList;
import java.util.List;
import zombie.SandboxOptions;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.core.raknet.UdpConnection;
import zombie.core.random.Rand;
import zombie.iso.IsoUtils;
import zombie.network.GameServer;
import zombie.network.ServerMap;
import zombie.network.statistics.data.GameStatistic;
import zombie.popman.NetworkZombiePacker;

public class ZombieCountOptimiser {
    private static final List<IsoZombie> zombiesForDelete = new ArrayList<IsoZombie>();

    public static void prepareZombiesForDeletion() {
        if (GameServer.udpEngine == null) {
            return;
        }
        int zombiesCountBeforeDeletion = SandboxOptions.instance.zombieConfig.zombiesCountBeforeDeletion.getValue();
        if (zombiesCountBeforeDeletion <= 0) {
            return;
        }
        for (int n = 0; n < GameServer.udpEngine.connections.size(); ++n) {
            UdpConnection connection = GameServer.udpEngine.connections.get(n);
            List<Short> zombiesOnlineIdList = NetworkZombiePacker.getInstance().zombiesToSend.get(connection);
            if (zombiesOnlineIdList == null || zombiesOnlineIdList.isEmpty()) continue;
            int zombiesCountForDelete = Math.max(0, zombiesOnlineIdList.size() - zombiesCountBeforeDeletion);
            for (Short zombieOnlineId : zombiesOnlineIdList) {
                IsoZombie zombie = ServerMap.instance.zombieMap.get(zombieOnlineId);
                if (zombie == null || zombiesCountForDelete <= 0 || Rand.Next(Rand.AdjustForFramerate(10)) != 0 || zombie.isReanimatedPlayer() || zombie.getTarget() != null || !ZombieCountOptimiser.isOutside(zombie) || !ZombieCountOptimiser.canBeDeletedUnnoticed(zombie, connection)) continue;
                zombiesForDelete.add(zombie);
                GameStatistic.getInstance().zombiesCulled.increase();
                --zombiesCountForDelete;
            }
        }
    }

    public static void deleteZombies() {
        if (zombiesForDelete.isEmpty()) {
            return;
        }
        for (IsoZombie zombieForDelete : zombiesForDelete) {
            NetworkZombiePacker.getInstance().deleteZombie(zombieForDelete);
            zombieForDelete.removeFromWorld();
            zombieForDelete.removeFromSquare();
        }
        zombiesForDelete.clear();
    }

    private static boolean canBeDeletedUnnoticed(IsoZombie zombie, UdpConnection connection) {
        if (!GameServer.server) {
            return false;
        }
        for (IsoPlayer player : connection.players) {
            float relevantDistance;
            float distance;
            if (player == null || !((distance = IsoUtils.DistanceToSquared(zombie.getX(), zombie.getY(), player.getX(), player.getY())) <= (relevantDistance = (float)((connection.getRelevantRange() - 2) * 10)) * relevantDistance)) continue;
            return false;
        }
        return true;
    }

    private static boolean isOutside(IsoZombie zombie) {
        return zombie.getCurrentSquare() == null || !zombie.getCurrentSquare().isInARoom() && !zombie.getCurrentSquare().haveRoof;
    }
}

