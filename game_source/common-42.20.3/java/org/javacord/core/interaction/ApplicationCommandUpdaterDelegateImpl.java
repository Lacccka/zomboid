/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.internal.ApplicationCommandUpdaterDelegate;

public abstract class ApplicationCommandUpdaterDelegateImpl<T extends ApplicationCommand>
implements ApplicationCommandUpdaterDelegate<T> {
    protected long commandId;
    protected String name = null;
    protected Map<DiscordLocale, String> nameLocalizations = new HashMap<DiscordLocale, String>();
    protected String description = null;
    protected Map<DiscordLocale, String> descriptionLocalizations = new HashMap<DiscordLocale, String>();
    protected Long defaultMemberPermissions = -1L;
    protected Boolean dmPermission = null;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addNameLocalization(DiscordLocale locale, String localization) {
        this.nameLocalizations.put(locale, localization);
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void addDescriptionLocalization(DiscordLocale locale, String localization) {
        this.descriptionLocalizations.put(locale, localization);
    }

    @Override
    public void setDefaultEnabledForPermissions(EnumSet<PermissionType> requiredPermissions) {
        this.defaultMemberPermissions = requiredPermissions.stream().mapToLong(PermissionType::getValue).sum();
    }

    @Override
    public void setDefaultEnabledForEveryone() {
        this.defaultMemberPermissions = null;
    }

    @Override
    public void setDefaultDisabled() {
        this.defaultMemberPermissions = 0L;
    }

    @Override
    public void setEnabledInDms(boolean enabledInDms) {
        this.dmPermission = enabledInDms;
    }

    protected void prepareBody(ObjectNode body) {
        if (this.name != null && !this.name.isEmpty()) {
            body.put("name", this.name);
        }
        if (!this.nameLocalizations.isEmpty()) {
            ObjectNode nameLocalizationsJsonObject = body.putObject("name_localizations");
            this.nameLocalizations.forEach((locale, localization) -> nameLocalizationsJsonObject.put(locale.getLocaleCode(), (String)localization));
        }
        if (this.description != null && !this.description.isEmpty()) {
            body.put("description", this.description);
        }
        if (!this.descriptionLocalizations.isEmpty()) {
            ObjectNode descriptionLocalizationsJsonObject = body.putObject("description_localizations");
            this.descriptionLocalizations.forEach((locale, localization) -> descriptionLocalizationsJsonObject.put(locale.getLocaleCode(), (String)localization));
        }
        if (this.defaultMemberPermissions == null || this.defaultMemberPermissions != -1L) {
            body.put("default_member_permissions", this.defaultMemberPermissions != null ? String.valueOf(this.defaultMemberPermissions) : null);
        }
        if (this.dmPermission != null) {
            body.put("dm_permission", this.dmPermission);
        }
    }
}

