/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.internal.DiscordApiBuilderDelegate;
import org.javacord.api.listener.ChainableGloballyAttachableListenerManager;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.util.auth.Authenticator;
import org.javacord.api.util.internal.DelegateFactory;
import org.javacord.api.util.ratelimit.Ratelimiter;

public class DiscordApiBuilder
implements ChainableGloballyAttachableListenerManager {
    private final DiscordApiBuilderDelegate delegate = DelegateFactory.createDiscordApiBuilderDelegate();

    public CompletableFuture<DiscordApi> login() {
        return this.delegate.login();
    }

    public List<CompletableFuture<DiscordApi>> loginAllShards() {
        return this.loginShards(shard -> true);
    }

    public List<CompletableFuture<DiscordApi>> loginShards(IntPredicate shardsCondition) {
        return this.loginShards(IntStream.range(0, this.delegate.getTotalShards()).filter(shardsCondition).toArray());
    }

    public List<CompletableFuture<DiscordApi>> loginShards(int ... shards) {
        return this.delegate.loginShards(shards);
    }

    public DiscordApiBuilder setGlobalRatelimiter(Ratelimiter ratelimiter) {
        this.delegate.setGlobalRatelimiter(ratelimiter);
        return this;
    }

    public DiscordApiBuilder setGatewayIdentifyRatelimiter(Ratelimiter ratelimiter) {
        this.delegate.setGatewayIdentifyRatelimiter(ratelimiter);
        return this;
    }

    public DiscordApiBuilder setEventsDispatchable(boolean dispatchEvents) {
        this.delegate.setEventsDispatchable(dispatchEvents);
        return this;
    }

    public DiscordApiBuilder setProxySelector(ProxySelector proxySelector) {
        this.delegate.setProxySelector(proxySelector);
        return this;
    }

    public DiscordApiBuilder setProxy(Proxy proxy) {
        this.delegate.setProxy(proxy);
        return this;
    }

    public DiscordApiBuilder setProxyAuthenticator(Authenticator authenticator) {
        this.delegate.setProxyAuthenticator(authenticator);
        return this;
    }

    public DiscordApiBuilder setTrustAllCertificates(boolean trustAllCertificates) {
        this.delegate.setTrustAllCertificates(trustAllCertificates);
        return this;
    }

    public DiscordApiBuilder setToken(String token) {
        this.delegate.setToken(token);
        return this;
    }

    public Optional<String> getToken() {
        return this.delegate.getToken();
    }

    public DiscordApiBuilder setTotalShards(int totalShards) {
        this.delegate.setTotalShards(totalShards);
        return this;
    }

    public int getTotalShards() {
        return this.delegate.getTotalShards();
    }

    public DiscordApiBuilder setCurrentShard(int currentShard) {
        this.delegate.setCurrentShard(currentShard);
        return this;
    }

    public int getCurrentShard() {
        return this.delegate.getCurrentShard();
    }

    public DiscordApiBuilder setWaitForServersOnStartup(boolean waitForServersOnStartup) {
        this.delegate.setWaitForServersOnStartup(waitForServersOnStartup);
        return this;
    }

    public boolean isWaitingForServersOnStartup() {
        return this.delegate.isWaitingForServersOnStartup();
    }

    public DiscordApiBuilder setWaitForUsersOnStartup(boolean waitForUsersOnStartup) {
        this.delegate.setWaitForUsersOnStartup(waitForUsersOnStartup);
        return this;
    }

    public boolean isWaitingForUsersOnStartup() {
        return this.delegate.isWaitingForUsersOnStartup();
    }

    public DiscordApiBuilder setShutdownHookRegistrationEnabled(boolean registerShutdownHook) {
        this.delegate.setShutdownHookRegistrationEnabled(registerShutdownHook);
        return this;
    }

    public boolean isShutdownHookRegistrationEnabled() {
        return this.delegate.isShutdownHookRegistrationEnabled();
    }

    public DiscordApiBuilder setIntents(Intent ... intents) {
        this.setAllIntentsWhere(intent -> Arrays.asList(intents).contains(intent));
        return this;
    }

    public DiscordApiBuilder setAllIntents() {
        this.setAllIntentsWhere(intent -> true);
        return this;
    }

    public DiscordApiBuilder setAllNonPrivilegedIntents() {
        this.setAllIntentsWhere(intent -> !intent.isPrivileged());
        return this;
    }

    public DiscordApiBuilder setAllIntentsExcept(Intent ... intentsToOmit) {
        this.setAllIntentsWhere(intent -> !Arrays.asList(intentsToOmit).contains(intent));
        return this;
    }

    public DiscordApiBuilder setAllNonPrivilegedIntentsExcept(Intent ... intentsToOmit) {
        this.setAllIntentsWhere(intent -> !intent.isPrivileged() && !Arrays.asList(intentsToOmit).contains(intent));
        return this;
    }

    public DiscordApiBuilder setAllNonPrivilegedIntentsAnd(Intent ... intentsToInclude) {
        this.setAllIntentsWhere(intent -> !intent.isPrivileged());
        this.addIntents(intentsToInclude);
        return this;
    }

    public DiscordApiBuilder addIntents(Intent ... intents) {
        this.delegate.addIntents(intents);
        return this;
    }

    public DiscordApiBuilder setAllIntentsWhere(Predicate<Intent> condition) {
        this.delegate.setAllIntentsWhere(condition);
        return this;
    }

    public DiscordApiBuilder setUserCacheEnabled(boolean enabled) {
        this.delegate.setUserCacheEnabled(enabled);
        return this;
    }

    public boolean isUserCachedEnabled() {
        return this.delegate.isUserCacheEnabled();
    }

    public CompletableFuture<DiscordApiBuilder> setRecommendedTotalShards() {
        return this.delegate.setRecommendedTotalShards().thenCompose(nothing -> CompletableFuture.completedFuture(this));
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(Class<T> listenerClass, T listener) {
        this.delegate.addListener(listenerClass, listener);
        return this;
    }

    @Override
    public DiscordApiBuilder addListener(GloballyAttachableListener listener) {
        this.delegate.addListener(listener);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(Class<T> listenerClass, Supplier<T> listenerSupplier) {
        this.delegate.addListener(listenerClass, listenerSupplier);
        return this;
    }

    @Override
    public DiscordApiBuilder addListener(Supplier<GloballyAttachableListener> listenerSupplier) {
        this.delegate.addListener(listenerSupplier);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder addListener(Class<T> listenerClass, Function<DiscordApi, T> listenerFunction) {
        this.delegate.addListener(listenerClass, listenerFunction);
        return this;
    }

    @Override
    public DiscordApiBuilder addListener(Function<DiscordApi, GloballyAttachableListener> listenerFunction) {
        this.delegate.addListener(listenerFunction);
        return this;
    }

    @Override
    public DiscordApiBuilder removeListener(GloballyAttachableListener listener) {
        this.delegate.removeListener(listener);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListener(Class<T> listenerClass, T listener) {
        this.delegate.removeListener(listenerClass, listener);
        return this;
    }

    @Override
    public DiscordApiBuilder removeListenerSupplier(Supplier<GloballyAttachableListener> listenerSupplier) {
        this.delegate.removeListenerSupplier(listenerSupplier);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListenerSupplier(Class<T> listenerClass, Supplier<T> listenerSupplier) {
        this.delegate.removeListenerSupplier(listenerClass, listenerSupplier);
        return this;
    }

    @Override
    public DiscordApiBuilder removeListenerFunction(Function<DiscordApi, GloballyAttachableListener> listenerFunction) {
        this.delegate.removeListenerFunction(listenerFunction);
        return this;
    }

    @Override
    public <T extends GloballyAttachableListener> DiscordApiBuilder removeListenerFunction(Class<T> listenerClass, Function<DiscordApi, T> listenerFunction) {
        this.delegate.removeListenerFunction(listenerClass, listenerFunction);
        return this;
    }
}

