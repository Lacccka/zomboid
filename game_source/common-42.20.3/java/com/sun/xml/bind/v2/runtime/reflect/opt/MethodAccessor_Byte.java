/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class MethodAccessor_Byte
extends Accessor {
    public MethodAccessor_Byte() {
        super(Byte.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).get_byte();
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).set_byte(value == null ? (byte)0 : (Byte)value);
    }
}

