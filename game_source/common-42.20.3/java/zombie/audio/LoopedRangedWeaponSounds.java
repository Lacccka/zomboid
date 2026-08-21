/*
 * Decompiled with CFR 0.152.
 */
package zombie.audio;

import fmod.fmod.FMODManager;
import fmod.fmod.FMOD_STUDIO_PARAMETER_DESCRIPTION;
import java.util.HashMap;
import java.util.Map;
import zombie.audio.BaseSoundEmitter;
import zombie.audio.FMODParameterUtils;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.util.StringUtils;

public final class LoopedRangedWeaponSounds {
    public static final LoopedRangedWeaponSounds INSTANCE = new LoopedRangedWeaponSounds();
    private final Map<Object, Sound> soundMap = new HashMap<Object, Sound>();
    private final Map<Short, Object> ownerMap = new HashMap<Short, Object>();

    private Sound getOrCreateSound(Object owner) {
        Sound sound = this.soundMap.get(owner);
        if (sound == null) {
            sound = new Sound();
            sound.owner = owner;
            this.soundMap.put(owner, sound);
        }
        return sound;
    }

    public void startAttackLoopSound(Object owner, String soundName) {
        Sound sound = this.getOrCreateSound(owner);
        sound.startAttackLoopSound(soundName);
    }

    public void stopAttackLoopSound(Object owner, boolean cancelPrevious) {
        Sound sound = this.soundMap.get(owner);
        if (sound == null) {
            return;
        }
        sound.stopAttackLoopSound(cancelPrevious);
    }

    public boolean isPlaying(Object owner) {
        Sound sound = this.soundMap.get(owner);
        if (sound == null) {
            return false;
        }
        return sound.isPlaying();
    }

    public boolean isPlaying(Object owner, String soundName) {
        Sound sound = this.soundMap.get(owner);
        if (sound == null) {
            return false;
        }
        return sound.isPlaying(soundName);
    }

    public void setPosition(Object owner, float x, float y, float z) {
        Sound sound = this.getOrCreateSound(owner);
        sound.setPosition(x, y, z);
    }

    public void setParameters(Object owner, float firearmInside, float firearmRoomSize) {
        Sound sound = this.getOrCreateSound(owner);
        sound.setParameters(firearmInside, firearmRoomSize);
    }

    public void update() {
        for (Sound sound : this.soundMap.values()) {
            sound.update();
        }
    }

    public Object getOwnerObjectForRemotePlayer(short playerId) {
        return this.ownerMap.computeIfAbsent(playerId, Short::valueOf);
    }

    private static boolean isOwnerRemotePlayer(Object owner) {
        return owner instanceof Short;
    }

    public void disconnectPlayer(short playerId) {
        Object owner = this.ownerMap.get(playerId);
        if (owner == null) {
            return;
        }
        Sound sound = this.soundMap.get(owner);
        if (sound == null) {
            return;
        }
        sound.stopAttackLoopSound(true);
    }

    private static final class Sound {
        Object owner;
        float x;
        float y;
        float z;
        final Parameter firearmInside = new Parameter("FirearmInside");
        final Parameter firearmRoomSize = new Parameter("FirearmRoomSize");
        final Parameter localPlayer = new Parameter("LocalPlayer");
        final Parameter occlusion = new Parameter("Occlusion");
        String attackLoopSoundName;
        long attackLoopInstance;
        long attackLoopInstancePrev;
        BaseSoundEmitter emitter;

        private Sound() {
        }

        BaseSoundEmitter getEmitter() {
            if (this.emitter == null) {
                this.emitter = IsoWorld.instance.getFreeEmitter();
                if (LoopedRangedWeaponSounds.isOwnerRemotePlayer(this.owner)) {
                    this.emitter.setPlayRemoteEvents(true);
                }
                IsoWorld.instance.takeOwnershipOfEmitter(this.emitter);
            }
            return this.emitter;
        }

        void setPosition(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        void setParameters(float firearmInside, float firearmRoomSize) {
            this.firearmInside.value = firearmInside;
            this.firearmRoomSize.value = firearmRoomSize;
        }

        boolean isPlaying() {
            return this.emitter != null && this.emitter.isPlaying(this.attackLoopInstance);
        }

        boolean isPlaying(String soundName) {
            return StringUtils.equalsIgnoreCase(soundName, this.attackLoopSoundName) && this.isPlaying();
        }

        void startAttackLoopSound(String soundName) {
            this.stopAttackLoopSound(true);
            this.resetParameters();
            this.attackLoopSoundName = soundName;
            this.attackLoopInstance = this.getEmitter().playSoundImpl(soundName, (IsoObject)null);
        }

        void stopAttackLoopSound(boolean cancelPrevious) {
            this.attackLoopSoundName = null;
            if (cancelPrevious && this.attackLoopInstancePrev != 0L) {
                this.getEmitter().setParameterValueByName(this.attackLoopInstancePrev, "FirearmCancelShot", 1.0f);
                this.attackLoopInstancePrev = 0L;
            }
            if (this.attackLoopInstance != 0L) {
                this.getEmitter().stopSoundDelayRelease(this.attackLoopInstance);
                this.attackLoopInstancePrev = this.attackLoopInstance;
                this.attackLoopInstance = 0L;
            }
        }

        void updateParameters() {
            if (this.attackLoopInstance == 0L) {
                return;
            }
            IsoGridSquare square = IsoWorld.instance.currentCell.getGridSquare(this.x, this.y, this.z);
            IsoPlayer listener = FMODParameterUtils.getClosestListener(this.x, this.y, this.z);
            this.occlusion.value = listener == null || square == null ? 1.0f : (square.isCouldSee(listener.getIndex()) ? 0.0f : 1.0f);
            this.localPlayer.value = IsoPlayer.isLocalPlayer(this.owner) ? 1.0f : 0.0f;
            this.firearmInside.update(this.emitter, this.attackLoopInstance);
            this.firearmRoomSize.update(this.emitter, this.attackLoopInstance);
            this.localPlayer.update(this.emitter, this.attackLoopInstance);
            this.occlusion.update(this.emitter, this.attackLoopInstance);
        }

        void resetParameters() {
            this.firearmInside.valueSet = Float.NaN;
            this.firearmRoomSize.valueSet = Float.NaN;
            this.localPlayer.valueSet = Float.NaN;
            this.occlusion.valueSet = Float.NaN;
        }

        void update() {
            if (this.emitter == null) {
                return;
            }
            this.emitter.setPos(this.x, this.y, this.z);
            this.updateParameters();
            this.emitter.tick();
            if (this.emitter.isEmpty()) {
                this.resetParameters();
                this.emitter.setPlayRemoteEvents(false);
                IsoWorld.instance.returnOwnershipOfEmitter(this.emitter);
                this.emitter = null;
            }
        }
    }

    private static final class Parameter {
        final String name;
        final FMOD_STUDIO_PARAMETER_DESCRIPTION parameterDescription;
        float value;
        float valueSet = Float.NaN;

        Parameter(String name) {
            this.name = name;
            this.parameterDescription = FMODManager.instance.getParameterDescription(name);
        }

        void update(BaseSoundEmitter emitter, long instance) {
            if (Float.compare(this.value, this.valueSet) != 0) {
                this.valueSet = this.value;
                emitter.setParameterValue(instance, this.parameterDescription, this.value);
            }
        }
    }
}

