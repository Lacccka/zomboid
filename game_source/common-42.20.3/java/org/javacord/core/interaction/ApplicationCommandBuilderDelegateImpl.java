/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.api.interaction.DiscordLocale;
import org.javacord.api.interaction.internal.ApplicationCommandBuilderDelegate;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public abstract class ApplicationCommandBuilderDelegateImpl<T extends ApplicationCommand>
implements ApplicationCommandBuilderDelegate<T> {
    protected String name;
    protected Map<DiscordLocale, String> nameLocalizations = new HashMap<DiscordLocale, String>();
    protected String description;
    protected Map<DiscordLocale, String> descriptionLocalizations = new HashMap<DiscordLocale, String>();
    protected Long defaultMemberPermissions = null;
    protected Boolean dmPermission = true;
    protected Boolean nsfw = false;

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

    @Override
    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Override
    public CompletableFuture<T> createGlobal(DiscordApi api) {
        return new RestRequest(api, RestMethod.POST, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(api.getClientId())).setBody(this.getJsonBodyForApplicationCommand()).execute(result -> this.createInstance((DiscordApiImpl)api, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<T> createForServer(DiscordApi api, long server) {
        return new RestRequest(api, RestMethod.POST, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(api.getClientId()), String.valueOf(server)).setBody(this.getJsonBodyForApplicationCommand()).execute(result -> this.createInstance((DiscordApiImpl)api, result.getJsonBody()));
    }

    public ObjectNode getJsonBodyForApplicationCommand() {
        ObjectNode jsonBody = JsonNodeFactory.instance.objectNode().put("name", this.name);
        if (!this.nameLocalizations.isEmpty()) {
            ObjectNode nameLocalizationsJsonObject = jsonBody.putObject("name_localizations");
            this.nameLocalizations.forEach((locale, localization) -> nameLocalizationsJsonObject.put(locale.getLocaleCode(), (String)localization));
        }
        jsonBody.put("description", this.description);
        if (!this.descriptionLocalizations.isEmpty()) {
            ObjectNode descriptionLocalizationsJsonObject = jsonBody.putObject("description_localizations");
            this.descriptionLocalizations.forEach((locale, localization) -> descriptionLocalizationsJsonObject.put(locale.getLocaleCode(), (String)localization));
        }
        jsonBody.put("default_member_permissions", this.defaultMemberPermissions != null ? String.valueOf(this.defaultMemberPermissions) : null);
        jsonBody.put("dm_permission", this.dmPermission);
        jsonBody.put("nsfw", this.nsfw);
        return jsonBody;
    }

    public abstract T createInstance(DiscordApiImpl var1, JsonNode var2);
}

