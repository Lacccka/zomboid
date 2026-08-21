/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class MethodAccessor_Integer
extends Accessor {
    public MethodAccessor_Integer() {
        super(Integer.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).get_int();
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).set_int(value == null ? 0 : (Integer)value);
    }
}

