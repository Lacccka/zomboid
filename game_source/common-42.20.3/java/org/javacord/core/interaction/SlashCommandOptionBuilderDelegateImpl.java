/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.internal.SlashCommandOptionBuilderDelegate;
import org.javacord.core.interaction.SlashCommandOptionImpl;

public class SlashCommandOptionBuilderDelegateImpl
implements SlashCommandOptionBuilderDelegate {
    private SlashCommandOptionType type;
    private String name;
    private Map<DiscordLocale, String> nameLocalizations = new HashMap<DiscordLocale, String>();
    private String description;
    private Map<DiscordLocale, String> descriptionLocalizations = new HashMap<DiscordLocale, String>();
    private boolean required = false;
    private boolean autocompletable = false;
    private List<SlashCommandOptionChoice> choices = new ArrayList<SlashCommandOptionChoice>();
    private List<SlashCommandOption> options = new ArrayList<SlashCommandOption>();
    private Set<ChannelType> channelTypes = new HashSet<ChannelType>();
    private Long longMinValue;
    private Long longMaxValue;
    private Double decimalMinValue;
    private Double decimalMaxValue;
    private Long minLength;
    private Long maxLength;

    @Override
    public void setType(SlashCommandOptionType type) {
        this.type = type;
    }

    @Override
    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    @Override
    public void addNameLocalization(DiscordLocale locale, String localization) {
        this.nameLocalizations.put(locale, localization);
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void addDescriptionLocalization(DiscordLocale locale, String localization) {
        this.descriptionLocalizations.put(locale, localization);
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public void setAutocompletable(boolean autocompletable) {
        this.autocompletable = autocompletable;
    }

    @Override
    public void addChoice(SlashCommandOptionChoice choice) {
        this.choices.add(choice);
    }

    @Override
    public void setChoices(List<SlashCommandOptionChoice> choices) {
        if (choices == null) {
            this.choices.clear();
        } else {
            this.choices = new ArrayList<SlashCommandOptionChoice>(choices);
        }
    }

    @Override
    public void addOption(SlashCommandOption option) {
        this.options.add(option);
    }

    @Override
    public void setOptions(List<SlashCommandOption> options) {
        if (options == null) {
            this.options.clear();
        } else {
            this.options = new ArrayList<SlashCommandOption>(options);
        }
    }

    @Override
    public void addChannelType(ChannelType channelType) {
        this.channelTypes.add(channelType);
    }

    @Override
    public void setChannelTypes(Collection<ChannelType> channelTypes) {
        if (channelTypes == null) {
            this.channelTypes.clear();
        } else {
            this.channelTypes = new HashSet<ChannelType>(channelTypes);
        }
    }

    @Override
    public void setLongMinValue(long longMinValue) {
        this.longMinValue = longMinValue;
    }

    @Override
    public void setLongMaxValue(long longMaxValue) {
        this.longMaxValue = longMaxValue;
    }

    @Override
    public void setDecimalMinValue(double decimalMinValue) {
        this.decimalMinValue = decimalMinValue;
    }

    @Override
    public void setDecimalMaxValue(double decimalMaxValue) {
        this.decimalMaxValue = decimalMaxValue;
    }

    @Override
    public void setMinLength(long minLength) {
        this.minLength = minLength;
    }

    @Override
    public void setMaxLength(long maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public SlashCommandOption build() {
        return new SlashCommandOptionImpl(this.type, this.name, this.nameLocalizations, this.description, this.descriptionLocalizations, this.required, this.autocompletable, this.choices, this.options, this.channelTypes, this.longMinValue, this.longMaxValue, this.decimalMinValue, this.decimalMaxValue, this.minLength, this.maxLength);
    }
}

