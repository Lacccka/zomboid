/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.Util;
import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorEx;
import com.sun.xml.bind.v2.runtime.unmarshaller.LocatorExWrapper;
import com.sun.xml.bind.v2.runtime.unmarshaller.TagName;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.XmlVisitor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public final class SAXConnector
implements UnmarshallerHandler {
    private LocatorEx loc;
    private static final Logger logger = Util.getClassLogger();
    private final StringBuilder buffer = new StringBuilder();
    private final XmlVisitor next;
    private final UnmarshallingContext context;
    private final XmlVisitor.TextPredictor predictor;
    private final TagNameImpl tagName = new TagNameImpl();

    public SAXConnector(XmlVisitor next, LocatorEx externalLocator) {
        this.next = next;
        this.context = next.getContext();
        this.predictor = next.getPredictor();
        this.loc = externalLocator;
    }

    @Override
    public Object getResult() throws JAXBException, IllegalStateException {
        return this.context.getResult();
    }

    public UnmarshallingContext getContext() {
        return this.context;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        if (this.loc != null) {
            return;
        }
        this.loc = new LocatorExWrapper(locator);
    }

    @Override
    public void startDocument() throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.startDocument");
        }
        this.next.startDocument(this.loc, null);
    }

    @Override
    public void endDocument() throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.endDocument");
        }
        this.next.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.startPrefixMapping: {0}:{1}", new Object[]{prefix, uri});
        }
        this.next.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.endPrefixMapping: {0}", new Object[]{prefix});
        }
        this.next.endPrefixMapping(prefix);
    }

    @Override
    public void startElement(String uri, String local, String qname, Attributes atts) throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.startElement: {0}:{1}:{2}, attrs: {3}", new Object[]{uri, local, qname, atts});
        }
        if (uri == null || uri.length() == 0) {
            uri = "";
        }
        if (local == null || local.length() == 0) {
            local = qname;
        }
        if (qname == null || qname.length() == 0) {
            qname = local;
        }
        this.processText(!this.context.getCurrentState().isMixed());
        this.tagName.uri = uri;
        this.tagName.local = local;
        this.tagName.qname = qname;
        this.tagName.atts = atts;
        this.next.startElement(this.tagName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "SAXConnector.startElement: {0}:{1}:{2}", new Object[]{uri, localName, qName});
        }
        this.processText(false);
        this.tagName.uri = uri;
        this.tagName.local = localName;
        this.tagName.qname = qName;
        this.next.endElement(this.tagName);
    }

    @Override
    public final void characters(char[] buf, int start, int len) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "SAXConnector.characters: {0}", (Object)buf);
        }
        if (this.predictor.expectText()) {
            this.buffer.append(buf, start, len);
        }
    }

    @Override
    public final void ignorableWhitespace(char[] buf, int start, int len) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "SAXConnector.characters{0}", (Object)buf);
        }
        this.characters(buf, start, len);
    }

    @Override
    public void processingInstruction(String target, String data) {
    }

    @Override
    public void skippedEntity(String name) {
    }

    private void processText(boolean ignorable) throws SAXException {
        if (!(!this.predictor.expectText() || ignorable && WhiteSpaceProcessor.isWhiteSpace(this.buffer))) {
            this.next.text(this.buffer);
        }
        this.buffer.setLength(0);
    }

    private static final class TagNameImpl
    extends TagName {
        String qname;

        private TagNameImpl() {
        }

        @Override
        public String getQname() {
            return this.qname;
        }
    }
}

