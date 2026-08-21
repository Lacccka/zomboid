/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util.auth;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public interface Route {
    public URL getUrl();

    public Proxy getProxy();

    public InetSocketAddress getInetSocketAddress();
}

