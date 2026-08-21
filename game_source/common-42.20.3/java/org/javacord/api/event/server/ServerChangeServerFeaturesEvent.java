/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Set;
import org.javacord.api.entity.server.ServerFeature;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeServerFeaturesEvent
extends ServerEvent {
    public Set<ServerFeature> getOldServerFeatures();

    public Set<ServerFeature> getNewServerFeatures();
}

