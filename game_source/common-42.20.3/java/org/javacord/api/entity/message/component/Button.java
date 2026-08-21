/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.Optional;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;
import org.javacord.api.entity.message.component.LowLevelComponent;

public interface Button
extends LowLevelComponent {
    public ButtonStyle getStyle();

    public Optional<String> getCustomId();

    public Optional<String> getLabel();

    public Optional<String> getUrl();

    public Optional<Boolean> isDisabled();

    public Optional<Emoji> getEmoji();

    public static Button danger(String customId, String label) {
        return Button.create(customId, ButtonStyle.DANGER, label, (Emoji)null, false);
    }

    public static Button danger(String customId, String label, boolean disabled) {
        return Button.create(customId, ButtonStyle.DANGER, label, (Emoji)null, disabled);
    }

    public static Button danger(String customId, String label, Emoji emoji) {
        return Button.create(customId, ButtonStyle.DANGER, label, emoji, false);
    }

    public static Button danger(String customId, String label, Emoji emoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.DANGER, label, emoji, disabled);
    }

    public static Button danger(String customId, String label, String unicodeEmoji) {
        return Button.create(customId, ButtonStyle.DANGER, label, unicodeEmoji, false);
    }

    public static Button danger(String customId, String label, String unicodeEmoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.DANGER, label, unicodeEmoji, disabled);
    }

    public static Button danger(String customId, Emoji emoji) {
        return Button.create(customId, ButtonStyle.DANGER, null, emoji, false);
    }

    public static Button danger(String customId, Emoji emoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.DANGER, null, emoji, disabled);
    }

    public static Button primary(String customId, String label) {
        return Button.create(customId, ButtonStyle.PRIMARY, label, (Emoji)null, false);
    }

    public static Button primary(String customId, String label, boolean disabled) {
        return Button.create(customId, ButtonStyle.PRIMARY, label, (Emoji)null, disabled);
    }

    public static Button primary(String customId, String label, Emoji emoji) {
        return Button.create(customId, ButtonStyle.PRIMARY, label, emoji, false);
    }

    public static Button primary(String customId, String label, Emoji emoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.PRIMARY, label, emoji, disabled);
    }

    public static Button primary(String customId, String label, String unicodeEmoji) {
        return Button.create(customId, ButtonStyle.PRIMARY, label, unicodeEmoji, false);
    }

    public static Button primary(String customId, String label, String unicodeEmoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.PRIMARY, label, unicodeEmoji, disabled);
    }

    public static Button primary(String customId, Emoji emoji) {
        return Button.create(customId, ButtonStyle.PRIMARY, null, emoji, false);
    }

    public static Button primary(String customId, Emoji emoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.PRIMARY, null, emoji, disabled);
    }

    public static Button secondary(String customId, String label) {
        return Button.create(customId, ButtonStyle.SECONDARY, label, (Emoji)null, false);
    }

    public static Button secondary(String customId, String label, boolean disabled) {
        return Button.create(customId, ButtonStyle.SECONDARY, label, (Emoji)null, disabled);
    }

    public static Button secondary(String customId, String label, Emoji emoji) {
        return Button.create(customId, ButtonStyle.SECONDARY, label, emoji, false);
    }

    public static Button secondary(String customId, String label, Emoji emoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.SECONDARY, label, emoji, disabled);
    }

    public static Button secondary(String customId, String label, String unicodeEmoji) {
        return Button.create(customId, ButtonStyle.SECONDARY, label, unicodeEmoji, false);
    }

    public static Button secondary(String customId, String label, String unicodeEmoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.SECONDARY, label, unicodeEmoji, disabled);
    }

    public static Button secondary(String customId, Emoji emoji) {
        return Button.create(customId, ButtonStyle.SECONDARY, null, emoji, false);
    }

    public static Button secondary(String customId, Emoji emoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.SECONDARY, null, emoji, disabled);
    }

    public static Button success(String customId, String label) {
        return Button.create(customId, ButtonStyle.SUCCESS, label, (Emoji)null, false);
    }

    public static Button success(String customId, String label, boolean disabled) {
        return Button.create(customId, ButtonStyle.SUCCESS, label, (Emoji)null, disabled);
    }

    public static Button success(String customId, String label, Emoji emoji) {
        return Button.create(customId, ButtonStyle.SUCCESS, label, emoji, false);
    }

    public static Button success(String customId, String label, Emoji emoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.SUCCESS, label, emoji, disabled);
    }

    public static Button success(String customId, String label, String unicodeEmoji) {
        return Button.create(customId, ButtonStyle.SUCCESS, label, unicodeEmoji, false);
    }

    public static Button success(String customId, String label, String unicodeEmoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.SUCCESS, label, unicodeEmoji, disabled);
    }

    public static Button success(String customId, Emoji emoji) {
        return Button.create(customId, ButtonStyle.SUCCESS, null, emoji, false);
    }

    public static Button success(String customId, Emoji emoji, boolean disabled) {
        return Button.create(customId, ButtonStyle.SUCCESS, null, emoji, disabled);
    }

    public static Button link(String url, String label) {
        return Button.link(url, label, false);
    }

    public static Button link(String url, String label, boolean disabled) {
        return new ButtonBuilder().setStyle(ButtonStyle.LINK).setUrl(url).setLabel(label).setDisabled(disabled).build();
    }

    public static Button link(String url, String label, Emoji emoji) {
        return Button.link(url, label, emoji, false);
    }

    public static Button link(String url, String label, Emoji emoji, boolean disabled) {
        return new ButtonBuilder().setStyle(ButtonStyle.LINK).setUrl(url).setLabel(label).setEmoji(emoji).setDisabled(disabled).build();
    }

    public static Button link(String url, String label, String unicodeEmoji) {
        return Button.link(url, label, unicodeEmoji, false);
    }

    public static Button link(String url, String label, String unicodeEmoji, boolean disabled) {
        return new ButtonBuilder().setStyle(ButtonStyle.LINK).setUrl(url).setLabel(label).setEmoji(unicodeEmoji).setDisabled(disabled).build();
    }

    public static Button link(String url, Emoji emoji) {
        return Button.link(url, emoji, false);
    }

    public static Button link(String url, Emoji emoji, boolean disabled) {
        return new ButtonBuilder().setStyle(ButtonStyle.LINK).setUrl(url).setEmoji(emoji).setDisabled(disabled).build();
    }

    public static Button create(String customId, ButtonStyle style, String label) {
        return Button.create(customId, style, label, (Emoji)null, false);
    }

    public static Button create(String customId, ButtonStyle style, String label, boolean disabled) {
        return Button.create(customId, style, label, (Emoji)null, disabled);
    }

    public static Button create(String customId, ButtonStyle style, String label, Emoji emoji) {
        return Button.create(customId, style, label, emoji, false);
    }

    public static Button create(String customId, ButtonStyle style, String label, Emoji emoji, boolean disabled) {
        if (style == ButtonStyle.LINK) {
            throw new IllegalArgumentException("You can not use the link style with this method.Please use Button#link() instead or create a custom ButtonBuilder by calling 'new ButtonBuilder()'.");
        }
        return new ButtonBuilder().setCustomId(customId).setStyle(style).setLabel(label).setEmoji(emoji).setDisabled(disabled).build();
    }

    public static Button create(String customId, ButtonStyle style, String label, String unicodeEmoji) {
        return Button.create(customId, style, label, unicodeEmoji, false);
    }

    public static Button create(String customId, ButtonStyle style, String label, String unicodeEmoji, boolean disabled) {
        if (style == ButtonStyle.LINK) {
            throw new IllegalArgumentException("You can not use the link style with this method.Please use Button#link() instead or create a custom ButtonBuilder by calling 'new ButtonBuilder()'.");
        }
        return new ButtonBuilder().setCustomId(customId).setStyle(style).setLabel(label).setEmoji(unicodeEmoji).setDisabled(disabled).build();
    }

    public static Button create(String customId, ButtonStyle style, Emoji emoji) {
        return Button.create(customId, style, null, emoji, false);
    }
}

