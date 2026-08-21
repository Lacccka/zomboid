/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.reflect.DefaultTransducedAccessor;
import com.sun.xml.bind.v2.runtime.reflect.opt.Bean;

public final class TransducedAccessor_method_Byte
extends DefaultTransducedAccessor {
    @Override
    public String print(Object o) {
        return DatatypeConverterImpl._printByte(((Bean)o).get_byte());
    }

    @Override
    public void parse(Object o, CharSequence lexical) {
        ((Bean)o).set_byte(DatatypeConverterImpl._parseByte(lexical));
    }

    @Override
    public boolean hasValue(Object o) {
        return true;
    }
}

