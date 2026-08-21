/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.perfmon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.driver.windows.perfmon.ProcessInformation;
import oshi.driver.windows.perfmon.SystemInformation;
import oshi.util.tuples.Pair;

@ThreadSafe
public final class LoadAverage {
    private static Thread loadAvgThread = null;
    private static double[] loadAverages = new double[]{-1.0, -1.0, -1.0};
    private static final double[] EXP_WEIGHT = new double[]{Math.exp(-0.08333333333333333), Math.exp(-0.016666666666666666), Math.exp(-0.005555555555555556)};

    private LoadAverage() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static double[] queryLoadAverage(int nelem) {
        double[] dArray = loadAverages;
        synchronized (loadAverages) {
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return Arrays.copyOf(loadAverages, nelem);
        }
    }

    public static synchronized void stopDaemon() {
        if (loadAvgThread != null) {
            loadAvgThread.interrupt();
            loadAvgThread = null;
        }
    }

    public static synchronized void startDaemon() {
        if (loadAvgThread != null) {
            return;
        }
        loadAvgThread = new Thread("OSHI Load Average daemon"){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             * Enabled aggressive block sorting
             * Enabled unnecessary exception pruning
             * Enabled aggressive exception aggregation
             * Converted monitor instructions to comments
             * Lifted jumps to return sites
             */
            @Override
            public void run() {
                Pair<Long, Long> nonIdlePair = LoadAverage.queryNonIdleTicks();
                long nonIdleTicks0 = nonIdlePair.getA();
                long nonIdleBase0 = nonIdlePair.getB();
                long initNanos = System.nanoTime();
                try {
                    Thread.sleep(2500L);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                while (!Thread.currentThread().isInterrupted()) {
                    nonIdlePair = LoadAverage.queryNonIdleTicks();
                    long nonIdleTicks = nonIdlePair.getA() - nonIdleTicks0;
                    long nonIdleBase = nonIdlePair.getB() - nonIdleBase0;
                    double runningProcesses = nonIdleBase > 0L && nonIdleTicks > 0L ? (double)nonIdleTicks / (double)nonIdleBase : 0.0;
                    nonIdleTicks0 = nonIdlePair.getA();
                    nonIdleBase0 = nonIdlePair.getB();
                    long queueLength = SystemInformation.queryProcessorQueueLength().getOrDefault(SystemInformation.ProcessorQueueLengthProperty.PROCESSORQUEUELENGTH, 0L);
                    double[] e = loadAverages;
                    // MONITORENTER : loadAverages
                    if (loadAverages[0] < 0.0) {
                        Arrays.fill(loadAverages, runningProcesses);
                    }
                    for (int i = 0; i < loadAverages.length; ++i) {
                        int n = i;
                        loadAverages[n] = loadAverages[n] * EXP_WEIGHT[i];
                        int n2 = i;
                        loadAverages[n2] = loadAverages[n2] + (runningProcesses + (double)queueLength) * (1.0 - EXP_WEIGHT[i]);
                    }
                    // MONITOREXIT : e
                    long delay = 5000L - (System.nanoTime() - initNanos) % 5000000000L / 1000000L;
                    if (delay < 500L) {
                        delay += 5000L;
                    }
                    try {
                        Thread.sleep(delay);
                    }
                    catch (InterruptedException e2) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        loadAvgThread.setDaemon(true);
        loadAvgThread.start();
    }

    private static Pair<Long, Long> queryNonIdleTicks() {
        Pair<List<String>, Map<ProcessInformation.IdleProcessorTimeProperty, List<Long>>> idleValues = ProcessInformation.queryIdleProcessCounters();
        List<String> instances = idleValues.getA();
        Map<ProcessInformation.IdleProcessorTimeProperty, List<Long>> valueMap = idleValues.getB();
        List<Long> proctimeTicks = valueMap.get(ProcessInformation.IdleProcessorTimeProperty.PERCENTPROCESSORTIME);
        List<Long> proctimeBase = valueMap.get(ProcessInformation.IdleProcessorTimeProperty.ELAPSEDTIME);
        long nonIdleTicks = 0L;
        long nonIdleBase = 0L;
        for (int i = 0; i < instances.size(); ++i) {
            if ("_Total".equals(instances.get(i))) {
                nonIdleTicks += proctimeTicks.get(i).longValue();
                nonIdleBase += proctimeBase.get(i).longValue();
                continue;
            }
            if (!"Idle".equals(instances.get(i))) continue;
            nonIdleTicks -= proctimeTicks.get(i).longValue();
        }
        return new Pair<Long, Long>(nonIdleTicks, nonIdleBase);
    }
}

