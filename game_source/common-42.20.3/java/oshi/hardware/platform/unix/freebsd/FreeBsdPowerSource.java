/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.freebsd;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.PowerSource;
import oshi.hardware.common.AbstractPowerSource;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;
import oshi.util.platform.unix.freebsd.BsdSysctlUtil;

@ThreadSafe
public final class FreeBsdPowerSource
extends AbstractPowerSource {
    public FreeBsdPowerSource(String psName, String psDeviceName, double psRemainingCapacityPercent, double psTimeRemainingEstimated, double psTimeRemainingInstant, double psPowerUsageRate, double psVoltage, double psAmperage, boolean psPowerOnLine, boolean psCharging, boolean psDischarging, PowerSource.CapacityUnits psCapacityUnits, int psCurrentCapacity, int psMaxCapacity, int psDesignCapacity, int psCycleCount, String psChemistry, LocalDate psManufactureDate, String psManufacturer, String psSerialNumber, double psTemperature) {
        super(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated, psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging, psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }

    public static List<PowerSource> getPowerSources() {
        return Arrays.asList(FreeBsdPowerSource.getPowerSource("BAT0"));
    }

    private static FreeBsdPowerSource getPowerSource(String name) {
        String volts;
        String rate;
        String[] hhmm;
        String psName = name;
        double psRemainingCapacityPercent = 1.0;
        double psTimeRemainingEstimated = -1.0;
        double psPowerUsageRate = 0.0;
        int psVoltage = -1;
        double psAmperage = 0.0;
        boolean psPowerOnLine = false;
        boolean psCharging = false;
        boolean psDischarging = false;
        PowerSource.CapacityUnits psCapacityUnits = PowerSource.CapacityUnits.RELATIVE;
        int psCurrentCapacity = 0;
        int psMaxCapacity = 1;
        int psDesignCapacity = 1;
        int psCycleCount = -1;
        LocalDate psManufactureDate = null;
        double psTemperature = 0.0;
        int state = BsdSysctlUtil.sysctl("hw.acpi.battery.state", 0);
        if (state == 2) {
            psCharging = true;
        } else {
            int time = BsdSysctlUtil.sysctl("hw.acpi.battery.time", -1);
            double d = psTimeRemainingEstimated = time < 0 ? -1.0 : 60.0 * (double)time;
            if (state == 1) {
                psDischarging = true;
            }
        }
        int life = BsdSysctlUtil.sysctl("hw.acpi.battery.life", -1);
        if (life > 0) {
            psRemainingCapacityPercent = (double)life / 100.0;
        }
        List<String> acpiconf = ExecutingCommand.runNative("acpiconf -i 0");
        HashMap<String, String> psMap = new HashMap<String, String>();
        for (String line : acpiconf) {
            String value;
            String[] split = line.split(":", 2);
            if (split.length <= 1 || (value = split[1].trim()).isEmpty()) continue;
            psMap.put(split[0], value);
        }
        String psDeviceName = psMap.getOrDefault("Model number", "unknown");
        String psSerialNumber = psMap.getOrDefault("Serial number", "unknown");
        String psChemistry = psMap.getOrDefault("Type", "unknown");
        String psManufacturer = psMap.getOrDefault("OEM info", "unknown");
        String cap = (String)psMap.get("Design capacity");
        if (cap != null) {
            psDesignCapacity = ParseUtil.getFirstIntValue(cap);
            if (cap.toLowerCase(Locale.ROOT).contains("mah")) {
                psCapacityUnits = PowerSource.CapacityUnits.MAH;
            } else if (cap.toLowerCase(Locale.ROOT).contains("mwh")) {
                psCapacityUnits = PowerSource.CapacityUnits.MWH;
            }
        }
        psMaxCapacity = (cap = (String)psMap.get("Last full capacity")) != null ? ParseUtil.getFirstIntValue(cap) : psDesignCapacity;
        double psTimeRemainingInstant = psTimeRemainingEstimated;
        String time = (String)psMap.get("Remaining time");
        if (time != null && (hhmm = time.split(":")).length == 2) {
            psTimeRemainingInstant = 3600.0 * (double)ParseUtil.parseIntOrDefault(hhmm[0], 0) + 60.0 * (double)ParseUtil.parseIntOrDefault(hhmm[1], 0);
        }
        if ((rate = (String)psMap.get("Present rate")) != null) {
            psPowerUsageRate = ParseUtil.getFirstIntValue(rate);
        }
        if ((volts = (String)psMap.get("Present voltage")) != null && (psVoltage = ParseUtil.getFirstIntValue(volts)) != 0) {
            psAmperage = psPowerUsageRate / (double)psVoltage;
        }
        return new FreeBsdPowerSource(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated, psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging, psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }
}

