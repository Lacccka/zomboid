/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.LowLevelComponentBuilder;
import org.javacord.api.entity.message.component.internal.ButtonBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class ButtonBuilder
implements LowLevelComponentBuilder {
    private final ButtonBuilderDelegate delegate = DelegateFactory.createButtonBuilderDelegate();

    public ButtonBuilder copy(Button button) {
        this.delegate.copy(button);
        return this;
    }

    @Override
    public ComponentType getType() {
        return this.delegate.getType();
    }

    public ActionRowBuilder inActionRow() {
        return new ActionRowBuilder().addComponents(this.build());
    }

    public ButtonBuilder setStyle(ButtonStyle style) {
        this.delegate.setStyle(style);
        return this;
    }

    public ButtonBuilder setStyle(String styleName) {
        ButtonStyle parsed = ButtonStyle.fromName(styleName);
        this.delegate.setStyle(parsed);
        return this;
    }

    public ButtonBuilder setStyle(int styleValue) {
        ButtonStyle parsed = ButtonStyle.fromId(styleValue);
        this.delegate.setStyle(parsed);
        return this;
    }

    public ButtonBuilder setLabel(String label) {
        this.delegate.setLabel(label);
        return this;
    }

    public ButtonBuilder setCustomId(String customId) {
        this.delegate.setCustomId(customId);
        return this;
    }

    public ButtonBuilder setUrl(String url) {
        this.delegate.setUrl(url);
        return this;
    }

    public ButtonBuilder setDisabled(Boolean isDisabled) {
        this.delegate.setDisabled(isDisabled);
        return this;
    }

    public ButtonBuilder setEmoji(String unicode) {
        this.delegate.setEmoji(unicode);
        return this;
    }

    public ButtonBuilder setEmoji(Emoji emoji) {
        this.delegate.setEmoji(emoji);
        return this;
    }

    public ButtonBuilder setEmoji(CustomEmoji emoji) {
        this.delegate.setEmoji(emoji);
        return this;
    }

    public ButtonBuilder setStyleIgnoreCase(String styleName) {
        ButtonStyle parsed = ButtonStyle.fromName(styleName.toLowerCase());
        this.delegate.setStyle(parsed);
        return this;
    }

    public Button build() {
        return this.delegate.build();
    }

    @Override
    public ButtonBuilderDelegate getDelegate() {
        return this.delegate;
    }
}

