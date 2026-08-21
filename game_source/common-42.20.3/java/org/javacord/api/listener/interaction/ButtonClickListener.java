/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.interaction;

import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.message.MessageAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.user.UserAttachableListener;

@FunctionalInterface
public interface ButtonClickListener
extends ServerAttachableListener,
UserAttachableListener,
TextChannelAttachableListener,
MessageAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onButtonClick(ButtonClickEvent var1);
}

