/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.message.reaction;

import org.javacord.api.event.message.reaction.ReactionRemoveAllEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ReactionRemoveAllListener
extends ServerAttachableListener,
TextChannelAttachableListener,
MessageAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onReactionRemoveAll(ReactionRemoveAllEvent var1);
}

