/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound;

import zombie.GameTime;
import zombie.SandboxOptions;
import zombie.audio.BaseSoundEmitter;
import zombie.audio.parameters.ParameterVehicleRoadMaterial;
import zombie.scripting.objects.VehicleScript;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.LightbarSirenMode;

public interface VehicleSoundOwner {
    public float getX();

    public float getY();

    public float getZ();

    public int getXi();

    public int getYi();

    public int getZi();

    public boolean isListenerInRange(float var1);

    public String getScriptName();

    public VehicleScript getScript();

    public int getEngineCondition();

    public int getEngineQuality();

    public BaseVehicle.engineStateTypes getEngineState();

    public boolean isEngineRunning();

    public boolean isEngineSounding();

    public double getEngineSpeed();

    public int getTransmissionNumber();

    public float getCurrentSpeedKmHour();

    public float getMaxSpeed();

    default public boolean hasAlarm() {
        return this.getScript().getSounds().alarmEnable;
    }

    public boolean isAlarmActive();

    public boolean isAlarmSoundOn();

    public boolean isAlarmSounding();

    public boolean isBrakePedalPressed();

    public boolean isGasPedalPressed();

    public ParameterVehicleRoadMaterial.Material getRoadMaterial();

    public String getChosenAlarmSound();

    public boolean isBackupBeeperSounding();

    public boolean isDoorAlarmSounding();

    default public boolean hasHorn() {
        return this.getScript().getSounds().hornEnable;
    }

    public boolean isHornSounding();

    public BaseSoundEmitter getVehicleSoundEmitter();

    public boolean isAnyListenerInside();

    public boolean isSirenActive();

    public boolean isSirenSounding();

    default public boolean hasSiren() {
        return this.getScript().getLightbar().enable;
    }

    public double getSirenStartTime();

    public void setSirenStartTime(double var1);

    default public boolean sirenShutoffTimeExpired() {
        double shutoffHours = SandboxOptions.instance.sirenShutoffHours.getValue();
        if (shutoffHours <= 0.0) {
            return false;
        }
        double worldAge = GameTime.instance.getWorldAgeHours();
        this.setSirenStartTime(GameTime.minHours(this.getSirenStartTime(), worldAge));
        return this.getSirenStartTime() + shutoffHours < worldAge;
    }

    default public boolean hasLightbar() {
        return this.getScript().getLightbar().enable;
    }

    public LightbarSirenMode getLightbarSirenModeObject();

    default public int getLightbarSirenMode() {
        return this.getLightbarSirenModeObject().get();
    }

    default public void setLightbarSirenMode(int mode) {
        this.getLightbarSirenModeObject().set(mode);
    }

    public float getMaxWheelSteering();

    public float getMinWheelSkid();

    public boolean isAnyTireMissing();
}

