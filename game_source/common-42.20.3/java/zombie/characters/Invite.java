/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import java.util.HashSet;
import java.util.Set;

public class Invite {
    private final Set<String> invites = new HashSet<String>();

    public boolean hasInvite(String player) {
        return this.invites.contains(player);
    }

    public void removeInvite(String player) {
        this.invites.remove(player);
    }

    public void addInvite(String invited) {
        this.invites.add(invited);
    }
}

