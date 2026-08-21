/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.XmlOutput;
import com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class MTOMXmlOutput
extends XmlOutputAbstractImpl {
    private final XmlOutput next;
    private String nsUri;
    private String localName;

    public MTOMXmlOutput(XmlOutput next) {
        this.next = next;
    }

    @Override
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
        this.next.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
    }

    @Override
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        this.next.endDocument(fragment);
        super.endDocument(fragment);
    }

    @Override
    public void beginStartTag(Name name) throws IOException, XMLStreamException {
        this.next.beginStartTag(name);
        this.nsUri = name.nsUri;
        this.localName = name.localName;
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException, XMLStreamException {
        this.next.beginStartTag(prefix, localName);
        this.nsUri = this.nsContext.getNamespaceURI(prefix);
        this.localName = localName;
    }

    @Override
    public void attribute(Name name, String value) throws IOException, XMLStreamException {
        this.next.attribute(name, value);
    }

    @Override
    public void attribute(int prefix, String localName, String value) throws IOException, XMLStreamException {
        this.next.attribute(prefix, localName, value);
    }

    @Override
    public void endStartTag() throws IOException, SAXException {
        this.next.endStartTag();
    }

    @Override
    public void endTag(Name name) throws IOException, SAXException, XMLStreamException {
        this.next.endTag(name);
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException, SAXException, XMLStreamException {
        this.next.endTag(prefix, localName);
    }

    @Override
    public void text(String value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        this.next.text(value, needsSeparatingWhitespace);
    }

    @Override
    public void text(Pcdata value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        Base64Data b64d;
        String cid;
        if (value instanceof Base64Data && !this.serializer.getInlineBinaryFlag() && (cid = (b64d = (Base64Data)value).hasData() ? this.serializer.attachmentMarshaller.addMtomAttachment(b64d.get(), 0, b64d.getDataLen(), b64d.getMimeType(), this.nsUri, this.localName) : this.serializer.attachmentMarshaller.addMtomAttachment(b64d.getDataHandler(), this.nsUri, this.localName)) != null) {
            this.nsContext.getCurrent().push();
            int prefix = this.nsContext.declareNsUri("http://www.w3.org/2004/08/xop/include", "xop", false);
            this.beginStartTag(prefix, "Include");
            this.attribute(-1, "href", cid);
            this.endStartTag();
            this.endTag(prefix, "Include");
            this.nsContext.getCurrent().pop();
            return;
        }
        this.next.text(value, needsSeparatingWhitespace);
    }
}

