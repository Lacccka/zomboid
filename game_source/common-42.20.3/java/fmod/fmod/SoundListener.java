/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import fmod.fmod.BaseSoundListener;
import fmod.javafmod;
import zombie.SoundManager;
import zombie.characters.IsoPlayer;
import zombie.iso.Vector3;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.scripting.objects.CharacterTrait;

public class SoundListener
extends BaseSoundListener {
    public float lx;
    public float ly;
    public float lz;
    private static final Vector3 vec = new Vector3();

    public SoundListener(int index) {
        super(index);
    }

    @Override
    public void tick() {
        if (GameServer.server) {
            return;
        }
        int listenerIndex = 0;
        for (int i = 0; i < IsoPlayer.numPlayers && i != this.index; ++i) {
            if (IsoPlayer.players[i] == null) continue;
            ++listenerIndex;
        }
        SoundListener.vec.x = -1.0f;
        SoundListener.vec.y = -1.0f;
        vec.normalize();
        if (IsoPlayer.players[this.index] != null && IsoPlayer.players[this.index].hasTrait(CharacterTrait.DEAF)) {
            this.x = -1000.0f;
            this.y = -1000.0f;
            this.z = 0.0f;
        }
        this.lx = this.x;
        this.ly = this.y;
        this.lz = this.z;
        if (!GameClient.client || SoundManager.instance.getSoundVolume() > 0.0f) {
            javafmod.FMOD_Studio_Listener3D(listenerIndex, this.x, this.y, this.z * 3.0f, this.x - this.lx, this.y - this.ly, this.z - this.lz, SoundListener.vec.x, SoundListener.vec.y, SoundListener.vec.z, 0.0f, 0.0f, 1.0f);
        }
        this.lx = this.x;
        this.ly = this.y;
        this.lz = this.z;
    }
}

