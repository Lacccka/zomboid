/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.vehicleSound.VehicleSoundOwner;

public class ParameterVehicleLoad
extends FMODLocalParameter {
    private final VehicleSoundOwner vehicle;

    public ParameterVehicleLoad(VehicleSoundOwner vehicle) {
        super("VehicleLoad");
        this.vehicle = vehicle;
    }

    @Override
    public float calculateCurrentValue() {
        return this.vehicle.isGasPedalPressed() ? 1.0f : 0.0f;
    }
}

