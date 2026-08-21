/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.message.component.HighLevelComponent;
import org.javacord.api.interaction.InteractionBase;

public interface ModalInteraction
extends InteractionBase {
    public String getCustomId();

    public List<HighLevelComponent> getComponents();

    public List<String> getTextInputValues();

    public Optional<String> getTextInputValueByCustomId(String var1);
}

