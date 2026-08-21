/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.activity;

import java.util.Optional;

public interface ActivityParty {
    public Optional<String> getId();

    public Optional<Integer> getCurrentSize();

    public Optional<Integer> getMaximumSize();
}

