/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogSender;
import pl.mjaron.tinyloki.LogSenderSettings;
import pl.mjaron.tinyloki.third_party.Base64Coder;

public class HttpLogSender
implements ILogSender {
    private LogSenderSettings settings;
    private ILogMonitor logMonitor;
    private URL url;

    @Override
    public void configure(LogSenderSettings logSenderSettings, ILogMonitor logMonitor) {
        this.settings = logSenderSettings;
        this.logMonitor = logMonitor;
        try {
            this.url = new URL(this.settings.getUrl());
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("Failed to initialize URL with: [" + this.settings.getUrl() + "].", e);
        }
    }

    private static InputStream tryGetInputStream(HttpURLConnection connection) {
        try {
            return connection.getInputStream();
        }
        catch (IOException ignored) {
            return null;
        }
    }

    private static InputStream tryGetErrorStream(HttpURLConnection connection) {
        return connection.getErrorStream();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private String tryStreamToString(InputStream stream) {
        if (stream == null) {
            return "";
        }
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));){
            String string = bufferedReader.lines().collect(Collectors.joining("\n"));
            return string;
        }
        catch (IOException ignored) {
            return "";
        }
    }

    private void tryConsumeStream(InputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            while (stream.read() != -1) {
            }
        }
        catch (IOException iOException) {
        }
        finally {
            try {
                stream.close();
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void send(byte[] message) throws IOException {
        this.logMonitor.send(message);
        HttpURLConnection connection = null;
        int responseCode = -1;
        boolean isError = true;
        try {
            Object authHeaderEncoded;
            connection = (HttpURLConnection)this.url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(this.settings.getConnectTimeout());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", this.settings.getContentType());
            if (this.settings.getContentEncoding() != null) {
                connection.setRequestProperty("Content-Encoding", this.settings.getContentEncoding());
            }
            connection.setRequestProperty("Content-Length", Integer.toString(message.length));
            if (this.settings.getUser() != null && this.settings.getPassword() != null) {
                String authHeaderContentString = this.settings.getUser() + ":" + this.settings.getPassword();
                authHeaderEncoded = Base64Coder.encodeString(authHeaderContentString);
                connection.setRequestProperty("Authorization", "Basic " + (String)authHeaderEncoded);
            }
            OutputStream outputStream2 = connection.getOutputStream();
            authHeaderEncoded = null;
            try {
                outputStream2.write(message);
            }
            catch (Throwable throwable) {
                authHeaderEncoded = throwable;
                throw throwable;
            }
            finally {
                if (outputStream2 != null) {
                    if (authHeaderEncoded != null) {
                        try {
                            outputStream2.close();
                        }
                        catch (Throwable throwable) {
                            ((Throwable)authHeaderEncoded).addSuppressed(throwable);
                        }
                    } else {
                        outputStream2.close();
                    }
                }
            }
            responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 204) {
                this.logMonitor.sendOk(responseCode);
                isError = false;
            }
        }
        finally {
            if (connection != null) {
                if (isError) {
                    String responseString = this.tryStreamToString(HttpLogSender.tryGetInputStream(connection));
                    String errorString = this.tryStreamToString(HttpLogSender.tryGetErrorStream(connection));
                    String errorDescription = "HTTP response code: [" + responseCode + "], response: [" + responseString + "], error: [" + errorString + "].";
                    this.logMonitor.sendErr(responseCode, errorDescription);
                } else {
                    this.tryConsumeStream(HttpLogSender.tryGetInputStream(connection));
                    this.tryConsumeStream(HttpLogSender.tryGetErrorStream(connection));
                }
            }
        }
    }
}

