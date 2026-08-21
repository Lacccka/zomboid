/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.user;

public enum UserFlag {
    NONE(0, "None"),
    STAFF(1, "Discord Employee"),
    PARTNER(2, "Partnered Server Owner"),
    HYPESQUAD(4, "HypeSquad Events Coordinator"),
    BUG_HUNTER_LEVEL_1(8, "Bug Hunter Level 1"),
    HOUSE_BRAVERY(64, "House Bravery Member"),
    HOUSE_BRILLIANCE(128, "House Brilliance Member"),
    HOUSE_BALANCE(256, "House Balance Member"),
    PREMIUM_EARLY_SUPPORTER(512, "Early Nitro Supporter"),
    TEAM_PSEUDO_USER(1024, "User is a team"),
    BUG_HUNTER_LEVEL_2(16384, "Bug Hunter Level 2"),
    VERIFIED_BOT(65536, "Verified Bot"),
    VERIFIED_DEVELOPER(131072, "Early Verified Bot Developer"),
    CERTIFIED_MODERATOR(262144, "Discord Certified Moderator"),
    BOT_HTTP_INTERACTIONS(524288, "Bot uses only HTTP interactions and is shown in the online member list");

    private final int flag;
    private final String description;

    private UserFlag(int flag, String description) {
        this.flag = flag;
        this.description = description;
    }

    public int asInt() {
        return this.flag;
    }

    public String getDescription() {
        return this.description;
    }
}

