/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.internal.ServerForumChannelBuilderDelegate;
import org.javacord.core.entity.channel.RegularServerChannelBuilderDelegateImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerForumChannelBuilderDelegateImpl
extends RegularServerChannelBuilderDelegateImpl
implements ServerForumChannelBuilderDelegate {
    private ChannelCategory category = null;

    public ServerForumChannelBuilderDelegateImpl(ServerImpl server) {
        super(server);
    }

    @Override
    public void setCategory(ChannelCategory category) {
        this.category = category;
    }

    @Override
    public CompletableFuture<ServerForumChannel> create() {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        body.put("type", ChannelType.SERVER_FORUM_CHANNEL.getId());
        super.prepareBody(body);
        if (this.category != null) {
            body.put("parent_id", this.category.getIdAsString());
        }
        return new RestRequest(this.server.getApi(), RestMethod.POST, RestEndpoint.SERVER_CHANNEL).setUrlParameters(this.server.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> this.server.getOrCreateServerForumChannel(result.getJsonBody()));
    }
}

