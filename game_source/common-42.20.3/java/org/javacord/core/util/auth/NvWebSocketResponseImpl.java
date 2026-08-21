/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.auth;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.javacord.api.util.auth.Response;

public class NvWebSocketResponseImpl
implements Response {
    @Override
    public int getCode() {
        return 407;
    }

    @Override
    public String getMessage() {
        return "Proxy Authentication Required";
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return Collections.singletonMap("Proxy-Authenticate", Collections.singletonList("Basic realm=proxy"));
    }
}

