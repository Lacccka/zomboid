/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.NoEscapeHandler;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.XmlOutput;
import com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.SAXException;

public class XMLStreamWriterOutput
extends XmlOutputAbstractImpl {
    private final XMLStreamWriter out;
    private final CharacterEscapeHandler escapeHandler;
    private final XmlStreamOutWriterAdapter writerWrapper;
    protected final char[] buf = new char[256];
    private static final Class FI_STAX_WRITER_CLASS = XMLStreamWriterOutput.initFIStAXWriterClass();
    private static final Constructor<? extends XmlOutput> FI_OUTPUT_CTOR = XMLStreamWriterOutput.initFastInfosetOutputClass();
    private static final Class STAXEX_WRITER_CLASS = XMLStreamWriterOutput.initStAXExWriterClass();
    private static final Constructor<? extends XmlOutput> STAXEX_OUTPUT_CTOR = XMLStreamWriterOutput.initStAXExOutputClass();

    public static XmlOutput create(XMLStreamWriter out, JAXBContextImpl context, CharacterEscapeHandler escapeHandler) {
        Class<?> writerClass = out.getClass();
        if (writerClass == FI_STAX_WRITER_CLASS) {
            try {
                return FI_OUTPUT_CTOR.newInstance(out, context);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (STAXEX_WRITER_CLASS != null && STAXEX_WRITER_CLASS.isAssignableFrom(writerClass)) {
            try {
                return STAXEX_OUTPUT_CTOR.newInstance(out);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        CharacterEscapeHandler xmlStreamEscapeHandler = escapeHandler != null ? escapeHandler : NoEscapeHandler.theInstance;
        return new XMLStreamWriterOutput(out, xmlStreamEscapeHandler);
    }

    protected XMLStreamWriterOutput(XMLStreamWriter out, CharacterEscapeHandler escapeHandler) {
        this.out = out;
        this.escapeHandler = escapeHandler;
        this.writerWrapper = new XmlStreamOutWriterAdapter(out);
    }

    @Override
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
        if (!fragment) {
            this.out.writeStartDocument();
        }
    }

    @Override
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        if (!fragment) {
            this.out.writeEndDocument();
            this.out.flush();
        }
        super.endDocument(fragment);
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException, XMLStreamException {
        this.out.writeStartElement(this.nsContext.getPrefix(prefix), localName, this.nsContext.getNamespaceURI(prefix));
        NamespaceContextImpl.Element nse = this.nsContext.getCurrent();
        if (nse.count() > 0) {
            for (int i = nse.count() - 1; i >= 0; --i) {
                String uri = nse.getNsUri(i);
                if (uri.length() == 0 && nse.getBase() == 1) continue;
                this.out.writeNamespace(nse.getPrefix(i), uri);
            }
        }
    }

    @Override
    public void attribute(int prefix, String localName, String value) throws IOException, XMLStreamException {
        if (prefix == -1) {
            this.out.writeAttribute(localName, value);
        } else {
            this.out.writeAttribute(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName, value);
        }
    }

    @Override
    public void endStartTag() throws IOException, SAXException {
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException, SAXException, XMLStreamException {
        this.out.writeEndElement();
    }

    @Override
    public void text(String value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        if (needsSeparatingWhitespace) {
            this.out.writeCharacters(" ");
        }
        this.escapeHandler.escape(value.toCharArray(), 0, value.length(), false, this.writerWrapper);
    }

    @Override
    public void text(Pcdata value, boolean needsSeparatingWhitespace) throws IOException, SAXException, XMLStreamException {
        int len;
        if (needsSeparatingWhitespace) {
            this.out.writeCharacters(" ");
        }
        if ((len = value.length()) < this.buf.length) {
            value.writeTo(this.buf, 0);
            this.out.writeCharacters(this.buf, 0, len);
        } else {
            this.out.writeCharacters(value.toString());
        }
    }

    private static Class initFIStAXWriterClass() {
        try {
            Class<?> llfisw = Class.forName("org.jvnet.fastinfoset.stax.LowLevelFastInfosetStreamWriter");
            Class<?> sds = Class.forName("com.sun.xml.fastinfoset.stax.StAXDocumentSerializer");
            if (llfisw.isAssignableFrom(sds)) {
                return sds;
            }
            return null;
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends XmlOutput> initFastInfosetOutputClass() {
        try {
            if (FI_STAX_WRITER_CLASS == null) {
                return null;
            }
            Class<?> c = Class.forName("com.sun.xml.bind.v2.runtime.output.FastInfosetStreamWriterOutput");
            return c.getConstructor(FI_STAX_WRITER_CLASS, JAXBContextImpl.class);
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static Class initStAXExWriterClass() {
        try {
            return Class.forName("org.jvnet.staxex.XMLStreamWriterEx");
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static Constructor<? extends XmlOutput> initStAXExOutputClass() {
        try {
            Class<?> c = Class.forName("com.sun.xml.bind.v2.runtime.output.StAXExStreamWriterOutput");
            return c.getConstructor(STAXEX_WRITER_CLASS);
        }
        catch (Throwable e) {
            return null;
        }
    }

    private static final class XmlStreamOutWriterAdapter
    extends Writer {
        private final XMLStreamWriter writer;

        private XmlStreamOutWriterAdapter(XMLStreamWriter writer) {
            this.writer = writer;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            try {
                this.writer.writeCharacters(cbuf, off, len);
            }
            catch (XMLStreamException e) {
                throw new IOException("Error writing XML stream", e);
            }
        }

        public void writeEntityRef(String entityReference) throws XMLStreamException {
            this.writer.writeEntityRef(entityReference);
        }

        @Override
        public void flush() throws IOException {
            try {
                this.writer.flush();
            }
            catch (XMLStreamException e) {
                throw new IOException("Error flushing XML stream", e);
            }
        }

        @Override
        public void close() throws IOException {
            try {
                this.writer.close();
            }
            catch (XMLStreamException e) {
                throw new IOException("Error closing XML stream", e);
            }
        }
    }
}

