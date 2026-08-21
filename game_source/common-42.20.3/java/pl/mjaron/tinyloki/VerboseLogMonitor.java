/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import java.nio.charset.StandardCharsets;
import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.Utils;

public class VerboseLogMonitor
implements ILogMonitor {
    private final boolean printMessages;
    private String contentType = null;
    private String contentEncoding = null;

    public VerboseLogMonitor() {
        this.printMessages = true;
    }

    public VerboseLogMonitor(boolean printMessages) {
        this.printMessages = printMessages;
    }

    @Override
    public boolean isVerbose() {
        return true;
    }

    @Override
    public void logVerbose(String what) {
        ILogMonitor.printVerbose(what);
    }

    @Override
    public void logInfo(String what) {
        ILogMonitor.printInfo(what);
    }

    @Override
    public void logError(String what) {
        ILogMonitor.printError(what);
    }

    @Override
    public void onConfigured(String contentType, String contentEncoding) {
        ILogMonitor.printInfo("TinyLoki configured.");
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
    }

    @Override
    public void onEncoded(byte[] in, byte[] out) {
        ILogMonitor.printInfo("<|> " + in.length + " bytes encoded to " + out.length + " bytes");
    }

    @Override
    public void send(byte[] message) {
        if (this.contentEncoding == null && this.contentType != null && this.contentType.equals("application/json")) {
            if (this.printMessages) {
                ILogMonitor.printInfo("<<< " + new String(message, StandardCharsets.UTF_8));
            } else {
                ILogMonitor.printInfo("<<< " + message.length + " bytes sent.");
            }
        } else {
            ILogMonitor.printInfo("<<< " + message.length + " bytes sent (encoding undefined).");
        }
    }

    @Override
    public void sendOk(int status) {
        ILogMonitor.printInfo(">>> " + status);
    }

    @Override
    public void sendErr(int status, String message) {
        ILogMonitor.printError("Unexpected server response status: " + status + ": " + message);
    }

    @Override
    public void onException(Exception exception) {
        ILogMonitor.printError("Exception occurred: " + exception.toString() + "\n" + Utils.stackTraceString(exception));
    }

    @Override
    public void onSync(boolean isSuccess) {
        if (isSuccess) {
            ILogMonitor.printInfo("Sync operation success.");
        } else {
            ILogMonitor.printError("Sync operation failed.");
        }
    }

    @Override
    public void onStart() {
        ILogMonitor.printInfo("Started.");
    }

    @Override
    public void onStop(boolean isSuccess) {
        if (isSuccess) {
            ILogMonitor.printInfo("Stop operation success.");
        } else {
            ILogMonitor.printError("Stop operation failed.");
        }
    }
}

