/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public final class IndentingUTF8XmlOutput
extends UTF8XmlOutput {
    private final Encoded indent8;
    private final int unitLen;
    private int depth = 0;
    private boolean seenText = false;

    public IndentingUTF8XmlOutput(OutputStream out, String indentStr, Encoded[] localNames, CharacterEscapeHandler escapeHandler) {
        super(out, localNames, escapeHandler);
        if (indentStr != null) {
            Encoded e = new Encoded(indentStr);
            this.indent8 = new Encoded();
            this.indent8.ensureSize(e.len * 8);
            this.unitLen = e.len;
            for (int i = 0; i < 8; ++i) {
                System.arraycopy(e.buf, 0, this.indent8.buf, this.unitLen * i, this.unitLen);
            }
        } else {
            this.indent8 = null;
            this.unitLen = 0;
        }
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException {
        this.indentStartTag();
        super.beginStartTag(prefix, localName);
    }

    @Override
    public void beginStartTag(Name name) throws IOException {
        this.indentStartTag();
        super.beginStartTag(name);
    }

    private void indentStartTag() throws IOException {
        this.closeStartTag();
        if (!this.seenText) {
            this.printIndent();
        }
        ++this.depth;
        this.seenText = false;
    }

    @Override
    public void endTag(Name name) throws IOException {
        this.indentEndTag();
        super.endTag(name);
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException {
        this.indentEndTag();
        super.endTag(prefix, localName);
    }

    private void indentEndTag() throws IOException {
        --this.depth;
        if (!this.closeStartTagPending && !this.seenText) {
            this.printIndent();
        }
        this.seenText = false;
    }

    private void printIndent() throws IOException {
        this.write(10);
        int i = this.depth % 8;
        this.write(this.indent8.buf, 0, i * this.unitLen);
        i >>= 3;
        while (i > 0) {
            this.indent8.write(this);
            --i;
        }
    }

    @Override
    public void text(String value, boolean needSP) throws IOException {
        this.seenText = true;
        super.text(value, needSP);
    }

    @Override
    public void text(Pcdata value, boolean needSP) throws IOException {
        this.seenText = true;
        super.text(value, needSP);
    }

    @Override
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        this.write(10);
        super.endDocument(fragment);
    }
}

