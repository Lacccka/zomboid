/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.rest;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.javacord.api.DiscordApi;
import org.javacord.api.exception.DiscordException;
import org.javacord.api.util.rest.RestRequestInformation;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequestHttpResponseCode;
import org.javacord.core.util.rest.RestRequestInformationImpl;
import org.javacord.core.util.rest.RestRequestResponseInformationImpl;
import org.javacord.core.util.rest.RestRequestResult;
import org.javacord.core.util.rest.RestRequestResultErrorCode;

public class RestRequest<T> {
    private static final Logger logger = LoggerUtil.getLogger(RestRequest.class);
    private final DiscordApiImpl api;
    private final RestMethod method;
    private final RestEndpoint endpoint;
    private volatile boolean includeAuthorizationHeader = true;
    private volatile boolean consumeGlobalRatelimit = true;
    private volatile String[] urlParameters = new String[0];
    private final Map<String, String> queryParameters = new HashMap<String, String>();
    private final Map<String, String> headers = new HashMap<String, String>();
    private volatile String body = null;
    private final CompletableFuture<RestRequestResult> result = new CompletableFuture();
    private MultipartBody multipartBody;
    private String customMajorParam = null;
    private final Exception origin;

    public RestRequest(DiscordApi api, RestMethod method, RestEndpoint endpoint) {
        this.api = (DiscordApiImpl)api;
        this.method = method;
        this.endpoint = endpoint;
        this.origin = new Exception("origin of RestRequest call");
    }

    public DiscordApiImpl getApi() {
        return this.api;
    }

    public RestMethod getMethod() {
        return this.method;
    }

    public RestEndpoint getEndpoint() {
        return this.endpoint;
    }

    public String[] getUrlParameters() {
        return this.urlParameters;
    }

    public Optional<String> getBody() {
        return Optional.ofNullable(this.body);
    }

    public Optional<String> getMajorUrlParameter() {
        if (this.customMajorParam != null) {
            return Optional.of(this.customMajorParam);
        }
        Optional<Integer> majorParameterPosition = this.endpoint.getMajorParameterPosition();
        if (!majorParameterPosition.isPresent()) {
            return Optional.empty();
        }
        if (majorParameterPosition.get() >= this.urlParameters.length) {
            return Optional.empty();
        }
        return Optional.of(this.urlParameters[majorParameterPosition.get()]);
    }

    public Exception getOrigin() {
        return this.origin;
    }

    public RestRequest<T> addQueryParameter(String key, String value) {
        this.queryParameters.put(key, value);
        return this;
    }

    public RestRequest<T> addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public RestRequest<T> setAuditLogReason(String reason) {
        if (reason != null) {
            try {
                this.addHeader("X-Audit-Log-Reason", URLEncoder.encode(reason, StandardCharsets.UTF_8.name()).replace("+", " "));
            }
            catch (UnsupportedEncodingException e) {
                throw new AssertionError((Object)e);
            }
        }
        return this;
    }

    public RestRequest<T> setUrlParameters(String ... parameters) {
        this.urlParameters = parameters;
        return this;
    }

    public RestRequest<T> setMultipartBody(MultipartBody multipartBody) {
        this.multipartBody = multipartBody;
        return this;
    }

    public RestRequest<T> setCustomMajorParam(String customMajorParam) {
        this.customMajorParam = customMajorParam;
        return this;
    }

    public RestRequest<T> setBody(JsonNode body) {
        return this.setBody(body.toString());
    }

    public RestRequest<T> setBody(String body) {
        this.body = body;
        return this;
    }

    public RestRequest<T> includeAuthorizationHeader(boolean includeAuthorizationHeader) {
        this.includeAuthorizationHeader = includeAuthorizationHeader;
        return this;
    }

    public RestRequest<T> consumeGlobalRatelimit(boolean consumeGlobalRatelimit) {
        this.consumeGlobalRatelimit = consumeGlobalRatelimit;
        return this;
    }

    public CompletableFuture<T> execute(Function<RestRequestResult, T> function) {
        this.api.getRatelimitManager().queueRequest(this);
        CompletableFuture future = new CompletableFuture();
        this.result.whenComplete((result, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally((Throwable)throwable);
                return;
            }
            try {
                future.complete(function.apply((RestRequestResult)result));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public CompletableFuture<RestRequestResult> getResult() {
        return this.result;
    }

    public RestRequestInformation asRestRequestInformation() {
        try {
            return new RestRequestInformationImpl(this.api, new URL(this.endpoint.getFullUrl(this.urlParameters)), this.queryParameters, this.headers, this.body);
        }
        catch (MalformedURLException e) {
            throw new AssertionError((Object)e);
        }
    }

    public RestRequestResult executeBlocking() throws Exception {
        if (this.consumeGlobalRatelimit) {
            this.api.getGlobalRatelimiter().ifPresent(ratelimiter -> {
                try {
                    ratelimiter.requestQuota();
                }
                catch (InterruptedException e) {
                    logger.warn("Encountered unexpected ratelimiter interrupt", (Throwable)e);
                }
            });
        }
        Request.Builder requestBuilder = new Request.Builder();
        HttpUrl.Builder httpUrlBuilder = this.endpoint.getOkHttpUrl(this.urlParameters).newBuilder();
        this.queryParameters.forEach(httpUrlBuilder::addQueryParameter);
        requestBuilder.url(httpUrlBuilder.build());
        RequestBody requestBody = this.multipartBody != null ? this.multipartBody : (this.body != null ? RequestBody.create(MediaType.parse("application/json"), this.body) : RequestBody.create(null, new byte[0]));
        switch (this.method) {
            case GET: {
                requestBuilder.get();
                break;
            }
            case POST: {
                requestBuilder.post(requestBody);
                break;
            }
            case PUT: {
                requestBuilder.put(requestBody);
                break;
            }
            case DELETE: {
                requestBuilder.delete(requestBody);
                break;
            }
            case PATCH: {
                requestBuilder.patch(requestBody);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported http method!");
            }
        }
        if (this.includeAuthorizationHeader) {
            requestBuilder.addHeader("authorization", this.api.getPrefixedToken());
        }
        this.headers.forEach(requestBuilder::addHeader);
        Supplier[] supplierArray = new Supplier[3];
        supplierArray[0] = this.method::name;
        supplierArray[1] = () -> this.endpoint.getFullUrl(this.urlParameters);
        supplierArray[2] = () -> this.body != null ? " with body " + this.body : "";
        logger.debug("Trying to send {} request to {}{}", supplierArray);
        try (Response response = this.getApi().getHttpClient().newCall(requestBuilder.build()).execute();){
            RestRequestResult result = new RestRequestResult(this, response);
            Supplier[] supplierArray2 = new Supplier[5];
            supplierArray2[0] = this.method::name;
            supplierArray2[1] = () -> this.endpoint.getFullUrl(this.urlParameters);
            supplierArray2[2] = response::code;
            supplierArray2[3] = () -> result.getBody().map(b -> "").orElse(" empty");
            supplierArray2[4] = () -> result.getStringBody().map(s -> " " + s).orElse("");
            logger.debug("Sent {} request to {} and received status code {} with{} body{}", supplierArray2);
            if (response.code() >= 300 || response.code() < 200) {
                RestRequestInformation requestInformation = this.asRestRequestInformation();
                RestRequestResponseInformationImpl responseInformation = new RestRequestResponseInformationImpl(requestInformation, result);
                Optional<RestRequestHttpResponseCode> responseCode = RestRequestHttpResponseCode.fromCode(response.code());
                if (!result.getJsonBody().isNull() && result.getJsonBody().has("code")) {
                    int code = result.getJsonBody().get("code").asInt();
                    String message = result.getJsonBody().has("message") ? result.getJsonBody().get("message").asText() : null;
                    Optional discordException = RestRequestResultErrorCode.fromCode(code, responseCode.orElse(null)).flatMap(restRequestResultCode -> restRequestResultCode.getDiscordException(this.origin, message == null ? restRequestResultCode.getMeaning() : message, requestInformation, responseInformation));
                    if (discordException.isPresent()) {
                        throw (DiscordException)discordException.get();
                    }
                }
                switch (response.code()) {
                    case 429: {
                        RestRequestResult code = result;
                        return code;
                    }
                }
                Optional discordException = responseCode.flatMap(restRequestHttpResponseCode -> restRequestHttpResponseCode.getDiscordException(this.origin, "Received a " + response.code() + " response from Discord with" + (result.getBody().isPresent() ? "" : " empty") + " body" + result.getStringBody().map(s -> " " + s).orElse("") + "!", requestInformation, responseInformation));
                if (discordException.isPresent()) {
                    throw (DiscordException)discordException.get();
                }
                throw new DiscordException(this.origin, "Received a " + response.code() + " response from Discord with" + (result.getBody().isPresent() ? "" : " empty") + " body" + result.getStringBody().map(s -> " " + s).orElse("") + "!", requestInformation, responseInformation);
            }
            RestRequestResult restRequestResult = result;
            return restRequestResult;
        }
    }
}

