/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class RuntimeUtil {
    public static final Map<Class, Class> boxToPrimitive;
    public static final Map<Class, Class> primitiveToBox;

    private static String getTypeName(Object o) {
        return o.getClass().getName();
    }

    static {
        HashMap<Class<Object>, Class<Void>> b = new HashMap<Class<Object>, Class<Void>>();
        b.put(Byte.TYPE, Byte.class);
        b.put(Short.TYPE, Short.class);
        b.put(Integer.TYPE, Integer.class);
        b.put(Long.TYPE, Long.class);
        b.put(Character.TYPE, Character.class);
        b.put(Boolean.TYPE, Boolean.class);
        b.put(Float.TYPE, Float.class);
        b.put(Double.TYPE, Double.class);
        b.put(Void.TYPE, Void.class);
        primitiveToBox = Collections.unmodifiableMap(b);
        HashMap p = new HashMap();
        for (Map.Entry e : b.entrySet()) {
            p.put(e.getValue(), e.getKey());
        }
        boxToPrimitive = Collections.unmodifiableMap(p);
    }

    public static final class ToStringAdapter
    extends XmlAdapter<String, Object> {
        @Override
        public Object unmarshal(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String marshal(Object o) {
            if (o == null) {
                return null;
            }
            return o.toString();
        }
    }
}

