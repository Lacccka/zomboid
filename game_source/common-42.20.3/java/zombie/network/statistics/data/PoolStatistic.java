/*
 * Decompiled with CFR 0.152.
 */
package zombie.network.statistics.data;

import zombie.network.statistics.data.IStatistic;
import zombie.network.statistics.data.Statistic;

public class PoolStatistic
extends Statistic
implements IStatistic {
    private static final PoolStatistic instance = new PoolStatistic("pool");

    private PoolStatistic(String application) {
        super(application);
    }

    public static PoolStatistic getInstance() {
        return instance;
    }
}

