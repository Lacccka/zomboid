/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeNameEvent;
import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeWhitelistedRolesEvent;
import org.javacord.api.event.server.emoji.KnownCustomEmojiCreateEvent;
import org.javacord.api.event.server.emoji.KnownCustomEmojiDeleteEvent;
import org.javacord.core.entity.emoji.KnownCustomEmojiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.emoji.KnownCustomEmojiChangeNameEventImpl;
import org.javacord.core.event.server.emoji.KnownCustomEmojiChangeWhitelistedRolesEventImpl;
import org.javacord.core.event.server.emoji.KnownCustomEmojiCreateEventImpl;
import org.javacord.core.event.server.emoji.KnownCustomEmojiDeleteEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildEmojisUpdateHandler
extends PacketHandler {
    public GuildEmojisUpdateHandler(DiscordApi api) {
        super(api, true, "GUILD_EMOJIS_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        long id = packet.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(id).map(server -> (ServerImpl)server).ifPresent(server -> {
            HashMap<Long, JsonNode> emojis = new HashMap<Long, JsonNode>();
            for (JsonNode emojiJson : packet.get("emojis")) {
                emojis.put(emojiJson.get("id").asLong(), emojiJson);
            }
            emojis.forEach((key, value) -> {
                Optional<KnownCustomEmoji> optionalEmoji = server.getCustomEmojiById((long)key);
                if (optionalEmoji.isPresent()) {
                    String newName;
                    KnownCustomEmoji emoji = optionalEmoji.get();
                    String oldName = emoji.getName();
                    if (!Objects.deepEquals(oldName, newName = value.get("name").asText())) {
                        KnownCustomEmojiChangeNameEventImpl event = new KnownCustomEmojiChangeNameEventImpl(emoji, newName, oldName);
                        ((KnownCustomEmojiImpl)emoji).setName(newName);
                        this.api.getEventDispatcher().dispatchKnownCustomEmojiChangeNameEvent((DispatchQueueSelector)server, emoji, (Server)server, (KnownCustomEmojiChangeNameEvent)event);
                    }
                    Set<Role> oldWhitelist = emoji.getWhitelistedRoles().orElse(Collections.emptySet());
                    JsonNode newWhitelistJson = value.get("roles");
                    HashSet<Role> newWhitelist = new HashSet<Role>();
                    if (newWhitelistJson != null && !newWhitelistJson.isNull()) {
                        for (JsonNode role : newWhitelistJson) {
                            server.getRoleById(role.asLong()).ifPresent(newWhitelist::add);
                        }
                    }
                    if (!newWhitelist.containsAll(oldWhitelist) || !oldWhitelist.containsAll(newWhitelist)) {
                        KnownCustomEmojiChangeWhitelistedRolesEventImpl event = new KnownCustomEmojiChangeWhitelistedRolesEventImpl(emoji, newWhitelist, oldWhitelist);
                        ((KnownCustomEmojiImpl)emoji).setWhitelist(newWhitelist);
                        this.api.getEventDispatcher().dispatchKnownCustomEmojiChangeWhitelistedRolesEvent((DispatchQueueSelector)server, emoji, (Server)server, (KnownCustomEmojiChangeWhitelistedRolesEvent)event);
                    }
                } else {
                    KnownCustomEmoji emoji = this.api.getOrCreateKnownCustomEmoji((Server)server, (JsonNode)value);
                    server.addCustomEmoji(emoji);
                    KnownCustomEmojiCreateEventImpl event = new KnownCustomEmojiCreateEventImpl(emoji);
                    this.api.getEventDispatcher().dispatchKnownCustomEmojiCreateEvent((DispatchQueueSelector)server, (Server)server, (KnownCustomEmojiCreateEvent)event);
                }
            });
            Set emojiIds = emojis.keySet();
            Set<KnownCustomEmoji> emojiToDelete = server.getCustomEmojis().stream().filter(emoji -> !emojiIds.contains(emoji.getId())).collect(Collectors.toSet());
            emojiToDelete.forEach(emoji -> {
                this.api.removeCustomEmoji((KnownCustomEmoji)emoji);
                server.removeCustomEmoji((KnownCustomEmoji)emoji);
                KnownCustomEmojiDeleteEventImpl event = new KnownCustomEmojiDeleteEventImpl((KnownCustomEmoji)emoji);
                this.api.getEventDispatcher().dispatchKnownCustomEmojiDeleteEvent((DispatchQueueSelector)server, (KnownCustomEmoji)emoji, (Server)server, (KnownCustomEmojiDeleteEvent)event);
            });
        });
    }
}

