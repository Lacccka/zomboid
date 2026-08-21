/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.interaction.ApplicationCommandUpdater;
import org.javacord.api.interaction.UserContextMenu;
import org.javacord.api.interaction.internal.UserContextMenuUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class UserContextMenuUpdater
extends ApplicationCommandUpdater<UserContextMenu, UserContextMenuUpdaterDelegate, UserContextMenuUpdater> {
    public UserContextMenuUpdater(long commandId) {
        super(DelegateFactory.createUserContextMenuUpdaterDelegate(commandId));
    }
}

