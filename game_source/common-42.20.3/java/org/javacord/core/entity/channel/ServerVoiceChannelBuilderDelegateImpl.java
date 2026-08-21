/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.internal.ServerVoiceChannelBuilderDelegate;
import org.javacord.core.entity.channel.RegularServerChannelBuilderDelegateImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerVoiceChannelBuilderDelegateImpl
extends RegularServerChannelBuilderDelegateImpl
implements ServerVoiceChannelBuilderDelegate {
    private Integer bitrate = null;
    private Integer userlimit = null;
    private ChannelCategory category = null;

    public ServerVoiceChannelBuilderDelegateImpl(ServerImpl server) {
        super(server);
    }

    @Override
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    @Override
    public void setUserlimit(int userlimit) {
        this.userlimit = userlimit;
    }

    @Override
    public void setCategory(ChannelCategory category) {
        this.category = category;
    }

    @Override
    public CompletableFuture<ServerVoiceChannel> create() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("type", 2);
        super.prepareBody(body);
        if (this.bitrate != null) {
            body.put("bitrate", (int)this.bitrate);
        }
        if (this.userlimit != null) {
            body.put("user_limit", (int)this.userlimit);
        }
        if (this.category != null) {
            body.put("parent_id", this.category.getIdAsString());
        }
        return new RestRequest(this.server.getApi(), RestMethod.POST, RestEndpoint.SERVER_CHANNEL).setUrlParameters(this.server.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> this.server.getOrCreateServerVoiceChannel(result.getJsonBody()));
    }
}

