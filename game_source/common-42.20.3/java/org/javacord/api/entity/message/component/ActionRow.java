/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.List;
import org.javacord.api.entity.message.component.ActionRowBuilder;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.entity.message.component.LowLevelComponent;

public interface ActionRow
extends HighLevelComponent {
    public List<LowLevelComponent> getComponents();

    public static ActionRow of(LowLevelComponent ... lowLevelComponents) {
        return new ActionRowBuilder().addComponents(lowLevelComponents).build();
    }

    public static ActionRow of(List<LowLevelComponent> lowLevelComponents) {
        return new ActionRowBuilder().addComponents(lowLevelComponents).build();
    }
}

