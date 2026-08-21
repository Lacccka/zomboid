/*
 * Decompiled with CFR 0.152.
 */
package pl.mjaron.tinyloki;

public interface ILogListener {
    public static ILogListener dummy() {
        return new ILogListener(){

            @Override
            public void onLog(int cachedLogsCount) {
            }
        };
    }

    public void onLog(int var1);
}

