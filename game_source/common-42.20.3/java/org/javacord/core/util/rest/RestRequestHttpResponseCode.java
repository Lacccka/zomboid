/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.javacord.api.exception.BadRequestException;
import org.javacord.api.exception.DiscordException;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.exception.NotFoundException;
import org.javacord.api.util.rest.RestRequestInformation;
import org.javacord.api.util.rest.RestRequestResponseInformation;
import org.javacord.core.util.exception.DiscordExceptionInstantiator;

public enum RestRequestHttpResponseCode {
    OK(200, "The request completed successfully"),
    CREATED(201, "The entity was created successfully"),
    NO_CONTENT(204, "The request completed successfully but returned no content"),
    NOT_MODIFIED(304, "The entity was not modified (no action was taken)"),
    BAD_REQUEST(400, "The request was improperly formatted, or the server couldn't understand it", BadRequestException::new, BadRequestException.class),
    UNAUTHORIZED(401, "The Authorization header was missing or invalid"),
    FORBIDDEN(403, "The Authorization token you passed did not have permission to the resource", MissingPermissionsException::new, MissingPermissionsException.class),
    NOT_FOUND(404, "The resource at the location specified doesn't exist", NotFoundException::new, NotFoundException.class),
    METHOD_NOT_ALLOWED(405, "The HTTP method used is not valid for the location specified"),
    TOO_MANY_REQUESTS(429, "You've made too many requests"),
    GATEWAY_UNAVAILABLE(502, "There was not a gateway available to process your request. Wait a bit and retry");

    private static final Map<Integer, RestRequestHttpResponseCode> instanceByCode;
    private static final Map<Class<? extends DiscordException>, RestRequestHttpResponseCode> instanceByExceptionClass;
    private final int code;
    private final String meaning;
    private final DiscordExceptionInstantiator<?> discordExceptionInstantiator;
    private final Class<? extends DiscordException> discordExceptionClass;

    private RestRequestHttpResponseCode(int code, String meaning) {
        this(code, meaning, null, null);
    }

    private <T extends DiscordException> RestRequestHttpResponseCode(int code, String meaning, DiscordExceptionInstantiator<T> discordExceptionInstantiator, Class<T> discordExceptionClass) {
        this.code = code;
        this.meaning = meaning;
        this.discordExceptionInstantiator = discordExceptionInstantiator;
        this.discordExceptionClass = discordExceptionClass;
        if (discordExceptionInstantiator == null && discordExceptionClass != null || discordExceptionInstantiator != null && discordExceptionClass == null) {
            throw new IllegalArgumentException("discordExceptionInstantiator and discordExceptionClass do not match");
        }
    }

    public static Optional<RestRequestHttpResponseCode> fromCode(int code) {
        return Optional.ofNullable(instanceByCode.get(code));
    }

    public static Optional<RestRequestHttpResponseCode> fromDiscordExceptionClass(Class<? extends DiscordException> discordExceptionClass) {
        Class<? extends DiscordException> clazz = discordExceptionClass;
        while (instanceByExceptionClass.get(clazz) == null) {
            if (clazz == DiscordException.class) {
                return Optional.empty();
            }
            clazz = clazz.getSuperclass();
        }
        return Optional.of(instanceByExceptionClass.get(clazz));
    }

    public int getCode() {
        return this.code;
    }

    public String getMeaning() {
        return this.meaning;
    }

    public Optional<? extends DiscordException> getDiscordException(Exception origin, String message, RestRequestInformation request, RestRequestResponseInformation response) {
        return Optional.ofNullable(this.discordExceptionInstantiator).map(instantiator -> instantiator.createInstance(origin, message, request, response));
    }

    public Optional<? extends Class<? extends DiscordException>> getDiscordExceptionClass() {
        return Optional.ofNullable(this.discordExceptionClass);
    }

    static {
        instanceByCode = Collections.unmodifiableMap(Arrays.stream(RestRequestHttpResponseCode.values()).collect(Collectors.toMap(RestRequestHttpResponseCode::getCode, Function.identity())));
        instanceByExceptionClass = Collections.unmodifiableMap(Arrays.stream(RestRequestHttpResponseCode.values()).filter(restRequestHttpResponseCode -> restRequestHttpResponseCode.getDiscordExceptionClass().isPresent()).collect(Collectors.toMap(restRequestHttpResponseCode -> restRequestHttpResponseCode.getDiscordExceptionClass().orElse(null), Function.identity())));
    }
}

