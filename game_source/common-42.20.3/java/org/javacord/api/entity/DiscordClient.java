/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

public enum DiscordClient {
    DESKTOP("desktop"),
    MOBILE("mobile"),
    WEB("web");

    private final String name;

    private DiscordClient(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

