/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.internal.DiscordApiBuilderDelegate;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.util.auth.Authenticator;
import org.javacord.api.util.ratelimit.Ratelimiter;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.gateway.DiscordWebSocketAdapter;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.logging.PrivacyProtectionLogger;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;
import org.javacord.core.util.rest.RestRequestResult;

public class DiscordApiBuilderDelegateImpl
implements DiscordApiBuilderDelegate {
    private static final Logger logger = LoggerUtil.getLogger(DiscordApiBuilderDelegateImpl.class);
    private volatile Ratelimiter globalRatelimiter;
    private volatile boolean dispatchEvents = true;
    private volatile Ratelimiter gatewayIdentifyRatelimiter;
    private volatile ProxySelector proxySelector;
    private volatile Proxy proxy;
    private volatile Authenticator proxyAuthenticator;
    private volatile boolean trustAllCertificates = false;
    private volatile String token = null;
    private final AtomicInteger currentShard = new AtomicInteger();
    private final AtomicInteger totalShards = new AtomicInteger(1);
    private final AtomicInteger retryAttempt = new AtomicInteger();
    private volatile boolean waitForServersOnStartup = true;
    private volatile boolean waitForUsersOnStartup = false;
    private volatile boolean registerShutdownHook = true;
    private Set<Intent> intents = Arrays.stream(Intent.values()).filter(intent -> !intent.isPrivileged()).collect(Collectors.toCollection(HashSet::new));
    private boolean userCacheEnabled = true;
    private final Map<Class<? extends GloballyAttachableListener>, List<GloballyAttachableListener>> listeners = new ConcurrentHashMap<Class<? extends GloballyAttachableListener>, List<GloballyAttachableListener>>();
    private final Map<Class<? extends GloballyAttachableListener>, List<Supplier<? extends GloballyAttachableListener>>> listenerSuppliers = new ConcurrentHashMap<Class<? extends GloballyAttachableListener>, List<Supplier<? extends GloballyAttachableListener>>>();
    private final Map<Class<? extends GloballyAttachableListener>, List<Function<DiscordApi, ? extends GloballyAttachableListener>>> listenerFunctions = new ConcurrentHashMap<Class<? extends GloballyAttachableListener>, List<Function<DiscordApi, ? extends GloballyAttachableListener>>>();
    private final List<GloballyAttachableListener> unspecifiedListeners = new CopyOnWriteArrayList<GloballyAttachableListener>();
    private final List<Supplier<GloballyAttachableListener>> unspecifiedListenerSuppliers = new CopyOnWriteArrayList<Supplier<GloballyAttachableListener>>();
    private final List<Function<DiscordApi, GloballyAttachableListener>> unspecifiedListenerFunctions = new CopyOnWriteArrayList<Function<DiscordApi, GloballyAttachableListener>>();
    private volatile Map<Class<? extends GloballyAttachableListener>, List<Function<DiscordApi, GloballyAttachableListener>>> preparedListeners;
    private volatile List<Function<DiscordApi, GloballyAttachableListener>> preparedUnspecifiedListeners;

    @Override
    public CompletableFuture<DiscordApi> login() {
        this.prepareListeners();
        logger.debug("Creating shard {} of {}", (Object)(this.currentShard.get() + 1), (Object)this.totalShards.get());
        CompletableFuture<DiscordApi> future = new CompletableFuture<DiscordApi>();
        if (this.token == null) {
            future.completeExceptionally(new IllegalArgumentException("You cannot login without a token!"));
            return future;
        }
        try (CloseableThreadContext.Instance closeableThreadContextInstance = CloseableThreadContext.put("shard", Integer.toString(this.currentShard.get()));){
            new DiscordApiImpl(this.token, this.currentShard.get(), this.totalShards.get(), this.intents, this.waitForServersOnStartup, this.waitForUsersOnStartup, this.registerShutdownHook, this.globalRatelimiter, this.gatewayIdentifyRatelimiter, this.proxySelector, this.proxy, this.proxyAuthenticator, this.trustAllCertificates, future, null, this.preparedListeners, this.preparedUnspecifiedListeners, this.userCacheEnabled, this.dispatchEvents);
        }
        return future;
    }

    private void prepareListeners() {
        if (this.preparedListeners != null && this.preparedUnspecifiedListeners != null) {
            return;
        }
        this.preparedListeners = new ConcurrentHashMap<Class<? extends GloballyAttachableListener>, List<Function<DiscordApi, GloballyAttachableListener>>>();
        Stream eventTypes = Stream.concat(this.listeners.keySet().stream(), Stream.concat(this.listenerSuppliers.keySet().stream(), this.listenerFunctions.keySet().stream())).distinct();
        eventTypes.forEach(type -> {
            ArrayList typeListenerFunctions = new ArrayList();
            this.listeners.getOrDefault(type, Collections.emptyList()).forEach(listener -> typeListenerFunctions.add(api -> listener));
            this.listenerSuppliers.getOrDefault(type, Collections.emptyList()).forEach(supplier -> typeListenerFunctions.add(arg_0 -> DiscordApiBuilderDelegateImpl.lambda$prepareListeners$3((Supplier)supplier, arg_0)));
            this.listenerFunctions.getOrDefault(type, Collections.emptyList()).forEach(function -> typeListenerFunctions.add(function));
            this.preparedListeners.put((Class<? extends GloballyAttachableListener>)type, typeListenerFunctions);
        });
        this.preparedUnspecifiedListeners = new CopyOnWriteArrayList<Function<DiscordApi, GloballyAttachableListener>>(this.unspecifiedListenerFunctions);
        this.unspecifiedListenerSuppliers.forEach(supplier -> this.preparedUnspecifiedListeners.add(arg_0 -> DiscordApiBuilderDelegateImpl.lambda$prepareListeners$7((Supplier)supplier, arg_0)));
        this.unspecifiedListeners.forEach(listener -> this.preparedUnspecifiedListeners.add(api -> listener));
    }

    @Override
    public List<CompletableFuture<DiscordApi>> loginShards(int ... shards) {
        Objects.requireNonNull(shards);
        if (shards.length == 0) {
            return Collections.emptyList();
        }
        if (Arrays.stream(shards).distinct().count() != (long)shards.length) {
            throw new IllegalArgumentException("shards cannot be started multiple times!");
        }
        if (Arrays.stream(shards).max().orElseThrow(AssertionError::new) >= this.getTotalShards()) {
            throw new IllegalArgumentException("shard cannot be greater or equal than totalShards!");
        }
        if (Arrays.stream(shards).min().orElseThrow(AssertionError::new) < 0) {
            throw new IllegalArgumentException("shard cannot be less than 0!");
        }
        if (shards.length == this.getTotalShards()) {
            logger.info("Creating {} {}", (Object)this.getTotalShards(), (Object)(this.getTotalShards() == 1 ? "shard" : "shards"));
        } else {
            logger.info("Creating {} out of {} shards ({})", (Object)shards.length, (Object)this.getTotalShards(), (Object)shards);
        }
        ArrayList<CompletableFuture<DiscordApi>> result = new ArrayList<CompletableFuture<DiscordApi>>(shards.length);
        int currentShard = this.getCurrentShard();
        for (int shard : shards) {
            if (currentShard != 0) {
                CompletableFuture future = new CompletableFuture();
                future.completeExceptionally(new IllegalArgumentException("You cannot use loginShards or loginAllShards after setting the current shard!"));
                result.add(future);
                continue;
            }
            this.setCurrentShard(shard);
            result.add(this.login());
        }
        this.setCurrentShard(currentShard);
        return result;
    }

    @Override
    public void setGlobalRatelimiter(Ratelimiter ratelimiter) {
        this.globalRatelimiter = ratelimiter;
    }

    @Override
    public void setEventsDispatchable(boolean dispatchEvents) {
        this.dispatchEvents = dispatchEvents;
    }

    @Override
    public void setGatewayIdentifyRatelimiter(Ratelimiter ratelimiter) {
        this.gatewayIdentifyRatelimiter = ratelimiter;
    }

    @Override
    public void setProxySelector(ProxySelector proxySelector) {
        this.proxySelector = proxySelector;
    }

    @Override
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void setProxyAuthenticator(Authenticator authenticator) {
        this.proxyAuthenticator = authenticator;
    }

    @Override
    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
        PrivacyProtectionLogger.addPrivateData(token);
    }

    @Override
    public Optional<String> getToken() {
        return Optional.ofNullable(this.token);
    }

    @Override
    public void setTotalShards(int totalShards) {
        if (this.currentShard.get() >= totalShards) {
            throw new IllegalArgumentException("currentShard cannot be greater or equal than totalShards!");
        }
        if (totalShards < 1) {
            throw new IllegalArgumentException("totalShards cannot be less than 1!");
        }
        this.totalShards.set(totalShards);
    }

    @Override
    public int getTotalShards() {
        return this.totalShards.get();
    }

    @Override
    public void setCurrentShard(int currentShard) {
        if (currentShard >= this.totalShards.get()) {
            throw new IllegalArgumentException("currentShard cannot be greater or equal than totalShards!");
        }
        if (currentShard < 0) {
            throw new IllegalArgumentException("currentShard cannot be less than 0!");
        }
        this.currentShard.set(currentShard);
    }

    @Override
    public int getCurrentShard() {
        return this.currentShard.get();
    }

    @Override
    public void setWaitForServersOnStartup(boolean waitForServersOnStartup) {
        this.waitForServersOnStartup = waitForServersOnStartup;
    }

    @Override
    public boolean isWaitingForServersOnStartup() {
        return this.waitForServersOnStartup;
    }

    @Override
    public void setWaitForUsersOnStartup(boolean waitForUsersOnStartup) {
        this.waitForUsersOnStartup = waitForUsersOnStartup;
    }

    @Override
    public boolean isWaitingForUsersOnStartup() {
        return this.waitForUsersOnStartup;
    }

    @Override
    public void setShutdownHookRegistrationEnabled(boolean registerShutdownHook) {
        this.registerShutdownHook = registerShutdownHook;
    }

    @Override
    public boolean isShutdownHookRegistrationEnabled() {
        return this.registerShutdownHook;
    }

    @Override
    public void addIntents(Intent ... intents) {
        this.intents.addAll(Arrays.asList(intents));
    }

    @Override
    public void setAllIntentsWhere(Predicate<Intent> condition) {
        this.intents = new HashSet<Intent>();
        for (Intent value : Intent.values()) {
            if (!condition.test(value)) continue;
            this.intents.add(value);
        }
    }

    @Override
    public void setUserCacheEnabled(boolean enabled) {
        this.userCacheEnabled = enabled;
    }

    @Override
    public boolean isUserCacheEnabled() {
        return this.userCacheEnabled;
    }

    @Override
    public CompletableFuture<Void> setRecommendedTotalShards() {
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        if (this.token == null) {
            future.completeExceptionally(new IllegalArgumentException("You cannot request the recommended total shards without a token!"));
        } else {
            this.retryAttempt.set(0);
            this.setRecommendedTotalShards(future);
        }
        return future;
    }

    private void setRecommendedTotalShards(CompletableFuture<Void> future) {
        DiscordApiImpl api = new DiscordApiImpl(this.token, this.globalRatelimiter, this.gatewayIdentifyRatelimiter, this.proxySelector, this.proxy, this.proxyAuthenticator, this.trustAllCertificates);
        RestRequest<JsonNode> botGatewayRequest = new RestRequest<JsonNode>(api, RestMethod.GET, RestEndpoint.GATEWAY_BOT);
        ((CompletableFuture)((CompletableFuture)botGatewayRequest.execute(RestRequestResult::getJsonBody).thenAccept(resultJson -> {
            DiscordWebSocketAdapter.setGateway(resultJson.get("url").asText());
            this.setTotalShards(resultJson.get("shards").asInt());
            this.retryAttempt.set(0);
            future.complete(null);
        })).exceptionally(t -> {
            int retryDelay = api.getReconnectDelay(this.retryAttempt.incrementAndGet());
            logger.info("Retrying to get recommended total shards in {} seconds!", (Object)retryDelay);
            api.getThreadPool().getScheduler().schedule(() -> this.setRecommendedTotalShards(future), (long)retryDelay, TimeUnit.SECONDS);
            return null;
        })).whenComplete((nothing, throwable) -> api.disconnect());
    }

    @Override
    public <T extends GloballyAttachableListener> void addListener(Class<T> listenerClass, T listener) {
        this.listeners.computeIfAbsent(listenerClass, clazz -> new CopyOnWriteArrayList());
        List<GloballyAttachableListener> listeners = this.listeners.get(listenerClass);
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void addListener(GloballyAttachableListener listener) {
        if (!this.unspecifiedListeners.contains(listener)) {
            this.unspecifiedListeners.add(listener);
        }
    }

    @Override
    public <T extends GloballyAttachableListener> void addListener(Class<T> listenerClass, Supplier<T> listenerSupplier) {
        this.listenerSuppliers.computeIfAbsent(listenerClass, clazz -> new CopyOnWriteArrayList());
        List<Supplier<? extends GloballyAttachableListener>> listeners = this.listenerSuppliers.get(listenerClass);
        if (!listeners.contains(listenerSupplier)) {
            listeners.add(listenerSupplier);
        }
    }

    @Override
    public void addListener(Supplier<GloballyAttachableListener> listenerSupplier) {
        if (!this.unspecifiedListenerSuppliers.contains(listenerSupplier)) {
            this.unspecifiedListenerSuppliers.add(listenerSupplier);
        }
    }

    @Override
    public <T extends GloballyAttachableListener> void addListener(Class<T> listenerClass, Function<DiscordApi, T> listenerFunction) {
        this.listenerFunctions.computeIfAbsent(listenerClass, clazz -> new CopyOnWriteArrayList());
        List<Function<DiscordApi, ? extends GloballyAttachableListener>> functions = this.listenerFunctions.get(listenerClass);
        if (!functions.contains(listenerFunction)) {
            functions.add(listenerFunction);
        }
    }

    @Override
    public void addListener(Function<DiscordApi, GloballyAttachableListener> listenerFunction) {
        if (!this.unspecifiedListenerFunctions.contains(listenerFunction)) {
            this.unspecifiedListenerFunctions.add(listenerFunction);
        }
    }

    @Override
    public void removeListener(GloballyAttachableListener listener) {
        this.unspecifiedListeners.remove(listener);
    }

    @Override
    public <T extends GloballyAttachableListener> void removeListener(Class<T> listenerClass, T listener) {
        this.listeners.computeIfPresent(listenerClass, (clazz, listeners) -> {
            listeners.remove(listener);
            return listeners.isEmpty() ? null : listeners;
        });
    }

    @Override
    public void removeListenerSupplier(Supplier<GloballyAttachableListener> listenerSupplier) {
        this.unspecifiedListenerSuppliers.remove(listenerSupplier);
    }

    @Override
    public <T extends GloballyAttachableListener> void removeListenerSupplier(Class<T> listenerClass, Supplier<T> listenerSupplier) {
        this.listenerSuppliers.computeIfPresent(listenerClass, (clazz, suppliers) -> {
            suppliers.remove(listenerSupplier);
            return suppliers.isEmpty() ? null : suppliers;
        });
    }

    @Override
    public void removeListenerFunction(Function<DiscordApi, GloballyAttachableListener> listenerFunction) {
        this.unspecifiedListenerFunctions.remove(listenerFunction);
    }

    @Override
    public <T extends GloballyAttachableListener> void removeListenerFunction(Class<T> listenerClass, Function<DiscordApi, T> listenerFunction) {
        this.listenerFunctions.computeIfPresent(listenerClass, (clazz, functions) -> {
            functions.remove(listenerFunction);
            return functions.isEmpty() ? null : functions;
        });
    }

    private static /* synthetic */ GloballyAttachableListener lambda$prepareListeners$7(Supplier supplier, DiscordApi api) {
        return (GloballyAttachableListener)supplier.get();
    }

    private static /* synthetic */ GloballyAttachableListener lambda$prepareListeners$3(Supplier supplier, DiscordApi api) {
        return (GloballyAttachableListener)supplier.get();
    }
}

