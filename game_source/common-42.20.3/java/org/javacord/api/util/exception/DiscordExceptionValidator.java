/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.exception;

import org.javacord.api.exception.DiscordException;

public interface DiscordExceptionValidator {
    public void validateException(DiscordException var1) throws AssertionError;
}

