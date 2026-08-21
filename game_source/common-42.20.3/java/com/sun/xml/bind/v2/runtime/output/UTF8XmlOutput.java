/*
 * Decompiled with CFR 0.152.
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.XmlOutputAbstractImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public class UTF8XmlOutput
extends XmlOutputAbstractImpl {
    protected final OutputStream out;
    private Encoded[] prefixes = new Encoded[8];
    private int prefixCount;
    private final Encoded[] localNames;
    private final Encoded textBuffer = new Encoded();
    protected final byte[] octetBuffer = new byte[1024];
    protected int octetBufferIndex;
    protected boolean closeStartTagPending = false;
    private String header;
    private CharacterEscapeHandler escapeHandler = null;
    private final byte[] XMLNS_EQUALS = (byte[])_XMLNS_EQUALS.clone();
    private final byte[] XMLNS_COLON = (byte[])_XMLNS_COLON.clone();
    private final byte[] EQUALS = (byte[])_EQUALS.clone();
    private final byte[] CLOSE_TAG = (byte[])_CLOSE_TAG.clone();
    private final byte[] EMPTY_TAG = (byte[])_EMPTY_TAG.clone();
    private final byte[] XML_DECL = (byte[])_XML_DECL.clone();
    private static final byte[] _XMLNS_EQUALS = UTF8XmlOutput.toBytes(" xmlns=\"");
    private static final byte[] _XMLNS_COLON = UTF8XmlOutput.toBytes(" xmlns:");
    private static final byte[] _EQUALS = UTF8XmlOutput.toBytes("=\"");
    private static final byte[] _CLOSE_TAG = UTF8XmlOutput.toBytes("</");
    private static final byte[] _EMPTY_TAG = UTF8XmlOutput.toBytes("/>");
    private static final byte[] _XML_DECL = UTF8XmlOutput.toBytes("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public UTF8XmlOutput(OutputStream out, Encoded[] localNames, CharacterEscapeHandler escapeHandler) {
        this.out = out;
        this.localNames = localNames;
        for (int i = 0; i < this.prefixes.length; ++i) {
            this.prefixes[i] = new Encoded();
        }
        this.escapeHandler = escapeHandler;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
        this.octetBufferIndex = 0;
        if (!fragment) {
            this.write(this.XML_DECL);
        }
        if (this.header != null) {
            this.textBuffer.set(this.header);
            this.textBuffer.write(this);
        }
    }

    @Override
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        this.flushBuffer();
        super.endDocument(fragment);
    }

    protected final void closeStartTag() throws IOException {
        if (this.closeStartTagPending) {
            this.write(62);
            this.closeStartTagPending = false;
        }
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException {
        this.closeStartTag();
        int base = this.pushNsDecls();
        this.write(60);
        this.writeName(prefix, localName);
        this.writeNsDecls(base);
    }

    @Override
    public void beginStartTag(Name name) throws IOException {
        this.closeStartTag();
        int base = this.pushNsDecls();
        this.write(60);
        this.writeName(name);
        this.writeNsDecls(base);
    }

    private int pushNsDecls() {
        int i;
        int total = this.nsContext.count();
        NamespaceContextImpl.Element ns = this.nsContext.getCurrent();
        if (total > this.prefixes.length) {
            int m = Math.max(total, this.prefixes.length * 2);
            Encoded[] buf = new Encoded[m];
            System.arraycopy(this.prefixes, 0, buf, 0, this.prefixes.length);
            for (i = this.prefixes.length; i < buf.length; ++i) {
                buf[i] = new Encoded();
            }
            this.prefixes = buf;
        }
        int base = Math.min(this.prefixCount, ns.getBase());
        int size = this.nsContext.count();
        for (i = base; i < size; ++i) {
            String p = this.nsContext.getPrefix(i);
            Encoded e = this.prefixes[i];
            if (p.length() == 0) {
                e.buf = EMPTY_BYTE_ARRAY;
                e.len = 0;
                continue;
            }
            e.set(p);
            e.append(':');
        }
        this.prefixCount = size;
        return base;
    }

    protected void writeNsDecls(int base) throws IOException {
        NamespaceContextImpl.Element ns = this.nsContext.getCurrent();
        int size = this.nsContext.count();
        for (int i = ns.getBase(); i < size; ++i) {
            this.writeNsDecl(i);
        }
    }

    protected final void writeNsDecl(int prefixIndex) throws IOException {
        String p = this.nsContext.getPrefix(prefixIndex);
        if (p.length() == 0) {
            if (this.nsContext.getCurrent().isRootElement() && this.nsContext.getNamespaceURI(prefixIndex).length() == 0) {
                return;
            }
            this.write(this.XMLNS_EQUALS);
        } else {
            Encoded e = this.prefixes[prefixIndex];
            this.write(this.XMLNS_COLON);
            this.write(e.buf, 0, e.len - 1);
            this.write(this.EQUALS);
        }
        this.doText(this.nsContext.getNamespaceURI(prefixIndex), true);
        this.write(34);
    }

    private void writePrefix(int prefix) throws IOException {
        this.prefixes[prefix].write(this);
    }

    private void writeName(Name name) throws IOException {
        this.writePrefix(this.nsUriIndex2prefixIndex[name.nsUriIndex]);
        this.localNames[name.localNameIndex].write(this);
    }

    private void writeName(int prefix, String localName) throws IOException {
        this.writePrefix(prefix);
        this.textBuffer.set(localName);
        this.textBuffer.write(this);
    }

    @Override
    public void attribute(Name name, String value) throws IOException {
        this.write(32);
        if (name.nsUriIndex == -1) {
            this.localNames[name.localNameIndex].write(this);
        } else {
            this.writeName(name);
        }
        this.write(this.EQUALS);
        this.doText(value, true);
        this.write(34);
    }

    @Override
    public void attribute(int prefix, String localName, String value) throws IOException {
        this.write(32);
        if (prefix == -1) {
            this.textBuffer.set(localName);
            this.textBuffer.write(this);
        } else {
            this.writeName(prefix, localName);
        }
        this.write(this.EQUALS);
        this.doText(value, true);
        this.write(34);
    }

    @Override
    public void endStartTag() throws IOException {
        this.closeStartTagPending = true;
    }

    @Override
    public void endTag(Name name) throws IOException {
        if (this.closeStartTagPending) {
            this.write(this.EMPTY_TAG);
            this.closeStartTagPending = false;
        } else {
            this.write(this.CLOSE_TAG);
            this.writeName(name);
            this.write(62);
        }
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException {
        if (this.closeStartTagPending) {
            this.write(this.EMPTY_TAG);
            this.closeStartTagPending = false;
        } else {
            this.write(this.CLOSE_TAG);
            this.writeName(prefix, localName);
            this.write(62);
        }
    }

    @Override
    public void text(String value, boolean needSP) throws IOException {
        this.closeStartTag();
        if (needSP) {
            this.write(32);
        }
        this.doText(value, false);
    }

    @Override
    public void text(Pcdata value, boolean needSP) throws IOException {
        this.closeStartTag();
        if (needSP) {
            this.write(32);
        }
        value.writeTo(this);
    }

    private void doText(String value, boolean isAttribute) throws IOException {
        if (this.escapeHandler != null) {
            StringWriter sw = new StringWriter();
            this.escapeHandler.escape(value.toCharArray(), 0, value.length(), isAttribute, sw);
            this.textBuffer.set(sw.toString());
        } else {
            this.textBuffer.setEscape(value, isAttribute);
        }
        this.textBuffer.write(this);
    }

    public final void text(int value) throws IOException {
        this.closeStartTag();
        boolean minus2 = value < 0;
        this.textBuffer.ensureSize(11);
        byte[] buf = this.textBuffer.buf;
        int idx = 11;
        do {
            int r;
            if ((r = value % 10) < 0) {
                r = -r;
            }
            buf[--idx] = (byte)(0x30 | r);
        } while ((value /= 10) != 0);
        if (minus2) {
            buf[--idx] = 45;
        }
        this.write(buf, idx, 11 - idx);
    }

    public void text(byte[] data, int dataLen) throws IOException {
        this.closeStartTag();
        int start = 0;
        while (dataLen > 0) {
            int batchSize = Math.min((this.octetBuffer.length - this.octetBufferIndex) / 4 * 3, dataLen);
            this.octetBufferIndex = DatatypeConverterImpl._printBase64Binary(data, start, batchSize, this.octetBuffer, this.octetBufferIndex);
            if (batchSize < dataLen) {
                this.flushBuffer();
            }
            start += batchSize;
            dataLen -= batchSize;
        }
    }

    public final void write(int i) throws IOException {
        if (this.octetBufferIndex < this.octetBuffer.length) {
            this.octetBuffer[this.octetBufferIndex++] = (byte)i;
        } else {
            this.out.write(this.octetBuffer);
            this.octetBufferIndex = 1;
            this.octetBuffer[0] = (byte)i;
        }
    }

    protected final void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    protected final void write(byte[] b, int start, int length) throws IOException {
        if (this.octetBufferIndex + length < this.octetBuffer.length) {
            System.arraycopy(b, start, this.octetBuffer, this.octetBufferIndex, length);
            this.octetBufferIndex += length;
        } else {
            this.out.write(this.octetBuffer, 0, this.octetBufferIndex);
            this.out.write(b, start, length);
            this.octetBufferIndex = 0;
        }
    }

    protected final void flushBuffer() throws IOException {
        this.out.write(this.octetBuffer, 0, this.octetBufferIndex);
        this.octetBufferIndex = 0;
    }

    static byte[] toBytes(String s) {
        byte[] buf = new byte[s.length()];
        for (int i = s.length() - 1; i >= 0; --i) {
            buf[i] = (byte)s.charAt(i);
        }
        return buf;
    }
}

