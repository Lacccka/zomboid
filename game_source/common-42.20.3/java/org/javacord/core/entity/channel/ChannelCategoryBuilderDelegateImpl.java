/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.internal.ChannelCategoryBuilderDelegate;
import org.javacord.core.entity.channel.RegularServerChannelBuilderDelegateImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ChannelCategoryBuilderDelegateImpl
extends RegularServerChannelBuilderDelegateImpl
implements ChannelCategoryBuilderDelegate {
    public ChannelCategoryBuilderDelegateImpl(ServerImpl server) {
        super(server);
    }

    @Override
    public CompletableFuture<ChannelCategory> create() {
        ObjectNode body = JsonNodeFactory.instance.objectNode().put("type", 4);
        super.prepareBody(body);
        return new RestRequest(this.server.getApi(), RestMethod.POST, RestEndpoint.SERVER_CHANNEL).setUrlParameters(this.server.getIdAsString()).setBody(body).setAuditLogReason(this.reason).execute(result -> this.server.getOrCreateChannelCategory(result.getJsonBody()));
    }
}

