/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.runtime.unmarshaller.Discarder;
import com.sun.xml.bind.v2.runtime.unmarshaller.DomLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.ProxyLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import javax.xml.bind.annotation.DomHandler;
import org.xml.sax.SAXException;

public final class WildcardLoader
extends ProxyLoader {
    private final DomLoader dom;
    private final WildcardMode mode;

    public WildcardLoader(DomHandler dom, WildcardMode mode) {
        this.dom = new DomLoader(dom);
        this.mode = mode;
    }

    @Override
    protected Loader selectLoader(UnmarshallingContext.State state, TagName tag) throws SAXException {
        Loader l;
        UnmarshallingContext context = state.getContext();
        if (this.mode.allowTypedObject && (l = context.selectRootLoader(state, tag)) != null) {
            return l;
        }
        if (this.mode.allowDom) {
            return this.dom;
        }
        return Discarder.INSTANCE;
    }
}

