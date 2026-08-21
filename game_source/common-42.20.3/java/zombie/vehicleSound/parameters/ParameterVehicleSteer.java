/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.core.math.PZMath;
import zombie.vehicleSound.VehicleSoundOwner;

public class ParameterVehicleSteer
extends FMODLocalParameter {
    private final VehicleSoundOwner vehicle;

    public ParameterVehicleSteer(VehicleSoundOwner vehicle) {
        super("VehicleSteer");
        this.vehicle = vehicle;
    }

    @Override
    public float calculateCurrentValue() {
        if (!this.vehicle.isEngineRunning()) {
            return 0.0f;
        }
        float value = this.vehicle.getMaxWheelSteering();
        return (float)((int)(PZMath.clamp(value, 0.0f, 1.0f) * 100.0f)) / 100.0f;
    }
}

