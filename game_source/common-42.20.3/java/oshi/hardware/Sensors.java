/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware;

import oshi.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface Sensors {
    public double getCpuTemperature();

    public int[] getFanSpeeds();

    public double getCpuVoltage();
}

