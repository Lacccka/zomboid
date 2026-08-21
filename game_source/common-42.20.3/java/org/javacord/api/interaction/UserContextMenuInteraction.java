/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ApplicationCommandInteraction;

public interface UserContextMenuInteraction
extends ApplicationCommandInteraction {
    public User getTarget();
}

