/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jvnet.staxex.Base64Data
 *  org.jvnet.staxex.XMLStreamReaderEx
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.bind.v2.runtime.unmarshaller.StAXStreamConnector;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jvnet.staxex.XMLStreamReaderEx;
import org.xml.sax.SAXException;

final class StAXExConnector
extends StAXStreamConnector {
    private final XMLStreamReaderEx in;

    public StAXExConnector(XMLStreamReaderEx in, XmlVisitor visitor) {
        super((XMLStreamReader)in, visitor);
        this.in = in;
    }

    @Override
    protected void handleCharacters() throws XMLStreamException, SAXException {
        if (this.predictor.expectText()) {
            CharSequence pcdata = this.in.getPCDATA();
            if (pcdata instanceof org.jvnet.staxex.Base64Data) {
                org.jvnet.staxex.Base64Data bd = (org.jvnet.staxex.Base64Data)pcdata;
                Base64Data binary = new Base64Data();
                if (!bd.hasData()) {
                    binary.set(bd.getDataHandler());
                } else {
                    binary.set(bd.get(), bd.getDataLen(), bd.getMimeType());
                }
                this.visitor.text(binary);
                this.textReported = true;
            } else {
                this.buffer.append(pcdata);
            }
        }
    }
}

