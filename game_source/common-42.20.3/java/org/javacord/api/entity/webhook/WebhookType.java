/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.webhook;

public enum WebhookType {
    INCOMING(1),
    CHANNEL_FOLLOWER(2),
    UNKNOWN(-1);

    private final int value;

    private WebhookType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static WebhookType fromValue(int value) {
        for (WebhookType type : WebhookType.values()) {
            if (type.getValue() != value) continue;
            return type;
        }
        return UNKNOWN;
    }
}

