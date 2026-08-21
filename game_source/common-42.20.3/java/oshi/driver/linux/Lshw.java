/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.linux;

import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.UserGroupInfo;

@ThreadSafe
public final class Lshw {
    private static final String MODEL;
    private static final String SERIAL;
    private static final String UUID;

    private Lshw() {
    }

    public static String queryModel() {
        return MODEL;
    }

    public static String querySerialNumber() {
        return SERIAL;
    }

    public static String queryUUID() {
        return UUID;
    }

    public static long queryCpuCapacity() {
        String capacityMarker = "capacity:";
        for (String checkLine : ExecutingCommand.runNative("lshw -class processor")) {
            if (!checkLine.contains(capacityMarker)) continue;
            return ParseUtil.parseHertz(checkLine.split(capacityMarker)[1].trim());
        }
        return -1L;
    }

    static {
        String model = null;
        String serial = null;
        String uuid = null;
        if (UserGroupInfo.isElevated()) {
            String modelMarker = "product:";
            String serialMarker = "serial:";
            String uuidMarker = "uuid:";
            for (String checkLine : ExecutingCommand.runNative("lshw -C system")) {
                if (checkLine.contains(modelMarker)) {
                    model = checkLine.split(modelMarker)[1].trim();
                    continue;
                }
                if (checkLine.contains(serialMarker)) {
                    serial = checkLine.split(serialMarker)[1].trim();
                    continue;
                }
                if (!checkLine.contains(uuidMarker)) continue;
                uuid = checkLine.split(uuidMarker)[1].trim();
            }
        }
        MODEL = model;
        SERIAL = serial;
        UUID = uuid;
    }
}

