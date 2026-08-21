/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.javacord.api.BotInviteScope;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;

public class BotInviteBuilder {
    private long clientId;
    public static final String BASE_LINK = "https://discord.com/oauth2/authorize?client_id=";
    private String redirectUri = "";
    private Collection<BotInviteScope> inviteScopes = new ArrayList<BotInviteScope>();
    private Permissions permissions = new PermissionsBuilder().build();
    private boolean consent = true;

    public BotInviteBuilder(long clientId) {
        this.clientId = clientId;
    }

    public BotInviteBuilder(String clientId) {
        try {
            this.clientId = Long.parseLong(clientId);
        }
        catch (NumberFormatException e) {
            this.clientId = 0L;
        }
    }

    public BotInviteBuilder(DiscordApi api) {
        this.clientId = api.getClientId();
    }

    public BotInviteBuilder setPermissions(Permissions permissions) {
        this.permissions = permissions;
        return this;
    }

    public BotInviteBuilder setPermissions(PermissionType ... permissions) {
        this.permissions = new PermissionsBuilder().setAllowed(permissions).build();
        return this;
    }

    public BotInviteBuilder setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public BotInviteBuilder setScopes(Collection<BotInviteScope> scopes) {
        this.inviteScopes = scopes;
        return this;
    }

    public BotInviteBuilder setScopes(BotInviteScope ... scopes) {
        this.inviteScopes = Arrays.asList(scopes);
        return this;
    }

    public BotInviteBuilder addScopes(Collection<BotInviteScope> scopes) {
        this.inviteScopes.addAll(scopes);
        return this;
    }

    public BotInviteBuilder addScopes(BotInviteScope ... scopes) {
        this.inviteScopes.addAll(Arrays.asList(scopes));
        return this;
    }

    public BotInviteBuilder setPromptConsent(boolean promptConsent) {
        this.consent = promptConsent;
        return this;
    }

    public String build() {
        if (this.inviteScopes.isEmpty()) {
            this.addScopes(BotInviteScope.BOT, BotInviteScope.APPLICATIONS_COMMANDS);
        }
        StringBuilder builder = new StringBuilder(BASE_LINK);
        builder.append(this.clientId).append("&scope=").append(this.inviteScopes.stream().map(BotInviteScope::getScope).collect(Collectors.joining("%20"))).append("&permissions=").append(this.permissions.getAllowedBitmask());
        if (this.redirectUri != null && !this.redirectUri.isEmpty()) {
            try {
                builder.append("&redirect_uri=").append(URLEncoder.encode(this.redirectUri, "UTF-8")).append("&response_type=code");
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (this.consent) {
            builder.append("&prompt=consent");
        }
        return builder.toString();
    }
}

