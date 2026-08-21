/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.SlashCommandOptionChoice;

public class SlashCommandOptionChoiceImpl
implements SlashCommandOptionChoice {
    private final String name;
    private final Map<DiscordLocale, String> nameLocalizations;
    private final String stringValue;
    private final Long longValue;

    public SlashCommandOptionChoiceImpl(JsonNode data) {
        this.name = data.get("name").asText();
        this.nameLocalizations = new HashMap<DiscordLocale, String>();
        data.path("name_localizations").fields().forEachRemaining(e -> this.nameLocalizations.put(DiscordLocale.fromLocaleCode((String)e.getKey()), ((JsonNode)e.getValue()).asText()));
        this.stringValue = data.get("value").isTextual() ? data.get("value").textValue() : null;
        this.longValue = data.get("value").isLong() ? Long.valueOf(data.get("value").asLong()) : null;
    }

    public SlashCommandOptionChoiceImpl(String name, Map<DiscordLocale, String> nameLocalizations, String stringValue, Long longValue) {
        this.name = name;
        this.nameLocalizations = nameLocalizations;
        this.stringValue = stringValue;
        this.longValue = longValue;
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
    public Optional<String> getStringValue() {
        return Optional.ofNullable(this.stringValue);
    }

    @Override
    public Optional<Long> getLongValue() {
        return Optional.ofNullable(this.longValue);
    }

    public JsonNode toJsonNode() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("name", this.name);
        if (!this.nameLocalizations.isEmpty()) {
            ObjectNode nameLocalizationsJsonObject = node.putObject("name_localizations");
            this.nameLocalizations.forEach((locale, localization) -> nameLocalizationsJsonObject.put(locale.getLocaleCode(), (String)localization));
        }
        this.getLongValue().ifPresent(value -> node.put("value", (Long)value));
        this.getStringValue().ifPresent(value -> node.put("value", (String)value));
        return node;
    }
}

