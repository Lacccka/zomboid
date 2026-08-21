/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import fmod.fmod.FMODFootstep;
import fmod.fmod.FMODVoice;
import fmod.javafmod;
import java.security.InvalidParameterException;
import java.util.HashMap;
import zombie.UsedFromLua;
import zombie.audio.BaseSoundBank;
import zombie.core.Core;

@UsedFromLua
public class FMODSoundBank
extends BaseSoundBank {
    public HashMap<String, FMODVoice> voiceMap = new HashMap();
    public HashMap<String, FMODFootstep> footstepMap = new HashMap();

    private void check(String alias) {
        if (Core.debug && javafmod.FMOD_Studio_System_GetEvent("event:/" + alias) < 0L) {
            System.out.println("MISSING in .bank " + alias);
        }
    }

    @Override
    public void addVoice(String alias, String sound, float priority) {
        FMODVoice e = new FMODVoice(sound, priority);
        this.voiceMap.put(alias, e);
    }

    @Override
    public void addFootstep(String alias, String grass, String wood, String concrete, String upstairs) {
        FMODFootstep e = new FMODFootstep(grass, wood, concrete, upstairs);
        this.footstepMap.put(alias, e);
    }

    @Override
    public FMODVoice getVoice(String alias) {
        if (this.voiceMap.containsKey(alias)) {
            return this.voiceMap.get(alias);
        }
        return null;
    }

    @Override
    public FMODFootstep getFootstep(String alias) {
        if (this.footstepMap.containsKey(alias)) {
            return this.footstepMap.get(alias);
        }
        throw new InvalidParameterException("Footstep not loaded: " + alias);
    }
}

