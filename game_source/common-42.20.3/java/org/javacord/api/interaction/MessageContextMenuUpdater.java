/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.interaction.ApplicationCommandUpdater;
import org.javacord.api.interaction.MessageContextMenu;
import org.javacord.api.interaction.internal.MessageContextMenuUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class MessageContextMenuUpdater
extends ApplicationCommandUpdater<MessageContextMenu, MessageContextMenuUpdaterDelegate, MessageContextMenuUpdater> {
    public MessageContextMenuUpdater(long commandId) {
        super(DelegateFactory.createMessageContextMenuUpdaterDelegate(commandId));
    }
}

