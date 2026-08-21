/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class MethodAccessor_Boolean
extends Accessor {
    public MethodAccessor_Boolean() {
        super(Boolean.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).get_boolean();
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).set_boolean(value == null ? false : (Boolean)value);
    }
}

