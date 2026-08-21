/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.listener.server.member;

import org.javacord.api.event.server.member.ServerMembersChunkEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.ObjectAttachableListener;
import org.javacord.api.listener.server.ServerAttachableListener;

@FunctionalInterface
public interface ServerMembersChunkListener
extends ServerAttachableListener,
GloballyAttachableListener,
ObjectAttachableListener {
    public void onServerMembersChunk(ServerMembersChunkEvent var1);
}

