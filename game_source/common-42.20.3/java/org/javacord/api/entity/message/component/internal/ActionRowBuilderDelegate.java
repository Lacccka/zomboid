/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component.internal;

import java.util.List;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.entity.message.component.LowLevelComponent;
import org.javacord.api.entity.message.component.internal.ComponentBuilderDelegate;

public interface ActionRowBuilderDelegate
extends ComponentBuilderDelegate {
    public void addComponents(List<LowLevelComponent> var1);

    public void copy(ActionRow var1);

    public void removeComponent(LowLevelComponent var1);

    public void removeComponent(int var1);

    public void removeComponent(String var1);

    public List<LowLevelComponent> getComponents();

    public ActionRow build();

    public ComponentType getType();
}

