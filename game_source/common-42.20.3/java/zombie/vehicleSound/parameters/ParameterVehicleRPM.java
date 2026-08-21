/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound.parameters;

import zombie.audio.FMODLocalParameter;
import zombie.core.math.PZMath;
import zombie.vehicleSound.VehicleSoundOwner;

public class ParameterVehicleRPM
extends FMODLocalParameter {
    private static final float MAX_IDLE_ENGINE_SPEED = 800.0f;
    private static final float MAX_ENGINE_SPEED = 7000.0f;
    private static final float MAX_IDLE_ENGINE_SPEED_MULTIPLIER = 1.1f;
    private final VehicleSoundOwner vehicle;

    public ParameterVehicleRPM(VehicleSoundOwner vehicle) {
        super("VehicleRPM");
        this.vehicle = vehicle;
    }

    @Override
    public float calculateCurrentValue() {
        float rpmIdle;
        float rpmIdleMax;
        float rpm1 = PZMath.clamp((float)this.vehicle.getEngineSpeed(), 0.0f, 7000.0f);
        float rpm2 = rpm1 < (rpmIdleMax = (rpmIdle = this.vehicle.getScript().getEngineIdleSpeed()) * 1.1f) ? rpm1 / rpmIdleMax * 800.0f : 800.0f + (rpm1 - rpmIdleMax) / (7000.0f - rpmIdleMax) * 6200.0f;
        float INCREMENT = 50.0f;
        return PZMath.ceil(rpm2 / 50.0f) * 50.0f;
    }
}

