/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.ratelimit;

public interface Ratelimiter {
    public void requestQuota() throws InterruptedException;
}

