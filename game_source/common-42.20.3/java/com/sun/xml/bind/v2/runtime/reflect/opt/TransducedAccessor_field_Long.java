/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public final class TransducedAccessor_field_Long
extends DefaultTransducedAccessor {
    @Override
    public String print(Object o) {
        return DatatypeConverterImpl._printLong(((Bean)o).f_long);
    }

    @Override
    public void parse(Object o, CharSequence lexical) {
        ((Bean)o).f_long = DatatypeConverterImpl._parseLong(lexical);
    }

    @Override
    public boolean hasValue(Object o) {
        return true;
    }
}

