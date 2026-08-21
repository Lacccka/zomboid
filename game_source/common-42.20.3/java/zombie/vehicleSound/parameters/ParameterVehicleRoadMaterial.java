/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.audio.parameters.ParameterVehicleRoadMaterial;
import zombie.vehicleSound.VehicleSoundOwner;

public final class ParameterVehicleRoadMaterial
extends FMODLocalParameter {
    private final VehicleSoundOwner vehicle;

    public ParameterVehicleRoadMaterial(VehicleSoundOwner vehicle) {
        super("VehicleRoadMaterial");
        this.vehicle = vehicle;
    }

    @Override
    public float calculateCurrentValue() {
        if (!this.vehicle.isEngineRunning()) {
            return Float.isNaN(this.getCurrentValue()) ? 0.0f : this.getCurrentValue();
        }
        return this.getMaterial().label;
    }

    private ParameterVehicleRoadMaterial.Material getMaterial() {
        return this.vehicle.getRoadMaterial();
    }
}

