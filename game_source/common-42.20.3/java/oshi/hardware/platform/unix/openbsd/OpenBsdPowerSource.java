/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.openbsd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.PowerSource;
import oshi.hardware.common.AbstractPowerSource;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

@ThreadSafe
public final class OpenBsdPowerSource
extends AbstractPowerSource {
    public OpenBsdPowerSource(String psName, String psDeviceName, double psRemainingCapacityPercent, double psTimeRemainingEstimated, double psTimeRemainingInstant, double psPowerUsageRate, double psVoltage, double psAmperage, boolean psPowerOnLine, boolean psCharging, boolean psDischarging, PowerSource.CapacityUnits psCapacityUnits, int psCurrentCapacity, int psMaxCapacity, int psDesignCapacity, int psCycleCount, String psChemistry, LocalDate psManufactureDate, String psManufacturer, String psSerialNumber, double psTemperature) {
        super(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated, psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging, psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }

    public static List<PowerSource> getPowerSources() {
        HashSet<String> psNames = new HashSet<String>();
        for (String line : ExecutingCommand.runNative("systat -ab sensors")) {
            if (!line.contains(".amphour") && !line.contains(".watthour")) continue;
            int dot = line.indexOf(46);
            psNames.add(line.substring(0, dot));
        }
        ArrayList<PowerSource> psList = new ArrayList<PowerSource>();
        for (String name : psNames) {
            psList.add(OpenBsdPowerSource.getPowerSource(name));
        }
        return psList;
    }

    private static OpenBsdPowerSource getPowerSource(String name) {
        int life;
        String psName = name.startsWith("acpi") ? name.substring(4) : name;
        double psRemainingCapacityPercent = 1.0;
        double psTimeRemainingEstimated = -1.0;
        double psPowerUsageRate = 0.0;
        double psVoltage = -1.0;
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
        for (String line : ExecutingCommand.runNative("systat -ab sensors")) {
            String[] split = ParseUtil.whitespaces.split(line);
            if (split.length <= 1 || !split[0].startsWith(name)) continue;
            if (split[0].contains("volt0") || split[0].contains("volt") && line.contains("current")) {
                psVoltage = ParseUtil.parseDoubleOrDefault(split[1], -1.0);
                continue;
            }
            if (split[0].contains("current0")) {
                psAmperage = ParseUtil.parseDoubleOrDefault(split[1], 0.0);
                continue;
            }
            if (split[0].contains("temp0")) {
                psTemperature = ParseUtil.parseDoubleOrDefault(split[1], 0.0);
                continue;
            }
            if (!split[0].contains("watthour") && !split[0].contains("amphour")) continue;
            PowerSource.CapacityUnits capacityUnits = psCapacityUnits = split[0].contains("watthour") ? PowerSource.CapacityUnits.MWH : PowerSource.CapacityUnits.MAH;
            if (line.contains("remaining")) {
                psCurrentCapacity = (int)(1000.0 * ParseUtil.parseDoubleOrDefault(split[1], 0.0));
                continue;
            }
            if (line.contains("full")) {
                psMaxCapacity = (int)(1000.0 * ParseUtil.parseDoubleOrDefault(split[1], 0.0));
                continue;
            }
            if (!line.contains("new") && !line.contains("design")) continue;
            psDesignCapacity = (int)(1000.0 * ParseUtil.parseDoubleOrDefault(split[1], 0.0));
        }
        int state = ParseUtil.parseIntOrDefault(ExecutingCommand.getFirstAnswer("apm -b"), 255);
        if (state < 4) {
            psPowerOnLine = true;
            if (state == 3) {
                psCharging = true;
            } else {
                int time = ParseUtil.parseIntOrDefault(ExecutingCommand.getFirstAnswer("apm -m"), -1);
                psTimeRemainingEstimated = time < 0 ? -1.0 : 60.0 * (double)time;
                psDischarging = true;
            }
        }
        if ((life = ParseUtil.parseIntOrDefault(ExecutingCommand.getFirstAnswer("apm -l"), -1)) > 0) {
            psRemainingCapacityPercent = (double)life / 100.0;
        }
        if (psMaxCapacity < psDesignCapacity && psMaxCapacity < psCurrentCapacity) {
            psMaxCapacity = psDesignCapacity;
        } else if (psDesignCapacity < psMaxCapacity && psDesignCapacity < psCurrentCapacity) {
            psDesignCapacity = psMaxCapacity;
        }
        String psDeviceName = "unknown";
        String psSerialNumber = "unknown";
        String psChemistry = "unknown";
        String psManufacturer = "unknown";
        double psTimeRemainingInstant = psTimeRemainingEstimated;
        if (psVoltage > 0.0) {
            if (psAmperage > 0.0 && psPowerUsageRate == 0.0) {
                psPowerUsageRate = psAmperage * psVoltage;
            } else if (psAmperage == 0.0 && psPowerUsageRate > 0.0) {
                psAmperage = psPowerUsageRate / psVoltage;
            }
        }
        return new OpenBsdPowerSource(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated, psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging, psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }
}

