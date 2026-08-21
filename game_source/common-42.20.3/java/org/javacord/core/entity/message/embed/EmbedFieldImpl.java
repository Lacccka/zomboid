/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.entity.message.embed.EmbedField;

public class EmbedFieldImpl
implements EmbedField {
    private String name;
    private String value;
    private boolean inline;

    public EmbedFieldImpl(JsonNode data) {
        this.name = data.has("name") ? data.get("name").asText() : null;
        this.value = data.has("value") ? data.get("value").asText() : null;
        this.inline = data.has("inline") && data.get("inline").asBoolean();
    }

    public EmbedFieldImpl(String name, String value, boolean inline) {
        this.name = name;
        this.value = value;
        this.inline = inline;
    }

    void setName(String name) {
        this.name = name;
    }

    void setValue(String value) {
        this.value = value;
    }

    void setInline(boolean inline) {
        this.inline = inline;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean isInline() {
        return this.inline;
    }
}

