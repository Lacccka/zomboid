/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicles;

import zombie.audio.BaseSoundEmitter;
import zombie.characters.IsoGameCharacter;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.scripting.objects.SoundKey;

public final class VehicleHitCharacterSounds {
    private static final int NUM_SOUNDS = 5;
    private static final long INTERVAL = 500L;
    private final Slot[] slots = new Slot[5];

    public VehicleHitCharacterSounds() {
        for (int i = 0; i < 5; ++i) {
            this.slots[i] = new Slot();
        }
    }

    public void possiblyPlay(IsoGameCharacter character, float x, float y, float z, int hitLocation, float absSpeedKmHour) {
        long millis = System.currentTimeMillis();
        Slot slot = this.getSlotForCharacter(character);
        if (slot == null) {
            slot = this.getOldestSlot();
        }
        if (slot == null) {
            return;
        }
        if (slot.time > millis - 500L) {
            return;
        }
        BaseSoundEmitter emitter = slot.checkEmitter(x, y, z);
        slot.character = character;
        emitter.stopAll();
        long soundRef = emitter.playSoundImpl(SoundKey.VEHICLE_HIT_CHARACTER.getSoundName(), (IsoObject)null);
        emitter.setParameterValueByName(soundRef, "VehicleHitLocation", hitLocation);
        emitter.setParameterValueByName(soundRef, "VehicleSpeed", absSpeedKmHour);
        slot.time = millis;
    }

    private Slot getSlotForCharacter(IsoGameCharacter character) {
        for (int i = 0; i < 5; ++i) {
            Slot slot = this.slots[i];
            if (slot.character != character) continue;
            return slot;
        }
        return null;
    }

    private Slot getOldestSlot() {
        Slot oldestSlot = null;
        for (int i = 0; i < 5; ++i) {
            Slot slot = this.slots[i];
            if (this.slots[i].emitter == null || this.slots[i].emitter.isEmpty()) {
                return slot;
            }
            if (oldestSlot != null && slot.time >= oldestSlot.time) continue;
            oldestSlot = slot;
        }
        return oldestSlot;
    }

    public void update() {
        for (int i = 0; i < this.slots.length; ++i) {
            Slot slot = this.slots[i];
            if (slot.emitter == null) continue;
            slot.emitter.tick();
        }
    }

    public void release() {
        for (int i = 0; i < this.slots.length; ++i) {
            Slot slot = this.slots[i];
            slot.release();
        }
    }

    private static final class Slot {
        IsoGameCharacter character;
        BaseSoundEmitter emitter;
        long time;

        private Slot() {
        }

        BaseSoundEmitter checkEmitter(float x, float y, float z) {
            if (this.emitter == null) {
                this.emitter = IsoWorld.instance.getFreeEmitter(x, y, z);
                IsoWorld.instance.takeOwnershipOfEmitter(this.emitter);
            }
            this.emitter.setPos(x, y, z);
            return this.emitter;
        }

        void release() {
            if (this.emitter != null) {
                this.emitter.stopAll();
                IsoWorld.instance.returnOwnershipOfEmitter(this.emitter);
                this.emitter = null;
                this.time = 0L;
            }
        }
    }
}

