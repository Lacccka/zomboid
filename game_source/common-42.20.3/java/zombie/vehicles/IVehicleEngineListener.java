/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicles;

import zombie.vehicles.BaseVehicle;
import zombie.vehicles.VehicleEngineStateChangeReason;

public interface IVehicleEngineListener {
    public void onEngineStateChanged(BaseVehicle.engineStateTypes var1, BaseVehicle.engineStateTypes var2, VehicleEngineStateChangeReason var3);
}

