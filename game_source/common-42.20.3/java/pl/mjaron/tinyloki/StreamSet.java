/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.ILogStream;
import pl.mjaron.tinyloki.Labels;
import pl.mjaron.tinyloki.TinyLoki;

public class StreamSet {
    private final TinyLoki loki;
    private final Labels labels;
    private ILogStream fatalStream;
    private ILogStream warningStream;
    private ILogStream infoStream;
    private ILogStream debugStream;
    private ILogStream traceStream;
    private ILogStream unknownStream;

    public StreamSet(TinyLoki loki, Labels labels) {
        this.loki = loki;
        this.labels = labels;
    }

    public synchronized void release() {
        if (this.fatalStream != null) {
            this.fatalStream.release();
        }
        if (this.warningStream != null) {
            this.warningStream.release();
        }
        if (this.infoStream != null) {
            this.infoStream.release();
        }
        if (this.debugStream != null) {
            this.debugStream.release();
        }
        if (this.traceStream != null) {
            this.traceStream.release();
        }
        if (this.unknownStream != null) {
            this.unknownStream.release();
        }
    }

    public synchronized ILogStream fatal() {
        if (this.fatalStream == null) {
            this.fatalStream = this.loki.stream().fatal().l(this.labels).open();
        }
        return this.fatalStream;
    }

    public synchronized ILogStream warning() {
        if (this.warningStream == null) {
            this.warningStream = this.loki.stream().warning().l(this.labels).open();
        }
        return this.warningStream;
    }

    public synchronized ILogStream info() {
        if (this.infoStream == null) {
            this.infoStream = this.loki.stream().info().l(this.labels).open();
        }
        return this.infoStream;
    }

    public synchronized ILogStream debug() {
        if (this.debugStream == null) {
            this.debugStream = this.loki.stream().debug().l(this.labels).open();
        }
        return this.debugStream;
    }

    public synchronized ILogStream verbose() {
        if (this.traceStream == null) {
            this.traceStream = this.loki.stream().trace().l(this.labels).open();
        }
        return this.traceStream;
    }

    public synchronized ILogStream unknown() {
        if (this.unknownStream == null) {
            this.unknownStream = this.loki.stream().unknown().l(this.labels).open();
        }
        return this.unknownStream;
    }

    public void fatal(String line) {
        this.fatal().log(line);
    }

    public void fatal(String line, Labels structuredMetadata) {
        this.fatal().log(line, structuredMetadata);
    }

    public void warning(String line) {
        this.warning().log(line);
    }

    public void warning(String line, Labels structuredMetadata) {
        this.warning().log(line, structuredMetadata);
    }

    public void info(String line) {
        this.info().log(line);
    }

    public void info(String line, Labels structuredMetadata) {
        this.info().log(line, structuredMetadata);
    }

    public void debug(String line) {
        this.debug().log(line);
    }

    public void debug(String line, Labels structuredMetadata) {
        this.debug().log(line, structuredMetadata);
    }

    public void verbose(String line) {
        this.verbose().log(line);
    }

    public void verbose(String line, Labels structuredMetadata) {
        this.verbose().log(line, structuredMetadata);
    }

    public void unknown(String line) {
        this.unknown().log(line);
    }

    public void unknown(String line, Labels structuredMetadata) {
        this.unknown().log(line, structuredMetadata);
    }
}

