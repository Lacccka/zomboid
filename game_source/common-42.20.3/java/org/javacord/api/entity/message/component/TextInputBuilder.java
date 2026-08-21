/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.LowLevelComponentBuilder;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.entity.message.component.internal.TextInputBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class TextInputBuilder
implements LowLevelComponentBuilder {
    private final TextInputBuilderDelegate delegate = DelegateFactory.createTextInputBuilderDelegate();

    public TextInputBuilder(TextInputStyle style, String customId, String label) {
        this.setStyle(style);
        this.setCustomId(customId);
        this.setLabel(label);
    }

    public TextInputBuilder(int style, String customId, String label) {
        this.setStyle(style);
        this.setCustomId(customId);
        this.setLabel(label);
    }

    public TextInputBuilder copy(TextInput textInput) {
        this.delegate.copy(textInput);
        return this;
    }

    @Override
    public ComponentType getType() {
        return this.delegate.getType();
    }

    public TextInputBuilder setStyle(TextInputStyle style) {
        this.delegate.setStyle(style);
        return this;
    }

    public TextInputBuilder setStyle(int styleValue) {
        TextInputStyle parsed = TextInputStyle.fromId(styleValue);
        this.delegate.setStyle(parsed);
        return this;
    }

    public TextInputBuilder setLabel(String label) {
        this.delegate.setLabel(label);
        return this;
    }

    public TextInputBuilder setPlaceholder(String placeholder) {
        this.delegate.setPlaceholder(placeholder);
        return this;
    }

    public TextInputBuilder setValue(String value) {
        this.delegate.setValue(value);
        return this;
    }

    public TextInputBuilder setMinimumLength(Integer minimumLength) {
        this.delegate.setMinimumLength(minimumLength);
        return this;
    }

    public TextInputBuilder setMaximumLength(Integer maximumLength) {
        this.delegate.setMaximumLength(maximumLength);
        return this;
    }

    public TextInputBuilder setCustomId(String customId) {
        this.delegate.setCustomId(customId);
        return this;
    }

    public TextInputBuilder setRequired(boolean required) {
        this.delegate.setRequired(required);
        return this;
    }

    public TextInput build() {
        return this.delegate.build();
    }

    @Override
    public TextInputBuilderDelegate getDelegate() {
        return this.delegate;
    }
}

