/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild.role;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Color;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.role.RoleChangeColorEvent;
import org.javacord.api.event.server.role.RoleChangeHoistEvent;
import org.javacord.api.event.server.role.RoleChangeMentionableEvent;
import org.javacord.api.event.server.role.RoleChangeNameEvent;
import org.javacord.api.event.server.role.RoleChangePermissionsEvent;
import org.javacord.api.event.server.role.RoleChangePositionEvent;
import org.javacord.core.entity.permission.PermissionsImpl;
import org.javacord.core.entity.permission.RoleImpl;
import org.javacord.core.event.server.role.RoleChangeColorEventImpl;
import org.javacord.core.event.server.role.RoleChangeHoistEventImpl;
import org.javacord.core.event.server.role.RoleChangeMentionableEventImpl;
import org.javacord.core.event.server.role.RoleChangeNameEventImpl;
import org.javacord.core.event.server.role.RoleChangePermissionsEventImpl;
import org.javacord.core.event.server.role.RoleChangePositionEventImpl;
import org.javacord.core.util.cache.MessageCacheImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildRoleUpdateHandler
extends PacketHandler {
    public GuildRoleUpdateHandler(DiscordApi api) {
        super(api, true, "GUILD_ROLE_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        JsonNode roleJson = packet.get("role");
        long roleId = roleJson.get("id").asLong();
        this.api.getRoleById(roleId).map(role -> (RoleImpl)role).ifPresent(role -> {
            int newRawPosition;
            PermissionsImpl newPermissions;
            Permissions oldPermissions;
            String newName;
            String oldName;
            boolean newMentionable;
            boolean oldMentionable;
            boolean newHoist;
            boolean oldHoist;
            int newColor;
            Color oldColorObject = role.getColor().orElse(null);
            int oldColor = role.getColorAsInt();
            if (oldColor != (newColor = roleJson.get("color").asInt(0))) {
                role.setColor(newColor);
                RoleChangeColorEventImpl event = new RoleChangeColorEventImpl((Role)role, role.getColor().orElse(null), oldColorObject);
                this.api.getEventDispatcher().dispatchRoleChangeColorEvent((DispatchQueueSelector)((Object)role.getServer()), (Role)role, role.getServer(), (RoleChangeColorEvent)event);
            }
            if ((oldHoist = role.isDisplayedSeparately()) != (newHoist = roleJson.get("hoist").asBoolean(false))) {
                role.setHoist(newHoist);
                RoleChangeHoistEventImpl event = new RoleChangeHoistEventImpl((Role)role, oldHoist);
                this.api.getEventDispatcher().dispatchRoleChangeHoistEvent((DispatchQueueSelector)((Object)role.getServer()), (Role)role, role.getServer(), (RoleChangeHoistEvent)event);
            }
            if ((oldMentionable = role.isMentionable()) != (newMentionable = roleJson.get("mentionable").asBoolean(false))) {
                role.setMentionable(newMentionable);
                RoleChangeMentionableEventImpl event = new RoleChangeMentionableEventImpl((Role)role, oldMentionable);
                this.api.getEventDispatcher().dispatchRoleChangeMentionableEvent((DispatchQueueSelector)((Object)role.getServer()), (Role)role, role.getServer(), (RoleChangeMentionableEvent)event);
            }
            if (!(oldName = role.getName()).equals(newName = roleJson.get("name").asText())) {
                role.setName(newName);
                RoleChangeNameEventImpl event = new RoleChangeNameEventImpl((Role)role, newName, oldName);
                this.api.getEventDispatcher().dispatchRoleChangeNameEvent((DispatchQueueSelector)((Object)role.getServer()), (Role)role, role.getServer(), (RoleChangeNameEvent)event);
            }
            if (!(oldPermissions = role.getPermissions()).equals(newPermissions = new PermissionsImpl(roleJson.get("permissions").asLong(), 0L))) {
                role.setPermissions(newPermissions);
                RoleChangePermissionsEventImpl event = new RoleChangePermissionsEventImpl((Role)role, newPermissions, oldPermissions);
                this.api.getEventDispatcher().dispatchRoleChangePermissionsEvent((DispatchQueueSelector)((Object)role.getServer()), (Role)role, role.getServer(), (RoleChangePermissionsEvent)event);
                if (role.getUsers().stream().anyMatch(User::isYourself)) {
                    Set unreadableChannels = role.getServer().getTextChannels().stream().filter(((Predicate<ServerTextChannel>)Channel::canYouSee).negate()).map(DiscordEntity::getId).collect(Collectors.toSet());
                    this.api.forEachCachedMessageWhere(msg -> unreadableChannels.contains(msg.getChannel().getId()), msg -> {
                        this.api.removeMessageFromCache(msg.getId());
                        ((MessageCacheImpl)msg.getChannel().getMessageCache()).removeMessage((Message)msg);
                    });
                }
            }
            int oldPosition = role.getPosition();
            int oldRawPosition = role.getRawPosition();
            if (oldRawPosition != (newRawPosition = roleJson.get("position").asInt())) {
                role.setRawPosition(newRawPosition);
                int newPosition = role.getPosition();
                RoleChangePositionEventImpl event = new RoleChangePositionEventImpl((Role)role, newRawPosition, oldRawPosition, newPosition, oldPosition);
                this.api.getEventDispatcher().dispatchRoleChangePositionEvent((DispatchQueueSelector)((Object)role.getServer()), (Role)role, role.getServer(), (RoleChangePositionEvent)event);
            }
        });
    }
}

