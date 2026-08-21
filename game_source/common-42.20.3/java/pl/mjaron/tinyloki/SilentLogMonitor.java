/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

import pl.mjaron.tinyloki.ILogMonitor;

public class SilentLogMonitor
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
    }

    @Override
    public void onException(Exception exception) {
    }

    @Override
    public void onSync(boolean isSuccess) {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop(boolean isSuccess) {
    }
}

