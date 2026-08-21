/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionChoiceBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

public interface SlashCommandOption {
    public SlashCommandOptionType getType();

    public String getName();

    public Map<DiscordLocale, String> getNameLocalizations();

    public String getDescription();

    public Map<DiscordLocale, String> getDescriptionLocalizations();

    public boolean isRequired();

    public boolean isAutocompletable();

    default public boolean isSubcommandOrGroup() {
        return this.getType() == SlashCommandOptionType.SUB_COMMAND || this.getType() == SlashCommandOptionType.SUB_COMMAND_GROUP;
    }

    public List<SlashCommandOptionChoice> getChoices();

    public List<SlashCommandOption> getOptions();

    public Set<ChannelType> getChannelTypes();

    public Optional<Long> getLongMinValue();

    public Optional<Long> getLongMaxValue();

    public Optional<Double> getDecimalMinValue();

    public Optional<Double> getDecimalMaxValue();

    public Optional<Long> getMinLength();

    public Optional<Long> getMaxLength();

    public static SlashCommandOption create(SlashCommandOptionType type, String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(type).setName(name).setDescription(description).setRequired(required).build();
    }

    public static SlashCommandOption create(SlashCommandOptionType type, String name, String description) {
        return new SlashCommandOptionBuilder().setType(type).setName(name).setDescription(description).build();
    }

    public static SlashCommandOption createWithOptions(SlashCommandOptionType type, String name, String description, SlashCommandOptionBuilder ... options) {
        return SlashCommandOption.createWithOptions(type, name, description, Arrays.stream(options).map(SlashCommandOptionBuilder::build).collect(Collectors.toList()));
    }

    public static SlashCommandOption createWithOptions(SlashCommandOptionType type, String name, String description, List<SlashCommandOption> options) {
        return new SlashCommandOptionBuilder().setType(type).setName(name).setDescription(description).setOptions(options).build();
    }

    public static SlashCommandOption createSubcommandGroup(String name, String description, List<SlashCommandOption> options) {
        return SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, name, description, options);
    }

    public static SlashCommandOption createSubcommand(String name, String description) {
        return SlashCommandOption.createSubcommand(name, description, Collections.emptyList());
    }

    public static SlashCommandOption createSubcommand(String name, String description, List<SlashCommandOption> options) {
        return SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, name, description, options);
    }

    public static SlashCommandOption createWithChoices(SlashCommandOptionType type, String name, String description, boolean required, SlashCommandOptionChoiceBuilder ... choices) {
        return SlashCommandOption.createWithChoices(type, name, description, required, Arrays.stream(choices).map(SlashCommandOptionChoiceBuilder::build).collect(Collectors.toList()));
    }

    public static SlashCommandOption createWithChoices(SlashCommandOptionType type, String name, String description, boolean required, List<SlashCommandOptionChoice> choices) {
        return new SlashCommandOptionBuilder().setType(type).setName(name).setDescription(description).setRequired(required).setChoices(choices).build();
    }

    public static SlashCommandOption createChannelOption(String name, String description, boolean required, Collection<ChannelType> channelTypes) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.CHANNEL).setName(name).setDescription(description).setRequired(required).setChannelTypes(channelTypes).build();
    }

    public static SlashCommandOption createDecimalOption(String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.DECIMAL).setName(name).setDescription(description).setRequired(required).build();
    }

    public static SlashCommandOption createDecimalOption(String name, String description, boolean required, boolean autocomplete) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.DECIMAL).setName(name).setDescription(description).setRequired(required).setAutocompletable(autocomplete).build();
    }

    public static SlashCommandOption createDecimalOption(String name, String description, boolean required, double minValue, double maxValue) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.DECIMAL).setName(name).setDescription(description).setRequired(required).setDecimalMinValue(minValue).setDecimalMaxValue(maxValue).build();
    }

    public static SlashCommandOption createAttachmentOption(String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.ATTACHMENT).setName(name).setDescription(description).setRequired(required).build();
    }

    public static SlashCommandOption createLongOption(String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.LONG).setName(name).setDescription(description).setRequired(required).build();
    }

    public static SlashCommandOption createLongOption(String name, String description, boolean required, long minValue, long maxValue) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.LONG).setName(name).setDescription(description).setRequired(required).setLongMinValue(minValue).setLongMaxValue(maxValue).build();
    }

    public static SlashCommandOption createLongOption(String name, String description, boolean required, boolean autocomplete) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.LONG).setName(name).setDescription(description).setRequired(required).setAutocompletable(autocomplete).build();
    }

    public static SlashCommandOption createStringOption(String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.STRING).setName(name).setDescription(description).setRequired(required).build();
    }

    public static SlashCommandOption createStringOption(String name, String description, boolean required, long minLength, long maxLength) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.STRING).setName(name).setDescription(description).setRequired(required).setMinLength(minLength).setMaxLength(maxLength).build();
    }

    public static SlashCommandOption createStringOption(String name, String description, boolean required, boolean autocomplete) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.STRING).setName(name).setDescription(description).setRequired(required).setAutocompletable(autocomplete).build();
    }

    public static SlashCommandOption createRoleOption(String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.ROLE).setName(name).setDescription(description).setRequired(required).build();
    }

    public static SlashCommandOption createMentionableOption(String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.MENTIONABLE).setName(name).setDescription(description).setRequired(required).build();
    }

    public static SlashCommandOption createUserOption(String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.USER).setName(name).setDescription(description).setRequired(required).build();
    }

    public static SlashCommandOption createBooleanOption(String name, String description, boolean required) {
        return new SlashCommandOptionBuilder().setType(SlashCommandOptionType.BOOLEAN).setName(name).setDescription(description).setRequired(required).build();
    }
}

