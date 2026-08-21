/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.javacord.api.AccountUpdater;
import org.javacord.api.BotInviteBuilder;
import org.javacord.api.entity.ApplicationInfo;
import org.javacord.api.entity.ApplicationOwner;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.UncachedMessageUtil;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.ServerBuilder;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.sticker.StickerPack;
import org.javacord.api.entity.team.Team;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.MessageContextMenu;
import org.javacord.api.interaction.ServerApplicationCommandPermissions;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.UserContextMenu;
import org.javacord.api.listener.GloballyAttachableListenerManager;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.api.util.concurrent.ThreadPool;
import org.javacord.api.util.ratelimit.Ratelimiter;

public interface DiscordApi
extends GloballyAttachableListenerManager {
    public static final Pattern ESCAPED_CHARACTER = Pattern.compile("\\\\(?<char>[^a-zA-Z0-9\\p{javaWhitespace}\\xa0\\u2007\\u202E\\u202F])");

    public String getToken();

    public String getPrefixedToken();

    public Set<Intent> getIntents();

    public ThreadPool getThreadPool();

    public void setEventsDispatchable(boolean var1);

    public boolean canDispatchEvents();

    public CompletableFuture<Set<ApplicationCommand>> getGlobalApplicationCommands();

    public CompletableFuture<ApplicationCommand> getGlobalApplicationCommandById(long var1);

    public CompletableFuture<Set<ApplicationCommand>> getServerApplicationCommands(Server var1);

    public CompletableFuture<ApplicationCommand> getServerApplicationCommandById(Server var1, long var2);

    public CompletableFuture<Set<SlashCommand>> getGlobalSlashCommands();

    public CompletableFuture<SlashCommand> getGlobalSlashCommandById(long var1);

    public CompletableFuture<Set<SlashCommand>> getServerSlashCommands(Server var1);

    public CompletableFuture<SlashCommand> getServerSlashCommandById(Server var1, long var2);

    public CompletableFuture<Set<UserContextMenu>> getGlobalUserContextMenus();

    public CompletableFuture<UserContextMenu> getGlobalUserContextMenuById(long var1);

    public CompletableFuture<Set<UserContextMenu>> getServerUserContextMenus(Server var1);

    public CompletableFuture<UserContextMenu> getServerUserContextMenuById(Server var1, long var2);

    public CompletableFuture<Set<MessageContextMenu>> getGlobalMessageContextMenus();

    public CompletableFuture<MessageContextMenu> getGlobalMessageContextMenuById(long var1);

    public CompletableFuture<Set<MessageContextMenu>> getServerMessageContextMenus(Server var1);

    public CompletableFuture<MessageContextMenu> getServerMessageContextMenuById(Server var1, long var2);

    public CompletableFuture<Set<ServerApplicationCommandPermissions>> getServerApplicationCommandPermissions(Server var1);

    public CompletableFuture<ServerApplicationCommandPermissions> getServerApplicationCommandPermissionsById(Server var1, long var2);

    public CompletableFuture<Set<ApplicationCommand>> bulkOverwriteGlobalApplicationCommands(Set<? extends ApplicationCommandBuilder<?, ?, ?>> var1);

    default public CompletableFuture<Set<ApplicationCommand>> bulkOverwriteServerApplicationCommands(Server server, Set<? extends ApplicationCommandBuilder<?, ?, ?>> applicationCommandBuilderList) {
        return this.bulkOverwriteServerApplicationCommands(server.getId(), applicationCommandBuilderList);
    }

    public CompletableFuture<Set<ApplicationCommand>> bulkOverwriteServerApplicationCommands(long var1, Set<? extends ApplicationCommandBuilder<?, ?, ?>> var3);

    public UncachedMessageUtil getUncachedMessageUtil();

    public Optional<Ratelimiter> getGlobalRatelimiter();

    public Ratelimiter getGatewayIdentifyRatelimiter();

    public Duration getLatestGatewayLatency();

    public CompletableFuture<Duration> measureRestLatency();

    default public String createBotInvite() {
        return new BotInviteBuilder(this.getClientId()).build();
    }

    default public String createBotInvite(Permissions permissions) {
        return new BotInviteBuilder(this.getClientId()).setPermissions(permissions).build();
    }

    public void setMessageCacheSize(int var1, int var2);

    public int getDefaultMessageCacheCapacity();

    public int getDefaultMessageCacheStorageTimeInSeconds();

    public void setAutomaticMessageCacheCleanupEnabled(boolean var1);

    public boolean isDefaultAutomaticMessageCacheCleanupEnabled();

    public int getCurrentShard();

    public int getTotalShards();

    public boolean isWaitingForServersOnStartup();

    public boolean isWaitingForUsersOnStartup();

    public void updateStatus(UserStatus var1);

    public UserStatus getStatus();

    public void updateActivity(String var1);

    public void updateActivity(ActivityType var1, String var2);

    public void updateActivity(String var1, String var2);

    public void unsetActivity();

    public Optional<Activity> getActivity();

    public User getYourself();

    default public Optional<Long> getOwnerId() {
        ApplicationInfo applicationInfo = this.getCachedApplicationInfo();
        return applicationInfo.getOwner().map(DiscordEntity::getId);
    }

    default public Optional<CompletableFuture<User>> getOwner() {
        return this.getCachedApplicationInfo().getOwner().map(ApplicationOwner::requestUser);
    }

    default public Optional<Team> getCachedTeam() {
        return this.getCachedApplicationInfo().getTeam();
    }

    default public CompletableFuture<Optional<Team>> requestTeam() {
        return this.requestApplicationInfo().thenApply(ApplicationInfo::getTeam);
    }

    default public long getClientId() {
        return this.getCachedApplicationInfo().getClientId();
    }

    public CompletableFuture<Void> disconnect();

    public void setReconnectDelay(Function<Integer, Integer> var1);

    public int getReconnectDelay(int var1);

    public ApplicationInfo getCachedApplicationInfo();

    public CompletableFuture<ApplicationInfo> requestApplicationInfo();

    public CompletableFuture<Webhook> getWebhookById(long var1);

    public CompletableFuture<IncomingWebhook> getIncomingWebhookByIdAndToken(String var1, String var2);

    default public CompletableFuture<IncomingWebhook> getIncomingWebhookByIdAndToken(long id, String token) {
        return this.getIncomingWebhookByIdAndToken(Long.toUnsignedString(id), token);
    }

    default public CompletableFuture<IncomingWebhook> getIncomingWebhookByUrl(String url) throws IllegalArgumentException {
        Matcher matcher = DiscordRegexPattern.WEBHOOK_URL.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The webhook url has an invalid format");
        }
        return this.getIncomingWebhookByIdAndToken(matcher.group("id"), matcher.group("token"));
    }

    public Set<Long> getUnavailableServers();

    public CompletableFuture<Invite> getInviteByCode(String var1);

    public CompletableFuture<Invite> getInviteWithMemberCountsByCode(String var1);

    default public ServerBuilder createServerBuilder() {
        return new ServerBuilder(this);
    }

    default public AccountUpdater createAccountUpdater() {
        return new AccountUpdater(this);
    }

    default public CompletableFuture<Void> updateUsername(String username) {
        return this.createAccountUpdater().setUsername(username).update();
    }

    default public CompletableFuture<Void> updateAvatar(BufferedImage avatar) {
        return this.createAccountUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Void> updateAvatar(BufferedImage avatar, String fileType) {
        return this.createAccountUpdater().setAvatar(avatar, fileType).update();
    }

    default public CompletableFuture<Void> updateAvatar(File avatar) {
        return this.createAccountUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Void> updateAvatar(Icon avatar) {
        return this.createAccountUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Void> updateAvatar(URL avatar) {
        return this.createAccountUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Void> updateAvatar(byte[] avatar) {
        return this.createAccountUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Void> updateAvatar(byte[] avatar, String fileType) {
        return this.createAccountUpdater().setAvatar(avatar, fileType).update();
    }

    default public CompletableFuture<Void> updateAvatar(InputStream avatar) {
        return this.createAccountUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Void> updateAvatar(InputStream avatar, String fileType) {
        return this.createAccountUpdater().setAvatar(avatar, fileType).update();
    }

    public boolean isUserCacheEnabled();

    default public boolean hasAllUsersInCache() {
        return this.getServers().stream().allMatch(Server::hasAllMembersInCache);
    }

    public Set<User> getCachedUsers();

    public Optional<User> getCachedUserById(long var1);

    default public Optional<User> getCachedUserById(String id) {
        try {
            return this.getCachedUserById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public CompletableFuture<User> getUserById(long var1);

    default public CompletableFuture<User> getUserById(String id) {
        try {
            return this.getUserById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            CompletableFuture<User> future = new CompletableFuture<User>();
            future.completeExceptionally(e);
            return future;
        }
    }

    default public String makeMentionsReadable(String content, Server server) {
        Matcher userMention = DiscordRegexPattern.USER_MENTION.matcher(content);
        while (userMention.find()) {
            String userId = userMention.group("id");
            Optional<User> userOptional = this.getCachedUserById(userId);
            if (!userOptional.isPresent()) continue;
            User user = userOptional.get();
            String userName = server == null ? user.getName() : server.getDisplayName(user);
            content = userMention.replaceFirst(Matcher.quoteReplacement("@" + userName));
            userMention.reset(content);
        }
        Matcher roleMention = DiscordRegexPattern.ROLE_MENTION.matcher(content);
        while (roleMention.find()) {
            String roleName = this.getRoleById(roleMention.group("id")).map(Nameable::getName).orElse("deleted-role");
            content = roleMention.replaceFirst(Matcher.quoteReplacement("@" + roleName));
            roleMention.reset(content);
        }
        Matcher channelMention = DiscordRegexPattern.CHANNEL_MENTION.matcher(content);
        while (channelMention.find()) {
            String channelId = channelMention.group("id");
            String channelName = this.getServerChannelById(channelId).map(Nameable::getName).orElse("deleted-channel");
            content = channelMention.replaceFirst("#" + channelName);
            channelMention.reset(content);
        }
        Matcher customEmoji = DiscordRegexPattern.CUSTOM_EMOJI.matcher(content);
        while (customEmoji.find()) {
            String emojiId = customEmoji.group("id");
            String name = this.getCustomEmojiById(emojiId).map(Nameable::getName).orElseGet(() -> customEmoji.group("name"));
            content = customEmoji.replaceFirst(":" + name + ":");
            customEmoji.reset(content);
        }
        return ESCAPED_CHARACTER.matcher(content).replaceAll("${char}");
    }

    default public String makeMentionsReadable(String content) {
        return this.makeMentionsReadable(content, null);
    }

    default public Optional<User> getCachedUserByDiscriminatedName(String discriminatedName) {
        String[] nameAndDiscriminator = discriminatedName.split("#", 2);
        return this.getCachedUserByNameAndDiscriminator(nameAndDiscriminator[0], nameAndDiscriminator[1]);
    }

    default public Optional<User> getCachedUserByDiscriminatedNameIgnoreCase(String discriminatedName) {
        String[] nameAndDiscriminator = discriminatedName.split("#", 2);
        return this.getCachedUserByNameAndDiscriminatorIgnoreCase(nameAndDiscriminator[0], nameAndDiscriminator[1]);
    }

    default public Optional<User> getCachedUserByNameAndDiscriminator(String name, String discriminator) {
        return this.getCachedUsersByName(name).stream().filter(user -> user.getDiscriminator().equals(discriminator)).findAny();
    }

    default public Optional<User> getCachedUserByNameAndDiscriminatorIgnoreCase(String name, String discriminator) {
        return this.getCachedUsersByNameIgnoreCase(name).stream().filter(user -> user.getDiscriminator().equalsIgnoreCase(discriminator)).findAny();
    }

    default public Set<User> getCachedUsersByName(String name) {
        return Collections.unmodifiableSet(this.getCachedUsers().stream().filter(user -> user.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<User> getCachedUsersByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getCachedUsers().stream().filter(user -> user.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Set<User> getCachedUsersByNickname(String nickname, Server server) {
        return Collections.unmodifiableSet(this.getCachedUsers().stream().filter(user -> user.getNickname(server).map(nickname::equals).orElse(false)).collect(Collectors.toSet()));
    }

    default public Set<User> getCachedUsersByNicknameIgnoreCase(String nickname, Server server) {
        return Collections.unmodifiableSet(this.getCachedUsers().stream().filter(user -> user.getNickname(server).map(nickname::equalsIgnoreCase).orElse(false)).collect(Collectors.toSet()));
    }

    default public Set<User> getCachedUsersByDisplayName(String displayName, Server server) {
        return Collections.unmodifiableSet(this.getCachedUsers().stream().filter(user -> user.getDisplayName(server).equals(displayName)).collect(Collectors.toSet()));
    }

    default public Set<User> getCachedUsersByDisplayNameIgnoreCase(String displayName, Server server) {
        return Collections.unmodifiableSet(this.getCachedUsers().stream().filter(user -> user.getDisplayName(server).equalsIgnoreCase(displayName)).collect(Collectors.toSet()));
    }

    public MessageSet getCachedMessages();

    public Optional<Message> getCachedMessageById(long var1);

    default public Optional<Message> getCachedMessageById(String id) {
        try {
            return this.getCachedMessageById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public CompletableFuture<Message> getMessageById(long id, TextChannel channel) {
        return channel.getMessageById(id);
    }

    default public CompletableFuture<Message> getMessageById(String id, TextChannel channel) {
        return channel.getMessageById(id);
    }

    default public Optional<CompletableFuture<Message>> getMessageByLink(String link) throws IllegalArgumentException {
        Matcher matcher = DiscordRegexPattern.MESSAGE_LINK.matcher(link);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The message link has an invalid format");
        }
        return this.getTextChannelById(matcher.group("channel")).map(textChannel -> textChannel.getMessageById(matcher.group("message")));
    }

    default public Optional<Message> getCachedMessageByLink(String link) throws IllegalArgumentException {
        Matcher matcher = DiscordRegexPattern.MESSAGE_LINK.matcher(link);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The message link has an invalid format");
        }
        return this.getCachedMessageById(matcher.group("message")).filter(message -> message.getChannel().getIdAsString().equals(matcher.group("channel")));
    }

    public Set<Server> getServers();

    default public Optional<Server> getServerById(long id) {
        return this.getServers().stream().filter(server -> server.getId() == id).findAny();
    }

    default public Optional<Server> getServerById(String id) {
        try {
            return this.getServerById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<Server> getServersByName(String name) {
        return Collections.unmodifiableSet(this.getServers().stream().filter(server -> server.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<Server> getServersByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServers().stream().filter(server -> server.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    public Set<KnownCustomEmoji> getCustomEmojis();

    default public Optional<KnownCustomEmoji> getCustomEmojiById(long id) {
        return this.getCustomEmojis().stream().filter(emoji -> emoji.getId() == id).findAny();
    }

    default public Optional<KnownCustomEmoji> getCustomEmojiById(String id) {
        try {
            return this.getCustomEmojiById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<KnownCustomEmoji> getCustomEmojisByName(String name) {
        return Collections.unmodifiableSet(this.getCustomEmojis().stream().filter(emoji -> emoji.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<KnownCustomEmoji> getCustomEmojisByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getCustomEmojis().stream().filter(emoji -> emoji.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    public CustomEmoji getKnownCustomEmojiOrCreateCustomEmoji(long var1, String var3, boolean var4);

    public CompletableFuture<Set<StickerPack>> getNitroStickerPacks();

    public Optional<Sticker> getStickerById(long var1);

    default public Optional<Sticker> getStickerById(String id) {
        try {
            return this.getStickerById(Long.parseLong(id));
        }
        catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    public CompletableFuture<Sticker> requestStickerById(long var1);

    default public CompletableFuture<Sticker> requestStickerById(String id) {
        try {
            return this.requestStickerById(Long.parseLong(id));
        }
        catch (NumberFormatException exception) {
            CompletableFuture<Sticker> future = new CompletableFuture<Sticker>();
            future.completeExceptionally(exception);
            return future;
        }
    }

    default public Set<Role> getRoles() {
        HashSet roles = new HashSet();
        this.getServers().stream().map(Server::getRoles).forEach(roles::addAll);
        return Collections.unmodifiableSet(roles);
    }

    default public Optional<Role> getRoleById(long id) {
        return this.getServers().stream().map(server -> server.getRoleById(id)).filter(Optional::isPresent).map(Optional::get).findAny();
    }

    default public Optional<Role> getRoleById(String id) {
        try {
            return this.getRoleById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<Role> getRolesByName(String name) {
        return Collections.unmodifiableSet(this.getRoles().stream().filter(role -> role.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<Role> getRolesByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getRoles().stream().filter(role -> role.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    public Set<Channel> getChannels();

    public Set<PrivateChannel> getPrivateChannels();

    public Set<ServerChannel> getServerChannels();

    public Set<RegularServerChannel> getRegularServerChannels();

    public Set<ChannelCategory> getChannelCategories();

    public Set<ServerTextChannel> getServerTextChannels();

    public Set<ServerForumChannel> getServerForumChannels();

    public Set<ServerThreadChannel> getServerThreadChannels();

    public Set<ServerThreadChannel> getPrivateServerThreadChannels();

    public Set<ServerThreadChannel> getPublicServerThreadChannels();

    public Set<ServerVoiceChannel> getServerVoiceChannels();

    public Set<ServerStageVoiceChannel> getServerStageVoiceChannels();

    public Set<TextChannel> getTextChannels();

    public Set<VoiceChannel> getVoiceChannels();

    public Optional<Channel> getChannelById(long var1);

    default public Optional<Channel> getChannelById(String id) {
        try {
            return this.getChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<Channel> getChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerChannelsByName(name));
    }

    default public Set<Channel> getChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerChannelsByNameIgnoreCase(name));
    }

    default public Optional<TextChannel> getTextChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asTextChannel);
    }

    default public Optional<TextChannel> getTextChannelById(String id) {
        try {
            return this.getTextChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<TextChannel> getTextChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerTextChannelsByName(name));
    }

    default public Set<TextChannel> getTextChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerTextChannelsByNameIgnoreCase(name));
    }

    default public Optional<VoiceChannel> getVoiceChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asVoiceChannel);
    }

    default public Optional<VoiceChannel> getVoiceChannelById(String id) {
        try {
            return this.getVoiceChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<VoiceChannel> getVoiceChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerVoiceChannelsByName(name));
    }

    default public Set<VoiceChannel> getVoiceChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerVoiceChannelsByNameIgnoreCase(name));
    }

    default public Optional<ServerChannel> getServerChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asServerChannel);
    }

    default public Optional<ServerChannel> getServerChannelById(String id) {
        try {
            return this.getServerChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<ServerChannel> getServerChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<ServerChannel> getServerChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Optional<RegularServerChannel> getRegularServerChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asRegularServerChannel);
    }

    default public Optional<RegularServerChannel> getRegularServerChannelById(String id) {
        try {
            return this.getRegularServerChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<RegularServerChannel> getRegularServerChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getRegularServerChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<RegularServerChannel> getRegularServerChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getRegularServerChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Optional<ChannelCategory> getChannelCategoryById(long id) {
        return this.getChannelById(id).flatMap(Channel::asChannelCategory);
    }

    default public Optional<ChannelCategory> getChannelCategoryById(String id) {
        try {
            return this.getChannelCategoryById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<ChannelCategory> getChannelCategoriesByName(String name) {
        return Collections.unmodifiableSet(this.getChannelCategories().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<ChannelCategory> getChannelCategoriesByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getChannelCategories().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Optional<ServerTextChannel> getServerTextChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asServerTextChannel);
    }

    default public Optional<ServerTextChannel> getServerTextChannelById(String id) {
        try {
            return this.getServerTextChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<ServerTextChannel> getServerTextChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerTextChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<ServerTextChannel> getServerTextChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerTextChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Optional<ServerForumChannel> getServerForumChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asServerForumChannel);
    }

    default public Optional<ServerForumChannel> getServerForumChannelById(String id) {
        try {
            return this.getServerForumChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<ServerForumChannel> getServerForumChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerForumChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<ServerForumChannel> getServerForumChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerForumChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Optional<ServerThreadChannel> getServerThreadChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asServerThreadChannel);
    }

    default public Optional<ServerThreadChannel> getServerThreadChannelById(String id) {
        try {
            return this.getServerThreadChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<ServerThreadChannel> getServerThreadChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerThreadChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<ServerThreadChannel> getServerThreadChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerThreadChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Optional<ServerVoiceChannel> getServerVoiceChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asServerVoiceChannel);
    }

    default public Optional<ServerVoiceChannel> getServerVoiceChannelById(String id) {
        try {
            return this.getServerVoiceChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<ServerVoiceChannel> getServerVoiceChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerVoiceChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<ServerVoiceChannel> getServerVoiceChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerVoiceChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Optional<ServerStageVoiceChannel> getServerStageVoiceChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asServerStageVoiceChannel);
    }

    default public Optional<ServerStageVoiceChannel> getServerStageVoiceChannelById(String id) {
        try {
            return this.getServerStageVoiceChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    default public Set<ServerStageVoiceChannel> getServerStageVoiceChannelsByName(String name) {
        return Collections.unmodifiableSet(this.getServerStageVoiceChannels().stream().filter(channel -> channel.getName().equals(name)).collect(Collectors.toSet()));
    }

    default public Set<ServerStageVoiceChannel> getServerStageVoiceChannelsByNameIgnoreCase(String name) {
        return Collections.unmodifiableSet(this.getServerStageVoiceChannels().stream().filter(channel -> channel.getName().equalsIgnoreCase(name)).collect(Collectors.toSet()));
    }

    default public Optional<PrivateChannel> getPrivateChannelById(long id) {
        return this.getChannelById(id).flatMap(Channel::asPrivateChannel);
    }

    default public Optional<PrivateChannel> getPrivateChannelById(String id) {
        try {
            return this.getPrivateChannelById(Long.parseLong(id));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}

