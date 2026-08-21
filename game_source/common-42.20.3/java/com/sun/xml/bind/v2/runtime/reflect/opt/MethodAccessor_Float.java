/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class MethodAccessor_Float
extends Accessor {
    public MethodAccessor_Float() {
        super(Float.class);
    }

    public Object get(Object bean) {
        return Float.valueOf(((Bean)bean).get_float());
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).set_float(value == null ? 0.0f : ((Float)value).floatValue());
    }
}

