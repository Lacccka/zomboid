/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.auth;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import org.javacord.api.util.auth.Route;

public class NvWebSocketRouteImpl
implements Route {
    private final URL url;
    private final Proxy proxy;
    private final InetSocketAddress inetSocketAddress;

    public NvWebSocketRouteImpl(URL url, Proxy proxy, InetSocketAddress inetSocketAddress) {
        this.url = url;
        this.proxy = proxy;
        this.inetSocketAddress = inetSocketAddress;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public Proxy getProxy() {
        return this.proxy;
    }

    @Override
    public InetSocketAddress getInetSocketAddress() {
        return this.inetSocketAddress;
    }
}

