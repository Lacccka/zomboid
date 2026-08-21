/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.exception;

import java.util.Optional;
import org.javacord.api.util.internal.DelegateFactory;
import org.javacord.api.util.rest.RestRequestInformation;
import org.javacord.api.util.rest.RestRequestResponseInformation;

public class DiscordException
extends Exception {
    private final RestRequestInformation request;
    private final RestRequestResponseInformation response;

    public DiscordException(Exception origin, String message, RestRequestInformation request, RestRequestResponseInformation response) {
        super(message, origin);
        this.request = request;
        this.response = response;
        DelegateFactory.getDiscordExceptionValidator().validateException(this);
    }

    public Optional<RestRequestInformation> getRequest() {
        return Optional.ofNullable(this.request);
    }

    public Optional<RestRequestResponseInformation> getResponse() {
        return Optional.ofNullable(this.response);
    }
}

