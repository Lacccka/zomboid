/*
 * Decompiled with CFR 0.152.
 */
package fmod.fmod;

import fmod.fmod.FMODManager;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.scripting.objects.CharacterTrait;

public class FMODFootstep {
    public String wood;
    public String concrete;
    public String grass;
    public String upstairs;
    public String woodCreak;

    public FMODFootstep(String grass, String wood, String concrete, String upstairs) {
        this.grass = grass;
        this.wood = wood;
        this.concrete = concrete;
        this.upstairs = upstairs;
        this.woodCreak = "HumanFootstepFloorCreaking";
    }

    public boolean isUpstairs(IsoGameCharacter character) {
        IsoGridSquare sq = IsoPlayer.getInstance().getCurrentSquare();
        return sq.getZ() < character.getCurrentSquare().getZ();
    }

    public String getSoundToPlay(IsoGameCharacter character) {
        IsoObject floor;
        if (FMODManager.instance.getNumListeners() == 1) {
            for (int i = 0; i < IsoPlayer.numPlayers; ++i) {
                IsoPlayer player = IsoPlayer.players[i];
                if (player == null || player == character || player.hasTrait(CharacterTrait.DEAF)) continue;
                if (player.getZi() >= character.getZi()) break;
                return this.upstairs;
            }
        }
        if ((floor = character.getCurrentSquare().getFloor()) != null && floor.getSprite() != null && floor.getSprite().getName() != null) {
            String floorName = floor.getSprite().getName();
            if (floorName.startsWith("blends_natural_01")) {
                return this.grass;
            }
            if (floorName.startsWith("floors_interior_tilesandwood_01_")) {
                int index = Integer.parseInt(floorName.replaceFirst("floors_interior_tilesandwood_01_", ""));
                if (index > 40 && index < 48) {
                    return this.wood;
                }
                return this.concrete;
            }
            if (floorName.startsWith("carpentry_02_")) {
                return this.wood;
            }
            if (floorName.startsWith("floors_interior_carpet_")) {
                return this.wood;
            }
            return this.concrete;
        }
        return this.concrete;
    }
}

