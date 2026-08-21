/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.core.interaction.SlashCommandOptionChoiceImpl;

public class SlashCommandOptionImpl
implements SlashCommandOption {
    private final SlashCommandOptionType type;
    private final String name;
    private final Map<DiscordLocale, String> nameLocalizations;
    private final String description;
    private final Map<DiscordLocale, String> descriptionLocalizations;
    private final boolean required;
    private final List<SlashCommandOptionChoice> choices;
    private final List<SlashCommandOption> options;
    private final Set<ChannelType> channelTypes;
    private final Long longMinValue;
    private final Long longMaxValue;
    private final Double decimalMinValue;
    private final Double decimalMaxValue;
    private final Long maxLength;
    private final Long minLength;
    private final boolean autocomplete;

    public SlashCommandOptionImpl(JsonNode data) {
        this.type = SlashCommandOptionType.fromValue(data.get("type").intValue());
        this.name = data.get("name").asText();
        this.nameLocalizations = new HashMap<DiscordLocale, String>();
        data.path("name_localizations").fields().forEachRemaining(e -> this.nameLocalizations.put(DiscordLocale.fromLocaleCode((String)e.getKey()), ((JsonNode)e.getValue()).asText()));
        this.description = data.get("description").asText();
        this.descriptionLocalizations = new HashMap<DiscordLocale, String>();
        data.path("description_localizations").fields().forEachRemaining(e -> this.descriptionLocalizations.put(DiscordLocale.fromLocaleCode((String)e.getKey()), ((JsonNode)e.getValue()).asText()));
        this.required = data.has("required") && data.get("required").asBoolean(false);
        this.autocomplete = data.has("autocomplete") && data.get("autocomplete").asBoolean();
        this.choices = new ArrayList<SlashCommandOptionChoice>();
        if (data.has("choices")) {
            for (JsonNode choiceJson : data.get("choices")) {
                this.choices.add(new SlashCommandOptionChoiceImpl(choiceJson));
            }
        }
        this.options = new ArrayList<SlashCommandOption>();
        if (data.has("options")) {
            for (JsonNode optionJson : data.get("options")) {
                this.options.add(new SlashCommandOptionImpl(optionJson));
            }
        }
        this.channelTypes = new HashSet<ChannelType>();
        if (data.has("channel_types")) {
            for (JsonNode channelTypeJson : data.get("channel_types")) {
                this.channelTypes.add(ChannelType.fromId(channelTypeJson.intValue()));
            }
        }
        if (this.type == SlashCommandOptionType.LONG) {
            this.longMinValue = data.has("min_value") ? Long.valueOf(data.get("min_value").asLong()) : null;
            this.longMaxValue = data.has("max_value") ? Long.valueOf(data.get("max_value").asLong()) : null;
            this.decimalMinValue = null;
            this.decimalMaxValue = null;
            this.minLength = null;
            this.maxLength = null;
        } else if (this.type == SlashCommandOptionType.DECIMAL) {
            this.decimalMinValue = data.has("min_value") ? Double.valueOf(data.get("min_value").asDouble()) : null;
            this.decimalMaxValue = data.has("max_value") ? Double.valueOf(data.get("max_value").asDouble()) : null;
            this.longMinValue = null;
            this.longMaxValue = null;
            this.minLength = null;
            this.maxLength = null;
        } else if (this.type == SlashCommandOptionType.STRING) {
            this.minLength = data.has("min_length") ? Long.valueOf(data.get("min_length").asLong()) : null;
            this.maxLength = data.has("max_length") ? Long.valueOf(data.get("max_length").asLong()) : null;
            this.longMinValue = null;
            this.longMaxValue = null;
            this.decimalMinValue = null;
            this.decimalMaxValue = null;
        } else {
            this.minLength = null;
            this.maxLength = null;
            this.longMinValue = null;
            this.longMaxValue = null;
            this.decimalMinValue = null;
            this.decimalMaxValue = null;
        }
    }

    public SlashCommandOptionImpl(SlashCommandOptionType type, String name, Map<DiscordLocale, String> nameLocalizations, String description, Map<DiscordLocale, String> descriptionLocalizations, boolean required, boolean autocomplete, List<SlashCommandOptionChoice> choices, List<SlashCommandOption> options, Set<ChannelType> channelTypes, Long longMinValue, Long longMaxValue, Double decimalMinValue, Double decimalMaxValue, Long minLength, Long maxLength) {
        this.type = type;
        this.name = name;
        this.nameLocalizations = nameLocalizations;
        this.description = description;
        this.descriptionLocalizations = descriptionLocalizations;
        this.required = required;
        this.autocomplete = autocomplete;
        this.choices = choices;
        this.options = options;
        this.channelTypes = channelTypes;
        this.longMinValue = longMinValue;
        this.longMaxValue = longMaxValue;
        this.decimalMinValue = decimalMinValue;
        this.decimalMaxValue = decimalMaxValue;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public SlashCommandOptionType getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<DiscordLocale, String> getNameLocalizations() {
        return Collections.unmodifiableMap(this.nameLocalizations);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Map<DiscordLocale, String> getDescriptionLocalizations() {
        return Collections.unmodifiableMap(this.descriptionLocalizations);
    }

    @Override
    public boolean isRequired() {
        return this.required;
    }

    @Override
    public boolean isAutocompletable() {
        return this.autocomplete;
    }

    @Override
    public List<SlashCommandOptionChoice> getChoices() {
        return Collections.unmodifiableList(this.choices);
    }

    @Override
    public List<SlashCommandOption> getOptions() {
        return Collections.unmodifiableList(this.options);
    }

    @Override
    public Set<ChannelType> getChannelTypes() {
        return Collections.unmodifiableSet(this.channelTypes);
    }

    @Override
    public Optional<Long> getLongMinValue() {
        return Optional.ofNullable(this.longMinValue);
    }

    @Override
    public Optional<Long> getLongMaxValue() {
        return Optional.ofNullable(this.longMaxValue);
    }

    @Override
    public Optional<Double> getDecimalMinValue() {
        return Optional.ofNullable(this.decimalMinValue);
    }

    @Override
    public Optional<Double> getDecimalMaxValue() {
        return Optional.ofNullable(this.decimalMaxValue);
    }

    @Override
    public Optional<Long> getMinLength() {
        return Optional.ofNullable(this.minLength);
    }

    @Override
    public Optional<Long> getMaxLength() {
        return Optional.ofNullable(this.maxLength);
    }

    public JsonNode toJsonNode() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", this.type.getValue());
        node.put("name", this.name);
        node.put("description", this.description);
        node.put("required", this.required);
        node.put("autocomplete", this.autocomplete);
        if (!this.choices.isEmpty()) {
            ArrayNode jsonChoices = node.putArray("choices");
            this.choices.forEach(choice -> jsonChoices.add(((SlashCommandOptionChoiceImpl)choice).toJsonNode()));
        }
        if (!this.options.isEmpty()) {
            ArrayNode jsonOptions = node.putArray("options");
            this.options.forEach(option -> jsonOptions.add(((SlashCommandOptionImpl)option).toJsonNode()));
        }
        if (!this.channelTypes.isEmpty()) {
            ArrayNode jsonChannelTypes = node.putArray("channel_types");
            this.channelTypes.forEach(channelType -> jsonChannelTypes.add(channelType.getId()));
        }
        if (this.type == SlashCommandOptionType.LONG) {
            if (this.longMinValue != null) {
                node.put("min_value", this.longMinValue);
            }
            if (this.longMaxValue != null) {
                node.put("max_value", this.longMaxValue);
            }
        } else if (this.type == SlashCommandOptionType.DECIMAL) {
            if (this.decimalMinValue != null) {
                node.put("min_value", this.decimalMinValue);
            }
            if (this.decimalMaxValue != null) {
                node.put("max_value", this.decimalMaxValue);
            }
        } else if (this.type == SlashCommandOptionType.STRING) {
            if (this.minLength != null) {
                node.put("min_length", this.minLength);
            }
            if (this.maxLength != null) {
                node.put("max_length", this.maxLength);
            }
        }
        if (!this.nameLocalizations.isEmpty()) {
            ObjectNode nameLocalizationsJsonObject = node.putObject("name_localizations");
            this.nameLocalizations.forEach((locale, localization) -> nameLocalizationsJsonObject.put(locale.getLocaleCode(), (String)localization));
        }
        if (!this.descriptionLocalizations.isEmpty()) {
            ObjectNode descriptionLocalizationsJsonObject = node.putObject("description_localizations");
            this.descriptionLocalizations.forEach((locale, localization) -> descriptionLocalizationsJsonObject.put(locale.getLocaleCode(), (String)localization));
        }
        return node;
    }
}

