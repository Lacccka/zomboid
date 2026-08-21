/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.rest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.Logger;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestRequest;

public class RestRequestResult {
    private static final Logger logger = LoggerUtil.getLogger(RestRequestResult.class);
    private final RestRequest<?> request;
    private final Response response;
    private final ResponseBody body;
    private final String stringBody;
    private final JsonNode jsonBody;

    public RestRequestResult(RestRequest<?> request, Response response) throws IOException {
        this.request = request;
        this.response = response;
        this.body = response.body();
        if (this.body == null) {
            this.stringBody = null;
            this.jsonBody = NullNode.getInstance();
        } else {
            JsonNode jsonBody;
            this.stringBody = this.body.string();
            ObjectMapper mapper = request.getApi().getObjectMapper();
            try {
                jsonBody = mapper.readTree(this.stringBody);
            }
            catch (JsonParseException e) {
                logger.debug("Failed to parse json response", (Throwable)e);
                jsonBody = null;
            }
            this.jsonBody = jsonBody == null ? NullNode.getInstance() : jsonBody;
        }
    }

    public RestRequest<?> getRequest() {
        return this.request;
    }

    public Response getResponse() {
        return this.response;
    }

    public Optional<ResponseBody> getBody() {
        return Optional.ofNullable(this.body);
    }

    public Optional<String> getStringBody() {
        return Optional.ofNullable(this.stringBody);
    }

    public JsonNode getJsonBody() {
        return this.jsonBody;
    }
}

