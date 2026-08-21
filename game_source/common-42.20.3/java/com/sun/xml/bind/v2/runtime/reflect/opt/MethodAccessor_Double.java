/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class MethodAccessor_Double
extends Accessor {
    public MethodAccessor_Double() {
        super(Double.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).get_double();
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).set_double(value == null ? 0.0 : (Double)value);
    }
}

