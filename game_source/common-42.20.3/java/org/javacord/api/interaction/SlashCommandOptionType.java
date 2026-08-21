/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

public enum SlashCommandOptionType {
    SUB_COMMAND(1),
    SUB_COMMAND_GROUP(2),
    STRING(3),
    LONG(4),
    BOOLEAN(5),
    USER(6),
    CHANNEL(7),
    ROLE(8),
    MENTIONABLE(9),
    DECIMAL(10),
    ATTACHMENT(11),
    UNKNOWN(-1);

    private final int value;

    private SlashCommandOptionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static SlashCommandOptionType fromValue(int value) {
        for (SlashCommandOptionType type : SlashCommandOptionType.values()) {
            if (type.getValue() != value) continue;
            return type;
        }
        return UNKNOWN;
    }
}

