/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public final class TransducedAccessor_field_Double
extends DefaultTransducedAccessor {
    @Override
    public String print(Object o) {
        return DatatypeConverterImpl._printDouble(((Bean)o).f_double);
    }

    @Override
    public void parse(Object o, CharSequence lexical) {
        ((Bean)o).f_double = DatatypeConverterImpl._parseDouble(lexical);
    }

    @Override
    public boolean hasValue(Object o) {
        return true;
    }
}

