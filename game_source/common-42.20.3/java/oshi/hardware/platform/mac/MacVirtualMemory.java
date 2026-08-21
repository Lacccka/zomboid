/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import java.util.function.Supplier;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.common.AbstractVirtualMemory;
import oshi.hardware.platform.mac.MacGlobalMemory;
import oshi.util.Memoizer;
import oshi.util.tuples.Pair;

@ThreadSafe
abstract class MacVirtualMemory
extends AbstractVirtualMemory {
    private final MacGlobalMemory global;
    private final Supplier<Pair<Long, Long>> usedTotal = Memoizer.memoize(this::querySwapUsage, Memoizer.defaultExpiration());
    private final Supplier<Pair<Long, Long>> inOut = Memoizer.memoize(this::queryVmStat, Memoizer.defaultExpiration());

    MacVirtualMemory(MacGlobalMemory macGlobalMemory) {
        this.global = macGlobalMemory;
    }

    @Override
    public long getSwapUsed() {
        return this.usedTotal.get().getA();
    }

    @Override
    public long getSwapTotal() {
        return this.usedTotal.get().getB();
    }

    @Override
    public long getVirtualMax() {
        return this.global.getTotal() + this.getSwapTotal();
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

    protected abstract Pair<Long, Long> querySwapUsage();

    protected abstract Pair<Long, Long> queryVmStat();
}

