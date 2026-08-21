/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.exception;

import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.rest.RestRequestInformation;
import org.javacord.api.util.rest.RestRequestResponseInformation;

public class ReactionBlockedException
extends MissingPermissionsException {
    public ReactionBlockedException(Exception origin, String message, RestRequestInformation request, RestRequestResponseInformation response) {
        super(origin, message, request, response);
    }
}

