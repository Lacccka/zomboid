/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.internal.SlashCommandOptionChoiceBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class SlashCommandOptionChoiceBuilder {
    private final SlashCommandOptionChoiceBuilderDelegate delegate = DelegateFactory.createSlashCommandOptionChoiceBuilderDelegate();

    public SlashCommandOptionChoiceBuilder setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public SlashCommandOptionChoiceBuilder addNameLocalization(DiscordLocale locale, String localization) {
        this.delegate.addNameLocalization(locale, localization);
        return this;
    }

    public SlashCommandOptionChoiceBuilder setValue(String value) {
        this.delegate.setValue(value);
        return this;
    }

    public SlashCommandOptionChoiceBuilder setValue(long value) {
        this.delegate.setValue(value);
        return this;
    }

    public SlashCommandOptionChoice build() {
        return this.delegate.build();
    }
}

