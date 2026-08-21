/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import zombie.characters.IsoGameCharacter;
import zombie.iso.objects.IsoDeadBody;

public interface CharacterDiedListener {
    public void onDied(IsoGameCharacter var1, IsoDeadBody var2);
}

