/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class FieldAccessor_Short
extends Accessor {
    public FieldAccessor_Short() {
        super(Short.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).f_short;
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).f_short = value == null ? (short)0 : (Short)value;
    }
}

