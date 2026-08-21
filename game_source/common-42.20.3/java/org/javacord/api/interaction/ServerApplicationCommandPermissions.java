/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Set;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.ApplicationCommandPermissions;

public interface ServerApplicationCommandPermissions {
    public long getId();

    public long getApplicationId();

    public long getServerId();

    public Server getServer();

    public Set<ApplicationCommandPermissions> getPermissions();
}

