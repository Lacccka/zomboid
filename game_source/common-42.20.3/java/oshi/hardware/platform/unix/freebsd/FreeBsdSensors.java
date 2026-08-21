/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.unix.freebsd;

import com.sun.jna.Memory;
import com.sun.jna.platform.unix.LibCAPI;
import java.util.Locale;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.common.AbstractSensors;
import oshi.jna.ByRef;
import oshi.jna.platform.unix.FreeBsdLibc;

@ThreadSafe
final class FreeBsdSensors
extends AbstractSensors {
    FreeBsdSensors() {
    }

    @Override
    public double queryCpuTemperature() {
        return FreeBsdSensors.queryKldloadCoretemp();
    }

    private static double queryKldloadCoretemp() {
        String name = "dev.cpu.%d.temperature";
        try (ByRef.CloseableSizeTByReference size = new ByRef.CloseableSizeTByReference((long)FreeBsdLibc.INT_SIZE);){
            int cpu = 0;
            double sumTemp = 0.0;
            try (Memory p = new Memory(size.longValue());){
                while (0 == FreeBsdLibc.INSTANCE.sysctlbyname(String.format(Locale.ROOT, name, cpu), p, size, null, LibCAPI.size_t.ZERO)) {
                    sumTemp += (double)p.getInt(0L) / 10.0 - 273.15;
                    ++cpu;
                }
            }
            double d = cpu > 0 ? sumTemp / (double)cpu : Double.NaN;
            return d;
        }
    }

    @Override
    public int[] queryFanSpeeds() {
        return new int[0];
    }

    @Override
    public double queryCpuVoltage() {
        return 0.0;
    }
}

