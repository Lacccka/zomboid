/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Request {
    default public String getMethod() {
        return "GET";
    }

    default public Map<String, List<String>> getHeaders() {
        return Collections.emptyMap();
    }

    default public List<String> getHeaders(String headerName) {
        return this.getHeaders().get(headerName);
    }

    default public Optional<String> getBody() throws IOException {
        return Optional.empty();
    }
}

