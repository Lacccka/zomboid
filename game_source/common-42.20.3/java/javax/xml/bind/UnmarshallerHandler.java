/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind;

import javax.xml.bind.JAXBException;
import org.xml.sax.ContentHandler;

public interface UnmarshallerHandler
extends ContentHandler {
    public Object getResult() throws JAXBException, IllegalStateException;
}

