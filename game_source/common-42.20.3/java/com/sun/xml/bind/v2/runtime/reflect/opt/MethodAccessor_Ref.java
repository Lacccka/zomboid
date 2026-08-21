/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;
import com.sun.xml.bind.v2.runtime.reflect.opt.Ref;

public class MethodAccessor_Ref
extends Accessor {
    public MethodAccessor_Ref() {
        super(Ref.class);
    }

    public Object get(Object bean) {
        return ((Bean)bean).get_ref();
    }

    public void set(Object bean, Object value) {
        ((Bean)bean).set_ref((Ref)value);
    }
}

