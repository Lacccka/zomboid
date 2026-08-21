/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class FieldAccessor_Double
extends Accessor {
    public FieldAccessor_Double() {
        super(Double.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).f_double;
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).f_double = value == null ? 0.0 : (Double)value;
    }
}

