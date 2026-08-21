/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.auth;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.javacord.api.util.auth.Request;
import org.javacord.api.util.auth.Response;
import org.javacord.api.util.auth.Route;

@FunctionalInterface
public interface Authenticator {
    public Map<String, List<String>> authenticate(Route var1, Request var2, Response var3) throws IOException;
}

