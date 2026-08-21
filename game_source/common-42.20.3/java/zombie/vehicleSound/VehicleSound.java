/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound;

import zombie.audio.BaseSoundEmitter;
import zombie.vehicleSound.VehicleSoundOwner;

abstract class VehicleSound {
    protected final VehicleSoundOwner owner;

    public VehicleSound(VehicleSoundOwner owner) {
        this.owner = owner;
    }

    public VehicleSoundOwner getOwner() {
        return this.owner;
    }

    public BaseSoundEmitter getEmitter() {
        return this.getOwner().getVehicleSoundEmitter();
    }

    public abstract void update();

    public abstract void remove();
}

