/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.internal;

import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOptionChoice;

public interface SlashCommandOptionChoiceBuilderDelegate {
    public void setName(String var1);

    public void addNameLocalization(DiscordLocale var1, String var2);

    public void setValue(String var1);

    public void setValue(long var1);

    public SlashCommandOptionChoice build();
}

