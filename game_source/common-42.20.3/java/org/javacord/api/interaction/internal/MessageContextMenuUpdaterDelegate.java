/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.internal;

import org.javacord.api.interaction.MessageContextMenu;
import org.javacord.api.interaction.internal.ApplicationCommandUpdaterDelegate;

public interface MessageContextMenuUpdaterDelegate
extends ApplicationCommandUpdaterDelegate<MessageContextMenu> {
    @Override
    public void setName(String var1);
}

