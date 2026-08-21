/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.rest;

import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.util.rest.RestRequestInformation;
import org.javacord.api.util.rest.RestRequestResponseInformation;
import org.javacord.core.util.rest.RestRequestResult;

public class RestRequestResponseInformationImpl
implements RestRequestResponseInformation {
    private final RestRequestInformation request;
    private final RestRequestResult restRequestResult;

    public RestRequestResponseInformationImpl(RestRequestInformation request, RestRequestResult restRequestResult) {
        this.request = request;
        this.restRequestResult = restRequestResult;
    }

    public RestRequestResult getRestRequestResult() {
        return this.restRequestResult;
    }

    @Override
    public DiscordApi getApi() {
        return this.getRequest().getApi();
    }

    @Override
    public RestRequestInformation getRequest() {
        return this.request;
    }

    @Override
    public int getCode() {
        return this.restRequestResult.getResponse().code();
    }

    @Override
    public Optional<String> getBody() {
        return this.restRequestResult.getStringBody();
    }
}

