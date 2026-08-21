/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel.server;

import org.javacord.api.listener.channel.server.ChannelCategoryAttachableListener;
import org.javacord.api.listener.channel.server.text.ServerTextChannelAttachableListener;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelAttachableListener;

public interface ServerChannelAttachableListener
extends ServerTextChannelAttachableListener,
ServerVoiceChannelAttachableListener,
ChannelCategoryAttachableListener {
}

