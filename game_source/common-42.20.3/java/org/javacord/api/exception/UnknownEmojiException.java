/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.exception;

import org.javacord.api.exception.BadRequestException;
import org.javacord.api.util.rest.RestRequestInformation;
import org.javacord.api.util.rest.RestRequestResponseInformation;

public class UnknownEmojiException
extends BadRequestException {
    public UnknownEmojiException(Exception origin, String message, RestRequestInformation request, RestRequestResponseInformation response) {
        super(origin, message, request, response);
    }
}

