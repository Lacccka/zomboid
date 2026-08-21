/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.component.internal;

import java.util.Optional;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.TextInput;
import org.javacord.api.entity.message.component.TextInputStyle;
import org.javacord.api.entity.message.component.internal.TextInputBuilderDelegate;
import org.javacord.core.entity.message.component.TextInputImpl;

public class TextInputBuilderDelegateImpl
implements TextInputBuilderDelegate {
    private final ComponentType type = ComponentType.TEXT_INPUT;
    private TextInputStyle style = null;
    private String customId = null;
    private String label = null;
    private String value = "";
    private String placeholder = null;
    private Integer minimumLength = null;
    private Integer maximumLength = null;
    private boolean required = false;

    @Override
    public ComponentType getType() {
        return this.type;
    }

    @Override
    public void copy(TextInput textInput) {
        this.style = textInput.getStyle().orElse(null);
        this.customId = textInput.getCustomId();
        this.label = textInput.getLabel().orElse(null);
        this.required = textInput.isRequired();
        this.minimumLength = textInput.getMinimumLength().orElse(null);
        this.maximumLength = textInput.getMaximumLength().orElse(null);
    }

    @Override
    public void setStyle(TextInputStyle style) {
        this.style = style;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setValue(String value) {
        this.value = value == null ? "" : value;
    }

    @Override
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public void setMinimumLength(Integer minimumLength) {
        this.minimumLength = minimumLength;
    }

    @Override
    public void setMaximumLength(Integer maximumLength) {
        this.maximumLength = maximumLength;
    }

    @Override
    public void setCustomId(String customId) {
        this.customId = customId;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public TextInputStyle getStyle() {
        return this.style;
    }

    @Override
    public String getCustomId() {
        return this.customId;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public Optional<String> getValue() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public Optional<String> getPlaceholder() {
        return Optional.ofNullable(this.placeholder);
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
    public TextInput build() {
        return new TextInputImpl(this.style, this.label, this.customId, this.value, this.placeholder, this.required, this.minimumLength, this.maximumLength);
    }
}

