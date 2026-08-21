/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public final class TransducedAccessor_method_Float
extends DefaultTransducedAccessor {
    @Override
    public String print(Object o) {
        return DatatypeConverterImpl._printFloat(((Bean)o).get_float());
    }

    @Override
    public void parse(Object o, CharSequence lexical) {
        ((Bean)o).set_float(DatatypeConverterImpl._parseFloat(lexical));
    }

    @Override
    public boolean hasValue(Object o) {
        return true;
    }
}

