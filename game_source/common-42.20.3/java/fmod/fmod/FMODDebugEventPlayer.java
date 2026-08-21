/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import fmod.fmod.FMODManager;
import fmod.fmod.FMOD_STUDIO_EVENT_DESCRIPTION;
import fmod.fmod.FMOD_STUDIO_PARAMETER_DESCRIPTION;
import fmod.fmod.FMOD_STUDIO_PLAYBACK_STATE;
import fmod.javafmod;
import fmod.javafmodJNI;
import java.util.ArrayList;
import java.util.List;
import zombie.UsedFromLua;
import zombie.audio.FMODGlobalParameter;
import zombie.characters.IsoPlayer;
import zombie.core.math.PZMath;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoWorld;
import zombie.popman.ObjectPool;

@UsedFromLua
public final class FMODDebugEventPlayer {
    private String eventPath;
    FMOD_STUDIO_EVENT_DESCRIPTION eventDescription;
    private long eventInstance;
    private long durationMs;
    private long startTimeMs;
    private boolean loop;
    private float volume = 1.0f;
    private int timelinePosition;
    private float setVolume = Float.NaN;
    private float setOcclusion = Float.NaN;
    private boolean followPlayer;
    private boolean triggeredCue;
    private long checkTimeMs;
    private float x;
    private float y;
    private float z;
    private final ArrayList<ParameterValue> parameterValues = new ArrayList();
    private final ObjectPool<ParameterValue> parameterValuePool = new ObjectPool<ParameterValue>(ParameterValue::new, "FMODDebugEventPlayer.parameterValuePool");

    public void play(String eventPath) {
        this.stop(false);
        IsoPlayer player = IsoPlayer.players[0];
        if (player == null) {
            throw new IllegalArgumentException("no IsoPlayer to get position from");
        }
        this.eventDescription = FMODManager.instance.getEventDescription(eventPath);
        if (this.eventDescription == null) {
            throw new IllegalArgumentException("unknown FMOD event \"%s\"".formatted(eventPath));
        }
        this.eventInstance = javafmod.FMOD_Studio_System_CreateEventInstance(this.eventDescription.address);
        if (this.eventInstance < 0L) {
            this.eventInstance = 0L;
            throw new IllegalArgumentException("CreateEventInstance() failed for FMOD event \"%s\"".formatted(eventPath));
        }
        javafmod.FMOD_Studio_EventInstance_SetVolume(this.eventInstance, 1.0f);
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        javafmod.FMOD_Studio_EventInstance3D(this.eventInstance, this.x, this.y, this.z);
        this.setVolume = this.volume;
        javafmod.FMOD_Studio_EventInstance_SetVolume(this.eventInstance, this.volume);
        this.setOcclusion = Float.NaN;
        for (int i = 0; i < this.parameterValues.size(); ++i) {
            ParameterValue parameterValue = this.parameterValues.get(i);
            parameterValue.setValue = Float.NaN;
        }
        this.updateParameterValues();
        javafmod.FMOD_Studio_StartEvent(this.eventInstance);
        if ((long)this.timelinePosition > 0L) {
            javafmodJNI.FMOD_Studio_EventInstance_SetTimelinePosition(this.eventInstance, this.timelinePosition);
        }
        this.eventPath = eventPath;
        this.startTimeMs = System.currentTimeMillis();
        this.checkTimeMs = 0L;
        this.triggeredCue = false;
    }

    public void stop() {
        this.stop(true);
    }

    public void stop(boolean bTriggerCue) {
        if (this.eventInstance == 0L) {
            return;
        }
        if (bTriggerCue && this.eventDescription.hasSustainPoints) {
            javafmodJNI.FMOD_Studio_EventInstance_KeyOff(this.eventInstance);
            this.triggeredCue = true;
            this.checkTimeMs = System.currentTimeMillis();
        } else {
            javafmod.FMOD_Studio_EventInstance_Stop(this.eventInstance, false);
            javafmod.FMOD_Studio_ReleaseEventInstance(this.eventInstance);
            this.eventInstance = 0L;
            this.triggeredCue = false;
            this.checkTimeMs = 0L;
        }
    }

    public void setDurationMillis(long ms) {
        this.durationMs = Math.max(ms, 0L);
    }

    public void setLoop(boolean bLoop) {
        this.loop = bLoop;
    }

    public void setFollowPlayer(boolean bFollowPlayer) {
        this.followPlayer = bFollowPlayer;
    }

    public void setVolume(float volume) {
        this.volume = PZMath.clamp(volume, 0.0f, 10.0f);
    }

    public void setTimelinePosition(int ms) {
        this.timelinePosition = ms;
        if (this.eventInstance != 0L) {
            javafmodJNI.FMOD_Studio_EventInstance_SetTimelinePosition(this.eventInstance, this.timelinePosition);
        }
    }

    private void updateOcclusion() {
        float occlusion = 1.0f;
        for (int playerIndex = 0; playerIndex < 4; ++playerIndex) {
            float value = this.calculateValueForPlayer(playerIndex);
            occlusion = PZMath.min(occlusion, value);
        }
        if (occlusion == this.setOcclusion) {
            return;
        }
        this.setOcclusion = occlusion;
        javafmod.FMOD_Studio_EventInstance_SetParameterByName(this.eventInstance, "Occlusion", occlusion);
    }

    private float calculateValueForPlayer(int playerIndex) {
        IsoPlayer player = IsoPlayer.players[playerIndex];
        if (player == null) {
            return 1.0f;
        }
        IsoGridSquare sqPlayer = player.getCurrentSquare();
        IsoGridSquare sqSound = IsoWorld.instance.getCell().getGridSquare(this.x, this.y, this.z);
        if (sqSound == null) {
            boolean bl = true;
        }
        float occlusion = 0.0f;
        if (sqPlayer != null && sqSound != null && !sqSound.isCouldSee(playerIndex)) {
            occlusion = 1.0f;
        }
        return occlusion;
    }

    public void update() {
        IsoPlayer player;
        if (this.eventInstance == 0L) {
            return;
        }
        int state = javafmod.FMOD_Studio_GetPlaybackState(this.eventInstance);
        if (state == FMOD_STUDIO_PLAYBACK_STATE.FMOD_STUDIO_PLAYBACK_STOPPING.index) {
            return;
        }
        long currentTimeMS = System.currentTimeMillis();
        if (state == FMOD_STUDIO_PLAYBACK_STATE.FMOD_STUDIO_PLAYBACK_STOPPED.index) {
            if (this.loop || this.durationMs != 0L && currentTimeMS < this.startTimeMs + this.durationMs) {
                javafmod.FMOD_Studio_StartEvent(this.eventInstance);
                return;
            }
            javafmod.FMOD_Studio_ReleaseEventInstance(this.eventInstance);
            this.eventInstance = 0L;
            this.checkTimeMs = 0L;
            this.triggeredCue = false;
            return;
        }
        if (!this.triggeredCue && this.durationMs != 0L && currentTimeMS >= this.startTimeMs + this.durationMs) {
            this.stop(true);
            return;
        }
        if (this.triggeredCue && currentTimeMS - this.checkTimeMs > 250L && state == FMOD_STUDIO_PLAYBACK_STATE.FMOD_STUDIO_PLAYBACK_SUSTAINING.index) {
            javafmodJNI.FMOD_Studio_EventInstance_KeyOff(this.eventInstance);
        }
        if (this.triggeredCue && this.eventDescription.length > 0L && currentTimeMS - this.checkTimeMs > 1500L) {
            long position = javafmodJNI.FMOD_Studio_GetTimelinePosition(this.eventInstance);
            if (position > this.eventDescription.length + 1000L) {
                javafmod.FMOD_Studio_EventInstance_Stop(this.eventInstance, false);
                this.eventInstance = 0L;
                this.triggeredCue = false;
                this.checkTimeMs = 0L;
                return;
            }
            this.checkTimeMs = currentTimeMS;
        }
        if (this.followPlayer && (player = IsoPlayer.players[0]) != null) {
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            javafmod.FMOD_Studio_EventInstance3D(this.eventInstance, this.x, this.y, this.z);
        }
        this.updateParameterValues();
        if (Float.compare(this.volume, this.setVolume) != 0) {
            this.setVolume = this.volume;
            javafmod.FMOD_Studio_EventInstance_SetVolume(this.eventInstance, this.volume);
        }
    }

    public boolean isPlaying() {
        if (this.eventInstance == 0L) {
            return false;
        }
        int state = javafmod.FMOD_Studio_GetPlaybackState(this.eventInstance);
        if (state == FMOD_STUDIO_PLAYBACK_STATE.FMOD_STUDIO_PLAYBACK_STOPPING.index) {
            return true;
        }
        return state != FMOD_STUDIO_PLAYBACK_STATE.FMOD_STUDIO_PLAYBACK_STOPPED.index;
    }

    public void initParameterValues(String eventPath) {
        ArrayList<ParameterValue> previousValues = new ArrayList<ParameterValue>(this.parameterValues);
        this.parameterValues.clear();
        FMOD_STUDIO_EVENT_DESCRIPTION eventDescription1 = FMODManager.instance.getEventDescription(eventPath);
        if (eventDescription1 == null) {
            return;
        }
        int n = eventDescription1.parameters.size();
        for (int i = 0; i < n; ++i) {
            ParameterValue parameterValue = this.parameterValuePool.alloc();
            parameterValue.parameterDescription = eventDescription1.parameters.get(i);
            parameterValue.value = Float.NaN;
            parameterValue.setValue = Float.NaN;
            this.setPreviousValue(parameterValue, previousValues);
            this.parameterValues.add(parameterValue);
        }
        this.parameterValuePool.releaseAll((List<ParameterValue>)previousValues);
    }

    private void setPreviousValue(ParameterValue parameterValue, ArrayList<ParameterValue> previousValues) {
        for (int i = 0; i < previousValues.size(); ++i) {
            ParameterValue parameterValue1 = previousValues.get(i);
            if (parameterValue1.parameterDescription != parameterValue.parameterDescription) continue;
            parameterValue.value = parameterValue1.value;
            return;
        }
    }

    public int getParameterCount(String eventPath) {
        FMOD_STUDIO_EVENT_DESCRIPTION eventDescription1 = FMODManager.instance.getEventDescription(eventPath);
        return eventDescription1 == null ? 0 : eventDescription1.parameters.size();
    }

    public String getParameterName(String eventPath, int index) {
        FMOD_STUDIO_EVENT_DESCRIPTION eventDescription1 = FMODManager.instance.getEventDescription(eventPath);
        return eventDescription1.parameters.get((int)index).name;
    }

    public void setParameterValue(int index, float value) {
        this.parameterValues.get((int)index).value = value;
    }

    public void clearParameterValue(int index) {
        this.parameterValues.get((int)index).value = Float.NaN;
    }

    public float getParameterValue(int index) {
        ParameterValue parameterValue = this.parameterValues.get(index);
        if (parameterValue.parameterDescription.isGlobal()) {
            FMODGlobalParameter globalParameter = FMODManager.instance.getGlobalParameter(parameterValue.parameterDescription.name);
            return globalParameter == null ? -666.0f : globalParameter.getCurrentValue();
        }
        return Float.isNaN(parameterValue.value) ? -666.0f : parameterValue.value;
    }

    public boolean isGlobalParameter(String eventPath, int index) {
        FMOD_STUDIO_EVENT_DESCRIPTION eventDescription1 = FMODManager.instance.getEventDescription(eventPath);
        return eventDescription1.parameters.get(index).isGlobal();
    }

    public float getGlobalParameterValue(String eventPath, int index) {
        FMODGlobalParameter globalParameter = FMODManager.instance.getGlobalParameter(this.getParameterName(eventPath, index));
        return globalParameter == null ? -666.0f : globalParameter.getCurrentValue();
    }

    private void updateParameterValues() {
        if (this.eventInstance == 0L) {
            return;
        }
        for (int i = 0; i < this.parameterValues.size(); ++i) {
            ParameterValue parameterValue = this.parameterValues.get(i);
            if (Float.compare(parameterValue.value, parameterValue.setValue) == 0) continue;
            parameterValue.setValue = parameterValue.value;
            if (Float.isNaN(parameterValue.value)) continue;
            javafmod.FMOD_Studio_EventInstance_SetParameterByID(this.eventInstance, parameterValue.parameterDescription.id, parameterValue.value);
        }
    }

    private static final class ParameterValue {
        FMOD_STUDIO_PARAMETER_DESCRIPTION parameterDescription;
        float value = Float.NaN;
        float setValue = Float.NaN;

        private ParameterValue() {
        }
    }
}

