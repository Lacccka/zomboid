/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.activity;

import java.util.Optional;

public interface ActivitySecrets {
    public Optional<String> getJoin();

    public Optional<String> getSpectate();

    public Optional<String> getMatch();
}

