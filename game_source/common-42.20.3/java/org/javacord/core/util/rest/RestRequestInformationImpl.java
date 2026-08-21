/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.rest;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.util.rest.RestRequestInformation;

public class RestRequestInformationImpl
implements RestRequestInformation {
    private final DiscordApi api;
    private final URL url;
    private final Map<String, String> queryParameters;
    private final Map<String, String> headers;
    private final String body;

    public RestRequestInformationImpl(DiscordApi api, URL url, Map<String, String> queryParameter, Map<String, String> headers, String body) {
        this.api = api;
        this.url = url;
        this.queryParameters = queryParameter;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return Collections.unmodifiableMap(this.queryParameters);
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(this.headers);
    }

    @Override
    public Optional<String> getBody() {
        return Optional.ofNullable(this.body);
    }
}

