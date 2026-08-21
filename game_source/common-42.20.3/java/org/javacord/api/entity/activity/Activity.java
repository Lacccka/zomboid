/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.activity;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.activity.ActivityAssets;
import org.javacord.api.entity.activity.ActivityFlag;
import org.javacord.api.entity.activity.ActivityParty;
import org.javacord.api.entity.activity.ActivitySecrets;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.emoji.Emoji;

public interface Activity
extends Nameable {
    public ActivityType getType();

    public Optional<String> getStreamingUrl();

    public Instant getCreatedAt();

    public Optional<String> getDetails();

    public Optional<String> getState();

    public Optional<ActivityParty> getParty();

    public Optional<ActivityAssets> getAssets();

    public Optional<ActivitySecrets> getSecrets();

    public Optional<Long> getApplicationId();

    public Optional<Instant> getStartTime();

    public Optional<Instant> getEndTime();

    public Optional<Emoji> getEmoji();

    public Optional<Boolean> getInstance();

    public EnumSet<ActivityFlag> getFlags();

    public List<String> getButtonLabels();
}

