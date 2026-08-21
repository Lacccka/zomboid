/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public final class TransducedAccessor_method_Boolean
extends DefaultTransducedAccessor {
    @Override
    public String print(Object o) {
        return DatatypeConverterImpl._printBoolean(((Bean)o).get_boolean());
    }

    @Override
    public void parse(Object o, CharSequence lexical) {
        ((Bean)o).set_boolean(DatatypeConverterImpl._parseBoolean(lexical));
    }

    @Override
    public boolean hasValue(Object o) {
        return true;
    }
}

