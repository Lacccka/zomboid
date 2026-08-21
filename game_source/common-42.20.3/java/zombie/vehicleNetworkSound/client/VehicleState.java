/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleNetworkSound.client;

import zombie.GameTime;
import zombie.audio.BaseSoundEmitter;
import zombie.audio.FMODParameterUtils;
import zombie.audio.parameters.ParameterVehicleRoadMaterial;
import zombie.core.math.PZMath;
import zombie.iso.IsoWorld;
import zombie.scripting.objects.SoundKey;
import zombie.scripting.objects.VehicleScript;
import zombie.vehicleNetworkSound.SharedVehicleState;
import zombie.vehicleSound.VehicleSoundOwner;
import zombie.vehicleSound.VehicleSounds;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.LightbarSirenMode;
import zombie.vehicles.VehicleManager;

final class VehicleState
extends SharedVehicleState
implements VehicleSoundOwner {
    private VehicleScript script;
    private VehicleSounds vehicleSounds;

    VehicleState(short id) {
        this.id = id;
        this.setVehicleSounds(new VehicleSounds());
    }

    void setScript(VehicleScript script) {
        this.script = script;
    }

    void setVehicleSounds(VehicleSounds vehicleSounds) {
        this.vehicleSounds = vehicleSounds;
        vehicleSounds.setOwner(this);
    }

    @Override
    public float getX() {
        return this.x;
    }

    @Override
    public float getY() {
        return this.y;
    }

    @Override
    public float getZ() {
        return this.z;
    }

    @Override
    public int getXi() {
        return PZMath.fastfloor(this.getX());
    }

    @Override
    public int getYi() {
        return PZMath.fastfloor(this.getY());
    }

    @Override
    public int getZi() {
        return PZMath.fastfloor(this.getZ());
    }

    @Override
    public boolean isListenerInRange(float range) {
        float closestListenerDistSq = FMODParameterUtils.getClosestListenerDistanceSquared(this.getX(), this.getY(), this.getZ());
        return closestListenerDistSq < PZMath.squared(range);
    }

    @Override
    public String getScriptName() {
        return this.scriptName;
    }

    @Override
    public VehicleScript getScript() {
        return this.script;
    }

    @Override
    public int getEngineCondition() {
        return this.engineCondition;
    }

    @Override
    public int getEngineQuality() {
        return this.engineQuality;
    }

    @Override
    public BaseVehicle.engineStateTypes getEngineState() {
        return this.engineState;
    }

    @Override
    public boolean isEngineRunning() {
        return this.getEngineState() == BaseVehicle.engineStateTypes.Running;
    }

    @Override
    public boolean isEngineSounding() {
        boolean isStartingWithCombinedSound;
        if (!this.isListenerInRange(200.0f)) {
            return false;
        }
        boolean bl = isStartingWithCombinedSound = this.getEngineState() == BaseVehicle.engineStateTypes.StartingSuccess && this.getEngineStartSound().equals(this.getEngineSound());
        if (isStartingWithCombinedSound) {
            return true;
        }
        return this.getEngineState() == BaseVehicle.engineStateTypes.Running;
    }

    @Override
    public double getEngineSpeed() {
        return this.engineSpeed;
    }

    @Override
    public int getTransmissionNumber() {
        return this.gear;
    }

    @Override
    public float getCurrentSpeedKmHour() {
        return this.currentSpeedKmHour;
    }

    @Override
    public float getMaxSpeed() {
        return this.getScript() == null ? 0.0f : this.getScript().maxSpeed;
    }

    @Override
    public boolean isAlarmActive() {
        return (this.vehicleStateFlags & 0x200) != 0;
    }

    @Override
    public boolean isAlarmSoundOn() {
        return this.isAlarmActive() && (this.vehicleStateFlags & 1) != 0;
    }

    @Override
    public boolean isAlarmSounding() {
        if (!this.isListenerInRange(500.0f)) {
            return false;
        }
        return this.isAlarmSoundOn();
    }

    @Override
    public boolean isBrakePedalPressed() {
        return (this.vehicleStateFlags & 0x40) != 0;
    }

    @Override
    public boolean isGasPedalPressed() {
        return (this.vehicleStateFlags & 0x80) != 0;
    }

    @Override
    public ParameterVehicleRoadMaterial.Material getRoadMaterial() {
        return this.roadMaterial;
    }

    @Override
    public String getChosenAlarmSound() {
        return this.chosenAlarmSound;
    }

    @Override
    public boolean isBackupBeeperSounding() {
        if (!this.isListenerInRange(150.0f)) {
            return false;
        }
        return (this.vehicleStateFlags & 2) != 0;
    }

    @Override
    public boolean isDoorAlarmSounding() {
        if (!this.isListenerInRange(50.0f)) {
            return false;
        }
        return (this.vehicleStateFlags & 8) != 0;
    }

    @Override
    public boolean isHornSounding() {
        if (!this.isListenerInRange(500.0f)) {
            return false;
        }
        return (this.vehicleStateFlags & 0x10) != 0;
    }

    @Override
    public BaseSoundEmitter getVehicleSoundEmitter() {
        return this.vehicleSounds.getVehicleSoundEmitter();
    }

    @Override
    public boolean isAnyListenerInside() {
        return false;
    }

    @Override
    public boolean isSirenActive() {
        return this.hasSiren() && this.getLightbarSirenModeObject().isEnable() && (this.vehicleStateFlags & 4) != 0;
    }

    @Override
    public boolean isSirenSounding() {
        if (!this.isListenerInRange(500.0f)) {
            return false;
        }
        return this.isSirenActive();
    }

    @Override
    public double getSirenStartTime() {
        return 0.0;
    }

    @Override
    public void setSirenStartTime(double worldAgeHours) {
    }

    @Override
    public LightbarSirenMode getLightbarSirenModeObject() {
        return this.lightbarSirenMode;
    }

    @Override
    public float getMaxWheelSteering() {
        return (float)this.steering / 100.0f;
    }

    @Override
    public float getMinWheelSkid() {
        return (float)this.minWheelSkid / 100.0f;
    }

    @Override
    public boolean isAnyTireMissing() {
        return (this.vehicleStateFlags & 0x100) != 0;
    }

    void update() {
        BaseVehicle baseVehicle = this.getVehicleInWorld();
        if (baseVehicle instanceof BaseVehicle) {
            BaseVehicle vehicle = baseVehicle;
            if (this.vehicleSounds.getOwner() != vehicle) {
                if (this.isAlarmActive() && !vehicle.isAlarmActive()) {
                    vehicle.getVehicleAlarmObject().setStartTime(GameTime.instance.getWorldAgeHours());
                }
                if (this.isAlarmSoundOn() && !vehicle.isAlarmSoundOn()) {
                    vehicle.onAlarmStart();
                }
            }
            vehicle.setVehicleSounds(this.vehicleSounds);
        } else {
            this.vehicleSounds.setOwner(this);
        }
        this.vehicleSounds.update();
    }

    void remove() {
        this.vehicleSounds.remove();
    }

    private BaseVehicle getVehicleInWorld() {
        if (IsoWorld.instance == null || IsoWorld.instance.currentCell == null) {
            return null;
        }
        BaseVehicle vehicle = VehicleManager.instance.getVehicleByID(this.id);
        if (!IsoWorld.instance.currentCell.getVehicles().contains(vehicle)) {
            return null;
        }
        return vehicle;
    }

    private String getEngineStartSound() {
        if (this.getScript() != null && this.getScript().getSounds().engineStart != null) {
            return this.getScript().getSounds().engineStart;
        }
        return "VehicleStarted";
    }

    private String getEngineSound() {
        if (this.getScript() != null && this.getScript().getSounds().engine != null) {
            return this.getScript().getSounds().engine;
        }
        return SoundKey.VEHICLE_ENGINE_DEFAULT.getSoundName();
    }
}

