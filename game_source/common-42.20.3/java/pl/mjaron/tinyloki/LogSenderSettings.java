/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

public class LogSenderSettings {
    public static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private String url = null;
    private String user = null;
    private String password = null;
    private String contentType = "application/json";
    private String contentEncoding = null;
    private int connectTimeout = 5000;

    public String getUrl() {
        return this.url;
    }

    public LogSenderSettings setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUser() {
        return this.user;
    }

    public LogSenderSettings setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return this.password;
    }

    public LogSenderSettings setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getContentType() {
        return this.contentType;
    }

    public LogSenderSettings setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentEncoding() {
        return this.contentEncoding;
    }

    public LogSenderSettings setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public LogSenderSettings setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }
}

