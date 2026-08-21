/*
 * Decompiled with CFR 0.152.
 */
package com.sun.istack;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class FragmentContentHandler
extends XMLFilterImpl {
    public FragmentContentHandler() {
    }

    public FragmentContentHandler(XMLReader parent) {
        super(parent);
    }

    public FragmentContentHandler(ContentHandler handler) {
        this.setContentHandler(handler);
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }
}

