/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.internal;

import java.util.Collection;
import java.util.List;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;

public interface SlashCommandOptionBuilderDelegate {
    public void setType(SlashCommandOptionType var1);

    public void setName(String var1);

    public void addNameLocalization(DiscordLocale var1, String var2);

    public void setDescription(String var1);

    public void addDescriptionLocalization(DiscordLocale var1, String var2);

    public void setRequired(boolean var1);

    public void setAutocompletable(boolean var1);

    public void addChoice(SlashCommandOptionChoice var1);

    public void setChoices(List<SlashCommandOptionChoice> var1);

    public void addOption(SlashCommandOption var1);

    public void setOptions(List<SlashCommandOption> var1);

    public void addChannelType(ChannelType var1);

    public void setChannelTypes(Collection<ChannelType> var1);

    public void setLongMinValue(long var1);

    public void setLongMaxValue(long var1);

    public void setDecimalMinValue(double var1);

    public void setDecimalMaxValue(double var1);

    public void setMinLength(long var1);

    public void setMaxLength(long var1);

    public SlashCommandOption build();
}

