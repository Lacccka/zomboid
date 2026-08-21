/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.reflect;

import javax.xml.bind.JAXBException;
import org.xml.sax.SAXException;

public interface ListIterator<E> {
    public boolean hasNext();

    public E next() throws SAXException, JAXBException;
}

