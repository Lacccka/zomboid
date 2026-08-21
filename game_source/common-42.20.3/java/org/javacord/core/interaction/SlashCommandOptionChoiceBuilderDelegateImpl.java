/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import java.util.HashMap;
import java.util.Map;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.internal.SlashCommandOptionChoiceBuilderDelegate;
import org.javacord.core.interaction.SlashCommandOptionChoiceImpl;

public class SlashCommandOptionChoiceBuilderDelegateImpl
implements SlashCommandOptionChoiceBuilderDelegate {
    private String name;
    private Map<DiscordLocale, String> nameLocalizations = new HashMap<DiscordLocale, String>();
    private String stringValue;
    private Long longValue;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addNameLocalization(DiscordLocale locale, String localization) {
        this.nameLocalizations.put(locale, localization);
    }

    @Override
    public void setValue(String value) {
        this.stringValue = value;
        this.longValue = null;
    }

    @Override
    public void setValue(long value) {
        this.stringValue = null;
        this.longValue = value;
    }

    @Override
    public SlashCommandOptionChoice build() {
        return new SlashCommandOptionChoiceImpl(this.name, this.nameLocalizations, this.stringValue, this.longValue);
    }
}

