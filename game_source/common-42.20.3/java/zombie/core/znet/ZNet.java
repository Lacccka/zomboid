/*
 * Decompiled with CFR 0.152.
 */
package zombie.core.znet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;

public class ZNet {
    private static final SimpleDateFormat s_logSdf = new SimpleDateFormat("dd-MM-yy HH:mm:ss.SSS");

    public static native void init();

    private static native void setLogLevel(int var0);

    public static void setLogLevel(LogSeverity severity) {
        ZNet.setLogLevel(severity.ordinal());
    }

    private static void logPutsCallback(String s) {
        String time = s_logSdf.format(Calendar.getInstance().getTime());
        DebugType.ZNet.print("[" + time + "] > " + s);
        System.out.flush();
    }
}

