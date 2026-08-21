/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.auth;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Objects;
import okhttp3.Route;

public class OkHttpRouteImpl
implements org.javacord.api.util.auth.Route {
    private final Route route;

    public OkHttpRouteImpl(Route route) {
        this.route = route;
    }

    @Override
    public URL getUrl() {
        return this.route.address().url().url();
    }

    @Override
    public Proxy getProxy() {
        return this.route.proxy();
    }

    @Override
    public InetSocketAddress getInetSocketAddress() {
        return this.route.socketAddress();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        OkHttpRouteImpl that = (OkHttpRouteImpl)o;
        return Objects.equals(this.getUrl(), that.getUrl()) && Objects.equals(this.getProxy(), that.getProxy()) && Objects.equals(this.getInetSocketAddress(), that.getInetSocketAddress());
    }

    public int hashCode() {
        return Objects.hash(this.getUrl(), this.getProxy(), this.getInetSocketAddress());
    }

    public String toString() {
        return String.format("Route (url: %s, proxy: %s, inetSocketAddress: %s)", this.getUrl(), this.getProxy(), this.getInetSocketAddress());
    }
}

