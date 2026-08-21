/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.exception;

import java.util.Optional;
import org.javacord.api.exception.DiscordException;
import org.javacord.api.util.exception.DiscordExceptionValidator;
import org.javacord.api.util.rest.RestRequestResponseInformation;
import org.javacord.core.util.rest.RestRequestHttpResponseCode;

public class DiscordExceptionValidatorImpl
implements DiscordExceptionValidator {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void validateException(DiscordException exception) throws AssertionError {
        Optional<RestRequestHttpResponseCode> expectedResponseCodeOptional = RestRequestHttpResponseCode.fromDiscordExceptionClass(exception.getClass());
        Optional<RestRequestResponseInformation> restRequestResponseInformationOptional = exception.getResponse();
        if (expectedResponseCodeOptional.isPresent()) {
            RestRequestHttpResponseCode expectedResponseCode = expectedResponseCodeOptional.get();
            if (!restRequestResponseInformationOptional.isPresent()) throw new AssertionError((Object)(exception.getClass().getSimpleName() + " should only be thrown on " + expectedResponseCode.getCode() + " HTTP response code but there is no result. Please contact the developer!"));
            if (restRequestResponseInformationOptional.get().getCode() == expectedResponseCode.getCode()) return;
            throw new AssertionError((Object)(exception.getClass().getSimpleName() + " should only be thrown on " + expectedResponseCode.getCode() + " HTTP response code (was " + restRequestResponseInformationOptional.get().getCode() + ") or it should not be a subclass of " + expectedResponseCode.getDiscordExceptionClass().orElseThrow(AssertionError::new).getSimpleName() + ". Please contact the developer!"));
        }
        Optional<Integer> responseCodeOptional = restRequestResponseInformationOptional.map(RestRequestResponseInformation::getCode);
        Optional discordExceptionClassOptional = responseCodeOptional.flatMap(RestRequestHttpResponseCode::fromCode).flatMap(RestRequestHttpResponseCode::getDiscordExceptionClass);
        if (!discordExceptionClassOptional.isPresent()) return;
        throw new AssertionError((Object)("For " + responseCodeOptional.orElseThrow(AssertionError::new) + " HTTP response code an exception of type " + ((Class)discordExceptionClassOptional.get()).getSimpleName() + " or a sub-class thereof should be thrown. Please contact the developer!"));
    }
}

