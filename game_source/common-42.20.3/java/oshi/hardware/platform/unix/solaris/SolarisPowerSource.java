/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.solaris;

import com.sun.jna.platform.unix.solaris.LibKstat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.PowerSource;
import oshi.hardware.common.AbstractPowerSource;
import oshi.util.platform.unix.solaris.KstatUtil;

@ThreadSafe
public final class SolarisPowerSource
extends AbstractPowerSource {
    private static final String[] KSTAT_BATT_MOD = new String[]{null, "battery", "acpi_drv"};
    private static final int KSTAT_BATT_IDX;

    public SolarisPowerSource(String psName, String psDeviceName, double psRemainingCapacityPercent, double psTimeRemainingEstimated, double psTimeRemainingInstant, double psPowerUsageRate, double psVoltage, double psAmperage, boolean psPowerOnLine, boolean psCharging, boolean psDischarging, PowerSource.CapacityUnits psCapacityUnits, int psCurrentCapacity, int psMaxCapacity, int psDesignCapacity, int psCycleCount, String psChemistry, LocalDate psManufactureDate, String psManufacturer, String psSerialNumber, double psTemperature) {
        super(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated, psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging, psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }

    public static List<PowerSource> getPowerSources() {
        return Arrays.asList(SolarisPowerSource.getPowerSource("BAT0"));
    }

    private static SolarisPowerSource getPowerSource(String name) {
        String psName = name;
        String psDeviceName = "unknown";
        double psRemainingCapacityPercent = 1.0;
        double psTimeRemainingEstimated = -1.0;
        double psTimeRemainingInstant = 0.0;
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
        String psChemistry = "unknown";
        LocalDate psManufactureDate = null;
        String psManufacturer = "unknown";
        String psSerialNumber = "unknown";
        double psTemperature = 0.0;
        if (KSTAT_BATT_IDX > 0) {
            try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
                LibKstat.Kstat ksp = kc.lookup(KSTAT_BATT_MOD[KSTAT_BATT_IDX], 0, "battery BIF0");
                if (ksp != null && kc.read(ksp)) {
                    long unit;
                    long energyFull = KstatUtil.dataLookupLong(ksp, "bif_last_cap");
                    if (energyFull == -1L || energyFull <= 0L) {
                        energyFull = KstatUtil.dataLookupLong(ksp, "bif_design_cap");
                    }
                    if (energyFull != -1L && energyFull > 0L) {
                        psMaxCapacity = (int)energyFull;
                    }
                    if ((unit = KstatUtil.dataLookupLong(ksp, "bif_unit")) == 0L) {
                        psCapacityUnits = PowerSource.CapacityUnits.MWH;
                    } else if (unit == 1L) {
                        psCapacityUnits = PowerSource.CapacityUnits.MAH;
                    }
                    psDeviceName = KstatUtil.dataLookupString(ksp, "bif_model");
                    psSerialNumber = KstatUtil.dataLookupString(ksp, "bif_serial");
                    psChemistry = KstatUtil.dataLookupString(ksp, "bif_type");
                    psManufacturer = KstatUtil.dataLookupString(ksp, "bif_oem_info");
                }
                if ((ksp = kc.lookup(KSTAT_BATT_MOD[KSTAT_BATT_IDX], 0, "battery BST0")) != null && kc.read(ksp)) {
                    long voltageNow;
                    boolean isCharging;
                    long powerNow;
                    long energyNow = KstatUtil.dataLookupLong(ksp, "bst_rem_cap");
                    if (energyNow >= 0L) {
                        psCurrentCapacity = (int)energyNow;
                    }
                    if ((powerNow = KstatUtil.dataLookupLong(ksp, "bst_rate")) == -1L) {
                        powerNow = 0L;
                    }
                    boolean bl = isCharging = (KstatUtil.dataLookupLong(ksp, "bst_state") & 0x10L) > 0L;
                    if (!isCharging) {
                        double d = psTimeRemainingEstimated = powerNow > 0L ? 3600.0 * (double)energyNow / (double)powerNow : -1.0;
                    }
                    if ((voltageNow = KstatUtil.dataLookupLong(ksp, "bst_voltage")) > 0L) {
                        psVoltage = (double)voltageNow / 1000.0;
                        psAmperage = psPowerUsageRate * 1000.0 / (double)voltageNow;
                    }
                }
            }
        }
        return new SolarisPowerSource(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated, psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging, psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }

    static {
        try (KstatUtil.KstatChain kc = KstatUtil.openChain();){
            KSTAT_BATT_IDX = kc.lookup(KSTAT_BATT_MOD[1], 0, null) != null ? 1 : (kc.lookup(KSTAT_BATT_MOD[2], 0, null) != null ? 2 : 0);
        }
    }
}

