/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.rest;

import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.util.rest.RestRequestInformation;

public interface RestRequestResponseInformation {
    public DiscordApi getApi();

    public RestRequestInformation getRequest();

    public int getCode();

    public Optional<String> getBody();
}

