/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

public interface ILogMonitor {
    public static void printVerbose(String what) {
        System.out.println("[TinyLoki][" + System.currentTimeMillis() + "][V] " + what);
    }

    public static void printInfo(String what) {
        System.out.println("[TinyLoki][" + System.currentTimeMillis() + "][I] " + what);
    }

    public static void printError(String what) {
        System.err.println("[TinyLoki][" + System.currentTimeMillis() + "][E] " + what);
    }

    public boolean isVerbose();

    public void logVerbose(String var1);

    public void logInfo(String var1);

    public void logError(String var1);

    public void onConfigured(String var1, String var2);

    public void onEncoded(byte[] var1, byte[] var2);

    public void send(byte[] var1);

    public void sendOk(int var1);

    public void sendErr(int var1, String var2);

    public void onException(Exception var1);

    public void onSync(boolean var1);

    public void onStart();

    public void onStop(boolean var1);
}

