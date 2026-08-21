/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.rest;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import org.javacord.api.DiscordApi;

public interface RestRequestInformation {
    public DiscordApi getApi();

    public URL getUrl();

    public Map<String, String> getQueryParameters();

    public Map<String, String> getHeaders();

    public Optional<String> getBody();
}

