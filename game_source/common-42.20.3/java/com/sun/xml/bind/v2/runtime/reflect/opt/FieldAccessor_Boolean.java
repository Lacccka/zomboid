/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class FieldAccessor_Boolean
extends Accessor {
    public FieldAccessor_Boolean() {
        super(Boolean.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).f_boolean;
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).f_boolean = value == null ? false : (Boolean)value;
    }
}

