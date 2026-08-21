/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.VanityUrlCode;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.BoostLevel;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.MultiFactorAuthenticationLevel;
import org.javacord.api.entity.server.NsfwLevel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.ServerFeature;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.event.server.ServerChangeAfkChannelEvent;
import org.javacord.api.event.server.ServerChangeAfkTimeoutEvent;
import org.javacord.api.event.server.ServerChangeBoostCountEvent;
import org.javacord.api.event.server.ServerChangeBoostLevelEvent;
import org.javacord.api.event.server.ServerChangeDefaultMessageNotificationLevelEvent;
import org.javacord.api.event.server.ServerChangeDescriptionEvent;
import org.javacord.api.event.server.ServerChangeDiscoverySplashEvent;
import org.javacord.api.event.server.ServerChangeExplicitContentFilterLevelEvent;
import org.javacord.api.event.server.ServerChangeIconEvent;
import org.javacord.api.event.server.ServerChangeModeratorsOnlyChannelEvent;
import org.javacord.api.event.server.ServerChangeMultiFactorAuthenticationLevelEvent;
import org.javacord.api.event.server.ServerChangeNameEvent;
import org.javacord.api.event.server.ServerChangeNsfwLevelEvent;
import org.javacord.api.event.server.ServerChangeOwnerEvent;
import org.javacord.api.event.server.ServerChangePreferredLocaleEvent;
import org.javacord.api.event.server.ServerChangeRegionEvent;
import org.javacord.api.event.server.ServerChangeRulesChannelEvent;
import org.javacord.api.event.server.ServerChangeServerFeaturesEvent;
import org.javacord.api.event.server.ServerChangeSplashEvent;
import org.javacord.api.event.server.ServerChangeSystemChannelEvent;
import org.javacord.api.event.server.ServerChangeVanityUrlCodeEvent;
import org.javacord.api.event.server.ServerChangeVerificationLevelEvent;
import org.javacord.core.entity.VanityUrlCodeImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerChangeAfkChannelEventImpl;
import org.javacord.core.event.server.ServerChangeAfkTimeoutEventImpl;
import org.javacord.core.event.server.ServerChangeBoostCountEventImpl;
import org.javacord.core.event.server.ServerChangeBoostLevelEventImpl;
import org.javacord.core.event.server.ServerChangeDefaultMessageNotificationLevelEventImpl;
import org.javacord.core.event.server.ServerChangeDescriptionEventImpl;
import org.javacord.core.event.server.ServerChangeDiscoverySplashEventImpl;
import org.javacord.core.event.server.ServerChangeExplicitContentFilterLevelEventImpl;
import org.javacord.core.event.server.ServerChangeIconEventImpl;
import org.javacord.core.event.server.ServerChangeModeratorsOnlyChannelEventImpl;
import org.javacord.core.event.server.ServerChangeMultiFactorAuthenticationLevelEventImpl;
import org.javacord.core.event.server.ServerChangeNameEventImpl;
import org.javacord.core.event.server.ServerChangeNsfwLevelEventImpl;
import org.javacord.core.event.server.ServerChangeOwnerEventImpl;
import org.javacord.core.event.server.ServerChangePreferredLocaleEventImpl;
import org.javacord.core.event.server.ServerChangeRegionEventImpl;
import org.javacord.core.event.server.ServerChangeRulesChannelEventImpl;
import org.javacord.core.event.server.ServerChangeServerFeaturesEventImpl;
import org.javacord.core.event.server.ServerChangeSplashEventImpl;
import org.javacord.core.event.server.ServerChangeSystemChannelEventImpl;
import org.javacord.core.event.server.ServerChangeVanityUrlCodeEventImpl;
import org.javacord.core.event.server.ServerChangeVerificationLevelEventImpl;
import org.javacord.core.event.server.ServerEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class GuildUpdateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(GuildUpdateHandler.class);

    public GuildUpdateHandler(DiscordApi api) {
        super(api, true, "GUILD_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        if (packet.has("unavailable") && packet.get("unavailable").asBoolean()) {
            return;
        }
        long id = packet.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(id).map(server -> (ServerImpl)server).ifPresent(server -> {
            String newVanityCode;
            String newDescription;
            int newBoostCount;
            NsfwLevel newNsfwLevel;
            NsfwLevel oldNsfwLevel;
            BoostLevel newBoostLevel;
            BoostLevel oldBoostLevel;
            ServerEventImpl event;
            ServerEventImpl event2;
            long oldOwnerId;
            long newOwnerId;
            DefaultMessageNotificationLevel oldDefaultMessageNotificationLevel;
            DefaultMessageNotificationLevel newDefaultMessageNotificationLevel;
            VerificationLevel oldVerificationLevel;
            VerificationLevel newVerificationLevel;
            long newApplicationId;
            long oldApplicationId = server.getApplicationId().orElse(-1L);
            long l = newApplicationId = packet.hasNonNull("application_id") ? packet.get("application_id").asLong() : -1L;
            if (oldApplicationId != newApplicationId) {
                server.setApplicationId(newApplicationId);
            }
            String newName = packet.get("name").asText();
            String oldName = server.getName();
            if (!Objects.deepEquals(oldName, newName)) {
                server.setName(newName);
                ServerChangeNameEventImpl event3 = new ServerChangeNameEventImpl((Server)server, newName, oldName);
                this.api.getEventDispatcher().dispatchServerChangeNameEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeNameEvent)event3);
            }
            String newIconHash = packet.get("icon").asText(null);
            String oldIconHash = server.getIconHash();
            if (!Objects.deepEquals(oldIconHash, newIconHash)) {
                server.setIconHash(newIconHash);
                ServerChangeIconEventImpl event4 = new ServerChangeIconEventImpl((Server)server, newIconHash, oldIconHash);
                this.api.getEventDispatcher().dispatchServerChangeIconEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeIconEvent)event4);
            }
            String newSplashHash = packet.get("splash").asText(null);
            String oldSplashHash = server.getSplashHash();
            if (!Objects.deepEquals(oldSplashHash, newSplashHash)) {
                server.setSplashHash(newSplashHash);
                ServerChangeSplashEventImpl event5 = new ServerChangeSplashEventImpl((Server)server, newSplashHash, oldSplashHash);
                this.api.getEventDispatcher().dispatchServerChangeSplashEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeSplashEvent)event5);
            }
            if ((newVerificationLevel = VerificationLevel.fromId(packet.get("verification_level").asInt())) != (oldVerificationLevel = server.getVerificationLevel())) {
                server.setVerificationLevel(newVerificationLevel);
                ServerChangeVerificationLevelEventImpl event6 = new ServerChangeVerificationLevelEventImpl((Server)server, newVerificationLevel, oldVerificationLevel);
                this.api.getEventDispatcher().dispatchServerChangeVerificationLevelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeVerificationLevelEvent)event6);
            }
            Region newRegion = Region.getRegionByKey(packet.get("region").asText());
            Region oldRegion = server.getRegion();
            if (oldRegion != newRegion) {
                server.setRegion(newRegion);
                ServerChangeRegionEventImpl event7 = new ServerChangeRegionEventImpl((Server)server, newRegion, oldRegion);
                this.api.getEventDispatcher().dispatchServerChangeRegionEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeRegionEvent)event7);
            }
            if ((newDefaultMessageNotificationLevel = DefaultMessageNotificationLevel.fromId(packet.get("default_message_notifications").asInt())) != (oldDefaultMessageNotificationLevel = server.getDefaultMessageNotificationLevel())) {
                server.setDefaultMessageNotificationLevel(newDefaultMessageNotificationLevel);
                ServerChangeDefaultMessageNotificationLevelEventImpl event8 = new ServerChangeDefaultMessageNotificationLevelEventImpl((Server)server, newDefaultMessageNotificationLevel, oldDefaultMessageNotificationLevel);
                this.api.getEventDispatcher().dispatchServerChangeDefaultMessageNotificationLevelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeDefaultMessageNotificationLevelEvent)event8);
            }
            if ((newOwnerId = packet.get("owner_id").asLong()) != (oldOwnerId = server.getOwnerId())) {
                server.setOwnerId(newOwnerId);
                ServerChangeOwnerEventImpl event9 = new ServerChangeOwnerEventImpl((Server)server, newOwnerId, oldOwnerId);
                this.api.getEventDispatcher().dispatchServerChangeOwnerEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeOwnerEvent)event9);
            }
            if (packet.has("system_channel_id")) {
                ServerTextChannel newSystemChannel = packet.get("system_channel_id").isNull() ? null : (ServerTextChannel)server.getTextChannelById(packet.get("system_channel_id").asLong()).orElse(null);
                ServerTextChannel oldSystemChannel = server.getSystemChannel().orElse(null);
                if (oldSystemChannel != newSystemChannel) {
                    server.setSystemChannelId(newSystemChannel == null ? -1L : newSystemChannel.getId());
                    event2 = new ServerChangeSystemChannelEventImpl((Server)server, newSystemChannel, oldSystemChannel);
                    this.api.getEventDispatcher().dispatchServerChangeSystemChannelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeSystemChannelEvent)((Object)event2));
                }
            }
            if (packet.has("afk_channel_id")) {
                ServerVoiceChannel newAfkChannel = packet.get("afk_channel_id").isNull() ? null : (ServerVoiceChannel)server.getVoiceChannelById(packet.get("afk_channel_id").asLong()).orElse(null);
                ServerVoiceChannel oldAfkChannel = server.getAfkChannel().orElse(null);
                if (oldAfkChannel != newAfkChannel) {
                    server.setAfkChannelId(newAfkChannel == null ? -1L : newAfkChannel.getId());
                    event2 = new ServerChangeAfkChannelEventImpl((Server)server, newAfkChannel, oldAfkChannel);
                    this.api.getEventDispatcher().dispatchServerChangeAfkChannelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeAfkChannelEvent)((Object)event2));
                }
            }
            int newAfkTimeout = packet.get("afk_timeout").asInt();
            int oldAfkTimeout = server.getAfkTimeoutInSeconds();
            if (oldAfkTimeout != newAfkTimeout) {
                server.setAfkTimeout(newAfkTimeout);
                event2 = new ServerChangeAfkTimeoutEventImpl((Server)server, newAfkTimeout, oldAfkTimeout);
                this.api.getEventDispatcher().dispatchServerChangeAfkTimeoutEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeAfkTimeoutEvent)((Object)event2));
            }
            ExplicitContentFilterLevel newExplicitContentFilterLevel = ExplicitContentFilterLevel.fromId(packet.get("explicit_content_filter").asInt());
            ExplicitContentFilterLevel oldExplicitContentFilterLevel = server.getExplicitContentFilterLevel();
            if (oldExplicitContentFilterLevel != newExplicitContentFilterLevel) {
                server.setExplicitContentFilterLevel(newExplicitContentFilterLevel);
                ServerChangeExplicitContentFilterLevelEventImpl event10 = new ServerChangeExplicitContentFilterLevelEventImpl((Server)server, newExplicitContentFilterLevel, oldExplicitContentFilterLevel);
                this.api.getEventDispatcher().dispatchServerChangeExplicitContentFilterLevelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeExplicitContentFilterLevelEvent)event10);
            }
            MultiFactorAuthenticationLevel newMultiFactorAuthenticationLevel = MultiFactorAuthenticationLevel.fromId(packet.get("mfa_level").asInt());
            MultiFactorAuthenticationLevel oldMultiFactorAuthenticationLevel = server.getMultiFactorAuthenticationLevel();
            if (oldMultiFactorAuthenticationLevel != newMultiFactorAuthenticationLevel) {
                server.setMultiFactorAuthenticationLevel(newMultiFactorAuthenticationLevel);
                ServerChangeMultiFactorAuthenticationLevelEventImpl event11 = new ServerChangeMultiFactorAuthenticationLevelEventImpl((Server)server, newMultiFactorAuthenticationLevel, oldMultiFactorAuthenticationLevel);
                this.api.getEventDispatcher().dispatchServerChangeMultiFactorAuthenticationLevelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeMultiFactorAuthenticationLevelEvent)event11);
            }
            if (packet.has("rules_channel_id")) {
                ServerTextChannel newRulesChannel = packet.get("rules_channel_id").isNull() ? null : (ServerTextChannel)server.getTextChannelById(packet.get("rules_channel_id").asLong()).orElse(null);
                ServerTextChannel oldRulesChannel = server.getRulesChannel().orElse(null);
                if (oldRulesChannel != newRulesChannel) {
                    server.setRulesChannelId(newRulesChannel == null ? -1L : newRulesChannel.getId());
                    event = new ServerChangeRulesChannelEventImpl((ServerImpl)server, newRulesChannel, oldRulesChannel);
                    this.api.getEventDispatcher().dispatchServerChangeRulesChannelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeRulesChannelEvent)((Object)event));
                }
            }
            if (packet.has("public_updates_channel_id")) {
                ServerTextChannel newModeratorsOnlyChannel = packet.get("public_updates_channel_id").isNull() ? null : (ServerTextChannel)server.getTextChannelById(packet.get("public_updates_channel_id").asLong()).orElse(null);
                ServerTextChannel oldModeratorsOnlyChannel = server.getModeratorsOnlyChannel().orElse(null);
                if (oldModeratorsOnlyChannel != newModeratorsOnlyChannel) {
                    server.setModeratorsOnlyChannelId(newModeratorsOnlyChannel == null ? -1L : newModeratorsOnlyChannel.getId());
                    event = new ServerChangeModeratorsOnlyChannelEventImpl((ServerImpl)server, newModeratorsOnlyChannel, oldModeratorsOnlyChannel);
                    this.api.getEventDispatcher().dispatchServerChangeModeratorsOnlyChannelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeModeratorsOnlyChannelEvent)((Object)event));
                }
            }
            if ((oldBoostLevel = server.getBoostLevel()) != (newBoostLevel = BoostLevel.fromId(packet.get("premium_tier").asInt()))) {
                server.setBoostLevel(newBoostLevel);
                event = new ServerChangeBoostLevelEventImpl((ServerImpl)server, newBoostLevel, oldBoostLevel);
                this.api.getEventDispatcher().dispatchServerChangeBoostLevelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeBoostLevelEvent)((Object)event));
            }
            if ((oldNsfwLevel = server.getNsfwLevel()) != (newNsfwLevel = NsfwLevel.fromId(packet.get("nsfw_level").asInt()))) {
                server.setNsfwLevel(newNsfwLevel);
                ServerChangeNsfwLevelEventImpl event12 = new ServerChangeNsfwLevelEventImpl((ServerImpl)server, newNsfwLevel, oldNsfwLevel);
                this.api.getEventDispatcher().dispatchServerChangeNsfwLevelEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeNsfwLevelEvent)event12);
            }
            Locale newPreferredLocale = new Locale.Builder().setLanguageTag(packet.get("preferred_locale").asText()).build();
            Locale oldPreferredLocale = server.getPreferredLocale();
            if (!oldPreferredLocale.equals(newPreferredLocale)) {
                server.setPreferredLocale(newPreferredLocale);
                ServerChangePreferredLocaleEventImpl event13 = new ServerChangePreferredLocaleEventImpl((ServerImpl)server, newPreferredLocale, oldPreferredLocale);
                this.api.getEventDispatcher().dispatchServerChangePreferredLocaleEvent((DispatchQueueSelector)server, (Server)server, (ServerChangePreferredLocaleEvent)event13);
            }
            int oldBoostCount = server.getBoostCount();
            int n = newBoostCount = packet.has("premium_subscription_count") ? packet.get("premium_subscription_count").asInt() : 0;
            if (oldBoostCount != newBoostCount) {
                server.setServerBoostCount(newBoostCount);
                ServerChangeBoostCountEventImpl event14 = new ServerChangeBoostCountEventImpl((ServerImpl)server, newBoostCount, oldBoostCount);
                this.api.getEventDispatcher().dispatchServerChangeBoostCountEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeBoostCountEvent)event14);
            }
            String oldDescription = server.getDescription().isPresent() ? server.getDescription().get() : null;
            String string = newDescription = packet.hasNonNull("description") ? packet.get("description").asText() : null;
            if (!Objects.deepEquals(oldDescription, newDescription)) {
                server.setDescription(newDescription);
                ServerChangeDescriptionEventImpl event15 = new ServerChangeDescriptionEventImpl((ServerImpl)server, newDescription, oldDescription);
                this.api.getEventDispatcher().dispatchServerChangeDescriptionEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeDescriptionEvent)event15);
            }
            String newDiscoverySplashHash = packet.get("discovery_splash").asText(null);
            String oldDiscoverySplashHash = server.getDiscoverySplashHash();
            if (!Objects.deepEquals(oldDiscoverySplashHash, newDiscoverySplashHash)) {
                server.setDiscoverySplashHash(newDiscoverySplashHash);
                ServerChangeDiscoverySplashEventImpl event16 = new ServerChangeDiscoverySplashEventImpl((ServerImpl)server, newDiscoverySplashHash, oldDiscoverySplashHash);
                this.api.getEventDispatcher().dispatchServerChangeDiscoverySplashEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeDiscoverySplashEvent)event16);
            }
            String oldVanityCode = server.getVanityUrlCode().map(VanityUrlCode::getCode).orElse(null);
            String string2 = newVanityCode = packet.hasNonNull("vanity_url_code") ? packet.get("vanity_url_code").asText() : null;
            if (!Objects.deepEquals(oldVanityCode, newVanityCode)) {
                server.setVanityUrlCode(new VanityUrlCodeImpl(packet.get("vanity_url_code").asText()));
                ServerChangeVanityUrlCodeEventImpl event17 = new ServerChangeVanityUrlCodeEventImpl((ServerImpl)server, newVanityCode, oldVanityCode);
                this.api.getEventDispatcher().dispatchServerChangeVanityUrlCodeEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeVanityUrlCodeEvent)event17);
            }
            Set<ServerFeature> oldServerFeature = server.getFeatures();
            HashSet<ServerFeature> newServerFeature = new HashSet<ServerFeature>();
            if (packet.has("features")) {
                packet.get("features").forEach(jsonNode -> {
                    try {
                        newServerFeature.add(ServerFeature.valueOf(jsonNode.asText()));
                    }
                    catch (Exception ignored) {
                        logger.debug("Encountered server with unknown feature {}. Please update to the latest Javacord version or create an issue on the Javacord GitHub page if you are already on the latest version.", (Object)jsonNode.asText());
                    }
                });
            }
            if (!oldServerFeature.containsAll(newServerFeature) || !newServerFeature.containsAll(oldServerFeature)) {
                server.setServerFeatures(newServerFeature);
                ServerChangeServerFeaturesEventImpl event18 = new ServerChangeServerFeaturesEventImpl((ServerImpl)server, (Set<ServerFeature>)newServerFeature, oldServerFeature);
                this.api.getEventDispatcher().dispatchServerChangeServerFeaturesEvent((DispatchQueueSelector)server, (Server)server, (ServerChangeServerFeaturesEvent)event18);
            }
            if (packet.has("system_channel_flags")) {
                server.setSystemChannelFlag(packet.get("system_channel_flags").asInt());
            }
        });
    }
}

