/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.Optional;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.component.TextInputBuilder;
import org.javacord.api.entity.message.component.TextInputStyle;

public interface TextInput
extends LowLevelComponent {
    public Optional<TextInputStyle> getStyle();

    public String getCustomId();

    public Optional<String> getLabel();

    public Optional<Integer> getMinimumLength();

    public Optional<Integer> getMaximumLength();

    public boolean isRequired();

    public String getValue();

    public Optional<String> getPlaceholder();

    public static TextInput create(TextInputStyle style, String customId, String label) {
        return new TextInputBuilder(style, customId, label).build();
    }

    public static TextInput create(TextInputStyle style, String customId, String label, boolean required) {
        return new TextInputBuilder(style, customId, label).setRequired(required).build();
    }

    public static TextInput create(TextInputStyle style, String customId, String label, Integer minimumLength, Integer maximumLength) {
        return new TextInputBuilder(style, customId, label).setMinimumLength(minimumLength).setMaximumLength(maximumLength).build();
    }

    public static TextInput create(TextInputStyle style, String customId, String label, Integer minimumLength, Integer maximumLength, boolean required) {
        return new TextInputBuilder(style, customId, label).setMinimumLength(minimumLength).setMaximumLength(maximumLength).setRequired(required).build();
    }

    public static TextInput create(TextInputStyle style, String customId, String label, String placeholder, String value) {
        return new TextInputBuilder(style, customId, label).setPlaceholder(placeholder).setValue(value).build();
    }

    public static TextInput create(TextInputStyle style, String customId, String label, String placeholder, String value, boolean required) {
        return new TextInputBuilder(style, customId, label).setPlaceholder(placeholder).setValue(value).setRequired(required).build();
    }
}

