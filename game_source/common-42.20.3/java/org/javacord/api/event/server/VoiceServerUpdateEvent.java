/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.event.server.ServerEvent;

public interface VoiceServerUpdateEvent
extends ServerEvent {
    public String getToken();

    public String getEndpoint();
}

