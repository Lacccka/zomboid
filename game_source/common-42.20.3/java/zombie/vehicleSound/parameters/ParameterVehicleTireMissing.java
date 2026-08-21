/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.vehicleSound.VehicleSoundOwner;

public class ParameterVehicleTireMissing
extends FMODLocalParameter {
    private final VehicleSoundOwner vehicle;

    public ParameterVehicleTireMissing(VehicleSoundOwner vehicle) {
        super("VehicleTireMissing");
        this.vehicle = vehicle;
    }

    @Override
    public float calculateCurrentValue() {
        return this.vehicle.isAnyTireMissing() ? 1.0f : 0.0f;
    }
}

