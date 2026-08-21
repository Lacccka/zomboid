/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.linux;

import java.util.List;
import java.util.function.Supplier;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.common.AbstractVirtualMemory;
import oshi.hardware.platform.linux.LinuxGlobalMemory;
import oshi.util.FileUtil;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.linux.ProcPath;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

@ThreadSafe
final class LinuxVirtualMemory
extends AbstractVirtualMemory {
    private final LinuxGlobalMemory global;
    private final Supplier<Triplet<Long, Long, Long>> usedTotalCommitLim = Memoizer.memoize(LinuxVirtualMemory::queryMemInfo, Memoizer.defaultExpiration());
    private final Supplier<Pair<Long, Long>> inOut = Memoizer.memoize(LinuxVirtualMemory::queryVmStat, Memoizer.defaultExpiration());

    LinuxVirtualMemory(LinuxGlobalMemory linuxGlobalMemory) {
        this.global = linuxGlobalMemory;
    }

    @Override
    public long getSwapUsed() {
        return this.usedTotalCommitLim.get().getA();
    }

    @Override
    public long getSwapTotal() {
        return this.usedTotalCommitLim.get().getB();
    }

    @Override
    public long getVirtualMax() {
        return this.usedTotalCommitLim.get().getC();
    }

    @Override
    public long getVirtualInUse() {
        return this.global.getTotal() - this.global.getAvailable() + this.getSwapUsed();
    }

    @Override
    public long getSwapPagesIn() {
        return this.inOut.get().getA();
    }

    @Override
    public long getSwapPagesOut() {
        return this.inOut.get().getB();
    }

    private static Triplet<Long, Long, Long> queryMemInfo() {
        long swapFree = 0L;
        long swapTotal = 0L;
        long commitLimit = 0L;
        List<String> procMemInfo = FileUtil.readFile(ProcPath.MEMINFO);
        for (String checkLine : procMemInfo) {
            String[] memorySplit = ParseUtil.whitespaces.split(checkLine);
            if (memorySplit.length <= 1) continue;
            switch (memorySplit[0]) {
                case "SwapTotal:": {
                    swapTotal = LinuxVirtualMemory.parseMeminfo(memorySplit);
                    break;
                }
                case "SwapFree:": {
                    swapFree = LinuxVirtualMemory.parseMeminfo(memorySplit);
                    break;
                }
                case "CommitLimit:": {
                    commitLimit = LinuxVirtualMemory.parseMeminfo(memorySplit);
                    break;
                }
            }
        }
        return new Triplet<Long, Long, Long>(swapTotal - swapFree, swapTotal, commitLimit);
    }

    private static Pair<Long, Long> queryVmStat() {
        long swapPagesIn = 0L;
        long swapPagesOut = 0L;
        List<String> procVmStat = FileUtil.readFile(ProcPath.VMSTAT);
        for (String checkLine : procVmStat) {
            String[] memorySplit = ParseUtil.whitespaces.split(checkLine);
            if (memorySplit.length <= 1) continue;
            switch (memorySplit[0]) {
                case "pswpin": {
                    swapPagesIn = ParseUtil.parseLongOrDefault(memorySplit[1], 0L);
                    break;
                }
                case "pswpout": {
                    swapPagesOut = ParseUtil.parseLongOrDefault(memorySplit[1], 0L);
                    break;
                }
            }
        }
        return new Pair<Long, Long>(swapPagesIn, swapPagesOut);
    }

    private static long parseMeminfo(String[] memorySplit) {
        if (memorySplit.length < 2) {
            return 0L;
        }
        long memory = ParseUtil.parseLongOrDefault(memorySplit[1], 0L);
        if (memorySplit.length > 2 && "kB".equals(memorySplit[2])) {
            memory *= 1024L;
        }
        return memory;
    }
}

