/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import java.time.LocalDate;
import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface PowerSource {
    public String getName();

    public String getDeviceName();

    public double getRemainingCapacityPercent();

    public double getTimeRemainingEstimated();

    public double getTimeRemainingInstant();

    public double getPowerUsageRate();

    public double getVoltage();

    public double getAmperage();

    public boolean isPowerOnLine();

    public boolean isCharging();

    public boolean isDischarging();

    public CapacityUnits getCapacityUnits();

    public int getCurrentCapacity();

    public int getMaxCapacity();

    public int getDesignCapacity();

    public int getCycleCount();

    public String getChemistry();

    public LocalDate getManufactureDate();

    public String getManufacturer();

    public String getSerialNumber();

    public double getTemperature();

    public boolean updateAttributes();

    public static enum CapacityUnits {
        MWH,
        MAH,
        RELATIVE;

    }
}

