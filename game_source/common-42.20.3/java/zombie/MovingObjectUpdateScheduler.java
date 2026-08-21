/*
 * Decompiled with CFR 0.152.
 */
package zombie;

import zombie.GameWindow;
import zombie.MovingObjectUpdateSchedulerUpdateBucket;
import zombie.UpdateSchedulerSimulationLevel;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.core.math.PZMath;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoWorld;
import zombie.network.GameServer;
import zombie.popman.ZombieCountOptimiser;
import zombie.util.list.PZArrayUtil;

public final class MovingObjectUpdateScheduler {
    public static final MovingObjectUpdateScheduler instance = new MovingObjectUpdateScheduler();
    private final MovingObjectUpdateSchedulerUpdateBucket[] simulationLevels = new MovingObjectUpdateSchedulerUpdateBucket[UpdateSchedulerSimulationLevel.numValues()];
    private long frameCounter;
    private boolean isEnabled = true;

    private MovingObjectUpdateScheduler() {
        for (UpdateSchedulerSimulationLevel simulationLevel : UpdateSchedulerSimulationLevel.allValues()) {
            this.simulationLevels[simulationLevel.getUpdateOrderIndex()] = new MovingObjectUpdateSchedulerUpdateBucket(simulationLevel);
        }
    }

    public long getFrameCounter() {
        return this.frameCounter;
    }

    public void startFrame() {
        ++this.frameCounter;
        PZArrayUtil.forEach(this.simulationLevels, MovingObjectUpdateSchedulerUpdateBucket::clear);
        float averageFps = GameWindow.averageFPS;
        if (GameServer.server) {
            ZombieCountOptimiser.prepareZombiesForDeletion();
        }
        for (IsoMovingObject isoMovingObject : IsoWorld.instance.getCell().getObjectList()) {
            if (GameServer.server && isoMovingObject instanceof IsoZombie) {
                IsoZombie isoZombie = (IsoZombie)isoMovingObject;
                if (!GameServer.guiCommandline) continue;
                isoZombie.updateForServerGui();
                continue;
            }
            if (isoMovingObject.getCurrentSquare() == null) {
                isoMovingObject.setCurrentSquareFromPosition();
            }
            UpdateSchedulerSimulationLevel sim = this.getUpdateSchedulerSimulationLevelForObject(isoMovingObject, averageFps);
            this.simulationLevels[sim.getUpdateOrderIndex()].add(isoMovingObject);
        }
    }

    private UpdateSchedulerSimulationLevel getUpdateSchedulerSimulationLevelForObject(IsoMovingObject isoMovingObject, float averageFps) {
        if (!this.isEnabled || GameServer.server) {
            return UpdateSchedulerSimulationLevel.FULL;
        }
        UpdateSchedulerSimulationLevel minSim = isoMovingObject.getMinimumSimulationLevel();
        if (minSim == UpdateSchedulerSimulationLevel.FULL) {
            return minSim;
        }
        if (!isoMovingObject.getDoRender() || isoMovingObject.isSceneCulled()) {
            return minSim;
        }
        float distance = 1.0E8f;
        int levelSeparation = Integer.MAX_VALUE;
        float alpha = 0.0f;
        float targetAlpha = 0.0f;
        for (int playerIndex = 0; playerIndex < IsoPlayer.numPlayers; ++playerIndex) {
            IsoPlayer player = IsoPlayer.players[playerIndex];
            if (player == null) continue;
            if (player == isoMovingObject) {
                return UpdateSchedulerSimulationLevel.FULL;
            }
            distance = PZMath.min(isoMovingObject.DistTo(player), distance);
            levelSeparation = PZMath.min(PZMath.abs(isoMovingObject.getZi() - player.getZi()), levelSeparation);
            alpha = PZMath.max(isoMovingObject.getAlpha(playerIndex), alpha);
            targetAlpha = PZMath.max(isoMovingObject.getTargetAlpha(playerIndex), targetAlpha);
        }
        UpdateSchedulerSimulationLevel sim = UpdateSchedulerSimulationLevel.FULL;
        float minAlpha = 0.25f;
        if (alpha < 0.25f && targetAlpha < 0.25f) {
            sim = sim.less();
            if (distance > 10.0f) {
                sim = sim.less();
            }
            if (levelSeparation > 1) {
                sim = minSim;
            }
        }
        if (distance > 30.0f) {
            sim = sim.less();
        }
        if (distance > 60.0f) {
            sim = sim.less();
            if (averageFps < 20.0f) {
                sim = sim.less();
            }
            if (averageFps < 10.0f) {
                sim = sim.less();
            }
        }
        if (distance > 80.0f) {
            sim = sim.less();
            if (averageFps < 20.0f) {
                sim = sim.less();
            }
        }
        if (averageFps > 25.0f) {
            sim = sim.more();
        }
        if (averageFps > 35.0f) {
            sim = sim.more();
        }
        if (averageFps > 45.0f) {
            sim = sim.more();
        }
        if (averageFps > 55.0f) {
            sim = sim.more();
        }
        sim = sim.max(minSim);
        return sim;
    }

    public void update() {
        for (MovingObjectUpdateSchedulerUpdateBucket simulation : this.simulationLevels) {
            simulation.update((int)this.frameCounter);
        }
    }

    public void postupdate() {
        if (GameServer.server) {
            ZombieCountOptimiser.deleteZombies();
        }
        for (MovingObjectUpdateSchedulerUpdateBucket simulation : this.simulationLevels) {
            simulation.postupdate((int)this.frameCounter);
        }
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public void removeObject(IsoMovingObject object) {
        PZArrayUtil.forEach(this.simulationLevels, object, MovingObjectUpdateSchedulerUpdateBucket::removeObject);
    }
}

