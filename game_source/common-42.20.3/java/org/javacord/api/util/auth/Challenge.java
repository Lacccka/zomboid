/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class Challenge {
    private final String scheme;
    private final Map<String, String> authParams;

    public Challenge(String scheme, Map<String, String> authParams) {
        this.scheme = scheme.toLowerCase(Locale.US);
        HashMap<String, String> newAuthParams = new HashMap<String, String>();
        for (Map.Entry<String, String> authParam : authParams.entrySet()) {
            String key = authParam.getKey() == null ? null : authParam.getKey().toLowerCase(Locale.US);
            newAuthParams.put(key, authParam.getValue());
        }
        this.authParams = Collections.unmodifiableMap(newAuthParams);
    }

    public String getScheme() {
        return this.scheme;
    }

    public Map<String, String> getAuthParams() {
        return this.authParams;
    }

    public Optional<String> getRealm() {
        return Optional.ofNullable(this.authParams.get("realm"));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Challenge that = (Challenge)o;
        return Objects.equals(this.scheme, that.scheme) && Objects.equals(this.authParams, that.authParams);
    }

    public int hashCode() {
        return Objects.hash(this.scheme, this.authParams);
    }

    public String toString() {
        return String.format("Challenge (scheme: %s, authParams: %s)", this.scheme, this.authParams);
    }
}

