/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import oshi.annotation.concurrent.Immutable;
import oshi.util.FormatUtil;

@Immutable
public class PhysicalMemory {
    private final String bankLabel;
    private final long capacity;
    private final long clockSpeed;
    private final String manufacturer;
    private final String memoryType;
    private final String partNumber;
    private final String serialNumber;

    public PhysicalMemory(String bankLabel, long capacity, long clockSpeed, String manufacturer, String memoryType, String partNumber, String serialNumber) {
        this.bankLabel = bankLabel;
        this.capacity = capacity;
        this.clockSpeed = clockSpeed;
        this.manufacturer = manufacturer;
        this.memoryType = memoryType;
        this.partNumber = partNumber;
        this.serialNumber = serialNumber;
    }

    public String getBankLabel() {
        return this.bankLabel;
    }

    public long getCapacity() {
        return this.capacity;
    }

    public long getClockSpeed() {
        return this.clockSpeed;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public String getMemoryType() {
        return this.memoryType;
    }

    public String getPartNumber() {
        return this.partNumber;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bank label: " + this.getBankLabel());
        sb.append(", Capacity: " + FormatUtil.formatBytes(this.getCapacity()));
        sb.append(", Clock speed: " + FormatUtil.formatHertz(this.getClockSpeed()));
        sb.append(", Manufacturer: " + this.getManufacturer());
        sb.append(", Memory type: " + this.getMemoryType());
        sb.append(", Part number: " + this.getPartNumber());
        sb.append(", Serial number: " + this.getSerialNumber());
        return sb.toString();
    }
}

