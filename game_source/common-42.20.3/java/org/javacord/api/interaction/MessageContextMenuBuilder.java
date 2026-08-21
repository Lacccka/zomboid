/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.MessageContextMenu;
import org.javacord.api.interaction.internal.MessageContextMenuBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class MessageContextMenuBuilder
extends ApplicationCommandBuilder<MessageContextMenu, MessageContextMenuBuilderDelegate, MessageContextMenuBuilder> {
    public MessageContextMenuBuilder() {
        super(DelegateFactory.createMessageContextMenuBuilderDelegate());
    }
}

