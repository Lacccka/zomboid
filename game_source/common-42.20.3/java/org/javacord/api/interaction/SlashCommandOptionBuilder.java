/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Collection;
import java.util.List;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.interaction.internal.SlashCommandOptionBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class SlashCommandOptionBuilder {
    private final SlashCommandOptionBuilderDelegate delegate = DelegateFactory.createSlashCommandOptionBuilderDelegate();

    public SlashCommandOptionBuilder setType(SlashCommandOptionType type) {
        this.delegate.setType(type);
        return this;
    }

    public SlashCommandOptionBuilder setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public SlashCommandOptionBuilder addNameLocalization(DiscordLocale locale, String localization) {
        this.delegate.addNameLocalization(locale, localization);
        return this;
    }

    public SlashCommandOptionBuilder setDescription(String description) {
        this.delegate.setDescription(description);
        return this;
    }

    public SlashCommandOptionBuilder addDescriptionLocalization(DiscordLocale locale, String localization) {
        this.delegate.addDescriptionLocalization(locale, localization);
        return this;
    }

    public SlashCommandOptionBuilder setRequired(boolean required) {
        this.delegate.setRequired(required);
        return this;
    }

    public SlashCommandOptionBuilder setAutocompletable(boolean autocompletable) {
        this.delegate.setAutocompletable(autocompletable);
        return this;
    }

    public SlashCommandOptionBuilder addChoice(SlashCommandOptionChoice choice) {
        this.delegate.addChoice(choice);
        return this;
    }

    public SlashCommandOptionBuilder addChoice(String name, String value) {
        this.delegate.addChoice(new SlashCommandOptionChoiceBuilder().setName(name).setValue(value).build());
        return this;
    }

    public SlashCommandOptionBuilder addChoice(String name, int value) {
        this.delegate.addChoice(new SlashCommandOptionChoiceBuilder().setName(name).setValue(value).build());
        return this;
    }

    public SlashCommandOptionBuilder setChoices(List<SlashCommandOptionChoice> choices) {
        this.delegate.setChoices(choices);
        return this;
    }

    public SlashCommandOptionBuilder addOption(SlashCommandOption option) {
        this.delegate.addOption(option);
        return this;
    }

    public SlashCommandOptionBuilder setOptions(List<SlashCommandOption> options) {
        this.delegate.setOptions(options);
        return this;
    }

    public SlashCommandOptionBuilder addChannelType(ChannelType channelType) {
        this.delegate.addChannelType(channelType);
        return this;
    }

    public SlashCommandOptionBuilder setChannelTypes(Collection<ChannelType> channelTypes) {
        this.delegate.setChannelTypes(channelTypes);
        return this;
    }

    public SlashCommandOptionBuilder setLongMinValue(long longMinValue) {
        this.delegate.setLongMinValue(longMinValue);
        return this;
    }

    public SlashCommandOptionBuilder setLongMaxValue(long longMaxValue) {
        this.delegate.setLongMaxValue(longMaxValue);
        return this;
    }

    public SlashCommandOptionBuilder setDecimalMinValue(double decimalMinValue) {
        this.delegate.setDecimalMinValue(decimalMinValue);
        return this;
    }

    public SlashCommandOptionBuilder setDecimalMaxValue(double decimalMaxValue) {
        this.delegate.setDecimalMaxValue(decimalMaxValue);
        return this;
    }

    public SlashCommandOptionBuilder setMinLength(long minLength) {
        this.delegate.setMinLength(minLength);
        return this;
    }

    public SlashCommandOptionBuilder setMaxLength(long maxLength) {
        this.delegate.setMaxLength(maxLength);
        return this;
    }

    public SlashCommandOption build() {
        return this.delegate.build();
    }
}

