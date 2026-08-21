/*
 * Decompiled with CFR 0.152.
 */
package io.prometheus.metrics.exporter.httpserver;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

class BlockingRejectedExecutionHandler
implements RejectedExecutionHandler {
    BlockingRejectedExecutionHandler() {
    }

    @Override
    public void rejectedExecution(Runnable runnable2, ThreadPoolExecutor threadPoolExecutor) {
        if (!threadPoolExecutor.isShutdown()) {
            try {
                threadPoolExecutor.getQueue().put(runnable2);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
    }
}

