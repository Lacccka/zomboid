/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class FieldAccessor_Long
extends Accessor {
    public FieldAccessor_Long() {
        super(Long.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).f_long;
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).f_long = value == null ? 0L : (Long)value;
    }
}

