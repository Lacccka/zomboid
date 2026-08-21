/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Map;
import java.util.Optional;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;

public interface SlashCommandOptionChoice {
    public String getName();

    public Map<DiscordLocale, String> getNameLocalizations();

    public Optional<String> getStringValue();

    public Optional<Long> getLongValue();

    default public String getValueAsString() {
        return this.getStringValue().orElseGet(() -> this.getLongValue().map(String::valueOf).orElseThrow(() -> new AssertionError((Object)"slash command option choice value that's neither a string nor int")));
    }

    public static SlashCommandOptionChoice create(String name, String value) {
        return new SlashCommandOptionChoiceBuilder().setName(name).setValue(value).build();
    }

    public static SlashCommandOptionChoice create(String name, long value) {
        return new SlashCommandOptionChoiceBuilder().setName(name).setValue(value).build();
    }
}

