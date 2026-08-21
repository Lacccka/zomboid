/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.util.AttributesImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.AttributesEx;

public final class AttributesExImpl
extends AttributesImpl
implements AttributesEx {
    @Override
    public CharSequence getData(int idx) {
        return this.getValue(idx);
    }

    @Override
    public CharSequence getData(String nsUri, String localName) {
        return this.getValue(nsUri, localName);
    }
}

