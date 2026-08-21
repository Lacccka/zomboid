/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.user;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordClient;
import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.event.user.UserChangeActivityEvent;
import org.javacord.api.event.user.UserChangeStatusEvent;
import org.javacord.core.entity.activity.ActivityImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.entity.user.UserPresence;
import org.javacord.core.event.user.UserChangeActivityEventImpl;
import org.javacord.core.event.user.UserChangeStatusEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class PresenceUpdateHandler
extends PacketHandler {
    public PresenceUpdateHandler(DiscordApi api) {
        super(api, true, "PRESENCE_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        UserStatus newStatus;
        long userId = packet.get("user").get("id").asLong();
        AtomicReference<UserPresence> presence = new AtomicReference<UserPresence>(this.api.getEntityCache().get().getUserPresenceCache().getPresenceByUserId(userId).orElseGet(() -> new UserPresence(userId, null, null, HashMap.empty())));
        if (packet.hasNonNull("activities")) {
            HashSet<Activity> newActivities = new HashSet<Activity>();
            for (JsonNode activityJson : packet.get("activities")) {
                if (activityJson.isNull()) continue;
                newActivities.add(new ActivityImpl(this.api, activityJson));
            }
            Set<Activity> oldActivities = this.api.getEntityCache().get().getUserPresenceCache().getPresenceByUserId(userId).map(UserPresence::getActivities).orElse(Collections.emptySet());
            presence.set(presence.get().setActivities(newActivities));
            if (!Objects.deepEquals(newActivities.toArray(), oldActivities.toArray())) {
                this.dispatchUserActivityChangeEvent(userId, newActivities, oldActivities);
            }
        }
        UserStatus oldStatus = this.api.getEntityCache().get().getUserPresenceCache().getPresenceByUserId(userId).map(UserPresence::getStatus).orElse(UserStatus.OFFLINE);
        if (packet.has("status")) {
            newStatus = UserStatus.fromString(packet.get("status").asText(null));
            presence.set(presence.get().setStatus(newStatus));
        } else {
            newStatus = oldStatus;
        }
        Map oldClientStatus = this.api.getEntityCache().get().getUserPresenceCache().getPresenceByUserId(userId).map(UserPresence::getClientStatus).orElse(HashMap.empty());
        for (DiscordClient client : DiscordClient.values()) {
            if (!packet.has("client_status")) continue;
            JsonNode clientStatus = packet.get("client_status");
            if (clientStatus.hasNonNull(client.getName())) {
                UserStatus status = UserStatus.fromString(clientStatus.get(client.getName()).asText());
                presence.set(presence.get().setClientStatus(presence.get().getClientStatus().put(client, status)));
                continue;
            }
            presence.set(presence.get().setClientStatus(presence.get().getClientStatus().put(client, UserStatus.OFFLINE)));
        }
        Map newClientStatus = this.api.getEntityCache().get().getUserPresenceCache().getPresenceByUserId(userId).map(UserPresence::getClientStatus).orElse(HashMap.empty());
        this.api.updateUserPresence(userId, p -> (UserPresence)presence.get());
        this.dispatchUserStatusChangeEventIfChangeDetected(userId, newStatus, oldStatus, newClientStatus, oldClientStatus);
    }

    private void dispatchUserActivityChangeEvent(long userId, Set<Activity> newActivities, Set<Activity> oldActivities) {
        UserImpl user = this.api.getCachedUserById(userId).map(UserImpl.class::cast).orElse(null);
        UserChangeActivityEventImpl event = new UserChangeActivityEventImpl(this.api, userId, newActivities, oldActivities);
        this.api.getEventDispatcher().dispatchUserChangeActivityEvent((DispatchQueueSelector)this.api, user == null ? Collections.emptySet() : user.getMutualServers(), user == null ? Collections.emptySet() : Collections.singleton(user), (UserChangeActivityEvent)event);
    }

    private void dispatchUserStatusChangeEventIfChangeDetected(long userId, UserStatus newStatus, UserStatus oldStatus, Map<DiscordClient, UserStatus> newClientStatus, Map<DiscordClient, UserStatus> oldClientStatus) {
        UserImpl user = this.api.getCachedUserById(userId).map(UserImpl.class::cast).orElse(null);
        boolean shouldDispatch = false;
        if (newClientStatus != oldClientStatus) {
            shouldDispatch = true;
        }
        for (DiscordClient client : DiscordClient.values()) {
            if (newClientStatus.get(client) == oldClientStatus.get(client)) continue;
            shouldDispatch = true;
        }
        if (!shouldDispatch) {
            return;
        }
        UserChangeStatusEventImpl event = new UserChangeStatusEventImpl(this.api, userId, newStatus, oldStatus, newClientStatus, oldClientStatus);
        this.api.getEventDispatcher().dispatchUserChangeStatusEvent((DispatchQueueSelector)this.api, user == null ? Collections.emptySet() : user.getMutualServers(), user == null ? Collections.emptySet() : Collections.singleton(user), (UserChangeStatusEvent)event);
    }
}

