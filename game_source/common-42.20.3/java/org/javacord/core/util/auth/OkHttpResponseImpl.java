/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.auth;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpResponseImpl
implements org.javacord.api.util.auth.Response {
    private final Response response;

    public OkHttpResponseImpl(Response response) {
        this.response = response;
    }

    @Override
    public int getCode() {
        return this.response.code();
    }

    @Override
    public String getMessage() {
        return this.response.message();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return this.response.headers().toMultimap();
    }

    @Override
    public Optional<String> getBody() throws IOException {
        ResponseBody responseBody = this.response.body();
        if (responseBody == null) {
            return Optional.empty();
        }
        return Optional.of(responseBody.string());
    }

    public boolean equals(Object o) {
        try {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            OkHttpResponseImpl that = (OkHttpResponseImpl)o;
            return this.getCode() == that.getCode() && Objects.equals(this.getMessage(), that.getMessage()) && Objects.equals(this.getHeaders(), that.getHeaders()) && Objects.equals(this.getBody(), that.getBody());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int hashCode() {
        try {
            return Objects.hash(this.getCode(), this.getMessage(), this.getHeaders(), this.getBody());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        String body;
        try {
            body = this.getBody().toString();
        }
        catch (IOException e) {
            body = "<unknown>";
        }
        return String.format("Response (code: %d, message: %s, headers: %s, body: %s)", this.getCode(), this.getMessage(), this.getHeaders(), body);
    }
}

