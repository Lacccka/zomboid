/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

import java.util.Optional;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public interface Ban {
    public Server getServer();

    public User getUser();

    public Optional<String> getReason();
}

