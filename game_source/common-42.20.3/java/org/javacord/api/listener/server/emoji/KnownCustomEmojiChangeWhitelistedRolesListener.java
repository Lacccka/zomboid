/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.emoji;

import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeWhitelistedRolesEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiAttachableListener;

@FunctionalInterface
public interface KnownCustomEmojiChangeWhitelistedRolesListener
extends ServerAttachableListener,
KnownCustomEmojiAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onKnownCustomEmojiChangeWhitelistedRoles(KnownCustomEmojiChangeWhitelistedRolesEvent var1);
}

