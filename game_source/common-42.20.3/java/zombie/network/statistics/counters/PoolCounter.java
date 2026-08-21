/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.statistics.counters;

import java.util.HashSet;
import java.util.Set;
import zombie.network.statistics.counters.Counter;
import zombie.network.statistics.data.PerformanceStatistic;
import zombie.util.Pool;

public final class PoolCounter {
    public final Counter total;
    public final Counter used;
    public final Counter free;
    public final Counter alloc;
    public final Counter reused;
    public final Counter reusedTotal;
    public final Counter release;
    private final Set<Pool.PoolStacks> poolStacks = new HashSet<Pool.PoolStacks>();

    public PoolCounter(PerformanceStatistic statistic, Pool<?> pool, String poolName) {
        this.total = new Counter(statistic, poolName + "_total", 0.0, this::getTotalPoolCount, "Total pool size.", "objects");
        this.used = new Counter(statistic, poolName + "_used", 0.0, this::getUsedCount, "Amount of pooled items in use.", "objects");
        this.free = new Counter(statistic, poolName + "_free", 0.0, this::getFreeCount, "Amount of pooled items free.", "objects");
        this.alloc = new Counter(statistic, poolName + "_allocf", 0.0, null, "Num pooled items alloc'ed this frame.", "objects");
        this.reused = new Counter(statistic, poolName + "_reusedf", 0.0, null, "Amount of pooled items re-used this frame.", "objects");
        this.reusedTotal = new Counter(statistic, poolName + "_reusedTotal", 0.0, null, "Amount of pooled items re-used since session start.", "objects", false);
        this.release = new Counter(statistic, poolName + "_releasef", 0.0, null, "Num pooled items released this frame.", "objects");
    }

    public void onPooledObjectAlloc(Pool.PoolStacks threadLocalPoolStacks, boolean isReused) {
        this.ensureMonitoringPoolStacks(threadLocalPoolStacks);
        if (isReused) {
            this.reused.increase();
            this.reusedTotal.increase();
        } else {
            this.alloc.increase();
        }
    }

    public void onPooledObjectReleased(Pool.PoolStacks threadLocalPoolStacks) {
        this.ensureMonitoringPoolStacks(threadLocalPoolStacks);
        this.release.increase();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void ensureMonitoringPoolStacks(Pool.PoolStacks threadLocalPoolStacks) {
        if (threadLocalPoolStacks.isBeingMonitoredBy(this)) {
            return;
        }
        Set<Pool.PoolStacks> set = this.poolStacks;
        synchronized (set) {
            this.poolStacks.add(threadLocalPoolStacks);
            threadLocalPoolStacks.setPoolMonitoringCounter(this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getTotalPoolCount() {
        int result = 0;
        Set<Pool.PoolStacks> set = this.poolStacks;
        synchronized (set) {
            for (Pool.PoolStacks ps : this.poolStacks) {
                result += ps.getTotalObjectsCount();
            }
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getUsedCount() {
        int result = 0;
        Set<Pool.PoolStacks> set = this.poolStacks;
        synchronized (set) {
            for (Pool.PoolStacks ps : this.poolStacks) {
                result += ps.getUsedCount();
            }
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getFreeCount() {
        int result = 0;
        Set<Pool.PoolStacks> set = this.poolStacks;
        synchronized (set) {
            for (Pool.PoolStacks ps : this.poolStacks) {
                result += ps.getReleasedCount();
            }
        }
        return result;
    }
}

