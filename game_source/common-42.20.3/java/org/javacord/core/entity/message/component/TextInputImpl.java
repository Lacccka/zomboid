/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.core.entity.message.component.ComponentImpl;

public class TextInputImpl
extends ComponentImpl
implements TextInput {
    private final TextInputStyle style;
    private final String label;
    private final String customId;
    private final String value;
    private final String placeholder;
    private final boolean required;
    private final Integer minimumLength;
    private final Integer maximumLength;

    public TextInputImpl(JsonNode data) {
        super(ComponentType.TEXT_INPUT);
        this.customId = data.get("custom_id").asText();
        this.value = data.get("value").asText();
        this.label = data.has("label") ? data.get("label").asText() : null;
        this.style = data.has("style") ? TextInputStyle.fromId(data.get("style").asInt()) : null;
        this.required = data.has("required") && data.get("required").asBoolean();
        this.placeholder = data.has("placeholder") ? data.get("placeholder").asText() : null;
        this.minimumLength = data.has("min_length") ? Integer.valueOf(data.get("min_length").asInt()) : null;
        this.maximumLength = data.has("max_length") ? Integer.valueOf(data.get("max_length").asInt()) : null;
    }

    public TextInputImpl(TextInputStyle style, String label, String customId, String value, String placeholder, boolean required, Integer minimumLength, Integer maximumLength) {
        super(ComponentType.TEXT_INPUT);
        this.style = style;
        this.label = label;
        this.customId = customId;
        this.value = value;
        this.placeholder = placeholder;
        this.required = required;
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
    }

    @Override
    public Optional<TextInputStyle> getStyle() {
        return Optional.ofNullable(this.style);
    }

    @Override
    public String getCustomId() {
        return this.customId;
    }

    @Override
    public Optional<String> getLabel() {
        return Optional.ofNullable(this.label);
    }

    @Override
    public Optional<Integer> getMinimumLength() {
        return Optional.ofNullable(this.minimumLength);
    }

    @Override
    public Optional<Integer> getMaximumLength() {
        return Optional.ofNullable(this.maximumLength);
    }

    @Override
    public boolean isRequired() {
        return this.required;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public Optional<String> getPlaceholder() {
        return Optional.ofNullable(this.placeholder);
    }

    @Override
    public ObjectNode toJsonNode() {
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        return this.toJsonNode(object);
    }

    public ObjectNode toJsonNode(ObjectNode object) {
        object.put("type", ComponentType.TEXT_INPUT.value());
        object.put("custom_id", this.customId);
        object.put("style", this.style.getValue());
        object.put("required", this.required);
        if (this.label != null && !this.label.isEmpty()) {
            object.put("label", this.label);
        }
        if (!this.value.isEmpty()) {
            object.put("value", this.value);
        }
        if (this.placeholder != null && !this.placeholder.isEmpty()) {
            object.put("placeholder", this.placeholder);
        }
        if (this.minimumLength != null) {
            object.put("min_length", this.minimumLength);
        }
        if (this.maximumLength != null) {
            object.put("max_length", this.maximumLength);
        }
        return object;
    }
}

