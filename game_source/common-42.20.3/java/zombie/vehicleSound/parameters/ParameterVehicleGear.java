/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.vehicleSound.VehicleSoundOwner;

public class ParameterVehicleGear
extends FMODLocalParameter {
    private final VehicleSoundOwner vehicle;

    public ParameterVehicleGear(VehicleSoundOwner vehicle) {
        super("VehicleGear");
        this.vehicle = vehicle;
    }

    @Override
    public float calculateCurrentValue() {
        return (float)this.vehicle.getTransmissionNumber() + 1.0f;
    }
}

