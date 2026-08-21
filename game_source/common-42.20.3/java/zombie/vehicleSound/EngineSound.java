/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleSound;

import zombie.iso.IsoObject;
import zombie.scripting.objects.SoundKey;
import zombie.vehicleSound.VehicleSound;
import zombie.vehicleSound.VehicleSoundOwner;
import zombie.vehicleSound.VehicleSounds;

final class EngineSound
extends VehicleSound {
    private long instance;

    EngineSound(VehicleSoundOwner owner) {
        super(owner);
    }

    @Override
    public void update() {
        if (this.getOwner().isEngineSounding()) {
            if (!this.getEmitter().isPlaying(this.instance)) {
                this.instance = this.getEmitter().playSoundImpl(this.getEngineSound(), (IsoObject)null);
            }
            this.getEmitter().setVolume(this.instance, VehicleSounds.SOUND_VOLUME);
        } else if (this.instance != 0L) {
            if (this.getEmitter().hasSustainPoints(this.instance)) {
                this.getEmitter().triggerCue(this.instance);
            } else {
                this.getEmitter().stopSound(this.instance);
            }
            this.instance = 0L;
        }
    }

    @Override
    public void remove() {
        if (this.instance != 0L) {
            this.getEmitter().stopSound(this.instance);
            this.instance = 0L;
        }
    }

    private String getEngineSound() {
        if (this.getOwner().getScript() != null && this.getOwner().getScript().getSounds().engine != null) {
            return this.getOwner().getScript().getSounds().engine;
        }
        return SoundKey.VEHICLE_ENGINE_DEFAULT.getSoundName();
    }
}

