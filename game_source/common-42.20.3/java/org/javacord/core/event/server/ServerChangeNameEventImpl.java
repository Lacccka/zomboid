/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeNameEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeNameEventImpl
extends ServerEventImpl
implements ServerChangeNameEvent {
    private final String newName;
    private final String oldName;

    public ServerChangeNameEventImpl(Server server, String newName, String oldName) {
        super(server);
        this.newName = newName;
        this.oldName = oldName;
    }

    @Override
    public String getOldName() {
        return this.oldName;
    }

    @Override
    public String getNewName() {
        return this.newName;
    }
}

