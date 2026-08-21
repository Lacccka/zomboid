/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.marshaller;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.DumbEscapeHandler;
import com.sun.xml.bind.marshaller.XMLWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DataWriter
extends XMLWriter {
    private static final Object SEEN_NOTHING = new Object();
    private static final Object SEEN_ELEMENT = new Object();
    private static final Object SEEN_DATA = new Object();
    private Object state = SEEN_NOTHING;
    private Stack<Object> stateStack = new Stack();
    private String indentStep = "";
    private int depth = 0;

    public DataWriter(Writer writer, String encoding, CharacterEscapeHandler _escapeHandler) {
        super(writer, encoding, _escapeHandler);
    }

    public DataWriter(Writer writer, String encoding) {
        this(writer, encoding, DumbEscapeHandler.theInstance);
    }

    public int getIndentStep() {
        return this.indentStep.length();
    }

    public void setIndentStep(int indentStep) {
        StringBuilder buf = new StringBuilder();
        while (indentStep > 0) {
            buf.append(' ');
            --indentStep;
        }
        this.setIndentStep(buf.toString());
    }

    public void setIndentStep(String s) {
        this.indentStep = s;
    }

    @Override
    public void reset() {
        this.depth = 0;
        this.state = SEEN_NOTHING;
        this.stateStack = new Stack();
        super.reset();
    }

    @Override
    protected void writeXmlDecl(String decl) throws IOException {
        super.writeXmlDecl(decl);
        this.write('\n');
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        this.stateStack.push(SEEN_ELEMENT);
        this.state = SEEN_NOTHING;
        if (this.depth > 0) {
            super.characters("\n");
        }
        this.doIndent();
        super.startElement(uri, localName, qName, atts);
        ++this.depth;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        --this.depth;
        if (this.state == SEEN_ELEMENT) {
            super.characters("\n");
            this.doIndent();
        }
        super.endElement(uri, localName, qName);
        this.state = this.stateStack.pop();
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            this.write('\n');
        }
        catch (IOException e) {
            throw new SAXException(e);
        }
        super.endDocument();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.state = SEEN_DATA;
        super.characters(ch, start, length);
    }

    private void doIndent() throws SAXException {
        if (this.depth > 0) {
            char[] ch = this.indentStep.toCharArray();
            for (int i = 0; i < this.depth; ++i) {
                this.characters(ch, 0, ch.length);
            }
        }
    }
}

