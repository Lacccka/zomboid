/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.event.user.UserChangeAvatarEvent;
import org.javacord.api.event.user.UserChangeDiscriminatorEvent;
import org.javacord.api.event.user.UserChangeNameEvent;
import org.javacord.api.event.user.UserChangeNicknameEvent;
import org.javacord.api.event.user.UserChangePendingEvent;
import org.javacord.api.event.user.UserChangeServerAvatarEvent;
import org.javacord.api.event.user.UserChangeTimeoutEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.server.role.UserRoleAddEventImpl;
import org.javacord.core.event.server.role.UserRoleRemoveEventImpl;
import org.javacord.core.event.user.ServerUserEventImpl;
import org.javacord.core.event.user.UserChangeAvatarEventImpl;
import org.javacord.core.event.user.UserChangeDiscriminatorEventImpl;
import org.javacord.core.event.user.UserChangeNameEventImpl;
import org.javacord.core.event.user.UserChangeNicknameEventImpl;
import org.javacord.core.event.user.UserChangePendingEventImpl;
import org.javacord.core.event.user.UserChangeServerAvatarEventImpl;
import org.javacord.core.event.user.UserChangeTimeoutEventImpl;
import org.javacord.core.util.cache.MessageCacheImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class GuildMemberUpdateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(GuildMemberUpdateHandler.class);

    public GuildMemberUpdateHandler(DiscordApi api) {
        super(api, true, "GUILD_MEMBER_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        this.api.getPossiblyUnreadyServerById(packet.get("guild_id").asLong()).map(server -> (ServerImpl)server).ifPresent(server -> {
            ServerUserEventImpl event;
            long userId = packet.get("user").get("id").asLong();
            boolean selfMuted = server.isSelfMuted(userId);
            boolean selfDeafened = server.isSelfDeafened(userId);
            MemberImpl newMember = new MemberImpl(this.api, (ServerImpl)server, packet, null).setSelfMuted(selfMuted).setSelfDeafened(selfDeafened);
            Member oldMember = server.getRealMemberById(userId).orElse(null);
            this.api.addMemberToCacheOrReplaceExisting(newMember);
            if (oldMember == null) {
                return;
            }
            if (!newMember.getNickname().equals(oldMember.getNickname())) {
                event = new UserChangeNicknameEventImpl(newMember, oldMember);
                this.api.getEventDispatcher().dispatchUserChangeNicknameEvent((DispatchQueueSelector)server, (Server)server, newMember.getUser(), (UserChangeNicknameEvent)((Object)event));
            }
            if (!newMember.getTimeout().equals(oldMember.getTimeout())) {
                event = new UserChangeTimeoutEventImpl(newMember, oldMember);
                this.api.getEventDispatcher().dispatchUserChangeTimeoutEvent((DispatchQueueSelector)server, (Server)server, newMember.getUser(), (UserChangeTimeoutEvent)((Object)event));
            }
            if (!newMember.getServerAvatarHash().equals(oldMember.getServerAvatarHash())) {
                event = new UserChangeServerAvatarEventImpl(newMember, oldMember);
                this.api.getEventDispatcher().dispatchUserChangeServerAvatarEvent((DispatchQueueSelector)server, (Server)server, newMember.getUser(), (UserChangeServerAvatarEvent)((Object)event));
            }
            if (newMember.isPending() != oldMember.isPending()) {
                event = new UserChangePendingEventImpl(oldMember, newMember);
                this.api.getEventDispatcher().dispatchUserChangePendingEvent((DispatchQueueSelector)server, (Server)server, newMember.getUser(), (UserChangePendingEvent)((Object)event));
            }
            if (packet.has("roles")) {
                JsonNode jsonRoles = packet.get("roles");
                HashSet newRoles = new HashSet();
                List<Role> oldRoles = oldMember.getRoles();
                HashSet intersection = new HashSet();
                for (Object roleIdJson : jsonRoles) {
                    this.api.getRoleById(((JsonNode)roleIdJson).asText()).map(role -> {
                        newRoles.add(role);
                        return role;
                    }).filter(oldRoles::contains).ifPresent(intersection::add);
                }
                ArrayList addedRoles = new ArrayList(newRoles);
                addedRoles.removeAll(intersection);
                for (Role role2 : addedRoles) {
                    if (role2.isEveryoneRole()) continue;
                    UserRoleAddEventImpl event2 = new UserRoleAddEventImpl(role2, newMember);
                    this.api.getEventDispatcher().dispatchUserRoleAddEvent((DispatchQueueSelector)((Object)role2.getServer()), role2, role2.getServer(), newMember.getId(), (UserRoleAddEvent)event2);
                }
                ArrayList<Role> removedRoles = new ArrayList<Role>(oldRoles);
                removedRoles.removeAll(intersection);
                for (Role role3 : removedRoles) {
                    if (role3.isEveryoneRole()) continue;
                    UserRoleRemoveEventImpl event3 = new UserRoleRemoveEventImpl(role3, newMember);
                    this.api.getEventDispatcher().dispatchUserRoleRemoveEvent((DispatchQueueSelector)((Object)role3.getServer()), role3, role3.getServer(), newMember.getId(), (UserRoleRemoveEvent)event3);
                }
            }
            if (newMember.getUser().isYourself()) {
                Set unreadableChannels = server.getTextChannels().stream().filter(((Predicate<ServerTextChannel>)Channel::canYouSee).negate()).map(DiscordEntity::getId).collect(Collectors.toSet());
                this.api.forEachCachedMessageWhere(msg -> unreadableChannels.contains(msg.getChannel().getId()), msg -> {
                    this.api.removeMessageFromCache(msg.getId());
                    ((MessageCacheImpl)msg.getChannel().getMessageCache()).removeMessage((Message)msg);
                });
            }
            if (oldMember.getUser() != null) {
                String oldAvatarHash;
                String newAvatarHash;
                UserImpl oldUser = (UserImpl)oldMember.getUser();
                boolean userChanged = false;
                UserImpl updatedUser = oldUser.replacePartialUserData(packet.get("user"));
                if (packet.get("user").has("username")) {
                    String newName = packet.get("user").get("username").asText();
                    String oldName = oldUser.getName();
                    if (!oldName.equals(newName)) {
                        this.dispatchUserChangeNameEvent(updatedUser, newName, oldName);
                        userChanged = true;
                    }
                }
                if (packet.get("user").has("discriminator")) {
                    String newDiscriminator = packet.get("user").get("discriminator").asText();
                    String oldDiscriminator = oldUser.getDiscriminator();
                    if (!oldDiscriminator.equals(newDiscriminator)) {
                        this.dispatchUserChangeDiscriminatorEvent(updatedUser, newDiscriminator, oldDiscriminator);
                        userChanged = true;
                    }
                }
                if (packet.get("user").has("avatar") && !Objects.deepEquals(newAvatarHash = packet.get("user").get("avatar").asText(null), oldAvatarHash = (String)oldUser.getAvatarHash().orElse(null))) {
                    this.dispatchUserChangeAvatarEvent(updatedUser, newAvatarHash, oldAvatarHash);
                    userChanged = true;
                }
                if (userChanged) {
                    this.api.updateUserOfAllMembers(updatedUser);
                }
            }
        });
    }

    private void dispatchUserChangeNameEvent(User user, String newName, String oldName) {
        UserChangeNameEventImpl event = new UserChangeNameEventImpl(user, newName, oldName);
        this.api.getEventDispatcher().dispatchUserChangeNameEvent((DispatchQueueSelector)this.api, user.getMutualServers(), Collections.singleton(user), (UserChangeNameEvent)event);
    }

    private void dispatchUserChangeDiscriminatorEvent(User user, String newDiscriminator, String oldDiscriminator) {
        UserChangeDiscriminatorEventImpl event = new UserChangeDiscriminatorEventImpl(user, newDiscriminator, oldDiscriminator);
        this.api.getEventDispatcher().dispatchUserChangeDiscriminatorEvent((DispatchQueueSelector)this.api, user.getMutualServers(), Collections.singleton(user), (UserChangeDiscriminatorEvent)event);
    }

    private void dispatchUserChangeAvatarEvent(User user, String newAvatarHash, String oldAvatarHash) {
        UserChangeAvatarEventImpl event = new UserChangeAvatarEventImpl(user, newAvatarHash, oldAvatarHash);
        this.api.getEventDispatcher().dispatchUserChangeAvatarEvent((DispatchQueueSelector)this.api, user.getMutualServers(), Collections.singleton(user), (UserChangeAvatarEvent)event);
    }
}

