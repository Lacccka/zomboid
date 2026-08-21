/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.api;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public interface ErrorListener
extends ErrorHandler {
    @Override
    public void error(SAXParseException var1);

    @Override
    public void fatalError(SAXParseException var1);

    @Override
    public void warning(SAXParseException var1);

    public void info(SAXParseException var1);
}

