/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.system.ffm;

import java.lang.reflect.Method;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public interface TraceConsumer {
    public void accept(Method var1, @Nullable Object var2, Object ... var3);

    default public TraceConsumer andThen(TraceConsumer after) {
        Objects.requireNonNull(after);
        return (method, returnValue, args) -> {
            this.accept(method, returnValue, args);
            after.accept(method, returnValue, args);
        };
    }
}

