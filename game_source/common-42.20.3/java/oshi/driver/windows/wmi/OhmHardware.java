/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class OhmHardware {
    private static final String HARDWARE = "Hardware";

    private OhmHardware() {
    }

    public static WbemcliUtil.WmiResult<IdentifierProperty> queryHwIdentifier(WmiQueryHandler h, String typeToQuery, String typeName) {
        StringBuilder sb = new StringBuilder(HARDWARE);
        sb.append(" WHERE ").append(typeToQuery).append("Type=\"").append(typeName).append('\"');
        WbemcliUtil.WmiQuery<IdentifierProperty> cpuIdentifierQuery = new WbemcliUtil.WmiQuery<IdentifierProperty>("ROOT\\OpenHardwareMonitor", sb.toString(), IdentifierProperty.class);
        return h.queryWMI(cpuIdentifierQuery, false);
    }

    public static enum IdentifierProperty {
        IDENTIFIER;

    }
}

