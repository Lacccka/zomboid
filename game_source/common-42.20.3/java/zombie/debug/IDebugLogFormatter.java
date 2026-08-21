/*
 * Decompiled with CFR 0.152.
 */
package zombie.debug;

import zombie.debug.DebugType;
import zombie.debug.LogSeverity;

public interface IDebugLogFormatter {
    public String format(DebugType var1, LogSeverity var2, String var3, Object var4);
}

