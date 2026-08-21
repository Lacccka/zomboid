/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.core.entity.emoji.CustomEmojiImpl;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;

public class SelectMenuOptionImpl
implements SelectMenuOption {
    private final String label;
    private final String value;
    private final String description;
    private final Emoji emoji;
    private final boolean isDefault;

    public SelectMenuOptionImpl(JsonNode data) {
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
        this.description = data.has("description") ? data.get("description").asText() : null;
        this.isDefault = data.has("default") && data.get("default").asBoolean();
        this.value = data.get("value").asText();
        this.label = data.get("label").asText();
    }

    public SelectMenuOptionImpl(String label, String value, boolean isDefault, String description, Emoji emoji) {
        this.label = label;
        this.value = value;
        this.isDefault = isDefault;
        this.description = description;
        this.emoji = emoji;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    @Override
    public Optional<Emoji> getEmoji() {
        return Optional.ofNullable(this.emoji);
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    public ObjectNode toJson() {
        ObjectNode object = JsonNodeFactory.instance.objectNode();
        if (this.emoji != null) {
            ObjectNode emojiObj = JsonNodeFactory.instance.objectNode();
            if (this.emoji instanceof CustomEmojiImpl) {
                emojiObj.put("id", ((CustomEmojiImpl)this.emoji).getId());
                emojiObj.put("name", ((CustomEmojiImpl)this.emoji).getName());
            } else {
                emojiObj.put("name", this.emoji.asUnicodeEmoji().get());
            }
            object.set("emoji", emojiObj);
        }
        object.put("label", this.label);
        object.put("value", this.value);
        object.put("default", this.isDefault);
        if (this.description != null) {
            object.put("description", this.description);
        }
        return object;
    }
}

