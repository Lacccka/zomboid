/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.ClassBeanInfoImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.reflect.Accessor;
import com.sun.xml.bind.v2.runtime.reflect.TransducedAccessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.LeafPropertyLoader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.util.Collection;
import javax.xml.namespace.QName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class LeafPropertyXsiLoader
extends Loader {
    private final Loader defaultLoader;
    private final TransducedAccessor xacc;
    private final Accessor acc;

    public LeafPropertyXsiLoader(Loader defaultLoader, TransducedAccessor xacc, Accessor acc) {
        this.defaultLoader = defaultLoader;
        this.expectText = true;
        this.xacc = xacc;
        this.acc = acc;
    }

    @Override
    public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        Loader loader = this.selectLoader(state, ea);
        state.setLoader(loader);
        loader.startElement(state, ea);
    }

    protected Loader selectLoader(UnmarshallingContext.State state, TagName ea) throws SAXException {
        UnmarshallingContext context = state.getContext();
        JaxBeanInfo beanInfo = null;
        Attributes atts = ea.atts;
        int idx = atts.getIndex("http://www.w3.org/2001/XMLSchema-instance", "type");
        if (idx >= 0) {
            ClassBeanInfoImpl cbii;
            String value = atts.getValue(idx);
            QName type = DatatypeConverterImpl._parseQName(value, context);
            if (type == null) {
                return this.defaultLoader;
            }
            beanInfo = context.getJAXBContext().getGlobalType(type);
            if (beanInfo == null) {
                return this.defaultLoader;
            }
            try {
                cbii = (ClassBeanInfoImpl)beanInfo;
            }
            catch (ClassCastException cce) {
                return this.defaultLoader;
            }
            if (null == cbii.getTransducer()) {
                return this.defaultLoader;
            }
            return new LeafPropertyLoader(new TransducedAccessor.CompositeTransducedAccessorImpl(state.getContext().getJAXBContext(), cbii.getTransducer(), this.acc));
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
}

