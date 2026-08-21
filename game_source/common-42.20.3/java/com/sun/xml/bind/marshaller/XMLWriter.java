/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.marshaller;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.DumbEscapeHandler;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLWriter
extends XMLFilterImpl {
    private final HashMap<String, String> locallyDeclaredPrefix = new HashMap();
    private final Attributes EMPTY_ATTS = new AttributesImpl();
    private int elementLevel = 0;
    private Writer output;
    private String encoding;
    private boolean writeXmlDecl = true;
    private String header = null;
    private final CharacterEscapeHandler escapeHandler;
    private boolean startTagIsClosed = true;

    public XMLWriter(Writer writer, String encoding, CharacterEscapeHandler _escapeHandler) {
        this.init(writer, encoding);
        this.escapeHandler = _escapeHandler;
    }

    public XMLWriter(Writer writer, String encoding) {
        this(writer, encoding, DumbEscapeHandler.theInstance);
    }

    private void init(Writer writer, String encoding) {
        this.setOutput(writer, encoding);
    }

    public void reset() {
        this.elementLevel = 0;
        this.startTagIsClosed = true;
    }

    public void flush() throws IOException {
        this.output.flush();
    }

    public void setOutput(Writer writer, String _encoding) {
        this.output = writer == null ? new OutputStreamWriter(System.out) : writer;
        this.encoding = _encoding;
    }

    public void setXmlDecl(boolean _writeXmlDecl) {
        this.writeXmlDecl = _writeXmlDecl;
    }

    public void setHeader(String _header) {
        this.header = _header;
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.locallyDeclaredPrefix.put(prefix, uri);
    }

    @Override
    public void startDocument() throws SAXException {
        try {
            this.reset();
            if (this.writeXmlDecl) {
                String e = "";
                if (this.encoding != null) {
                    e = " encoding=\"" + this.encoding + '\"';
                }
                this.writeXmlDecl("<?xml version=\"1.0\"" + e + " standalone=\"yes\"?>");
            }
            if (this.header != null) {
                this.write(this.header);
            }
            super.startDocument();
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    protected void writeXmlDecl(String decl) throws IOException {
        this.write(decl);
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            super.endDocument();
            this.flush();
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        try {
            if (!this.startTagIsClosed) {
                this.write(">");
            }
            ++this.elementLevel;
            this.write('<');
            this.write(qName);
            this.writeAttributes(atts);
            if (!this.locallyDeclaredPrefix.isEmpty()) {
                for (Map.Entry<String, String> e : this.locallyDeclaredPrefix.entrySet()) {
                    String p = e.getKey();
                    String u = e.getValue();
                    if (u == null) {
                        u = "";
                    }
                    this.write(' ');
                    if ("".equals(p)) {
                        this.write("xmlns=\"");
                    } else {
                        this.write("xmlns:");
                        this.write(p);
                        this.write("=\"");
                    }
                    char[] ch = u.toCharArray();
                    this.writeEsc(ch, 0, ch.length, true);
                    this.write('\"');
                }
                this.locallyDeclaredPrefix.clear();
            }
            super.startElement(uri, localName, qName, atts);
            this.startTagIsClosed = false;
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (this.startTagIsClosed) {
                this.write("</");
                this.write(qName);
                this.write('>');
            } else {
                this.write("/>");
                this.startTagIsClosed = true;
            }
            super.endElement(uri, localName, qName);
            --this.elementLevel;
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void characters(char[] ch, int start, int len) throws SAXException {
        try {
            if (!this.startTagIsClosed) {
                this.write('>');
                this.startTagIsClosed = true;
            }
            this.writeEsc(ch, start, len, false);
            super.characters(ch, start, len);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        try {
            this.writeEsc(ch, start, length, false);
            super.ignorableWhitespace(ch, start, length);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        try {
            if (!this.startTagIsClosed) {
                this.write('>');
                this.startTagIsClosed = true;
            }
            this.write("<?");
            this.write(target);
            this.write(' ');
            this.write(data);
            this.write("?>");
            if (this.elementLevel < 1) {
                this.write('\n');
            }
            super.processingInstruction(target, data);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startElement(String uri, String localName) throws SAXException {
        this.startElement(uri, localName, "", this.EMPTY_ATTS);
    }

    public void startElement(String localName) throws SAXException {
        this.startElement("", localName, "", this.EMPTY_ATTS);
    }

    public void endElement(String uri, String localName) throws SAXException {
        this.endElement(uri, localName, "");
    }

    public void endElement(String localName) throws SAXException {
        this.endElement("", localName, "");
    }

    public void dataElement(String uri, String localName, String qName, Attributes atts, String content) throws SAXException {
        this.startElement(uri, localName, qName, atts);
        this.characters(content);
        this.endElement(uri, localName, qName);
    }

    public void dataElement(String uri, String localName, String content) throws SAXException {
        this.dataElement(uri, localName, "", this.EMPTY_ATTS, content);
    }

    public void dataElement(String localName, String content) throws SAXException {
        this.dataElement("", localName, "", this.EMPTY_ATTS, content);
    }

    public void characters(String data) throws SAXException {
        try {
            if (!this.startTagIsClosed) {
                this.write('>');
                this.startTagIsClosed = true;
            }
            char[] ch = data.toCharArray();
            this.characters(ch, 0, ch.length);
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
    }

    protected final void write(char c) throws IOException {
        this.output.write(c);
    }

    protected final void write(String s) throws IOException {
        this.output.write(s);
    }

    private void writeAttributes(Attributes atts) throws IOException {
        int len = atts.getLength();
        for (int i = 0; i < len; ++i) {
            char[] ch = atts.getValue(i).toCharArray();
            this.write(' ');
            this.write(atts.getQName(i));
            this.write("=\"");
            this.writeEsc(ch, 0, ch.length, true);
            this.write('\"');
        }
    }

    private void writeEsc(char[] ch, int start, int length, boolean isAttVal) throws IOException {
        this.escapeHandler.escape(ch, start, length, isAttVal, this.output);
    }
}

