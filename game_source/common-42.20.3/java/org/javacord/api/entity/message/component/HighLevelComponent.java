/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.component;

import java.util.Optional;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Component;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.util.Specializable;

public interface HighLevelComponent
extends Component,
Specializable<HighLevelComponent> {
    default public boolean isActionRow() {
        return this.getType() == ComponentType.ACTION_ROW;
    }

    default public Optional<ActionRow> asActionRow() {
        return this.isActionRow() ? Optional.of((ActionRow)this) : Optional.empty();
    }
}

