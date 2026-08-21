/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicles;

import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoGridSquare;
import zombie.scripting.objects.VehicleScript;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.IVehicleEngineListener;
import zombie.vehicles.LightbarLightsMode;
import zombie.vehicles.LightbarSirenMode;
import zombie.vehicles.VehiclePart;
import zombie.vehicles.VehicleParts;

public interface VehiclePartOwner
extends IVehicleEngineListener {
    public float getX();

    public float getY();

    public float getZ();

    public int getXi();

    public int getYi();

    public int getZi();

    public IsoGridSquare getSquare();

    default public IsoGameCharacter getDriver() {
        return null;
    }

    default public IsoGameCharacter getDriverRegardlessOfTow() {
        return null;
    }

    public VehicleParts getParts();

    default public int getPartCount() {
        return this.getParts().size();
    }

    default public VehiclePart getPartByIndex(int index) {
        return this.getParts().getPartByIndex(index);
    }

    default public VehiclePart getPartByPartId(zombie.scripting.objects.VehiclePart id) {
        if (id == null) {
            return null;
        }
        return this.getPartById(id.toString());
    }

    default public VehiclePart getPartById(String id) {
        return this.getParts().getPartById(id);
    }

    default public int getPartIndex(String id) {
        return this.getParts().getPartIndex(id);
    }

    default public VehiclePart getTrunkDoorPart() {
        return this.getPartByPartId(zombie.scripting.objects.VehiclePart.TRUNK_DOOR);
    }

    default public VehiclePart getTrunkPart() {
        VehiclePart truckBedPart = this.getPartByPartId(zombie.scripting.objects.VehiclePart.TRUCK_BED);
        if (truckBedPart != null) {
            return truckBedPart;
        }
        return this.getPartByPartId(zombie.scripting.objects.VehiclePart.TRUCK_BED_OPEN);
    }

    default public VehiclePart getTrailerTrunkPart() {
        return this.getPartByPartId(zombie.scripting.objects.VehiclePart.TRAILER_TRUNK);
    }

    default public int getNumberOfPartsWithContainers() {
        return this.getParts().getNumberOfPartsWithContainers();
    }

    default public VehiclePart getHeater() {
        return this.getPartById("Heater");
    }

    public VehicleScript getScript();

    default public VehiclePart getBattery() {
        return this.getParts().getBattery();
    }

    default public VehiclePart getEngine() {
        return this.getParts().getEngine();
    }

    default public VehiclePart getGasTank() {
        return this.getPartByPartId(zombie.scripting.objects.VehiclePart.GAS_TANK);
    }

    default public float getGasRemaining() {
        VehiclePart gasTank = this.getGasTank();
        return gasTank == null ? 0.0f : gasTank.getContainerContentAmount();
    }

    public LightbarLightsMode getLightbarLightsModeObject();

    default public int getLightbarLightsMode() {
        return this.getLightbarLightsModeObject().get();
    }

    default public void setLightbarLightsMode(int mode) {
        this.getLightbarLightsModeObject().set(mode);
    }

    public LightbarSirenMode getLightbarSirenModeObject();

    default public void setLightbarSirenMode(int mode) {
        this.getLightbarSirenModeObject().set(mode);
    }

    default public float getBatteryCharge() {
        return this.getParts().getBatteryCharge();
    }

    public boolean getHeadlightsOn();

    default public int windowsOpen() {
        int result = 0;
        for (int i = 0; i < this.getPartCount(); ++i) {
            VehiclePart part = this.getPartByIndex(i);
            if (part.window == null || !part.window.open) continue;
            ++result;
        }
        return result;
    }

    public void setEngineFeature(int var1, int var2, int var3);

    public boolean isEngineWorking();

    public float getBrakeSpeedBetweenUpdate();

    public BaseVehicle.ModelInfo setModelVisible(VehiclePart var1, VehicleScript.Model var2, boolean var3);

    public void updateDamageOverlayLater();

    public void updateTotalMass();

    public void updateBulletStats();

    public void updatePartStats();

    public void transmitEngine();

    public void transmitPartCondition(VehiclePart var1);

    public void transmitPartDoor(VehiclePart var1);

    public void transmitPartItem(VehiclePart var1);

    public void transmitPartLight(VehiclePart var1);

    public void transmitPartModData(VehiclePart var1);

    public void transmitPartUsedDelta(VehiclePart var1);

    public void transmitPartWindow(VehiclePart var1);
}

