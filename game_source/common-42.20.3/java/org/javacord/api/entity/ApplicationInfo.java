/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.util.Optional;
import org.javacord.api.entity.ApplicationOwner;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.team.Team;

public interface ApplicationInfo
extends Nameable {
    public long getClientId();

    public String getDescription();

    public boolean isPublicBot();

    default public boolean isOwnedByTeam() {
        return this.getTeam().isPresent();
    }

    public boolean botRequiresCodeGrant();

    public Optional<ApplicationOwner> getOwner();

    public Optional<Team> getTeam();
}

