/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.activity;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.activity.ActivityAssets;
import org.javacord.api.entity.activity.ActivityFlag;
import org.javacord.api.entity.activity.ActivityParty;
import org.javacord.api.entity.activity.ActivitySecrets;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.activity.ActivityAssetsImpl;
import org.javacord.core.entity.activity.ActivityPartyImpl;
import org.javacord.core.entity.activity.ActivitySecretsImpl;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;

public class ActivityImpl
implements Activity {
    private final ActivityType type;
    private final String name;
    private final String streamingUrl;
    private final Instant createdAt;
    private final String details;
    private final String state;
    private final ActivityParty party;
    private final ActivityAssets assets;
    private final ActivitySecrets secrets;
    private final Long applicationId;
    private final Long startTime;
    private final Long endTime;
    private final Emoji emoji;
    private final Boolean instance;
    private final EnumSet<ActivityFlag> flags = EnumSet.noneOf(ActivityFlag.class);
    private final List<String> buttonLabels = new ArrayList<String>();

    public ActivityImpl(DiscordApiImpl api, JsonNode data) {
        JsonNode emoji;
        this.type = ActivityType.getActivityTypeById(data.get("type").asInt());
        this.name = data.get("name").asText();
        this.streamingUrl = data.has("url") ? data.get("url").asText(null) : null;
        this.details = data.has("details") ? data.get("details").asText(null) : null;
        this.state = data.has("state") ? data.get("state").asText(null) : null;
        this.party = data.has("party") ? new ActivityPartyImpl(data.get("party")) : null;
        this.assets = data.has("assets") ? new ActivityAssetsImpl(this, data.get("assets")) : null;
        this.secrets = data.has("secrets") ? new ActivitySecretsImpl(data.get("secrets")) : null;
        this.applicationId = data.has("application_id") ? Long.valueOf(data.get("application_id").asLong()) : null;
        this.createdAt = Instant.ofEpochMilli(data.get("created_at").asLong());
        this.instance = data.has("instance") && data.get("instance").asBoolean();
        if (data.has("timestamps")) {
            JsonNode timestamps = data.get("timestamps");
            this.startTime = timestamps.has("start") ? Long.valueOf(timestamps.get("start").asLong()) : null;
            this.endTime = timestamps.has("end") ? Long.valueOf(timestamps.get("end").asLong()) : null;
        } else {
            this.startTime = null;
            this.endTime = null;
        }
        this.emoji = data.has("emoji") ? ((emoji = data.get("emoji")).has("id") ? api.getKnownCustomEmojiOrCreateCustomEmoji(emoji) : UnicodeEmojiImpl.fromString(emoji.get("name").asText())) : null;
        if (data.has("flags")) {
            int flags = data.get("flags").asInt();
            for (ActivityFlag flag : ActivityFlag.values()) {
                if ((flag.asInt() & flags) != flag.asInt()) continue;
                this.flags.add(flag);
            }
        }
        data.path("buttons").forEach(buttonLabel -> this.buttonLabels.add(buttonLabel.asText()));
    }

    public ActivityImpl(ActivityType type, String name, String streamingUrl) {
        this.type = type;
        this.name = name;
        this.streamingUrl = streamingUrl;
        this.createdAt = null;
        this.details = null;
        this.state = null;
        this.party = null;
        this.assets = null;
        this.secrets = null;
        this.applicationId = null;
        this.startTime = null;
        this.endTime = null;
        this.emoji = null;
        this.instance = null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<String> getStreamingUrl() {
        return Optional.ofNullable(this.streamingUrl);
    }

    @Override
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public ActivityType getType() {
        return this.type;
    }

    @Override
    public Optional<String> getDetails() {
        return Optional.ofNullable(this.details);
    }

    @Override
    public Optional<String> getState() {
        return Optional.ofNullable(this.state);
    }

    @Override
    public Optional<ActivityParty> getParty() {
        return Optional.ofNullable(this.party);
    }

    @Override
    public Optional<ActivityAssets> getAssets() {
        return Optional.ofNullable(this.assets);
    }

    @Override
    public Optional<ActivitySecrets> getSecrets() {
        return Optional.ofNullable(this.secrets);
    }

    @Override
    public Optional<Long> getApplicationId() {
        return Optional.ofNullable(this.applicationId);
    }

    @Override
    public Optional<Instant> getStartTime() {
        return Optional.ofNullable(this.startTime).map(Instant::ofEpochMilli);
    }

    @Override
    public Optional<Instant> getEndTime() {
        return Optional.ofNullable(this.endTime).map(Instant::ofEpochMilli);
    }

    @Override
    public Optional<Emoji> getEmoji() {
        return Optional.ofNullable(this.emoji);
    }

    @Override
    public Optional<Boolean> getInstance() {
        return Optional.ofNullable(this.instance);
    }

    @Override
    public EnumSet<ActivityFlag> getFlags() {
        return EnumSet.copyOf(this.flags);
    }

    @Override
    public List<String> getButtonLabels() {
        return Collections.unmodifiableList(this.buttonLabels);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ActivityImpl)) {
            return false;
        }
        ActivityImpl otherActivity = (ActivityImpl)obj;
        return Objects.deepEquals((Object)this.type, (Object)otherActivity.type) && Objects.deepEquals(this.name, otherActivity.name) && Objects.deepEquals(this.streamingUrl, otherActivity.streamingUrl) && Objects.deepEquals(this.details, otherActivity.details) && Objects.deepEquals(this.state, otherActivity.state) && Objects.deepEquals(this.party, otherActivity.party) && Objects.deepEquals(this.assets, otherActivity.assets) && Objects.deepEquals(this.applicationId, otherActivity.applicationId) && Objects.deepEquals(this.startTime, otherActivity.startTime) && Objects.deepEquals(this.endTime, otherActivity.endTime) && Objects.deepEquals(this.emoji, otherActivity.emoji) && Objects.deepEquals(this.flags, otherActivity.flags) && Objects.deepEquals(this.buttonLabels, otherActivity.buttonLabels) && Objects.deepEquals(this.secrets, otherActivity.secrets) && Objects.deepEquals(this.instance, otherActivity.instance);
    }

    public int hashCode() {
        int hash = 42;
        int typeHash = this.type.hashCode();
        int nameHash = this.name == null ? 0 : this.name.hashCode();
        int streamingUrlHash = this.streamingUrl == null ? 0 : this.streamingUrl.hashCode();
        int detailsHash = this.details == null ? 0 : this.details.hashCode();
        int stateHash = this.state == null ? 0 : this.state.hashCode();
        int partyHash = this.party == null ? 0 : this.party.hashCode();
        int assetsHash = this.assets == null ? 0 : this.assets.hashCode();
        int applicationIdHash = this.applicationId == null ? 0 : this.applicationId.toString().hashCode();
        int startTimeHash = this.startTime == null ? 0 : this.startTime.toString().hashCode();
        int endTimeHash = this.endTime == null ? 0 : this.endTime.toString().hashCode();
        int emojiHash = this.emoji == null ? 0 : this.emoji.hashCode();
        int flagsHash = this.flags == null ? 0 : this.flags.hashCode();
        int createdAtHash = this.createdAt == null ? 0 : this.createdAt.hashCode();
        int secretsHash = this.secrets == null ? 0 : this.secrets.hashCode();
        int instanceHash = this.instance == null ? 0 : this.instance.hashCode();
        int buttonsHash = this.buttonLabels == null ? 0 : this.buttonLabels.hashCode();
        hash = hash * 11 + typeHash;
        hash = hash * 13 + nameHash;
        hash = hash * 17 + streamingUrlHash;
        hash = hash * 19 + detailsHash;
        hash = hash * 23 + stateHash;
        hash = hash * 29 + partyHash;
        hash = hash * 31 + assetsHash;
        hash = hash * 37 + applicationIdHash;
        hash = hash * 41 + startTimeHash;
        hash = hash * 43 + endTimeHash;
        hash = hash * 47 + emojiHash;
        hash = hash * 53 + flagsHash;
        hash = hash * 59 + createdAtHash;
        hash = hash * 61 + secretsHash;
        hash = hash * 67 + instanceHash;
        hash = hash * 71 + buttonsHash;
        return hash;
    }
}

