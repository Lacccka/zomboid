/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.ILogMonitor;
import pl.mjaron.tinyloki.Utils;

public class ErrorLogMonitor
implements ILogMonitor {
    @Override
    public boolean isVerbose() {
        return false;
    }

    @Override
    public void logVerbose(String what) {
    }

    @Override
    public void logInfo(String what) {
    }

    @Override
    public void logError(String what) {
        ILogMonitor.printError(what);
    }

    @Override
    public void onConfigured(String contentType, String contentEncoding) {
    }

    @Override
    public void onEncoded(byte[] in, byte[] out) {
    }

    @Override
    public void send(byte[] message) {
    }

    @Override
    public void sendOk(int status) {
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
        if (!isSuccess) {
            ILogMonitor.printError("Sync operation failed.");
        }
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop(boolean isSuccess) {
        if (!isSuccess) {
            ILogMonitor.printError("Stop operation failed.");
        }
    }
}

