/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack;

import com.sun.istack.NotNull;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface Pool<T> {
    @NotNull
    public T take();

    public void recycle(@NotNull T var1);

    public static abstract class Impl<T>
    implements Pool<T> {
        private volatile WeakReference<ConcurrentLinkedQueue<T>> queue;

        @Override
        @NotNull
        public final T take() {
            T t = this.getQueue().poll();
            if (t == null) {
                return this.create();
            }
            return t;
        }

        @Override
        public final void recycle(T t) {
            this.getQueue().offer(t);
        }

        private ConcurrentLinkedQueue<T> getQueue() {
            ConcurrentLinkedQueue d;
            WeakReference<ConcurrentLinkedQueue<T>> q = this.queue;
            if (q != null && (d = (ConcurrentLinkedQueue)q.get()) != null) {
                return d;
            }
            d = new ConcurrentLinkedQueue();
            this.queue = new WeakReference(d);
            return d;
        }

        @NotNull
        protected abstract T create();
    }
}

