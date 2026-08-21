/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.auditlog;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.auditlog.AuditLog;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogChange;
import org.javacord.api.entity.auditlog.AuditLogChangeType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.auditlog.AuditLogEntryTarget;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.user.User;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.auditlog.AuditLogChangeImpl;
import org.javacord.core.entity.auditlog.AuditLogEntryTargetImpl;
import org.javacord.core.entity.permission.PermissionsImpl;
import org.javacord.core.interaction.ApplicationCommandPermissionsImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class AuditLogEntryImpl
implements AuditLogEntry {
    private static final Logger logger = LoggerUtil.getLogger(AuditLogEntryImpl.class);
    private final long id;
    private final AuditLog auditLog;
    private final long userId;
    private final String reason;
    private final AuditLogActionType actionType;
    private final AuditLogEntryTarget target;
    private final List<AuditLogChange<?>> changes = new ArrayList();

    public AuditLogEntryImpl(AuditLog auditLog, JsonNode data) {
        this.auditLog = auditLog;
        this.id = data.get("id").asLong();
        this.userId = data.get("user_id").asLong();
        this.reason = data.has("reason") ? data.get("reason").asText() : null;
        this.actionType = AuditLogActionType.fromValue(data.get("action_type").asInt());
        AuditLogEntryTarget auditLogEntryTarget = this.target = data.has("target_id") && !data.get("target_id").isNull() ? new AuditLogEntryTargetImpl(data.get("target_id").asLong(), this) : null;
        if (data.has("changes")) {
            for (JsonNode changeJson : data.get("changes")) {
                AuditLogChange<?> change;
                AuditLogChangeType type = AuditLogChangeType.fromName(changeJson.get("key").asText());
                JsonNode oldValue = changeJson.get("old_value");
                JsonNode newValue = changeJson.get("new_value");
                String baseUrl = null;
                switch (type) {
                    case NAME: 
                    case VANITY_URL_CODE: 
                    case TOPIC: 
                    case CODE: 
                    case NICK: {
                        change = new AuditLogChangeImpl<String>(type, oldValue != null ? oldValue.asText() : null, newValue != null ? newValue.asText() : null);
                        break;
                    }
                    case OWNER_ID: 
                    case AFK_CHANNEL_ID: 
                    case WIDGET_CHANNEL_ID: 
                    case APPLICATION_ID: 
                    case CHANNEL_ID: 
                    case INVITER_ID: 
                    case ID: {
                        change = new AuditLogChangeImpl<Object>(type, (oldValue != null ? Long.valueOf(oldValue.asLong()) : null), (newValue != null ? Long.valueOf(newValue.asLong()) : null));
                        break;
                    }
                    case AFK_TIMEOUT: 
                    case POSITION: 
                    case BITRATE: 
                    case MAX_USES: 
                    case USES: 
                    case MAX_AGE: {
                        change = new AuditLogChangeImpl<Object>(type, (oldValue != null ? Integer.valueOf(oldValue.asInt()) : null), (newValue != null ? Integer.valueOf(newValue.asInt()) : null));
                        break;
                    }
                    case WIDGET_ENABLED: 
                    case NSFW: 
                    case DISPLAY_SEPARATELY: 
                    case MENTIONABLE: 
                    case TEMPORARY: 
                    case DEAF: 
                    case MUTE: {
                        change = new AuditLogChangeImpl<Object>(type, (oldValue != null ? Boolean.valueOf(oldValue.asBoolean()) : null), (newValue != null ? Boolean.valueOf(newValue.asBoolean()) : null));
                        break;
                    }
                    case ICON: {
                        change = this.iconChange("https://cdn.discordapp.com/icons/", type, oldValue, newValue);
                        break;
                    }
                    case SPLASH: {
                        change = this.iconChange("https://cdn.discordapp.com/splashes/", type, oldValue, newValue);
                        break;
                    }
                    case REGION: {
                        Region oldRegion = Region.getRegionByKey(oldValue != null ? oldValue.asText() : "");
                        Region newRegion = Region.getRegionByKey(newValue != null ? newValue.asText() : "");
                        change = new AuditLogChangeImpl<Region>(type, oldRegion, newRegion);
                        break;
                    }
                    case MFA_LEVEL: {
                        change = new AuditLogChangeImpl<JsonNode>(type, oldValue, newValue);
                        break;
                    }
                    case VERIFICATION_LEVEL: {
                        VerificationLevel oldVerificationLevel = VerificationLevel.fromId(oldValue != null ? oldValue.asInt() : -1);
                        VerificationLevel newVerificationLevel = VerificationLevel.fromId(newValue != null ? newValue.asInt() : -1);
                        change = new AuditLogChangeImpl<VerificationLevel>(type, oldVerificationLevel, newVerificationLevel);
                        break;
                    }
                    case EXPLICIT_CONTENT_FILTER: {
                        ExplicitContentFilterLevel oldExplicitContentFilterLevel = ExplicitContentFilterLevel.fromId(oldValue != null ? oldValue.asInt() : -1);
                        ExplicitContentFilterLevel newExplicitContentFilterLevel = ExplicitContentFilterLevel.fromId(newValue != null ? newValue.asInt() : -1);
                        change = new AuditLogChangeImpl<ExplicitContentFilterLevel>(type, oldExplicitContentFilterLevel, newExplicitContentFilterLevel);
                        break;
                    }
                    case DEFAULT_MESSAGE_NOTIFICATIONS: {
                        DefaultMessageNotificationLevel oldDefaultMessageNotificationLevel = DefaultMessageNotificationLevel.fromId(oldValue != null ? oldValue.asInt() : -1);
                        DefaultMessageNotificationLevel newDefaultMessageNotificationLevel = DefaultMessageNotificationLevel.fromId(newValue != null ? newValue.asInt() : -1);
                        change = new AuditLogChangeImpl<DefaultMessageNotificationLevel>(type, oldDefaultMessageNotificationLevel, newDefaultMessageNotificationLevel);
                        break;
                    }
                    case ROLE_ADD: 
                    case ROLE_REMOVE: {
                        change = new AuditLogChangeImpl<JsonNode>(type, oldValue, newValue);
                        break;
                    }
                    case PERMISSION_OVERWRITES: {
                        change = new AuditLogChangeImpl<JsonNode>(type, oldValue, newValue);
                        break;
                    }
                    case PERMISSIONS: 
                    case ALLOWED_PERMISSIONS: {
                        PermissionsImpl oldPermissions = oldValue != null ? new PermissionsImpl(oldValue.asInt()) : null;
                        PermissionsImpl newPermissions = newValue != null ? new PermissionsImpl(newValue.asInt()) : null;
                        change = new AuditLogChangeImpl<PermissionsImpl>(type, oldPermissions, newPermissions);
                        break;
                    }
                    case COLOR: {
                        Color oldColor = oldValue != null ? new Color(oldValue.asInt()) : null;
                        Color newColor = newValue != null ? new Color(newValue.asInt()) : null;
                        change = new AuditLogChangeImpl<Color>(type, oldColor, newColor);
                        break;
                    }
                    case DENIED_PERMISSIONS: {
                        PermissionsImpl oldPermissions = oldValue != null ? new PermissionsImpl(0L, oldValue.asInt()) : null;
                        PermissionsImpl newPermissions = newValue != null ? new PermissionsImpl(0L, newValue.asInt()) : null;
                        change = new AuditLogChangeImpl<PermissionsImpl>(type, oldPermissions, newPermissions);
                        break;
                    }
                    case AVATAR: {
                        String oldUrl;
                        baseUrl = "https://cdn.discordapp.com/avatars/" + this.getTarget().map(DiscordEntity::getIdAsString).orElse("0") + "/";
                        String string = oldValue != null ? baseUrl + oldValue.asText() + (oldValue.asText().startsWith("a_") ? ".gif" : ".png") : (oldUrl = null);
                        String newUrl = newValue != null ? baseUrl + newValue.asText() + (newValue.asText().startsWith("a_") ? ".gif" : ".png") : null;
                        try {
                            IconImpl oldIcon = oldValue != null ? new IconImpl(this.getApi(), new URL(oldUrl)) : null;
                            IconImpl newIcon = newValue != null ? new IconImpl(this.getApi(), new URL(newUrl)) : null;
                            change = new AuditLogChangeImpl<IconImpl>(type, oldIcon, newIcon);
                        }
                        catch (MalformedURLException e) {
                            logger.warn("Seems like the icon's url is malformed! Please contact the developer!", (Throwable)e);
                            change = new AuditLogChangeImpl<JsonNode>(AuditLogChangeType.UNKNOWN, oldValue, newValue);
                        }
                        break;
                    }
                    case TYPE: {
                        change = new AuditLogChangeImpl<JsonNode>(type, oldValue, newValue);
                        break;
                    }
                    default: {
                        if (this.actionType == AuditLogActionType.APPLICATION_COMMAND_PERMISSION_UPDATE) {
                            ApplicationCommandPermissionsImpl oldPermissionsValue = oldValue != null ? new ApplicationCommandPermissionsImpl(auditLog.getServer(), oldValue) : null;
                            ApplicationCommandPermissionsImpl newPermissionsValue = oldValue != null ? new ApplicationCommandPermissionsImpl(auditLog.getServer(), newValue) : null;
                            change = new AuditLogChangeImpl<ApplicationCommandPermissionsImpl>(type, oldPermissionsValue, newPermissionsValue);
                            break;
                        }
                        change = new AuditLogChangeImpl<JsonNode>(type, oldValue, newValue);
                    }
                }
                this.changes.add(change);
            }
        }
    }

    private AuditLogChange<?> iconChange(String baseUrl, AuditLogChangeType type, JsonNode oldVal, JsonNode newVal) {
        try {
            IconImpl oldIcon = oldVal != null ? new IconImpl(this.getApi(), new URL(baseUrl + this.getTarget().map(DiscordEntity::getIdAsString).orElse("0") + "/" + oldVal.asText() + ".png")) : null;
            IconImpl newIcon = newVal != null ? new IconImpl(this.getApi(), new URL(baseUrl + this.getTarget().map(DiscordEntity::getIdAsString).orElse("0") + "/" + newVal.asText() + ".png")) : null;
            return new AuditLogChangeImpl<IconImpl>(type, oldIcon, newIcon);
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the icon's url is malformed! Please contact the developer!", (Throwable)e);
            return new AuditLogChangeImpl<JsonNode>(AuditLogChangeType.UNKNOWN, oldVal, newVal);
        }
    }

    @Override
    public DiscordApi getApi() {
        return this.auditLog.getApi();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public AuditLog getAuditLog() {
        return this.auditLog;
    }

    @Override
    public CompletableFuture<User> getUser() {
        return this.getApi().getUserById(this.userId);
    }

    @Override
    public Optional<String> getReason() {
        return Optional.ofNullable(this.reason);
    }

    @Override
    public AuditLogActionType getType() {
        return this.actionType;
    }

    @Override
    public Optional<AuditLogEntryTarget> getTarget() {
        return Optional.ofNullable(this.target);
    }

    @Override
    public List<AuditLogChange<?>> getChanges() {
        return this.changes;
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("AuditLogEntry (id: %s)", this.getIdAsString());
    }
}

