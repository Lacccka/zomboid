/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.NamespaceContext;
import org.xml.sax.SAXException;

final class MTOMDecorator
implements XmlVisitor {
    private final XmlVisitor next;
    private final AttachmentUnmarshaller au;
    private UnmarshallerImpl parent;
    private final Base64Data base64data = new Base64Data();
    private boolean inXopInclude;
    private boolean followXop;

    public MTOMDecorator(UnmarshallerImpl parent, XmlVisitor next, AttachmentUnmarshaller au) {
        this.parent = parent;
        this.next = next;
        this.au = au;
    }

    @Override
    public void startDocument(LocatorEx loc, NamespaceContext nsContext) throws SAXException {
        this.next.startDocument(loc, nsContext);
    }

    @Override
    public void endDocument() throws SAXException {
        this.next.endDocument();
    }

    @Override
    public void startElement(TagName tagName) throws SAXException {
        if (tagName.local.equals("Include") && tagName.uri.equals("http://www.w3.org/2004/08/xop/include")) {
            String href = tagName.atts.getValue("href");
            DataHandler attachment = this.au.getAttachmentAsDataHandler(href);
            if (attachment == null) {
                this.parent.getEventHandler().handleEvent(null);
            }
            this.base64data.set(attachment);
            this.next.text(this.base64data);
            this.inXopInclude = true;
            this.followXop = true;
        } else {
            this.next.startElement(tagName);
        }
    }

    @Override
    public void endElement(TagName tagName) throws SAXException {
        if (this.inXopInclude) {
            this.inXopInclude = false;
            this.followXop = true;
            return;
        }
        this.next.endElement(tagName);
    }

    @Override
    public void startPrefixMapping(String prefix, String nsUri) throws SAXException {
        this.next.startPrefixMapping(prefix, nsUri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        this.next.endPrefixMapping(prefix);
    }

    @Override
    public void text(CharSequence pcdata) throws SAXException {
        if (!this.followXop) {
            this.next.text(pcdata);
        } else {
            this.followXop = false;
        }
    }

    @Override
    public UnmarshallingContext getContext() {
        return this.next.getContext();
    }

    @Override
    public XmlVisitor.TextPredictor getPredictor() {
        return this.next.getPredictor();
    }
}

