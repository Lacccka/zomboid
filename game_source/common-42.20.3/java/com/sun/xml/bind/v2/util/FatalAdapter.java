/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FatalAdapter
implements ErrorHandler {
    private final ErrorHandler core;

    public FatalAdapter(ErrorHandler handler) {
        this.core = handler;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        this.core.warning(exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        this.core.fatalError(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        this.core.fatalError(exception);
    }
}

