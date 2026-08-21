/*
 * Decompiled with CFR 0.152.
 */
package javax.xml.bind.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.Messages;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class JAXBSource
extends SAXSource {
    private final Marshaller marshaller;
    private final Object contentObject;
    private final XMLReader pseudoParser = new XMLReader(){
        private LexicalHandler lexicalHandler;
        private EntityResolver entityResolver;
        private DTDHandler dtdHandler;
        private XMLFilter repeater = new XMLFilterImpl();
        private ErrorHandler errorHandler;

        @Override
        public boolean getFeature(String name) throws SAXNotRecognizedException {
            if (name.equals("http://xml.org/sax/features/namespaces")) {
                return true;
            }
            if (name.equals("http://xml.org/sax/features/namespace-prefixes")) {
                return false;
            }
            throw new SAXNotRecognizedException(name);
        }

        @Override
        public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
            if (name.equals("http://xml.org/sax/features/namespaces") && value) {
                return;
            }
            if (name.equals("http://xml.org/sax/features/namespace-prefixes") && !value) {
                return;
            }
            throw new SAXNotRecognizedException(name);
        }

        @Override
        public Object getProperty(String name) throws SAXNotRecognizedException {
            if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
                return this.lexicalHandler;
            }
            throw new SAXNotRecognizedException(name);
        }

        @Override
        public void setProperty(String name, Object value) throws SAXNotRecognizedException {
            if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
                this.lexicalHandler = (LexicalHandler)value;
                return;
            }
            throw new SAXNotRecognizedException(name);
        }

        @Override
        public void setEntityResolver(EntityResolver resolver) {
            this.entityResolver = resolver;
        }

        @Override
        public EntityResolver getEntityResolver() {
            return this.entityResolver;
        }

        @Override
        public void setDTDHandler(DTDHandler handler) {
            this.dtdHandler = handler;
        }

        @Override
        public DTDHandler getDTDHandler() {
            return this.dtdHandler;
        }

        @Override
        public void setContentHandler(ContentHandler handler) {
            this.repeater.setContentHandler(handler);
        }

        @Override
        public ContentHandler getContentHandler() {
            return this.repeater.getContentHandler();
        }

        @Override
        public void setErrorHandler(ErrorHandler handler) {
            this.errorHandler = handler;
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return this.errorHandler;
        }

        @Override
        public void parse(InputSource input) throws SAXException {
            this.parse();
        }

        @Override
        public void parse(String systemId) throws SAXException {
            this.parse();
        }

        public void parse() throws SAXException {
            try {
                JAXBSource.this.marshaller.marshal(JAXBSource.this.contentObject, (XMLFilterImpl)this.repeater);
            }
            catch (JAXBException e) {
                SAXParseException se = new SAXParseException(e.getMessage(), null, null, -1, -1, e);
                if (this.errorHandler != null) {
                    this.errorHandler.fatalError(se);
                }
                throw se;
            }
        }
    };

    public JAXBSource(JAXBContext context, Object contentObject) throws JAXBException {
        this(context == null ? JAXBSource.assertionFailed(Messages.format("JAXBSource.NullContext")) : context.createMarshaller(), contentObject == null ? JAXBSource.assertionFailed(Messages.format("JAXBSource.NullContent")) : contentObject);
    }

    public JAXBSource(Marshaller marshaller, Object contentObject) throws JAXBException {
        if (marshaller == null) {
            throw new JAXBException(Messages.format("JAXBSource.NullMarshaller"));
        }
        if (contentObject == null) {
            throw new JAXBException(Messages.format("JAXBSource.NullContent"));
        }
        this.marshaller = marshaller;
        this.contentObject = contentObject;
        super.setXMLReader(this.pseudoParser);
        super.setInputSource(new InputSource());
    }

    private static Marshaller assertionFailed(String message) throws JAXBException {
        throw new JAXBException(message);
    }
}

