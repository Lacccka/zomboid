/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.javacord.api.DiscordApi;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestRequest;

public class RatelimitBucket {
    private static final Map<String, Long> globalRatelimitResetTimestamp = new ConcurrentHashMap<String, Long>();
    private final DiscordApiImpl api;
    private final ConcurrentLinkedQueue<RestRequest<?>> requestQueue = new ConcurrentLinkedQueue();
    private final RestEndpoint endpoint;
    private final String majorUrlParameter;
    private volatile long ratelimitResetTimestamp = 0L;
    private volatile int ratelimitRemaining = 1;

    public RatelimitBucket(DiscordApi api, RestEndpoint endpoint) {
        this(api, endpoint, null);
    }

    public RatelimitBucket(DiscordApi api, RestEndpoint endpoint, String majorUrlParameter) {
        this.api = (DiscordApiImpl)api;
        this.endpoint = endpoint;
        this.majorUrlParameter = majorUrlParameter;
    }

    public static void setGlobalRatelimitResetTimestamp(DiscordApi api, long resetTimestamp) {
        globalRatelimitResetTimestamp.put(api.getToken(), resetTimestamp);
    }

    public void addRequestToQueue(RestRequest<?> request) {
        this.requestQueue.add(request);
    }

    public RestRequest<?> pollRequestFromQueue() {
        return this.requestQueue.poll();
    }

    public RestRequest<?> peekRequestFromQueue() {
        return this.requestQueue.peek();
    }

    public void setRatelimitRemaining(int ratelimitRemaining) {
        this.ratelimitRemaining = ratelimitRemaining;
    }

    public void setRatelimitResetTimestamp(long ratelimitResetTimestamp) {
        this.ratelimitResetTimestamp = ratelimitResetTimestamp;
    }

    public int getTimeTillSpaceGetsAvailable() {
        long globalRatelimitResetTimestamp = RatelimitBucket.globalRatelimitResetTimestamp.getOrDefault(this.api.getToken(), 0L);
        long timestamp = System.currentTimeMillis() + (this.api.getTimeOffset() == null ? 0L : this.api.getTimeOffset());
        if (this.ratelimitRemaining > 0 && globalRatelimitResetTimestamp - timestamp <= 0L) {
            return 0;
        }
        return (int)(Math.max(this.ratelimitResetTimestamp, globalRatelimitResetTimestamp) - timestamp);
    }

    public boolean equals(RestEndpoint endpoint, String majorUrlParameter) {
        boolean endpointSame = this.endpoint == endpoint;
        boolean majorUrlParameterBothNull = this.majorUrlParameter == null && majorUrlParameter == null;
        boolean majorUrlParameterEqual = this.majorUrlParameter != null && this.majorUrlParameter.equals(majorUrlParameter);
        return endpointSame && (majorUrlParameterBothNull || majorUrlParameterEqual);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof RatelimitBucket)) {
            return false;
        }
        RatelimitBucket otherBucket = (RatelimitBucket)obj;
        return this.equals(otherBucket.endpoint, otherBucket.majorUrlParameter);
    }

    public int hashCode() {
        int hash = 42;
        int urlParamHash = this.majorUrlParameter == null ? 0 : this.majorUrlParameter.hashCode();
        int endpointHash = this.endpoint == null ? 0 : this.endpoint.hashCode();
        hash = hash * 11 + urlParamHash;
        hash = hash * 17 + endpointHash;
        return hash;
    }

    public String toString() {
        String str = "Endpoint: " + (this.endpoint == null ? "global" : this.endpoint.getEndpointUrl());
        str = str + ", Major url parameter:" + (this.majorUrlParameter == null ? "none" : this.majorUrlParameter);
        return str;
    }
}

