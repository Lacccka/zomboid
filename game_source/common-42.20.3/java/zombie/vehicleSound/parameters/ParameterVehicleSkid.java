/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.core.math.PZMath;
import zombie.vehicleSound.VehicleSoundOwner;

public class ParameterVehicleSkid
extends FMODLocalParameter {
    private static final float NO_SKID = 0.0f;
    private static final float FULL_SKID = 1.0f;
    private final VehicleSoundOwner vehicle;

    public ParameterVehicleSkid(VehicleSoundOwner vehicle) {
        super("VehicleSkid");
        this.vehicle = vehicle;
    }

    @Override
    public float calculateCurrentValue() {
        float skid = PZMath.clamp_01(this.vehicle.getMinWheelSkid());
        return 1.0f * PZMath.roundFloat(1.0f - skid, 2);
    }
}

