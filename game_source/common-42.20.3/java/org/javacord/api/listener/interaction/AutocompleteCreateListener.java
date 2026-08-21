/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.interaction;

import org.javacord.api.event.interaction.AutocompleteCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface AutocompleteCreateListener
extends ServerAttachableListener,
UserAttachableListener,
TextChannelAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onAutocompleteCreate(AutocompleteCreateEvent var1);
}

