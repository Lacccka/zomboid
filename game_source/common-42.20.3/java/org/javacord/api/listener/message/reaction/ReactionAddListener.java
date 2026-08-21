/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.message.reaction;

import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface ReactionAddListener
extends ServerAttachableListener,
UserAttachableListener,
TextChannelAttachableListener,
MessageAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onReactionAdd(ReactionAddEvent var1);
}

