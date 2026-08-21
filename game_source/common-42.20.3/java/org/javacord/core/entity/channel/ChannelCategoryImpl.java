/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Categorizable;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.RegularServerChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.listener.channel.server.InternalChannelCategoryAttachableListenerManager;

public class ChannelCategoryImpl
extends RegularServerChannelImpl
implements ChannelCategory,
InternalChannelCategoryAttachableListenerManager {
    private volatile boolean nsfw;

    public ChannelCategoryImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
        this.nsfw = data.has("nsfw") && data.get("nsfw").asBoolean();
    }

    public void setNsfwFlag(boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Override
    public List<RegularServerChannel> getChannels() {
        return Collections.unmodifiableList(((ServerImpl)this.getServer()).getUnorderedChannels().stream().filter(channel -> channel.asCategorizable().flatMap(Categorizable::getCategory).map(this::equals).orElse(false)).map(Channel::asRegularServerChannel).filter(Optional::isPresent).map(Optional::get).sorted(Comparator.comparingInt(channel -> channel.getType().getId()).thenComparing(RegularServerChannelImpl.COMPARE_BY_RAW_POSITION)).collect(Collectors.toList()));
    }

    @Override
    public boolean isNsfw() {
        return this.nsfw;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return String.format("ChannelCategory (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

