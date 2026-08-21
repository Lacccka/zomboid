/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.team;

public enum TeamMembershipState {
    INVITED(1),
    ACCEPTED(2),
    UNKNOWN(-1);

    private final int id;

    private TeamMembershipState(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static TeamMembershipState fromId(int id) {
        for (TeamMembershipState membershipState : TeamMembershipState.values()) {
            if (membershipState.getId() != id) continue;
            return membershipState;
        }
        return UNKNOWN;
    }
}

