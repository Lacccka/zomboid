/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.Optional;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.SelectMenuOptionBuilder;

public interface SelectMenuOption {
    public String getLabel();

    public String getValue();

    public Optional<String> getDescription();

    public Optional<Emoji> getEmoji();

    public boolean isDefault();

    public static SelectMenuOption create(String label, String value) {
        return new SelectMenuOptionBuilder().setLabel(label).setValue(value).build();
    }

    public static SelectMenuOption create(String label, String value, boolean isDefault) {
        return new SelectMenuOptionBuilder().setLabel(label).setValue(value).setDefault(isDefault).build();
    }

    public static SelectMenuOption create(String label, String value, String description) {
        return new SelectMenuOptionBuilder().setLabel(label).setValue(value).setDescription(description).build();
    }

    public static SelectMenuOption create(String label, String value, String description, boolean isDefault) {
        return new SelectMenuOptionBuilder().setLabel(label).setValue(value).setDescription(description).setDefault(isDefault).build();
    }

    public static SelectMenuOption create(String label, String value, String description, Emoji emoji) {
        return new SelectMenuOptionBuilder().setLabel(label).setValue(value).setDescription(description).setEmoji(emoji).build();
    }

    public static SelectMenuOption create(String label, String value, String description, String unicodeEmoji) {
        return new SelectMenuOptionBuilder().setLabel(label).setValue(value).setDescription(description).setEmoji(unicodeEmoji).build();
    }

    public static SelectMenuOption create(String label, String value, String description, Emoji emoji, boolean isDefault) {
        return new SelectMenuOptionBuilder().setLabel(label).setValue(value).setDescription(description).setEmoji(emoji).setDefault(isDefault).build();
    }

    public static SelectMenuOption create(String label, String value, String description, String unicodeEmoji, boolean isDefault) {
        return new SelectMenuOptionBuilder().setLabel(label).setValue(value).setDescription(description).setEmoji(unicodeEmoji).setDefault(isDefault).build();
    }
}

