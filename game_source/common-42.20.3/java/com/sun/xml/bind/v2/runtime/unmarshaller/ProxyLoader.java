/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import org.xml.sax.SAXException;

public abstract class ProxyLoader
extends Loader {
    public ProxyLoader() {
        super(false);
    }

    @Override
    public final void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        Loader loader = this.selectLoader(state, ea);
        state.setLoader(loader);
        loader.startElement(state, ea);
    }

    protected abstract Loader selectLoader(UnmarshallingContext.State var1, TagName var2) throws SAXException;

    @Override
    public final void leaveElement(UnmarshallingContext.State state, TagName ea) {
        throw new IllegalStateException();
    }
}

