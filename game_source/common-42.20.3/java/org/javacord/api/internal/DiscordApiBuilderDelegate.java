/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.internal;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.util.auth.Authenticator;
import org.javacord.api.util.ratelimit.Ratelimiter;

public interface DiscordApiBuilderDelegate {
    public void setGlobalRatelimiter(Ratelimiter var1);

    public void setEventsDispatchable(boolean var1);

    public void setGatewayIdentifyRatelimiter(Ratelimiter var1);

    public void setProxySelector(ProxySelector var1);

    public void setProxy(Proxy var1);

    public void setProxyAuthenticator(Authenticator var1);

    public void setTrustAllCertificates(boolean var1);

    public void setToken(String var1);

    public Optional<String> getToken();

    public void setTotalShards(int var1);

    public int getTotalShards();

    public void setCurrentShard(int var1);

    public int getCurrentShard();

    public void setWaitForServersOnStartup(boolean var1);

    public boolean isWaitingForServersOnStartup();

    public void setWaitForUsersOnStartup(boolean var1);

    public boolean isWaitingForUsersOnStartup();

    public void setShutdownHookRegistrationEnabled(boolean var1);

    public boolean isShutdownHookRegistrationEnabled();

    public void addIntents(Intent ... var1);

    public void setAllIntentsWhere(Predicate<Intent> var1);

    public void setUserCacheEnabled(boolean var1);

    public boolean isUserCacheEnabled();

    public CompletableFuture<DiscordApi> login();

    public List<CompletableFuture<DiscordApi>> loginShards(int ... var1);

    public CompletableFuture<Void> setRecommendedTotalShards();

    public <T extends GloballyAttachableListener> void addListener(Class<T> var1, T var2);

    public void addListener(GloballyAttachableListener var1);

    public <T extends GloballyAttachableListener> void addListener(Class<T> var1, Supplier<T> var2);

    public void addListener(Supplier<GloballyAttachableListener> var1);

    public <T extends GloballyAttachableListener> void addListener(Class<T> var1, Function<DiscordApi, T> var2);

    public void addListener(Function<DiscordApi, GloballyAttachableListener> var1);

    public void removeListener(GloballyAttachableListener var1);

    public <T extends GloballyAttachableListener> void removeListener(Class<T> var1, T var2);

    public void removeListenerSupplier(Supplier<GloballyAttachableListener> var1);

    public <T extends GloballyAttachableListener> void removeListenerSupplier(Class<T> var1, Supplier<T> var2);

    public void removeListenerFunction(Function<DiscordApi, GloballyAttachableListener> var1);

    public <T extends GloballyAttachableListener> void removeListenerFunction(Class<T> var1, Function<DiscordApi, T> var2);
}

