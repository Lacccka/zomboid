/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.util;

import java.util.Objects;
import java.util.Optional;

public interface Specializable<S> {
    default public <T extends S> Optional<T> as(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        return type.isAssignableFrom(this.getClass()) ? Optional.of(type.cast(this)) : Optional.empty();
    }
}

