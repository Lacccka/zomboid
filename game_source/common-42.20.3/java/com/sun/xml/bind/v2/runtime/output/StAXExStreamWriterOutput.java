/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jvnet.staxex.XMLStreamWriterEx
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.marshaller.NoEscapeHandler;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.XMLStreamWriterOutput;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.XMLStreamWriterEx;

public final class StAXExStreamWriterOutput
extends XMLStreamWriterOutput {
    private final XMLStreamWriterEx out;

    public StAXExStreamWriterOutput(XMLStreamWriterEx out) {
        super((XMLStreamWriter)out, NoEscapeHandler.theInstance);
        this.out = out;
    }

    @Override
    public void text(Pcdata value, boolean needsSeparatingWhitespace) throws XMLStreamException {
        if (needsSeparatingWhitespace) {
            this.out.writeCharacters(" ");
        }
        if (!(value instanceof Base64Data)) {
            this.out.writeCharacters(value.toString());
        } else {
            Base64Data v = (Base64Data)value;
            this.out.writeBinary(v.getDataHandler());
        }
    }
}

