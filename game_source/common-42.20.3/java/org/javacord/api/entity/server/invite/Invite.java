/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server.invite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.TargetUserType;
import org.javacord.api.entity.user.User;

public interface Invite
extends Deletable {
    public String getCode();

    default public URL getUrl() {
        try {
            return new URL("https://discord.gg/" + this.getCode());
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    public Optional<Server> getServer();

    public long getServerId();

    public String getServerName();

    public Optional<Icon> getServerIcon();

    public Optional<Icon> getServerSplash();

    public Optional<ServerChannel> getChannel();

    public long getChannelId();

    public String getChannelName();

    public ChannelType getChannelType();

    public Optional<Integer> getApproximateMemberCount();

    public Optional<Integer> getApproximatePresenceCount();

    public Optional<User> getInviter();

    public Optional<User> getTargetUser();

    public Optional<TargetUserType> getTargetUserType();
}

