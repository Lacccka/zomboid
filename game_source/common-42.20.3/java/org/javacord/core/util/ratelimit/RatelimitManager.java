/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.ratelimit;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.javacord.api.exception.DiscordException;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.ratelimit.RatelimitBucket;
import org.javacord.core.util.rest.RestRequest;
import org.javacord.core.util.rest.RestRequestResponseInformationImpl;
import org.javacord.core.util.rest.RestRequestResult;

public class RatelimitManager {
    private static final Logger logger = LoggerUtil.getLogger(RatelimitManager.class);
    private final DiscordApiImpl api;
    private final Set<RatelimitBucket> buckets = new HashSet<RatelimitBucket>();

    public RatelimitManager(DiscordApiImpl api) {
        this.api = api;
    }

    public Set<RatelimitBucket> getBuckets() {
        return this.buckets;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void queueRequest(RestRequest<?> request) {
        boolean alreadyInQueue;
        RatelimitBucket bucket;
        Set<RatelimitBucket> set = this.buckets;
        synchronized (set) {
            bucket = this.buckets.stream().filter(b -> b.equals(request.getEndpoint(), request.getMajorUrlParameter().orElse(null))).findAny().orElseGet(() -> new RatelimitBucket(this.api, request.getEndpoint(), request.getMajorUrlParameter().orElse(null)));
            alreadyInQueue = bucket.peekRequestFromQueue() != null;
            this.buckets.add(bucket);
            bucket.addRequestToQueue(request);
        }
        if (alreadyInQueue) {
            return;
        }
        this.api.getThreadPool().getExecutorService().submit(() -> {
            RestRequest<?> currentRequest = bucket.peekRequestFromQueue();
            RestRequestResult result = null;
            long responseTimestamp = System.currentTimeMillis();
            while (currentRequest != null) {
                int sleepTime = bucket.getTimeTillSpaceGetsAvailable();
                if (sleepTime > 0) {
                    logger.debug("Delaying requests to {} for {}ms to prevent hitting ratelimits", (Object)bucket, (Object)sleepTime);
                }
                while (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    }
                    catch (InterruptedException e) {
                        logger.warn("We got interrupted while waiting for a rate limit!", (Throwable)e);
                    }
                    sleepTime = bucket.getTimeTillSpaceGetsAvailable();
                }
                result = currentRequest.executeBlocking();
                responseTimestamp = System.currentTimeMillis();
                try {
                    this.calculateOffset(responseTimestamp, result);
                    this.handleResponse(currentRequest, result, bucket, responseTimestamp);
                }
                catch (Throwable t) {
                    logger.warn("Encountered unexpected exception.", t);
                }
                if (!currentRequest.getResult().isDone()) continue;
                Set<RatelimitBucket> t = this.buckets;
                synchronized (t) {
                    bucket.pollRequestFromQueue();
                    currentRequest = bucket.peekRequestFromQueue();
                    if (currentRequest == null) {
                        this.buckets.remove(bucket);
                    }
                    continue;
                }
                catch (Throwable t2) {
                    try {
                        responseTimestamp = System.currentTimeMillis();
                        if (currentRequest.getResult().isDone()) {
                            logger.warn("Received exception for a request that is already done. This should not be able to happen!", t2);
                        }
                        if (t2 instanceof DiscordException) {
                            result = ((DiscordException)t2).getResponse().map(RestRequestResponseInformationImpl.class::cast).map(RestRequestResponseInformationImpl::getRestRequestResult).orElse(null);
                        }
                        currentRequest.getResult().completeExceptionally(t2);
                    }
                    catch (Throwable throwable) {
                        try {
                            this.calculateOffset(responseTimestamp, result);
                            this.handleResponse(currentRequest, result, bucket, responseTimestamp);
                        }
                        catch (Throwable t3) {
                            logger.warn("Encountered unexpected exception.", t3);
                        }
                        if (!currentRequest.getResult().isDone()) continue;
                        Set<RatelimitBucket> set = this.buckets;
                        synchronized (set) {
                            bucket.pollRequestFromQueue();
                            currentRequest = bucket.peekRequestFromQueue();
                            if (currentRequest == null) {
                                this.buckets.remove(bucket);
                            }
                        }
                        throw throwable;
                    }
                    try {
                        this.calculateOffset(responseTimestamp, result);
                        this.handleResponse(currentRequest, result, bucket, responseTimestamp);
                    }
                    catch (Throwable t4) {
                        logger.warn("Encountered unexpected exception.", t4);
                    }
                    if (!currentRequest.getResult().isDone()) continue;
                    Set<RatelimitBucket> set = this.buckets;
                    synchronized (set) {
                        bucket.pollRequestFromQueue();
                        currentRequest = bucket.peekRequestFromQueue();
                        if (currentRequest == null) {
                            this.buckets.remove(bucket);
                        }
                    }
                }
            }
        });
    }

    private void handleResponse(RestRequest<?> request, RestRequestResult result, RatelimitBucket bucket, long responseTimestamp) {
        if (result == null || result.getResponse() == null) {
            return;
        }
        Response response = result.getResponse();
        boolean global = response.header("X-RateLimit-Global", "false").equalsIgnoreCase("true");
        int remaining = Integer.parseInt(response.header("X-RateLimit-Remaining", "1"));
        long reset = (long)(Double.parseDouble(response.header("X-RateLimit-Reset", "0")) * 1000.0);
        if (result.getResponse().code() == 429) {
            long retryAfter;
            if (response.header("Via") == null) {
                logger.warn("Hit a CloudFlare API ban! This means you were sending a very large amount of invalid requests.");
                long retryAfter2 = Long.parseLong(response.header("Retry-after")) * 1000L;
                RatelimitBucket.setGlobalRatelimitResetTimestamp(this.api, responseTimestamp + retryAfter2);
                return;
            }
            long l = retryAfter = result.getJsonBody().isNull() ? 0L : (long)(result.getJsonBody().get("retry_after").asDouble() * 1000.0);
            if (global) {
                logger.warn("Hit a global ratelimit! This means you were sending a very large amount within a very short time frame.");
                RatelimitBucket.setGlobalRatelimitResetTimestamp(this.api, responseTimestamp + retryAfter);
            } else {
                logger.debug("Received a 429 response from Discord! Recalculating time offset...");
                this.api.setTimeOffset(null);
                bucket.setRatelimitRemaining(0);
                bucket.setRatelimitResetTimestamp(responseTimestamp + retryAfter);
            }
        } else {
            CompletableFuture<RestRequestResult> requestResult = request.getResult();
            if (!requestResult.isDone()) {
                requestResult.complete(result);
            }
            bucket.setRatelimitRemaining(remaining);
            bucket.setRatelimitResetTimestamp(reset);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void calculateOffset(long currentTime, RestRequestResult result) {
        if (this.api.getTimeOffset() != null || result == null || result.getResponse() == null) {
            return;
        }
        DiscordApiImpl discordApiImpl = this.api;
        synchronized (discordApiImpl) {
            String date;
            if (this.api.getTimeOffset() == null && (date = result.getResponse().header("Date")) != null) {
                long discordTimestamp = OffsetDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
                this.api.setTimeOffset(discordTimestamp - currentTime);
                Supplier[] supplierArray = new Supplier[1];
                supplierArray[0] = this.api::getTimeOffset;
                logger.debug("Calculated an offset of {} to the Discord time.", supplierArray);
            }
        }
    }
}

