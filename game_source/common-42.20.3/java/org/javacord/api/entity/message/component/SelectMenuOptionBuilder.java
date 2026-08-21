/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.component.internal.SelectMenuOptionBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class SelectMenuOptionBuilder {
    private final SelectMenuOptionBuilderDelegate delegate = DelegateFactory.createSelectMenuOptionBuilderDelegate();

    public SelectMenuOptionBuilder setLabel(String label) {
        this.delegate.setLabel(label);
        return this;
    }

    public SelectMenuOptionBuilder setValue(String value) {
        this.delegate.setValue(value);
        return this;
    }

    public SelectMenuOptionBuilder setDescription(String description) {
        this.delegate.setDescription(description);
        return this;
    }

    public SelectMenuOptionBuilder setEmoji(String unicode) {
        this.delegate.setEmoji(unicode);
        return this;
    }

    public SelectMenuOptionBuilder setEmoji(Emoji emoji) {
        this.delegate.setEmoji(emoji);
        return this;
    }

    public SelectMenuOptionBuilder setDefault(boolean isDefault) {
        this.delegate.setDefault(isDefault);
        return this;
    }

    public SelectMenuOption build() {
        return this.delegate.build();
    }
}

