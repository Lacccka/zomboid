/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.activity;

import java.util.Optional;
import org.javacord.api.entity.Icon;

public interface ActivityAssets {
    public Optional<Icon> getLargeImage();

    public Optional<String> getLargeText();

    public Optional<Icon> getSmallImage();

    public Optional<String> getSmallText();
}

