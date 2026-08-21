/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.xml.fastinfoset.stax.StAXDocumentSerializer
 *  org.jvnet.fastinfoset.VocabularyApplicationData
 */
package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.marshaller.NoEscapeHandler;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.XMLStreamWriterOutput;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.fastinfoset.stax.StAXDocumentSerializer;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.fastinfoset.VocabularyApplicationData;
import org.xml.sax.SAXException;

public final class FastInfosetStreamWriterOutput
extends XMLStreamWriterOutput {
    private final StAXDocumentSerializer fiout;
    private final Encoded[] localNames;
    private final TablesPerJAXBContext tables;

    public FastInfosetStreamWriterOutput(StAXDocumentSerializer out, JAXBContextImpl context) {
        super((XMLStreamWriter)out, NoEscapeHandler.theInstance);
        this.fiout = out;
        this.localNames = context.getUTF8NameTable();
        VocabularyApplicationData vocabAppData = this.fiout.getVocabularyApplicationData();
        AppData appData = null;
        if (vocabAppData == null || !(vocabAppData instanceof AppData)) {
            appData = new AppData();
            this.fiout.setVocabularyApplicationData((VocabularyApplicationData)appData);
        } else {
            appData = (AppData)vocabAppData;
        }
        TablesPerJAXBContext tablesPerContext = appData.contexts.get(context);
        if (tablesPerContext != null) {
            this.tables = tablesPerContext;
            this.tables.clearOrResetTables(out.getLocalNameIndex());
        } else {
            this.tables = new TablesPerJAXBContext(context, out.getLocalNameIndex());
            appData.contexts.put(context, this.tables);
        }
    }

    @Override
    public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext) throws IOException, SAXException, XMLStreamException {
        super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
        if (fragment) {
            this.fiout.initiateLowLevelWriting();
        }
    }

    @Override
    public void endDocument(boolean fragment) throws IOException, SAXException, XMLStreamException {
        super.endDocument(fragment);
    }

    @Override
    public void beginStartTag(Name name) throws IOException {
        this.fiout.writeLowLevelTerminationAndMark();
        if (this.nsContext.getCurrent().count() == 0) {
            int qNameIndex = this.tables.elementIndexes[name.qNameIndex] - this.tables.indexOffset;
            int prefixIndex = this.nsUriIndex2prefixIndex[name.nsUriIndex];
            if (qNameIndex >= 0 && this.tables.elementIndexPrefixes[name.qNameIndex] == prefixIndex) {
                this.fiout.writeLowLevelStartElementIndexed(0, qNameIndex);
            } else {
                this.tables.elementIndexes[name.qNameIndex] = this.fiout.getNextElementIndex() + this.tables.indexOffset;
                this.tables.elementIndexPrefixes[name.qNameIndex] = prefixIndex;
                this.writeLiteral(60, name, this.nsContext.getPrefix(prefixIndex), this.nsContext.getNamespaceURI(prefixIndex));
            }
        } else {
            this.beginStartTagWithNamespaces(name);
        }
    }

    public void beginStartTagWithNamespaces(Name name) throws IOException {
        NamespaceContextImpl.Element nse = this.nsContext.getCurrent();
        this.fiout.writeLowLevelStartNamespaces();
        for (int i = nse.count() - 1; i >= 0; --i) {
            String uri = nse.getNsUri(i);
            if (uri.length() == 0 && nse.getBase() == 1) continue;
            this.fiout.writeLowLevelNamespace(nse.getPrefix(i), uri);
        }
        this.fiout.writeLowLevelEndNamespaces();
        int qNameIndex = this.tables.elementIndexes[name.qNameIndex] - this.tables.indexOffset;
        int prefixIndex = this.nsUriIndex2prefixIndex[name.nsUriIndex];
        if (qNameIndex >= 0 && this.tables.elementIndexPrefixes[name.qNameIndex] == prefixIndex) {
            this.fiout.writeLowLevelStartElementIndexed(0, qNameIndex);
        } else {
            this.tables.elementIndexes[name.qNameIndex] = this.fiout.getNextElementIndex() + this.tables.indexOffset;
            this.tables.elementIndexPrefixes[name.qNameIndex] = prefixIndex;
            this.writeLiteral(60, name, this.nsContext.getPrefix(prefixIndex), this.nsContext.getNamespaceURI(prefixIndex));
        }
    }

    @Override
    public void attribute(Name name, String value) throws IOException {
        this.fiout.writeLowLevelStartAttributes();
        int qNameIndex = this.tables.attributeIndexes[name.qNameIndex] - this.tables.indexOffset;
        if (qNameIndex >= 0) {
            this.fiout.writeLowLevelAttributeIndexed(qNameIndex);
        } else {
            this.tables.attributeIndexes[name.qNameIndex] = this.fiout.getNextAttributeIndex() + this.tables.indexOffset;
            short namespaceURIId = name.nsUriIndex;
            if (namespaceURIId == -1) {
                this.writeLiteral(120, name, "", "");
            } else {
                int prefix = this.nsUriIndex2prefixIndex[namespaceURIId];
                this.writeLiteral(120, name, this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix));
            }
        }
        this.fiout.writeLowLevelAttributeValue(value);
    }

    private void writeLiteral(int type, Name name, String prefix, String namespaceURI) throws IOException {
        int localNameIndex = this.tables.localNameIndexes[name.localNameIndex] - this.tables.indexOffset;
        if (localNameIndex < 0) {
            this.tables.localNameIndexes[name.localNameIndex] = this.fiout.getNextLocalNameIndex() + this.tables.indexOffset;
            this.fiout.writeLowLevelStartNameLiteral(type, prefix, this.localNames[name.localNameIndex].buf, namespaceURI);
        } else {
            this.fiout.writeLowLevelStartNameLiteral(type, prefix, localNameIndex, namespaceURI);
        }
    }

    @Override
    public void endStartTag() throws IOException {
        this.fiout.writeLowLevelEndStartElement();
    }

    @Override
    public void endTag(Name name) throws IOException {
        this.fiout.writeLowLevelEndElement();
    }

    @Override
    public void endTag(int prefix, String localName) throws IOException {
        this.fiout.writeLowLevelEndElement();
    }

    @Override
    public void text(Pcdata value, boolean needsSeparatingWhitespace) throws IOException {
        if (needsSeparatingWhitespace) {
            this.fiout.writeLowLevelText(" ");
        }
        if (!(value instanceof Base64Data)) {
            int len = value.length();
            if (len < this.buf.length) {
                value.writeTo(this.buf, 0);
                this.fiout.writeLowLevelText(this.buf, len);
            } else {
                this.fiout.writeLowLevelText(value.toString());
            }
        } else {
            Base64Data dataValue = (Base64Data)value;
            this.fiout.writeLowLevelOctets(dataValue.get(), dataValue.getDataLen());
        }
    }

    @Override
    public void text(String value, boolean needsSeparatingWhitespace) throws IOException {
        if (needsSeparatingWhitespace) {
            this.fiout.writeLowLevelText(" ");
        }
        this.fiout.writeLowLevelText(value);
    }

    @Override
    public void beginStartTag(int prefix, String localName) throws IOException {
        boolean isIndexed;
        this.fiout.writeLowLevelTerminationAndMark();
        int type = 0;
        if (this.nsContext.getCurrent().count() > 0) {
            NamespaceContextImpl.Element nse = this.nsContext.getCurrent();
            this.fiout.writeLowLevelStartNamespaces();
            for (int i = nse.count() - 1; i >= 0; --i) {
                String uri = nse.getNsUri(i);
                if (uri.length() == 0 && nse.getBase() == 1) continue;
                this.fiout.writeLowLevelNamespace(nse.getPrefix(i), uri);
            }
            this.fiout.writeLowLevelEndNamespaces();
            type = 0;
        }
        if (!(isIndexed = this.fiout.writeLowLevelStartElement(type, this.nsContext.getPrefix(prefix), localName, this.nsContext.getNamespaceURI(prefix)))) {
            this.tables.incrementMaxIndexValue();
        }
    }

    @Override
    public void attribute(int prefix, String localName, String value) throws IOException {
        this.fiout.writeLowLevelStartAttributes();
        boolean isIndexed = prefix == -1 ? this.fiout.writeLowLevelAttribute("", "", localName) : this.fiout.writeLowLevelAttribute(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName);
        if (!isIndexed) {
            this.tables.incrementMaxIndexValue();
        }
        this.fiout.writeLowLevelAttributeValue(value);
    }

    static final class AppData
    implements VocabularyApplicationData {
        final Map<JAXBContext, TablesPerJAXBContext> contexts = new WeakHashMap<JAXBContext, TablesPerJAXBContext>();
        final Collection<TablesPerJAXBContext> collectionOfContexts = this.contexts.values();

        AppData() {
        }

        public void clear() {
            for (TablesPerJAXBContext c : this.collectionOfContexts) {
                c.requireClearTables();
            }
        }
    }

    static final class TablesPerJAXBContext {
        final int[] elementIndexes;
        final int[] elementIndexPrefixes;
        final int[] attributeIndexes;
        final int[] localNameIndexes;
        int indexOffset;
        int maxIndex;
        boolean requiresClear;

        TablesPerJAXBContext(JAXBContextImpl context, int initialIndexOffset) {
            this.elementIndexes = new int[context.getNumberOfElementNames()];
            this.elementIndexPrefixes = new int[context.getNumberOfElementNames()];
            this.attributeIndexes = new int[context.getNumberOfAttributeNames()];
            this.localNameIndexes = new int[context.getNumberOfLocalNames()];
            this.indexOffset = 1;
            this.maxIndex = initialIndexOffset + this.elementIndexes.length + this.attributeIndexes.length;
        }

        public void requireClearTables() {
            this.requiresClear = true;
        }

        public void clearOrResetTables(int intialIndexOffset) {
            if (this.requiresClear) {
                this.requiresClear = false;
                this.indexOffset += this.maxIndex;
                this.maxIndex = intialIndexOffset + this.elementIndexes.length + this.attributeIndexes.length;
                if (this.indexOffset + this.maxIndex < 0) {
                    this.clearAll();
                }
            } else {
                this.maxIndex = intialIndexOffset + this.elementIndexes.length + this.attributeIndexes.length;
                if (this.indexOffset + this.maxIndex < 0) {
                    this.resetAll();
                }
            }
        }

        private void clearAll() {
            this.clear(this.elementIndexes);
            this.clear(this.attributeIndexes);
            this.clear(this.localNameIndexes);
            this.indexOffset = 1;
        }

        private void clear(int[] array) {
            for (int i = 0; i < array.length; ++i) {
                array[i] = 0;
            }
        }

        public void incrementMaxIndexValue() {
            ++this.maxIndex;
            if (this.indexOffset + this.maxIndex < 0) {
                this.resetAll();
            }
        }

        private void resetAll() {
            this.clear(this.elementIndexes);
            this.clear(this.attributeIndexes);
            this.clear(this.localNameIndexes);
            this.indexOffset = 1;
        }

        private void reset(int[] array) {
            for (int i = 0; i < array.length; ++i) {
                array[i] = array[i] > this.indexOffset ? array[i] - this.indexOffset + 1 : 0;
            }
        }
    }
}

