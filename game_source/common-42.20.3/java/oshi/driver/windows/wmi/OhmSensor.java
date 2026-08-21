/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class OhmSensor {
    private static final String SENSOR = "Sensor";

    private OhmSensor() {
    }

    public static WbemcliUtil.WmiResult<ValueProperty> querySensorValue(WmiQueryHandler h, String identifier, String sensorType) {
        StringBuilder sb = new StringBuilder(SENSOR);
        sb.append(" WHERE Parent = \"").append(identifier);
        sb.append("\" AND SensorType=\"").append(sensorType).append('\"');
        WbemcliUtil.WmiQuery<ValueProperty> ohmSensorQuery = new WbemcliUtil.WmiQuery<ValueProperty>("ROOT\\OpenHardwareMonitor", sb.toString(), ValueProperty.class);
        return h.queryWMI(ohmSensorQuery, false);
    }

    public static enum ValueProperty {
        VALUE;

    }
}

