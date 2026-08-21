/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.UserContextMenu;
import org.javacord.api.interaction.internal.UserContextMenuBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class UserContextMenuBuilder
extends ApplicationCommandBuilder<UserContextMenu, UserContextMenuBuilderDelegate, UserContextMenuBuilder> {
    public UserContextMenuBuilder() {
        super(DelegateFactory.createUserContextMenuBuilderDelegate());
    }
}

