/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.core.entity.emoji.CustomEmojiImpl;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;
import org.javacord.core.entity.message.component.ComponentImpl;

public class ButtonImpl
extends ComponentImpl
implements Button {
    private final ButtonStyle style;
    private final String label;
    private final String customId;
    private final String url;
    private final Boolean disabled;
    private final Emoji emoji;

    public ButtonImpl(JsonNode data) {
        super(ComponentType.BUTTON);
        this.style = ButtonStyle.fromId(data.get("style").asInt());
        this.label = data.has("label") ? data.get("label").asText() : null;
        this.customId = data.has("custom_id") ? data.get("custom_id").asText() : null;
        this.url = data.has("url") ? data.get("url").asText() : null;
        Boolean bl = this.disabled = data.has("disabled") ? Boolean.valueOf(data.get("disabled").asBoolean()) : null;
        if (data.has("emoji")) {
            JsonNode emojiObj = data.get("emoji");
            if (emojiObj.has("id")) {
                long id = emojiObj.get("id").asLong();
                String name = emojiObj.get("name").asText();
                boolean isAnimated = emojiObj.has("animated");
                this.emoji = new CustomEmojiImpl(null, id, name, isAnimated);
            } else {
                String name = emojiObj.get("name").asText();
                this.emoji = UnicodeEmojiImpl.fromString(name);
            }
        } else {
            this.emoji = null;
        }
    }

    public ButtonImpl(ButtonStyle style, String label, String customId, String url, Boolean disabled, Emoji emoji) {
        super(ComponentType.BUTTON);
        this.style = style;
        this.label = label;
        this.customId = customId;
        this.url = url;
        this.disabled = disabled;
        this.emoji = emoji;
    }

    @Override
    public ButtonStyle getStyle() {
        return this.style;
    }

    @Override
    public Optional<String> getCustomId() {
        return Optional.ofNullable(this.customId);
    }

    @Override
    public Optional<String> getLabel() {
        return Optional.ofNullable(this.label);
    }

    @Override
    public Optional<String> getUrl() {
        return Optional.ofNullable(this.url);
    }

    @Override
    public Optional<Boolean> isDisabled() {
        return Optional.ofNullable(this.disabled);
    }

    @Override
    public Optional<Emoji> getEmoji() {
        return Optional.ofNullable(this.emoji);
    }

    @Override
    public ObjectNode toJsonNode() {
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        return this.toJsonNode(object);
    }

    public ObjectNode toJsonNode(ObjectNode object) {
        object.put("type", ComponentType.BUTTON.value());
        if (this.style == null) {
            throw new IllegalStateException("Button style is null.");
        }
        object.put("style", this.style.getValue());
        if (this.label != null && !this.label.equals("")) {
            object.put("label", this.label);
        }
        if (this.style != ButtonStyle.LINK) {
            if (this.customId == null || this.customId.equals("")) {
                throw new IllegalStateException("Button is missing a custom identifier.");
            }
            if (this.url != null) {
                throw new IllegalStateException("A non-button link must not have a URL.");
            }
            object.put("custom_id", this.customId);
        }
        if (this.style == ButtonStyle.LINK) {
            if (this.url == null || this.url.equals("")) {
                throw new IllegalStateException("Button link is missing a URL.");
            }
            if (this.customId != null) {
                throw new IllegalStateException("Button link must not have a custom identifier");
            }
            object.put("url", this.url);
        }
        if (this.disabled != null) {
            object.put("disabled", this.disabled);
        }
        if (this.emoji != null) {
            ObjectNode emojiObj = JsonNodeFactory.instance.objectNode();
            if (this.emoji.isUnicodeEmoji()) {
                Optional<String> unicodeEmojiOptional = this.emoji.asUnicodeEmoji();
                unicodeEmojiOptional.ifPresent(emojiName -> emojiObj.put("name", (String)emojiName));
            } else if (this.emoji.isCustomEmoji()) {
                Optional<CustomEmoji> customEmojiOptional = this.emoji.asCustomEmoji();
                customEmojiOptional.ifPresent(customEmoji -> emojiObj.put("id", customEmoji.getId()));
            }
            object.set("emoji", emojiObj);
        }
        return object;
    }
}

