/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.channel;

import org.javacord.api.listener.channel.TextChannelAttachableListener;
import org.javacord.api.listener.channel.VoiceChannelAttachableListener;
import org.javacord.api.listener.channel.server.ServerChannelAttachableListener;

public interface ChannelAttachableListener
extends TextChannelAttachableListener,
VoiceChannelAttachableListener,
ServerChannelAttachableListener {
}

