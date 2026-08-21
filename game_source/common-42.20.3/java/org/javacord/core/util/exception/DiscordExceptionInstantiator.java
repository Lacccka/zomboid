/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.exception;

import org.javacord.api.exception.DiscordException;
import org.javacord.api.util.rest.RestRequestInformation;
import org.javacord.api.util.rest.RestRequestResponseInformation;

@FunctionalInterface
public interface DiscordExceptionInstantiator<T extends DiscordException> {
    public T createInstance(Exception var1, String var2, RestRequestInformation var3, RestRequestResponseInformation var4);
}

