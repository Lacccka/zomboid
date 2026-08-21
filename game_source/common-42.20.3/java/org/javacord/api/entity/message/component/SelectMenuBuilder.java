/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.List;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.LowLevelComponentBuilder;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuOption;
import org.javacord.api.entity.message.component.internal.SelectMenuBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class SelectMenuBuilder
implements LowLevelComponentBuilder {
    private final SelectMenuBuilderDelegate delegate = DelegateFactory.createSelectMenuBuilderDelegate();

    @Override
    public ComponentType getType() {
        return this.delegate.getType();
    }

    @Override
    public SelectMenuBuilderDelegate getDelegate() {
        return this.delegate;
    }

    public SelectMenuBuilder(ComponentType type, String customId) {
        if (!type.isSelectMenuType()) {
            throw new IllegalArgumentException("Invalid SelectMenu type.");
        }
        this.delegate.setType(type);
        this.delegate.setCustomId(customId);
    }

    private SelectMenuBuilder() {
    }

    public SelectMenuBuilder setPlaceholder(String placeholder) {
        this.delegate.setPlaceholder(placeholder);
        return this;
    }

    public SelectMenuBuilder setMinimumValues(int minimumValues) {
        this.delegate.setMinimumValues(minimumValues);
        return this;
    }

    public SelectMenuBuilder setMaximumValues(int maximumValues) {
        this.delegate.setMaximumValues(maximumValues);
        return this;
    }

    public SelectMenuBuilder setCustomId(String customId) {
        this.delegate.setCustomId(customId);
        return this;
    }

    public SelectMenuBuilder addChannelType(ChannelType channelType) {
        this.delegate.addChannelType(channelType);
        return this;
    }

    public SelectMenuBuilder addChannelTypes(Iterable<ChannelType> channelTypes) {
        channelTypes.forEach(this.delegate::addChannelType);
        return this;
    }

    public SelectMenuBuilder addOption(SelectMenuOption selectMenuOption) {
        this.delegate.addOption(selectMenuOption);
        return this;
    }

    public SelectMenuBuilder removeOption(SelectMenuOption selectMenuOption) {
        this.delegate.removeOption(selectMenuOption);
        return this;
    }

    public SelectMenuBuilder addOptions(List<SelectMenuOption> selectMenuOptions) {
        selectMenuOptions.forEach(this.delegate::addOption);
        return this;
    }

    public SelectMenuBuilder removeAllOptions() {
        this.delegate.removeAllOptions();
        return this;
    }

    public SelectMenuBuilder setDisabled(boolean isDisabled) {
        this.delegate.setDisabled(isDisabled);
        return this;
    }

    public SelectMenuBuilder copy(SelectMenu selectMenu) {
        this.delegate.copy(selectMenu);
        return this;
    }

    public SelectMenu build() {
        return this.delegate.build();
    }
}

