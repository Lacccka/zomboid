/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.auth;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import okhttp3.RequestBody;
import okio.Buffer;
import org.javacord.api.util.auth.Request;

public class OkHttpRequestImpl
implements Request {
    private final okhttp3.Request request;

    public OkHttpRequestImpl(okhttp3.Request request) {
        this.request = request;
    }

    @Override
    public String getMethod() {
        return this.request.method();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return this.request.headers().toMultimap();
    }

    @Override
    public List<String> getHeaders(String headerName) {
        return this.request.headers(headerName);
    }

    @Override
    public Optional<String> getBody() throws IOException {
        RequestBody requestBody = this.request.body();
        if (requestBody == null) {
            return Optional.empty();
        }
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        return Optional.of(buffer.readUtf8());
    }

    public boolean equals(Object o) {
        try {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            OkHttpRequestImpl that = (OkHttpRequestImpl)o;
            return Objects.equals(this.getMethod(), that.getMethod()) && Objects.equals(this.getHeaders(), that.getHeaders()) && Objects.equals(this.getBody(), that.getBody());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int hashCode() {
        try {
            return Objects.hash(this.getMethod(), this.getHeaders(), this.getBody());
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
        return String.format("Request (method: %s, headers: %s, body: %s)", this.getMethod(), this.getHeaders(), body);
    }
}

