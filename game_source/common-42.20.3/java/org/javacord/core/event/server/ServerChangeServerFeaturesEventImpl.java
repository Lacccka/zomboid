/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Collections;
import java.util.Set;
import org.javacord.api.entity.server.ServerFeature;
import org.javacord.api.event.server.ServerChangeServerFeaturesEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeServerFeaturesEventImpl
extends ServerEventImpl
implements ServerChangeServerFeaturesEvent {
    private final Set<ServerFeature> newServerFeature;
    private final Set<ServerFeature> oldServerFeature;

    public ServerChangeServerFeaturesEventImpl(ServerImpl server, Set<ServerFeature> newServerFeature, Set<ServerFeature> oldServerFeature) {
        super(server);
        this.oldServerFeature = oldServerFeature;
        this.newServerFeature = newServerFeature;
    }

    @Override
    public Set<ServerFeature> getOldServerFeatures() {
        return Collections.unmodifiableSet(this.oldServerFeature);
    }

    @Override
    public Set<ServerFeature> getNewServerFeatures() {
        return Collections.unmodifiableSet(this.newServerFeature);
    }
}

