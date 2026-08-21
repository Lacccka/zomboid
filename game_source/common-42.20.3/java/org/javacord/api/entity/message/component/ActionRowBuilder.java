/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.Arrays;
import java.util.List;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.HighLevelComponentBuilder;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.component.internal.ActionRowBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class ActionRowBuilder
implements HighLevelComponentBuilder {
    private final ActionRowBuilderDelegate delegate = DelegateFactory.createActionRowBuilderDelegate();

    public ActionRowBuilder addComponents(LowLevelComponent ... components) {
        return this.addComponents(Arrays.asList(components));
    }

    public ActionRowBuilder addComponents(List<LowLevelComponent> components) {
        this.delegate.addComponents(components);
        return this;
    }

    public ActionRowBuilder copy(ActionRow actionRow) {
        this.delegate.copy(actionRow);
        return this;
    }

    public ActionRowBuilder removeComponent(LowLevelComponent component) {
        this.delegate.removeComponent(component);
        return this;
    }

    public ActionRowBuilder removeComponent(int index) {
        this.delegate.removeComponent(index);
        return this;
    }

    public ActionRowBuilder removeComponent(String customId) {
        this.delegate.removeComponent(customId);
        return this;
    }

    public List<LowLevelComponent> getComponents() {
        return this.delegate.getComponents();
    }

    @Override
    public ComponentType getType() {
        return this.delegate.getType();
    }

    public ActionRow build() {
        return this.delegate.build();
    }

    @Override
    public ActionRowBuilderDelegate getDelegate() {
        return this.delegate;
    }
}

