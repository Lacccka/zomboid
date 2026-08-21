/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.util;

import java.util.Map;

public class TypeCast {
    public static <K, V> Map<K, V> checkedCast(Map<?, ?> m, Class<K> keyType, Class<V> valueType) {
        if (m == null) {
            return null;
        }
        for (Map.Entry<?, ?> e : m.entrySet()) {
            if (!keyType.isInstance(e.getKey())) {
                throw new ClassCastException(e.getKey().getClass().toString());
            }
            if (valueType.isInstance(e.getValue())) continue;
            throw new ClassCastException(e.getValue().getClass().toString());
        }
        return m;
    }
}

