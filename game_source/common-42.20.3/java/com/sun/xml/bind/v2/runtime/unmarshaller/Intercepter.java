/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import org.xml.sax.SAXException;

public interface Intercepter {
    public Object intercept(UnmarshallingContext.State var1, Object var2) throws SAXException;
}

