/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.component.internal;

import java.util.ArrayList;
import java.util.List;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.component.SelectMenu;
import org.javacord.api.entity.message.component.SelectMenuBuilder;
import org.javacord.api.entity.message.component.internal.ActionRowBuilderDelegate;
import org.javacord.core.entity.message.component.ActionRowImpl;

public class ActionRowBuilderDelegateImpl
implements ActionRowBuilderDelegate {
    private final ComponentType type = ComponentType.ACTION_ROW;
    private final List<LowLevelComponent> components = new ArrayList<LowLevelComponent>();

    @Override
    public void addComponents(List<LowLevelComponent> components) {
        this.components.addAll(components);
    }

    @Override
    public void copy(ActionRow actionRow) {
        actionRow.getComponents().forEach(component -> {
            if (component.getType() == ComponentType.BUTTON) {
                ButtonBuilder builder = new ButtonBuilder();
                builder.copy((Button)component);
                this.components.add(builder.build());
            } else if (component.getType().isSelectMenuType()) {
                SelectMenu sm = (SelectMenu)component;
                SelectMenuBuilder builder = new SelectMenuBuilder(sm.getType(), sm.getCustomId());
                builder.copy(sm);
                this.components.add(builder.build());
            }
        });
    }

    @Override
    public void removeComponent(LowLevelComponent component) {
        this.components.remove(component);
    }

    @Override
    public void removeComponent(int index) {
        this.components.remove(index);
    }

    @Override
    public void removeComponent(String customId) {
        this.components.removeIf(componentBuilder -> {
            if (componentBuilder.getType() == ComponentType.BUTTON) {
                ButtonBuilder buttonBuilder = (ButtonBuilder)((Object)componentBuilder);
                return buttonBuilder.getDelegate().getCustomId().equals(customId);
            }
            if (componentBuilder.getType().isSelectMenuType()) {
                SelectMenuBuilder selectMenuBuilder = (SelectMenuBuilder)((Object)componentBuilder);
                return selectMenuBuilder.getDelegate().getCustomId().equals(customId);
            }
            return false;
        });
    }

    @Override
    public List<LowLevelComponent> getComponents() {
        return this.components;
    }

    @Override
    public ActionRow build() {
        return new ActionRowImpl(this.components);
    }

    @Override
    public ComponentType getType() {
        return this.type;
    }
}

