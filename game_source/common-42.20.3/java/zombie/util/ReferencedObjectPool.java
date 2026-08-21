/*
 * Decompiled with CFR 0.152.
 */
package zombie.util;

import java.util.function.Supplier;
import zombie.core.Core;
import zombie.debug.DebugType;
import zombie.network.statistics.counters.ObjectPoolCounter;
import zombie.util.CappedConcurrentQueue;
import zombie.util.ReferencedObject;

public class ReferencedObjectPool<T extends ReferencedObject>
extends ObjectPoolCounter {
    private final CappedConcurrentQueue<T> released;
    private final Supplier<T> allocator;

    public ReferencedObjectPool(Supplier<T> allocator, String name) {
        this(allocator, name, 1024);
    }

    public ReferencedObjectPool(Supplier<T> allocator, String name, int maxSize) {
        super(name);
        this.allocator = allocator;
        this.released = new CappedConcurrentQueue(maxSize);
    }

    public T alloc() {
        ReferencedObject obj = (ReferencedObject)this.released.poll();
        if (obj == null) {
            return this.create();
        }
        if (obj.getReferenceCount() == 0) {
            obj.retain();
            return (T)obj;
        }
        if (Core.debug) {
            DebugType.General.printStackTrace("Object is referenced " + obj.getReferenceCount() + " times");
        }
        return this.create();
    }

    public void release(T obj) {
        if (((ReferencedObject)obj).getReferenceCount() == 1) {
            ((ReferencedObject)obj).release();
            this.released.add(obj);
        } else if (Core.debug) {
            DebugType.General.printStackTrace("Object is referenced " + ((ReferencedObject)obj).getReferenceCount() + " times");
        }
    }

    @Override
    public int size() {
        return this.released.size();
    }

    private T create() {
        ReferencedObject obj = (ReferencedObject)this.allocator.get();
        obj.retain();
        return (T)obj;
    }
}

