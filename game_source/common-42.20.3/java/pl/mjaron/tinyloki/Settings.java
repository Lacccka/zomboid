/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.BasicBuffering;
import pl.mjaron.tinyloki.ErrorLogMonitor;
import pl.mjaron.tinyloki.GzipLogEncoder;
import pl.mjaron.tinyloki.HttpLogSender;
import pl.mjaron.tinyloki.IBuffering;
import pl.mjaron.tinyloki.IExecutor;
import pl.mjaron.tinyloki.ILogCollector;
import pl.mjaron.tinyloki.ILogEncoder;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.ILogSender;
import pl.mjaron.tinyloki.ITimestampProviderFactory;
import pl.mjaron.tinyloki.JsonLogCollector;
import pl.mjaron.tinyloki.LabelSettings;
import pl.mjaron.tinyloki.Labels;
import pl.mjaron.tinyloki.LogSenderSettings;
import pl.mjaron.tinyloki.ThreadExecutor;
import pl.mjaron.tinyloki.TinyLoki;
import pl.mjaron.tinyloki.VerboseLogMonitor;

public class Settings {
    private final LogSenderSettings logSenderSettings = new LogSenderSettings();
    private final LabelSettings labelSettings = new LabelSettings();
    private LabelSettings structuredMetadataLabelSettings = new LabelSettings();
    private Labels staticLabels = Labels.of();
    private ILogCollector logCollector = null;
    private ILogEncoder logEncoder = null;
    private IBuffering buffering = null;
    private ILogMonitor logMonitor = null;
    private ILogSender logSender = null;
    private IExecutor executor = null;
    private ITimestampProviderFactory timestampProviderFactory = null;

    public static String normalizeUrl(String url) {
        if (url.endsWith("loki/api/v1/push")) {
            return url;
        }
        if (url.endsWith("loki/api/v1/push/")) {
            return url.substring(0, url.length() - 1);
        }
        if (!url.endsWith("/")) {
            return url + "/" + "loki/api/v1/push";
        }
        return url + "loki/api/v1/push";
    }

    public static Settings fromUrl(String url) {
        return new Settings(Settings.normalizeUrl(url));
    }

    public static Settings fromExactUrl(String url) {
        return new Settings(url);
    }

    private Settings(String url) {
        this.logSenderSettings.setUrl(url);
    }

    public Settings withBasicAuth(String user, String pass) {
        this.logSenderSettings.setUser(user);
        this.logSenderSettings.setPassword(pass);
        return this;
    }

    public Settings withConnectTimeout(int connectTimeout) {
        this.logSenderSettings.setConnectTimeout(connectTimeout);
        return this;
    }

    public Settings withLogCollector(ILogCollector logCollector) {
        this.logCollector = logCollector;
        return this;
    }

    public Settings withLogEncoder(ILogEncoder logEncoder) {
        this.logEncoder = logEncoder;
        return this;
    }

    public Settings withGzipLogEncoder() {
        return this.withLogEncoder(new GzipLogEncoder());
    }

    public Settings withoutLogEncoder() {
        return this.withLogEncoder(null);
    }

    public Settings withLogMonitor(ILogMonitor logMonitor) {
        this.logMonitor = logMonitor;
        return this;
    }

    public Settings withErrorLogMonitor() {
        return this.withLogMonitor(new ErrorLogMonitor());
    }

    public Settings withVerboseLogMonitor() {
        return this.withLogMonitor(new VerboseLogMonitor());
    }

    public Settings withVerboseLogMonitor(boolean printMessages) {
        return this.withLogMonitor(new VerboseLogMonitor(printMessages));
    }

    public Settings withLogSender(ILogSender logSender) {
        this.logSender = logSender;
        return this;
    }

    public Settings withLabelLength(int maxLabelNameLength, int maxLabelValueLength) {
        this.labelSettings.setMaxLabelNameLength(maxLabelNameLength);
        this.labelSettings.setMaxLabelValueLength(maxLabelValueLength);
        return this;
    }

    public Settings withStructuredMetadata(LabelSettings structuredMetadataLabelSettings) {
        this.structuredMetadataLabelSettings = structuredMetadataLabelSettings;
        return this;
    }

    public Settings withStructuredMetadata() {
        return this.withStructuredMetadata(new LabelSettings());
    }

    public Settings withoutStructuredMetadata() {
        this.structuredMetadataLabelSettings = null;
        return this;
    }

    public Settings withExecutor(IExecutor executor) {
        this.executor = executor;
        return this;
    }

    public Settings withThreadExecutor(int processingIntervalTime) {
        return this.withExecutor(new ThreadExecutor(processingIntervalTime));
    }

    public Settings withThreadExecutor() {
        return this.withExecutor(new ThreadExecutor());
    }

    public Settings withBuffering(IBuffering buffering) {
        this.buffering = buffering;
        return this;
    }

    public Settings withTimestampProvider(ITimestampProviderFactory timestampProviderFactory) {
        this.timestampProviderFactory = timestampProviderFactory;
        return this;
    }

    public Settings withCurrentTimestampProvider() {
        return this.withTimestampProvider(ITimestampProviderFactory.current());
    }

    public Settings withIncrementingTimestampProvider() {
        return this.withTimestampProvider(ITimestampProviderFactory.incrementing());
    }

    public Settings withBasicBuffering(int maxMessageSize, int maxBuffersCount) {
        this.buffering = new BasicBuffering(maxMessageSize, maxBuffersCount);
        return this;
    }

    public Settings withLabels(Labels labels) {
        if (labels == null) {
            this.staticLabels = Labels.of();
        } else {
            this.staticLabels.l(labels);
        }
        return this;
    }

    public LogSenderSettings getLogSenderSettings() {
        return this.logSenderSettings;
    }

    public ILogCollector getLogCollector() {
        return this.logCollector != null ? this.logCollector : new JsonLogCollector();
    }

    public ILogEncoder getLogEncoder() {
        return this.logEncoder;
    }

    public IBuffering getBuffering() {
        if (this.buffering == null) {
            this.buffering = new BasicBuffering();
        }
        return this.buffering;
    }

    public ITimestampProviderFactory getTimestampProviderFactory() {
        if (this.timestampProviderFactory == null) {
            this.timestampProviderFactory = ITimestampProviderFactory.getDefault();
        }
        return this.timestampProviderFactory;
    }

    public ILogMonitor getLogMonitor() {
        if (this.logMonitor == null) {
            this.logMonitor = new ErrorLogMonitor();
        }
        return this.logMonitor;
    }

    public ILogSender getLogSender() {
        if (this.logSender == null) {
            this.logSender = new HttpLogSender();
        }
        return this.logSender;
    }

    public LabelSettings getLabelSettings() {
        return this.labelSettings;
    }

    public LabelSettings getStructuredMetadataLabelSettings() {
        return this.structuredMetadataLabelSettings;
    }

    public Labels getLabels() {
        return this.staticLabels;
    }

    public IExecutor getExecutor() {
        if (this.executor == null) {
            this.executor = new ThreadExecutor();
        }
        return this.executor;
    }

    public TinyLoki open() {
        return TinyLoki.open(this);
    }
}

