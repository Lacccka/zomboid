/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.event.server.sticker.StickerChangeDescriptionEvent;
import org.javacord.api.event.server.sticker.StickerChangeNameEvent;
import org.javacord.api.event.server.sticker.StickerChangeTagsEvent;
import org.javacord.api.event.server.sticker.StickerCreateEvent;
import org.javacord.api.event.server.sticker.StickerDeleteEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.sticker.StickerImpl;
import org.javacord.core.event.server.sticker.StickerChangeDescriptionEventImpl;
import org.javacord.core.event.server.sticker.StickerChangeNameEventImpl;
import org.javacord.core.event.server.sticker.StickerChangeTagsEventImpl;
import org.javacord.core.event.server.sticker.StickerCreateEventImpl;
import org.javacord.core.event.server.sticker.StickerDeleteEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildStickersUpdateHandler
extends PacketHandler {
    public GuildStickersUpdateHandler(DiscordApi api) {
        super(api, true, "GUILD_STICKERS_UPDATE");
    }

    @Override
    protected void handle(JsonNode packet) {
        long serverId = packet.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).map(server -> (ServerImpl)server).ifPresent(server -> {
            HashMap<Long, JsonNode> stickers = new HashMap<Long, JsonNode>();
            for (JsonNode sticketJson : packet.get("stickers")) {
                stickers.put(sticketJson.get("id").asLong(), sticketJson);
            }
            stickers.forEach((key, value) -> {
                Optional<Sticker> optionalSticker = server.getStickerById((long)key);
                if (optionalSticker.isPresent()) {
                    String newTags;
                    String oldTags;
                    String newDescription;
                    String oldDescription;
                    String newName;
                    Sticker sticker = optionalSticker.get();
                    String oldName = sticker.getName();
                    if (!Objects.deepEquals(oldName, newName = value.get("name").asText())) {
                        StickerChangeNameEventImpl event = new StickerChangeNameEventImpl(sticker, oldName, newName);
                        ((StickerImpl)sticker).setName(newName);
                        this.api.getEventDispatcher().dispatchStickerChangeNameEvent((DispatchQueueSelector)server, (Server)server, sticker, (StickerChangeNameEvent)event);
                    }
                    if (!Objects.deepEquals(oldDescription = sticker.getDescription(), newDescription = value.get("description").asText())) {
                        StickerChangeDescriptionEventImpl event = new StickerChangeDescriptionEventImpl(sticker, oldDescription, newDescription);
                        ((StickerImpl)sticker).setDescription(newDescription);
                        this.api.getEventDispatcher().dispatchStickerChangeDescriptionEvent((DispatchQueueSelector)server, (Server)server, sticker, (StickerChangeDescriptionEvent)event);
                    }
                    if (!Objects.deepEquals(oldTags = sticker.getTags(), newTags = value.get("tags").asText())) {
                        StickerChangeTagsEventImpl event = new StickerChangeTagsEventImpl(sticker, oldTags, newTags);
                        ((StickerImpl)sticker).setTags(newTags);
                        this.api.getEventDispatcher().dispatchStickerChangeTagsEvent((DispatchQueueSelector)server, (Server)server, sticker, (StickerChangeTagsEvent)event);
                    }
                } else {
                    Sticker sticker = this.api.getOrCreateSticker((JsonNode)value);
                    server.addSticker(sticker);
                    StickerCreateEventImpl event = new StickerCreateEventImpl(sticker);
                    this.api.getEventDispatcher().dispatchStickerCreateEvent((DispatchQueueSelector)server, (Server)server, (StickerCreateEvent)event);
                }
            });
            Set stickerIds = stickers.keySet();
            server.getStickers().stream().filter(sticker -> !stickerIds.contains(sticker.getId())).forEach(sticker -> {
                this.api.removeSticker((Sticker)sticker);
                server.removeSticker((Sticker)sticker);
                StickerDeleteEventImpl event = new StickerDeleteEventImpl((Sticker)sticker);
                this.api.getEventDispatcher().dispatchStickerDeleteEvent((DispatchQueueSelector)server, (Server)server, (Sticker)sticker, (StickerDeleteEvent)event);
            });
        });
    }
}

