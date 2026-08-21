/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import org.xml.sax.SAXException;

public final class DefaultValueLoaderDecorator
extends Loader {
    private final Loader l;
    private final String defaultValue;

    public DefaultValueLoaderDecorator(Loader l, String defaultValue) {
        this.l = l;
        this.defaultValue = defaultValue;
    }

    @Override
    public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        if (state.getElementDefaultValue() == null) {
            state.setElementDefaultValue(this.defaultValue);
        }
        state.setLoader(this.l);
        this.l.startElement(state, ea);
    }
}

