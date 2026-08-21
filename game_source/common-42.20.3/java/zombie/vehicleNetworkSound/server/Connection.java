/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleNetworkSound.server;

import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.set.hash.TShortHashSet;
import zombie.core.math.PZMath;
import zombie.core.raknet.UdpConnection;
import zombie.vehicleNetworkSound.server.VehicleState;
import zombie.vehicles.BaseVehicle;

final class Connection {
    private final UdpConnection udpConnection;
    private final TShortObjectHashMap<VehicleState> stateMap = new TShortObjectHashMap();
    private static final TShortHashSet irrelevantVehicleIDs = new TShortHashSet();

    Connection(UdpConnection udpConnection) {
        this.udpConnection = udpConnection;
    }

    void updateVehicle(BaseVehicle vehicle) {
        VehicleState state = this.getState(vehicle);
        if (state == null) {
            state = this.createState(vehicle);
            state.sendNew(this.udpConnection);
            return;
        }
        state.update(vehicle, this.udpConnection);
    }

    void forgetIrrelevantVehicles(TShortHashSet relevantVehicleIDs) {
        irrelevantVehicleIDs.clear();
        irrelevantVehicleIDs.addAll(this.stateMap.keySet());
        irrelevantVehicleIDs.removeAll(relevantVehicleIDs);
        irrelevantVehicleIDs.forEach(id -> {
            VehicleState state = this.stateMap.remove(id);
            state.remove(this.udpConnection);
            return true;
        });
    }

    void removeVehicle(BaseVehicle vehicle) {
        VehicleState state = this.getState(vehicle);
        if (state == null) {
            return;
        }
        state.remove(this.udpConnection);
        this.stateMap.remove(state.id);
    }

    boolean isRelevant(BaseVehicle vehicle) {
        float radius = 0.0f;
        if (vehicle.isAlarmActive()) {
            radius = PZMath.max(radius, 500.0f);
        }
        if (vehicle.isBackupBeeperSounding()) {
            radius = PZMath.max(radius, 150.0f);
        }
        if (vehicle.isDoorAlarmSounding()) {
            radius = PZMath.max(radius, 50.0f);
        }
        if (vehicle.getEngineState() != BaseVehicle.engineStateTypes.Idle) {
            radius = PZMath.max(radius, 200.0f);
        }
        if (vehicle.isHornSounding()) {
            radius = PZMath.max(radius, 500.0f);
        }
        if (vehicle.isSirenSounding()) {
            radius = PZMath.max(radius, 500.0f);
        }
        return radius > 0.0f && this.udpConnection.RelevantTo(vehicle.getX(), vehicle.getY(), radius);
    }

    VehicleState createState(BaseVehicle vehicle) {
        VehicleState state = new VehicleState();
        state.set(vehicle);
        this.stateMap.put(state.id, state);
        return state;
    }

    VehicleState getState(BaseVehicle vehicle) {
        return this.stateMap.get(vehicle.getId());
    }
}

