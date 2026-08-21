/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.activity;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.activity.ActivityAssets;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.activity.ActivityImpl;

public class ActivityAssetsImpl
implements ActivityAssets {
    private final ActivityImpl activity;
    private final String largeImage;
    private final String largeText;
    private final String smallImage;
    private final String smallText;

    public ActivityAssetsImpl(ActivityImpl activity, JsonNode data) {
        this.activity = activity;
        this.largeImage = data.has("large_image") ? data.get("large_image").asText(null) : null;
        this.largeText = data.has("large_text") ? data.get("large_text").asText(null) : null;
        this.smallImage = data.has("small_image") ? data.get("small_image").asText(null) : null;
        this.smallText = data.has("small_text") ? data.get("small_text").asText(null) : null;
    }

    @Override
    public Optional<Icon> getLargeImage() {
        return Optional.ofNullable(this.largeImage).flatMap(imageId -> this.activity.getApplicationId().map(applicationId -> String.format("https://cdn.discordapp.com/app-assets/%s/%s.png", applicationId, imageId))).map(url -> {
            try {
                return new URL((String)url);
            }
            catch (MalformedURLException e) {
                throw new CompletionException(e);
            }
        }).map(url -> new IconImpl(null, (URL)url));
    }

    @Override
    public Optional<String> getLargeText() {
        return Optional.ofNullable(this.largeText);
    }

    @Override
    public Optional<Icon> getSmallImage() {
        return Optional.ofNullable(this.smallImage).flatMap(imageId -> this.activity.getApplicationId().map(applicationId -> String.format("https://cdn.discordapp.com/app-assets/%s/%s.png", applicationId, imageId))).map(url -> {
            try {
                return new URL((String)url);
            }
            catch (MalformedURLException e) {
                throw new CompletionException(e);
            }
        }).map(url -> new IconImpl(null, (URL)url));
    }

    @Override
    public Optional<String> getSmallText() {
        return Optional.ofNullable(this.smallText);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ActivityAssetsImpl)) {
            return false;
        }
        ActivityAssetsImpl otherAssets = (ActivityAssetsImpl)obj;
        return Objects.deepEquals(this.largeImage, otherAssets.largeImage) && Objects.deepEquals(this.largeText, otherAssets.largeText) && Objects.deepEquals(this.smallImage, otherAssets.smallImage) && Objects.deepEquals(this.smallText, otherAssets.smallText);
    }

    public int hashCode() {
        int hash = 42;
        int largeImageHash = this.largeImage == null ? 0 : this.largeImage.hashCode();
        int largeTextHash = this.largeText == null ? 0 : this.largeText.hashCode();
        int smallImageHash = this.smallImage == null ? 0 : this.smallImage.hashCode();
        int smallTextHash = this.smallText == null ? 0 : this.smallText.hashCode();
        hash = hash * 11 + largeImageHash;
        hash = hash * 13 + largeTextHash;
        hash = hash * 17 + smallImageHash;
        hash = hash * 19 + smallTextHash;
        return hash;
    }
}

