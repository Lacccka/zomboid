/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public class FieldAccessor_Character
extends Accessor {
    public FieldAccessor_Character() {
        super(Character.class);
    }

    public Object get(Object bean) {
        return Character.valueOf(((Bean)bean).f_char);
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).f_char = value == null ? (char)'\u0000' : ((Character)value).charValue();
    }
}

