/*
 * Decompiled with CFR 0.152.
 */
package zombie.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.function.Supplier;

public class LazyValue<T> {
    protected static final Object UNSET = new Object();
    protected static final VarHandle HANDLE;
    private volatile Object value = UNSET;
    private final Supplier<T> supplier;

    public LazyValue(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    public T get() {
        Object existing = HANDLE.getVolatile(this);
        if (existing != UNSET) {
            return (T)existing;
        }
        T newValue = this.supplier.get();
        Object previous = HANDLE.compareAndExchange(this, UNSET, newValue);
        return (T)(previous == UNSET ? newValue : previous);
    }

    static {
        try {
            HANDLE = MethodHandles.lookup().findVarHandle(LazyValue.class, "value", Object.class);
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

