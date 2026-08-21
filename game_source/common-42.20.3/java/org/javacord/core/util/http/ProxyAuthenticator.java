/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.http;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.util.auth.Route;
import org.javacord.core.util.auth.OkHttpRequestImpl;
import org.javacord.core.util.auth.OkHttpResponseImpl;
import org.javacord.core.util.auth.OkHttpRouteImpl;

public class ProxyAuthenticator
implements okhttp3.Authenticator {
    private final org.javacord.api.util.auth.Authenticator authenticator;

    public ProxyAuthenticator() {
        this(null);
    }

    public ProxyAuthenticator(org.javacord.api.util.auth.Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public Request authenticate(okhttp3.Route route, Response response) throws IOException {
        Map<String, List<String>> requestHeaders;
        Request request = response.request();
        Map<String, List<String>> map = requestHeaders = this.authenticator == null ? this.systemDefaultAuthentication(new OkHttpRouteImpl(route), new OkHttpRequestImpl(request), new OkHttpResponseImpl(response)) : this.authenticator.authenticate(new OkHttpRouteImpl(route), new OkHttpRequestImpl(request), new OkHttpResponseImpl(response));
        if (requestHeaders == null || requestHeaders.isEmpty()) {
            return null;
        }
        Request.Builder resultBuilder = request.newBuilder();
        requestHeaders.forEach((headerName, headerValues) -> {
            if (headerValues == null) {
                resultBuilder.removeHeader((String)headerName);
                return;
            }
            if (headerValues.isEmpty()) {
                return;
            }
            String firstHeaderValue = (String)headerValues.get(0);
            if (firstHeaderValue == null) {
                resultBuilder.removeHeader((String)headerName);
            } else {
                resultBuilder.addHeader((String)headerName, firstHeaderValue);
            }
            headerValues.stream().skip(1L).forEach(headerValue -> resultBuilder.addHeader((String)headerName, (String)headerValue));
        });
        return resultBuilder.build();
    }

    private Map<String, List<String>> systemDefaultAuthentication(Route route, org.javacord.api.util.auth.Request request, org.javacord.api.util.auth.Response response) {
        InetSocketAddress proxyAddress = (InetSocketAddress)route.getProxy().address();
        String host = proxyAddress.getHostString();
        InetAddress addr = proxyAddress.getAddress();
        int port = proxyAddress.getPort();
        URL url = route.getUrl();
        String protocol = url.getProtocol();
        return response.getChallenges("basic").filter(challenge -> challenge.getRealm().isPresent()).filter(challenge -> {
            String charset = challenge.getAuthParams().get("charset");
            return charset == null || charset.equalsIgnoreCase("UTF-8");
        }).map(challenge -> {
            String realm = challenge.getRealm().orElseThrow(AssertionError::new);
            PasswordAuthentication passwordAuthentication = Authenticator.requestPasswordAuthentication(host, addr, port, protocol, realm, challenge.getScheme(), url, Authenticator.RequestorType.PROXY);
            if (passwordAuthentication != null) {
                Charset charset = challenge.getAuthParams().containsKey("charset") ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
                return Credentials.basic(passwordAuthentication.getUserName(), String.valueOf(passwordAuthentication.getPassword()), charset);
            }
            return null;
        }).filter(Objects::nonNull).filter(credentials -> request.getHeaders("Proxy-Authorization").stream().noneMatch(credentials::equals)).findAny().map(credentials -> Collections.singletonMap("Proxy-Authorization", Arrays.asList(null, credentials))).orElse(null);
    }
}

