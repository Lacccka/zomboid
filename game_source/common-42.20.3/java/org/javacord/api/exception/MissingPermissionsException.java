/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.exception;

import org.javacord.api.exception.DiscordException;
import org.javacord.api.util.rest.RestRequestInformation;
import org.javacord.api.util.rest.RestRequestResponseInformation;

public class MissingPermissionsException
extends DiscordException {
    public MissingPermissionsException(Exception origin, String message, RestRequestInformation request, RestRequestResponseInformation response) {
        super(origin, message, request, response);
    }
}

