/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.istack.Nullable;
import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.v2.runtime.JaxBeanInfo;
import com.sun.xml.bind.v2.runtime.unmarshaller.Loader;
import com.sun.xml.bind.v2.runtime.unmarshaller.Messages;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import javax.xml.namespace.QName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class XsiTypeLoader
extends Loader {
    private final JaxBeanInfo defaultBeanInfo;
    static final QName XsiTypeQNAME = new QName("http://www.w3.org/2001/XMLSchema-instance", "type");

    public XsiTypeLoader(JaxBeanInfo defaultBeanInfo) {
        super(true);
        this.defaultBeanInfo = defaultBeanInfo;
    }

    @Override
    public void startElement(UnmarshallingContext.State state, TagName ea) throws SAXException {
        JaxBeanInfo beanInfo = XsiTypeLoader.parseXsiType(state, ea, this.defaultBeanInfo);
        if (beanInfo == null) {
            beanInfo = this.defaultBeanInfo;
        }
        Loader loader = beanInfo.getLoader(null, false);
        state.setLoader(loader);
        loader.startElement(state, ea);
    }

    static JaxBeanInfo parseXsiType(UnmarshallingContext.State state, TagName ea, @Nullable JaxBeanInfo defaultBeanInfo) throws SAXException {
        UnmarshallingContext context = state.getContext();
        JaxBeanInfo beanInfo = null;
        Attributes atts = ea.atts;
        int idx = atts.getIndex("http://www.w3.org/2001/XMLSchema-instance", "type");
        if (idx >= 0) {
            String value = atts.getValue(idx);
            QName type = DatatypeConverterImpl._parseQName(value, context);
            if (type == null) {
                XsiTypeLoader.reportError(Messages.NOT_A_QNAME.format(value), true);
            } else {
                if (defaultBeanInfo != null && defaultBeanInfo.getTypeNames().contains(type)) {
                    return defaultBeanInfo;
                }
                beanInfo = context.getJAXBContext().getGlobalType(type);
                if (beanInfo == null && context.parent.hasEventHandler() && context.shouldErrorBeReported()) {
                    String nearest = context.getJAXBContext().getNearestTypeName(type);
                    if (nearest != null) {
                        XsiTypeLoader.reportError(Messages.UNRECOGNIZED_TYPE_NAME_MAYBE.format(type, nearest), true);
                    } else {
                        XsiTypeLoader.reportError(Messages.UNRECOGNIZED_TYPE_NAME.format(type), true);
                    }
                }
            }
        }
        return beanInfo;
    }

    @Override
    public Collection<QName> getExpectedAttributes() {
        HashSet<QName> expAttrs = new HashSet<QName>();
        expAttrs.addAll(super.getExpectedAttributes());
        expAttrs.add(XsiTypeQNAME);
        return Collections.unmodifiableCollection(expAttrs);
    }
}

