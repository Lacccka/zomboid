/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.internal.ServerThreadChannelBuilderDelegate;
import org.javacord.api.entity.message.Message;
import org.javacord.core.entity.channel.ServerChannelBuilderDelegateImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerThreadChannelBuilderDelegateImpl
extends ServerChannelBuilderDelegateImpl
implements ServerThreadChannelBuilderDelegate {
    private Integer slowMode = null;
    private ChannelType channelType = null;
    private Integer autoArchiveDuration = null;
    private Boolean inviteable = null;
    private Message message = null;
    private ServerTextChannel serverTextChannel = null;

    public ServerThreadChannelBuilderDelegateImpl(ServerTextChannel serverTextChannel) {
        super((ServerImpl)serverTextChannel.getServer());
        this.serverTextChannel = serverTextChannel;
    }

    public ServerThreadChannelBuilderDelegateImpl(Message message) {
        super((ServerImpl)message.getServer().orElseThrow(() -> new IllegalArgumentException("Message must be from a server")));
        this.message = message;
    }

    @Override
    public void setInvitableFlag(Boolean inviteable) {
        this.inviteable = inviteable;
    }

    @Override
    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    @Override
    public void setAutoArchiveDuration(Integer autoArchiveDuration) {
        this.autoArchiveDuration = autoArchiveDuration;
    }

    @Override
    public void setSlowmodeDelayInSeconds(int delay) {
        this.slowMode = delay;
    }

    @Override
    protected void prepareBody(ObjectNode body) {
        super.prepareBody(body);
        if (this.channelType != null) {
            body.put("type", this.channelType.getId());
        }
        if (this.slowMode != null) {
            body.put("rate_limit_per_user", this.slowMode);
        }
        if (this.autoArchiveDuration != null) {
            body.put("auto_archive_duration", this.autoArchiveDuration);
        }
        if (this.inviteable != null) {
            body.put("invitable", this.inviteable);
        }
    }

    @Override
    public CompletableFuture<ServerThreadChannel> create() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        this.prepareBody(body);
        if (this.message != null) {
            return new RestRequest(this.message.getApi(), RestMethod.POST, RestEndpoint.START_THREAD_WITH_MESSAGE).setUrlParameters(this.message.getChannel().getIdAsString(), this.message.getIdAsString()).setBody(body).execute(result -> ((ServerImpl)this.message.getServer().get()).getOrCreateServerThreadChannel(result.getJsonBody()));
        }
        return new RestRequest(this.serverTextChannel.getApi(), RestMethod.POST, RestEndpoint.START_THREAD_WITHOUT_MESSAGE).setUrlParameters(this.serverTextChannel.getIdAsString()).setBody(body).execute(result -> ((ServerImpl)this.serverTextChannel.getServer()).getOrCreateServerThreadChannel(result.getJsonBody()));
    }
}

