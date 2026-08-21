/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.exception;

import org.javacord.api.exception.DiscordException;
import org.javacord.api.util.rest.RestRequestInformation;

public class RatelimitException
extends DiscordException {
    public RatelimitException(Exception origin, String message, RestRequestInformation request) {
        super(origin, message, request, null);
    }
}

