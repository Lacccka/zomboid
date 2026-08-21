/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.internal.ServerTextChannelBuilderDelegate;
import org.javacord.core.entity.channel.RegularServerChannelBuilderDelegateImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerTextChannelBuilderDelegateImpl
extends RegularServerChannelBuilderDelegateImpl
implements ServerTextChannelBuilderDelegate {
    private String topic = null;
    private ChannelCategory category = null;
    private int delay;
    private boolean delayModified;

    public ServerTextChannelBuilderDelegateImpl(ServerImpl server) {
        super(server);
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void setCategory(ChannelCategory category) {
        this.category = category;
    }

    @Override
    public void setSlowmodeDelayInSeconds(int delay) {
        this.delay = delay;
        this.delayModified = true;
    }

    @Override
    public CompletableFuture<ServerTextChannel> create() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("type", 0);
        super.prepareBody(body);
        if (this.topic != null) {
            body.put("topic", this.topic);
        }
        if (this.category != null) {
            body.put("parent_id", this.category.getIdAsString());
        }
        if (this.delayModified) {
            body.put("rate_limit_per_user", this.delay);
        }
        return new RestRequest(this.server.getApi(), RestMethod.POST, RestEndpoint.SERVER_CHANNEL).setUrlParameters(this.server.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> this.server.getOrCreateServerTextChannel(result.getJsonBody()));
    }
}

