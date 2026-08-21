/*
 * Decompiled with CFR 0.152.
 */
package zombie.util;

import java.util.function.Supplier;
import zombie.util.LazyValue;

public class ResettableLazyValue<T>
extends LazyValue<T> {
    public ResettableLazyValue(Supplier<T> supplier) {
        super(supplier);
    }

    public void reset() {
        HANDLE.setVolatile(this, UNSET);
    }
}

