/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.Discarder;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.ProxyLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.util.Collection;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.xml.sax.SAXException;

public class XsiNilLoader
extends ProxyLoader {
    private final Loader defaultLoader;

    public XsiNilLoader(Loader defaultLoader) {
        this.defaultLoader = defaultLoader;
        assert (defaultLoader != null);
    }

    @Override
    protected Loader selectLoader(UnmarshallingContext.State state, TagName ea) throws SAXException {
        Boolean b;
        int idx = ea.atts.getIndex("http://www.w3.org/2001/XMLSchema-instance", "nil");
        if (idx != -1 && (b = DatatypeConverterImpl._parseBoolean(ea.atts.getValue(idx))) != null && b.booleanValue()) {
            boolean hasOtherAttributes;
            this.onNil(state);
            boolean bl = hasOtherAttributes = ea.atts.getLength() - 1 > 0;
            if (!hasOtherAttributes || !(state.getPrev().getTarget() instanceof JAXBElement)) {
                return Discarder.INSTANCE;
            }
        }
        return this.defaultLoader;
    }

    @Override
    public Collection<QName> getExpectedChildElements() {
        return this.defaultLoader.getExpectedChildElements();
    }

    @Override
    public Collection<QName> getExpectedAttributes() {
        return this.defaultLoader.getExpectedAttributes();
    }

    protected void onNil(UnmarshallingContext.State state) throws SAXException {
    }

    public static final class Array
    extends XsiNilLoader {
        public Array(Loader core) {
            super(core);
        }

        @Override
        protected void onNil(UnmarshallingContext.State state) {
            state.setTarget(null);
        }
    }

    public static final class Single
    extends XsiNilLoader {
        private final Accessor acc;

        public Single(Loader l, Accessor acc) {
            super(l);
            this.acc = acc;
        }

        @Override
        protected void onNil(UnmarshallingContext.State state) throws SAXException {
            try {
                this.acc.set(state.getPrev().getTarget(), null);
                state.getPrev().setNil(true);
            }
            catch (AccessorException e) {
                Single.handleGenericException(e, true);
            }
        }
    }
}

