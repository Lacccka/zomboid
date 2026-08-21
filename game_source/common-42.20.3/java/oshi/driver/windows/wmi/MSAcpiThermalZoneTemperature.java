/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class MSAcpiThermalZoneTemperature {
    public static final String WMI_NAMESPACE = "ROOT\\WMI";
    private static final String MS_ACPI_THERMAL_ZONE_TEMPERATURE = "MSAcpi_ThermalZoneTemperature";

    private MSAcpiThermalZoneTemperature() {
    }

    public static WbemcliUtil.WmiResult<TemperatureProperty> queryCurrentTemperature() {
        WbemcliUtil.WmiQuery<TemperatureProperty> curTempQuery = new WbemcliUtil.WmiQuery<TemperatureProperty>(WMI_NAMESPACE, MS_ACPI_THERMAL_ZONE_TEMPERATURE, TemperatureProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(curTempQuery);
    }

    public static enum TemperatureProperty {
        CURRENTTEMPERATURE;

    }
}

