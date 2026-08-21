/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public interface InfosetScanner<XmlNode> {
    public void scan(XmlNode var1) throws SAXException;

    public void setContentHandler(ContentHandler var1);

    public ContentHandler getContentHandler();

    public XmlNode getCurrentElement();

    public LocatorEx getLocator();
}

